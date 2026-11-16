import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


public class Model implements HttpHandler{

    // Logical operators as they appear in the UI dropdown (TriggerConditionController).
    public static final String AND = "AND";
    public static final String OR = "OR";

    /*
    Initialised empty rather than left null. Model's constructor registers the HTTP handler before
    SettingsController gets a chance to push the initial checkbox state, so a request arriving in that
    window used to NPE on enabledTools.contains() in handleHttpRequestToBeSent().
     */
    public EnumSet<ToolType> enabledTools = EnumSet.noneOf(ToolType.class);
    public Boolean triggerConditionIsSet;
    public List<TriggerCondition> triggerConditions;
    public CorrectiveCall followupCall;

    /*
    The intercepted call that triggered APE, as a corrective call that never fires.

    Only its extractList is used. It exists because the most natural way for an API to tell you how to
    fix a request is in the error response itself -- a 409 carrying the version you should have sent, a
    401 carrying a fresh nonce -- and until now nothing could read that: processCorrectiveCalls() only
    ever extracted from a corrective call's own request or response, so recovering such a value needed
    a corrective call that re-provoked the same error purely to extract from it.

    Modelled as a single CorrectiveCall on the Model rather than a copy injected into every
    CorrectiveCallSet. One object means no per-request mutation of shared config, nothing to keep in
    sync across sets, and no index offset in the call sequence table. Its extractions run against the
    REAL intercepted exchange each time, and land in that request's extraction map before any trigger
    condition is evaluated, so every corrective call and the followup can use them -- including the
    first call in a set, which has no earlier call to draw on.
     */
    public CorrectiveCall errorCall = new CorrectiveCall(null);

    public Model () {
        // TODO Maybe just move this to controller
        // Guarded so Model can be built outside of Burp (the logic harness in src/test/java does this).
        if (Controller.montoyaApi != null) {
            Controller.montoyaApi.extension().setName("APE");
            Controller.montoyaApi.http().registerHttpHandler(this);
        }
    }



    // HTTP Handler which will intercept all traffic generated in burp
    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {


        // This determines which tool (Repeater, Intruder, etc.) a request came from.
        // TODO make sure this check makes sense on the response.
        ToolType toolType = requestToBeSent.toolSource().toolType();

        /*
        TODO: Make this work with extensions in an elegant way. Extensions currently disabled.
         Since APE itself is an extension, it was triggering the processResponse function multiple
         times. Need a way to make sure processResponse is not called for requests made by APE.
         Currently no way to identify which extension sends a request.
         */


        if (!enabledTools.contains(toolType) || toolType.toolName().equals("Extension")) {
            DebugLog.log("Model: Not processing response!");
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        /*
        What each extraction pulled out, for this request only. Declared here so it is a local of this
        handler invocation and therefore confined to the thread serving this request. Burp calls this
        handler on many threads at once, and ExtractText objects are long lived config, one instance per
        row in the extraction table, shared by every request that triggers. Holding the extracted value
        on that shared object let two concurrent requests overwrite each other, so a followup call could
        go out carrying a token extracted for a different request. Keyed by identity because ExtractText
        does not override equals/hashCode, which is the comparison being relied on anyway.

        A fresh map per request is also empty per request, so nothing needs clearing and a failed
        extraction cannot fall back on a value left over from an earlier request.
         */
        Map<ExtractText, String> extracted = new IdentityHashMap<>();

        HttpRequestResponse requestResponse = Controller.montoyaApi.http().sendRequest(requestToBeSent);

        /*
        A send that never completed (connection refused, timeout, TLS failure) comes back with a null
        response. There is nothing to evaluate trigger conditions against and nothing to spoof, so hand
        the request back to Burp and let it do its own send and report its own connection error.

        This is the one path where continueWith() is correct despite the note below. That note is about
        not sending a request twice, and the send above produced no response at all, so Burp trying it
        is the user's single real attempt rather than a duplicate.

        Guarding here rather than at each spoof() below, because detectErrorCondition() reads the
        response too.
         */
        if (requestResponse.response() == null) {
            DebugLog.log("[Error] Model: Request produced no response. Handing it back to Burp.");
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        /*
        Before any trigger condition is evaluated, so whatever the errored exchange yields is already
        in the map by the time the first corrective call builds its replacements.
         */
        extractFromErrorCall(requestResponse, extracted);

        boolean triggered = detectErrorCondition(requestResponse, extracted);

        /*
        The request above has already gone out, so every path from here has to spoof a response rather
        than continueWith(). Returning continueWith() would hand the request back to Burp and send it a
        second time.
         */
        if (!triggered) {
            DebugLog.log("Model: No trigger condition matched. Returning the original response.");
            return RequestToBeSentAction.spoof(requestResponse.response());
        }

        // followupCall stays null until the user configures one on the Followup Call tab. This used to be
        // called unconditionally, which meant an unconfigured followup call threw a NullPointerException.
        if (followupCall == null) {
            DebugLog.log("[Error] Model: Trigger condition matched but no followup call is configured. "
                    + "Returning the original response.");
            return RequestToBeSentAction.spoof(requestResponse.response());
        }

        HttpResponse followupCallResponse = sendFollowupCall(extracted, null);

        if (followupCallResponse == null) {
            DebugLog.log("[Error] Model: Followup call returned no response. Returning the original response.");
            return RequestToBeSentAction.spoof(requestResponse.response());
        }

        return RequestToBeSentAction.spoof(followupCallResponse);
    }


    /*
    ------------------------------------------------------------------ Test Call

    Replays the errored call and reports, step by step, what APE does with it.

    Sends the request stored on errorCall -- the one sent to the Error Detection tab -- then runs the
    exact same pipeline a live interception does: errored-call extractions, trigger condition
    evaluation, the corrective call sets belonging to whichever conditions matched, and the followup.
    The only difference is that each send is recorded as a TestCallStep.

    Deliberately shares matchingTriggerConditions(), processCorrectiveCalls() and sendFollowupCall()
    with handleHttpRequestToBeSent() rather than reimplementing the sequence, because a test that can
    drift away from what interception actually does is worse than no test button at all.

    The returned list is never null. A run that cannot start comes back as a single step explaining
    why, so the table can show the reason instead of silently staying empty.
     */
    public List<TestCallStep> runTestCall() {
        List<TestCallStep> trace = new ArrayList<>();

        if (errorCall == null || errorCall.requestResponse == null
                || errorCall.requestResponse.request() == null) {
            trace.add(cannotRun("No errored call to replay. Right click a request and choose "
                    + "'Send to Error Detection' first."));
            return trace;
        }

        if (triggerConditions == null || triggerConditions.isEmpty()) {
            trace.add(cannotRun("No trigger conditions configured. Add one on the Trigger Condition tab."));
            return trace;
        }

        if (Controller.montoyaApi == null) {
            trace.add(cannotRun("No Montoya API available to send with."));
            return trace;
        }

        Map<ExtractText, String> extracted = new IdentityHashMap<>();
        HttpRequest request = errorCall.requestResponse.request();

        DebugLog.log("Model: Test call starting. Replaying " + request.method() + " " + request.path());

        HttpRequestResponse requestResponse = Controller.montoyaApi.http().sendRequest(request);

        TestCallStep first = new TestCallStep(1, TestCallStep.ERRORED, "Intercepted", requestResponse,
                requestResponse.response() == null ? TestCallStep.NO_RESPONSE : TestCallStep.SENT);
        first.extractionsRun = errorCall.extractList.size();
        trace.add(first);

        if (requestResponse.response() == null) {
            first.detail = "the call produced no response, so nothing could be evaluated";
            DebugLog.log("[Error] Model: Test call got no response. Stopping.");
            return trace;
        }

        // Same order as interception: errored-call extractions first, so corrective calls can use them.
        int before = extracted.size();
        extractFromErrorCall(requestResponse, extracted);
        first.extractionsHit = extracted.size() - before;

        List<String> conditionResults = new ArrayList<>();
        List<TriggerCondition> matched = matchingTriggerConditions(requestResponse.request(),
                requestResponse.response(), conditionResults);

        first.outcome = matched.isEmpty() ? TestCallStep.NO_MATCH : TestCallStep.TRIGGERED;
        first.detail = String.join(", ", conditionResults);

        if (matched.isEmpty()) {
            DebugLog.log("Model: Test call matched no trigger condition. Stopping after one step.");
            return trace;
        }

        for (TriggerCondition triggerCondition : matched) {
            processCorrectiveCalls(triggerCondition, extracted, trace);
        }

        if (followupCall == null || followupCall.requestResponse == null) {
            TestCallStep step = new TestCallStep(trace.size() + 1, TestCallStep.FOLLOWUP, "Followup",
                    null, TestCallStep.NOT_RUN);
            step.detail = "no followup call configured on the Followup Call tab";
            trace.add(step);
            return trace;
        }

        sendFollowupCall(extracted, trace);

        DebugLog.log("Model: Test call finished in " + trace.size() + " step(s).");
        return trace;
    }

    // A single row explaining why a test run could not start.
    private TestCallStep cannotRun(String reason) {
        TestCallStep step = new TestCallStep(1, TestCallStep.ERRORED, "Intercepted", null,
                TestCallStep.NOT_RUN);
        step.detail = reason;
        DebugLog.log("[Error] Model: Test call cannot run. " + reason);
        return step;
    }

    /*
    Runs the extractions configured against the errored call on the Error Detection tab, reading the
    exchange that actually triggered this interception rather than whatever was last sent to the panel.

    A failed extraction simply puts nothing in the map, and any replacement pointing at it is skipped
    downstream rather than inserting the text "null", which is the same contract corrective call
    extractions already have.
     */
    private void extractFromErrorCall(HttpRequestResponse requestResponse,
                                      Map<ExtractText, String> extracted) {
        if (errorCall == null || errorCall.extractList.isEmpty()) {
            return;
        }

        String rawRequest = requestResponse.request().toString();
        String rawResponse = requestResponse.response().toString();

        DebugLog.log("Model: Running " + errorCall.extractList.size()
                + " extraction(s) against the errored call.");

        for (ExtractText extractText : errorCall.extractList) {
            String source = extractText.extractFromRequest ? rawRequest : rawResponse;
            String from = extractText.extractFromRequest ? "request" : "response";

            DebugLog.log("Model: Extracting from errored " + from + ": " + extractText.debugString());
            String extractedText = extractBetween(source, extractText.startString, extractText.stopString);

            if (extractedText != null) {
                extractedText = Utils.applyUrlTransform(extractedText, extractText.urlEncodeDecode);
                extracted.put(extractText, extractedText);
                DebugLog.log("Model: Extracted from errored call: " + extractedText);
            }
            else {
                DebugLog.log("[Error] Model: Extraction from the errored call found nothing: "
                        + extractText.name);
            }
        }
    }

    // The extractions defined against the errored call. Available to every corrective call and to the
    // followup, since the errored exchange precedes all of them.
    public List<ExtractText> errorCallExtractions() {
        if (errorCall == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(errorCall.extractList);
    }


    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return ResponseReceivedAction.continueWith(responseReceived);
    }

    // Allows controller to update model's enabledTools
    public void updateEnabledTools(EnumSet<ToolType> enabledTools) {
        this.enabledTools = EnumSet.copyOf(enabledTools);
    }



    /*
    Detects error conditions and makes the corrective calls belonging to whichever trigger conditions
    matched. A response triggers APE if ANY of the configured trigger conditions evaluates to true.

    Returns true if at least one trigger condition matched, so the caller knows whether the followup call
    is supposed to run at all.
     */
    boolean detectErrorCondition(HttpRequestResponse requestResponse, Map<ExtractText, String> extracted) {
        return detectErrorCondition(requestResponse, extracted, null);
    }

    boolean detectErrorCondition(HttpRequestResponse requestResponse, Map<ExtractText, String> extracted,
                                 List<TestCallStep> trace) {
        List<TriggerCondition> matched = matchingTriggerConditions(requestResponse.request(),
                requestResponse.response(), null);

        for (TriggerCondition triggerCondition : matched) {
            DebugLog.log("Model: Trigger condition matched: " + triggerCondition.name
                    + ". Making corrective calls.");
            processCorrectiveCalls(triggerCondition, extracted, trace);
        }

        DebugLog.log("Model: Finished evaluating "
                + (triggerConditions == null ? 0 : triggerConditions.size())
                + " trigger condition(s). Triggered: " + !matched.isEmpty());

        return !matched.isEmpty();
    }

    /*
    Which trigger conditions match a response, deciding only -- nothing is sent here.

    Separated from firing the corrective calls so the Test Call button can report which conditions hit
    and which missed before anything goes on the wire, and so both it and the live interception path
    run the same evaluation rather than two implementations that can drift apart.

    results, when supplied, is filled with one human readable line per condition for the Test Calls
    table.
     */
    List<TriggerCondition> matchingTriggerConditions(HttpResponse response, List<String> results) {
        return matchingTriggerConditions(null, response, results);
    }

    List<TriggerCondition> matchingTriggerConditions(HttpRequest request, HttpResponse response,
                                                     List<String> results) {
        List<TriggerCondition> matched = new ArrayList<>();

        if (triggerConditions == null || triggerConditions.isEmpty()) {
            DebugLog.log("Model: No trigger conditions configured. Not processing response further.");
            if (results != null) {
                results.add("no trigger conditions configured");
            }
            return matched;
        }

        for (TriggerCondition triggerCondition : triggerConditions) {
            DebugLog.log("Model: Evaluating trigger condition: " + triggerCondition.name);
            boolean hit = evaluateTriggerCondition(triggerCondition, request, response);

            if (results != null) {
                results.add(triggerCondition.name + (hit ? ": HIT" : ": miss"));
            }

            if (hit) {
                matched.add(triggerCondition);
            }
        }

        return matched;
    }

    /*
    Evaluates one trigger condition in three steps, which is the only logic the UI can express:
      1. evaluate each error condition on its own and apply its negation checkbox
      2. collapse every AND (AND binds tighter than OR)
      3. collapse every remaining OR
    The working lists are local, so nothing carries over between responses or between trigger conditions.
     */
    Boolean evaluateTriggerCondition(TriggerCondition triggerCondition, HttpResponse response) {
        return evaluateTriggerCondition(triggerCondition, null, response);
    }

    Boolean evaluateTriggerCondition(TriggerCondition triggerCondition, HttpRequest request,
                                     HttpResponse response) {
        DebugLog.log("Model: Evaluating trigger condition " + triggerCondition.name
                + ". Condition string: " + triggerCondition.conditionString);

        List<Boolean> conditions = new ArrayList<>();
        List<String> operators = new ArrayList<>();

        // Step 1: each error condition on its own, negation applied immediately.
        for (int i = 0; i < triggerCondition.errorConditions.size(); i++) {
            ErrorCondition errorCondition = triggerCondition.errorConditions.get(i);

            Boolean raw = evaluateErrorCondition(errorCondition, request, response);
            Boolean isNegated = triggerCondition.isNegated.get(i);
            Boolean result = evaluateNegation(raw, isNegated);

            DebugLog.log("Model: Error condition " + i + " evaluated: " + errorCondition.name
                    + ". Category: '" + errorCondition.category + "', value: '" + errorCondition.value + "'");
            DebugLog.log("Model: Result: " + raw + ", negated: " + isNegated + ", result after negation: " + result);

            conditions.add(result);
        }

        // TriggerCondition leaves logicalOperator null when there is only one error condition.
        if (triggerCondition.logicalOperator != null) {
            operators.addAll(triggerCondition.logicalOperator);
        }

        DebugLog.log("Model: Parsed trigger condition. Conditions: " + conditions + ", operators: " + operators);

        // One operator joins each adjacent pair of error conditions, so there is always one fewer
        // operator than condition. Anything else means the UI handed us something malformed.
        if (operators.size() != conditions.size() - 1) {
            DebugLog.log("[Error] Malformed trigger condition: " + triggerCondition.name + ". Found "
                    + conditions.size() + " condition(s) and " + operators.size() + " operator(s).");
            DebugLog.log("A trigger condition must have exactly " + (conditions.size() - 1)
                    + " operator(s) for " + conditions.size() + " condition(s)");
            return false;
        }

        // Step 2 then step 3.
        evalAND(conditions, operators);
        evalOR(conditions, operators);

        if (conditions.size() != 1 || !operators.isEmpty()) {
            DebugLog.log("[Error] Trigger condition did not collapse to a single value: " + triggerCondition.name);
            DebugLog.log("Conditions: " + conditions + ", operators: " + operators);
            return false;
        }

        DebugLog.log("Model: Trigger condition " + triggerCondition.name + " evaluates to "
                + conditions.getFirst());
        return conditions.getFirst();
    }

    /*
    Collapses every AND in place, left to right. operators.get(i) joins conditions.get(i) and
    conditions.get(i+1), so folding an AND replaces that pair with a single value at index i and
    drops the operator. Mutates both lists.
     */
    void evalAND(List<Boolean> conditions, List<String> operators) {
        DebugLog.log("Model: evalAND() input. Conditions: " + conditions + ", operators: " + operators);

        int i = 0;
        while (i < operators.size()) {
            if (!operators.get(i).equals(AND)) {
                i++; // leave ORs alone, evalOR handles them
                continue;
            }

            Boolean left = conditions.get(i);
            Boolean right = conditions.get(i + 1);
            Boolean result = left && right;

            DebugLog.log("Model: Collapsing AND at operator " + i + ". " + left + " AND " + right + " = " + result);

            operators.remove(i);       // operator is consumed
            conditions.remove(i + 1);  // right operand folds into the left one
            conditions.set(i, result); // which now holds the result

            // int "i" is deliberately not advanced. operators.get(i) is now whatever followed the pair we
            // just folded, e.g. the second AND of "A AND B AND C", which must fold into this result.
            DebugLog.log("Model: Collapsed to conditions: " + conditions + ", operators: " + operators);
        }

        DebugLog.log("Model: evalAND() output. Conditions: " + conditions + ", operators: " + operators);
    }

    // Same collapse as evalAND. Runs second, so by now every remaining operator should be an OR.
    void evalOR(List<Boolean> conditions, List<String> operators) {
        DebugLog.log("Model: evalOR() input. Conditions: " + conditions + ", operators: " + operators);

        int i = 0;
        while (i < operators.size()) {
            if (!operators.get(i).equals(OR)) {
                DebugLog.log("[Error] Unrecognized logical operator at index " + i + ": '"
                        + operators.get(i) + "'. Skipping it.");
                i++;
                continue;
            }

            Boolean left = conditions.get(i);
            Boolean right = conditions.get(i + 1);
            Boolean result = left || right;

            DebugLog.log("Model: Collapsing OR at operator " + i + ". " + left + " OR " + right + " = " + result);

            operators.remove(i);
            conditions.remove(i + 1);
            conditions.set(i, result);

            DebugLog.log("Model: Collapsed to conditions: " + conditions + ", operators: " + operators);
        }

        DebugLog.log("Model: evalOR() output. Conditions: " + conditions + ", operators: " + operators);
    }

    private Boolean evaluateErrorCondition(ErrorCondition errorCondition, HttpRequest request,
                                           HttpResponse response) {
        boolean result = false; //Should use only primitive boolean not obj Boolean because there is no use for it, and it is more expensive

        /*
        Body and Header conditions may be aimed at either half of the exchange. A condition set to
        match the request when no request is available -- the back-compatible two argument
        evaluateTriggerCondition(), used by the condition parser tests -- cannot match, and says so
        rather than quietly returning false.
         */
        boolean useRequest = errorCondition.matchInRequest;

        if (useRequest && request == null && !errorCondition.category.equals("Status Code")) {
            DebugLog.log("[Error] Model: Error condition '" + errorCondition.name + "' matches the "
                    + "request, but no request was supplied. Treating it as not matched.");
            return false;
        }

        switch (errorCondition.category) {
            case "Status Code":
                // Responses have status codes; requests do not. matchInRequest is ignored here.
                result = String.valueOf(response.statusCode()).equals(errorCondition.value);
                DebugLog.log("Model: Status code " + response.statusCode() + " compared to '"
                        + errorCondition.value + "'. Result: " + result);
                return result;
            case "Body":
                String body = useRequest ? request.body().toString() : response.body().toString();
                result = body.contains(errorCondition.value);
                DebugLog.log("Model: " + errorCondition.source() + " body searched for '"
                        + errorCondition.value + "'. Result: " + result);
                return result;
            case "Header":
                List<HttpHeader> headers = useRequest ? request.headers() : response.headers();

                for (HttpHeader header : headers) {
                    // Match the configured header by name, case-insensitively, since header names are not
                    // case sensitive. A blank value means presence alone triggers; a non-blank value means
                    // the matched header's value must also contain it.
                    if (header.name().equalsIgnoreCase(errorCondition.headerName)) {
                        if (errorCondition.value == null || errorCondition.value.isEmpty()
                                || header.value().contains(errorCondition.value)) {
                            result = true;
                        }
                    }
                }
                DebugLog.log("Model: " + headers.size() + " " + errorCondition.source()
                        + " header(s) searched for name '" + errorCondition.headerName + "'"
                        + (errorCondition.value == null || errorCondition.value.isEmpty()
                                ? "" : " containing '" + errorCondition.value + "'")
                        + ". Result: " + result);
                return result;
            default:
                DebugLog.log("[Error] Unknown error condition category: '" + errorCondition.category + "'");
                return false;
        }
    }


    // Applies the negation checkbox that belongs to a single error condition. Through the UI, negation
    // can only be attached to an individual error condition, never to a group of them.
    private Boolean evaluateNegation(Boolean result, Boolean isNegated) {
        if (isNegated) {
            DebugLog.log("Model: Error condition is negated. Negating result " + result + ".");
            return !result;
        }
        else {
            return result;
        }
    }


    // Processes text replacements and extractions and sends requests.
    private void processCorrectiveCalls(TriggerCondition triggerCondition, Map<ExtractText, String> extracted) {
        processCorrectiveCalls(triggerCondition, extracted, null);
    }

    // trace, when supplied, collects one TestCallStep per corrective call for the Test Calls table.
    private void processCorrectiveCalls(TriggerCondition triggerCondition, Map<ExtractText, String> extracted,
                                        List<TestCallStep> trace) {
        DebugLog.log("Model: Processing corrective call set: " + triggerCondition.correctiveCallSet.name
                + ". Call sequence size: " + triggerCondition.correctiveCallSet.correctiveCallList.size());

        int callIndex = 0;
        String rawRequest;
        String rawResponse;

        for (CorrectiveCall correctiveCall : triggerCondition.correctiveCallSet.correctiveCallList) {
            HttpRequest correctiveCallRequest = correctiveCall.requestResponse.request();
            rawRequest = correctiveCallRequest.toString();

            // Declared inside the loop on purpose. It used to be declared outside, which meant a call with
            // no replacements of its own re-sent the previous call's modified request.
            String updatedRequest = null;

            HttpRequestResponse sentRequestResponse;
            HttpResponse sentResponse;

            DebugLog.log("Model: Processing corrective call at row " + callIndex + ". Replacements: "
                    + correctiveCall.replaceList.size() + ", extractions: " + correctiveCall.extractList.size());

            callIndex++;

            int replacementsApplied = 0;

            if (!correctiveCall.replaceList.isEmpty()) {
                for (ReplaceText replaceText : correctiveCall.replaceList) {
                    DebugLog.log("Model: Applying replacement to corrective call request: " + replaceText);

                    /*
                    The extraction this replacement points at has nothing in the map until it actually
                    runs. Without this guard the replacement inserts the four characters "null" into the
                    request. extractText itself is null when the user adds a replacement without picking
                    anything from the "Pick from Extraction" dropdown.
                     */
                    String replacement = replaceText.extractText == null
                            ? null
                            : extracted.get(replaceText.extractText);

                    if (replacement == null) {
                        DebugLog.log("[Error] Model: Replacement has no extracted text to insert, skipping it: "
                                + replaceText.name);
                        continue;
                    }

                    // Each replacement works on the result of the one before it, not on rawRequest, so that
                    // every replacement in the list survives instead of only the last one.
                    String textToReplace = updatedRequest != null ? updatedRequest : rawRequest;
                    String replacedRequest = replaceBetween(textToReplace, replaceText.startString,
                            replaceText.stopString, replacement, replaceText.occurrence);

                    if (replacedRequest != null) {
                        updatedRequest = replacedRequest;
                        replacementsApplied++;
                    }
                    else {
                        // Hold on to whatever the earlier replacements produced. Overwriting updatedRequest with
                        // null here used to throw away replacements that had already applied cleanly.
                        DebugLog.log("[Error] Model: Replacement did not apply, leaving request as it was: "
                                + replaceText.name);
                    }
                }
            }

            if (updatedRequest != null) {
                DebugLog.log("Model: Sending corrective call with replaced request body.");
                HttpRequest rebuiltRequest = HttpRequest.httpRequest(correctiveCallRequest.httpService(), updatedRequest);
                rebuiltRequest = rebuiltRequest.withBody(rebuiltRequest.bodyToString()); // recalculates content-length
                sentRequestResponse = Controller.montoyaApi.http().sendRequest(rebuiltRequest);
            }
            else {
                DebugLog.log("Model: Sending corrective call with unmodified request body.");
                sentRequestResponse = Controller.montoyaApi.http().sendRequest(correctiveCallRequest);
            }
            sentResponse = sentRequestResponse.response();

            /*
            Recorded before the extractions run, so the row exists even if an extraction below fails.
            The extraction tallies are filled in afterwards.
             */
            TestCallStep step = null;
            if (trace != null) {
                step = new TestCallStep(trace.size() + 1, TestCallStep.CORRECTIVE,
                        triggerCondition.correctiveCallSet.name + " / Call " + (callIndex - 1),
                        sentRequestResponse,
                        sentResponse == null ? TestCallStep.NO_RESPONSE : TestCallStep.SENT);
                step.replacementsTotal = correctiveCall.replaceList.size();
                step.replacementsApplied = replacementsApplied;
                step.extractionsRun = correctiveCall.extractList.size();
                step.detail = "fired by " + triggerCondition.name;
                trace.add(step);
            }

            /*
            A corrective call that never completed (connection refused, timeout, TLS failure) comes
            back with a null response, exactly as the intercepted request can. This used to be
            dereferenced unguarded a few lines down and threw NullPointerException out of the HTTP
            handler, taking the whole interception with it.

            Carrying on rather than abandoning the set: an extraction reading this call's own REQUEST
            still works without a response, and any replacement whose extraction did not run is already
            skipped downstream instead of inserting the text "null". So the remaining calls and the
            followup still get their chance, and what is lost is only what genuinely depended on the
            response that never arrived.
             */
            if (sentResponse == null) {
                DebugLog.log("[Error] Model: Corrective call at row " + (callIndex - 1)
                        + " produced no response. Its response extractions cannot run.");
            }

            if (!correctiveCall.extractList.isEmpty()) {
                for (ExtractText extractText : correctiveCall.extractList) {
                    if (extractText.extractFromResponse) { // Needs to come AFTER corrective call is made, this extracts from whatever is sent from repeater
                        DebugLog.log("Model: Extracting from corrective call response: " + extractText.debugString());

                        // Nothing to read when the call never completed. See the null check above.
                        if (sentResponse == null) {
                            DebugLog.log("[Error] Model: No response to extract from, skipping extraction: "
                                    + extractText.name);
                            continue;
                        }

                        rawResponse = sentResponse.toString();
                        String extractedText = extractBetween(rawResponse, extractText.startString, extractText.stopString);

                        if (extractedText != null) {
                            extractedText = Utils.applyUrlTransform(extractedText, extractText.urlEncodeDecode);
                            extracted.put(extractText, extractedText);
                            DebugLog.log("Model: Extracted text: " + extractedText);
                            if (step != null) {
                                step.extractionsHit++;
                            }
                        }
                    }
                    else if (extractText.extractFromRequest) {
                        DebugLog.log("Model: Extracting from corrective call request: " + extractText.debugString());

                        String extractedText = extractBetween(rawRequest, extractText.startString, extractText.stopString);
                        if (extractedText != null) {
                            extractedText = Utils.applyUrlTransform(extractedText, extractText.urlEncodeDecode);
                            extracted.put(extractText, extractedText);
                            DebugLog.log("Model: Extracted text: " + extractedText);
                            if (step != null) {
                                step.extractionsHit++;
                            }
                        }
                    }
                    else {
                        // Not possible but whatever
                        DebugLog.log("[Error] Extraction is set to extract from neither the request nor the response: "
                                + extractText.debugString());
                    }
                }
            }
        }
    }

    private HttpResponse sendFollowupCall(Map<ExtractText, String> extracted) {
        return sendFollowupCall(extracted, null);
    }

    private HttpResponse sendFollowupCall(Map<ExtractText, String> extracted, List<TestCallStep> trace) {
        DebugLog.log("Model: Sending followup call...");

        String rawRequest;
        String updatedRequest = null;


        HttpRequest followupCallRequest = followupCall.requestResponse.request();
        rawRequest = followupCallRequest.toString();

        HttpRequestResponse sentRequestResponse;
        HttpResponse sentResponse;

        /*
        Replacements only. The followup call has no extractions of its own: there is no UI to add any
        (the Followup tab's dropdown only CONSUMES extractions made on corrective calls) and nothing
        here processes followupCall.extractList. This used to log that list's size, which is always 0
        and read as though the feature existed.
         */
        DebugLog.log("Model: Processing followup call. Replacements: "
                + followupCall.replaceList.size());


        int replacementsApplied = 0;

        if (!followupCall.replaceList.isEmpty()) {
            for (ReplaceText replaceText : followupCall.replaceList) {
                DebugLog.log("Model: Applying replacement to followup call request: " + replaceText);

                // Same as processCorrectiveCalls(). Nothing to insert means skip the replacement rather
                // than writing "null" into the request.
                String replacement = replaceText.extractText == null
                        ? null
                        : extracted.get(replaceText.extractText);

                if (replacement == null) {
                    DebugLog.log("[Error] Model: Replacement has no extracted text to insert, skipping it: "
                            + replaceText.name);
                    continue;
                }

                // Same as processCorrectiveCalls(). Chain off the previous result so every replacement
                // survives, and keep the previous result if one of them fails to apply.
                String textToReplace = updatedRequest != null ? updatedRequest : rawRequest;
                String replacedRequest = replaceBetween(textToReplace, replaceText.startString,
                        replaceText.stopString, replacement, replaceText.occurrence);

                if (replacedRequest != null) {
                    updatedRequest = replacedRequest;
                    replacementsApplied++;
                }
                else {
                    DebugLog.log("[Error] Model: Replacement did not apply, leaving request as it was: "
                            + replaceText.name);
                }
            }
        }

        if (updatedRequest != null) {
            DebugLog.log("Model: Sending followup call with replaced request body.");
            HttpRequest rebuiltRequest = HttpRequest.httpRequest(followupCallRequest.httpService(), updatedRequest);
            rebuiltRequest = rebuiltRequest.withBody(rebuiltRequest.bodyToString()); // recalculates content-length
            sentRequestResponse = Controller.montoyaApi.http().sendRequest(rebuiltRequest);
        }
        else {
            DebugLog.log("Model: Sending followup call with unmodified request body.");
            sentRequestResponse = Controller.montoyaApi.http().sendRequest(followupCallRequest);
        }
        sentResponse = sentRequestResponse.response();

        if (trace != null) {
            TestCallStep step = new TestCallStep(trace.size() + 1, TestCallStep.FOLLOWUP, "Followup",
                    sentRequestResponse,
                    sentResponse == null ? TestCallStep.NO_RESPONSE : TestCallStep.SENT);
            step.replacementsTotal = followupCall.replaceList.size();
            step.replacementsApplied = replacementsApplied;
            step.detail = "response returned to the caller";
            trace.add(step);
        }

        return sentResponse;
    }

    private String replaceBetween(String text, String start, String stop, String replacement) {
        return replaceBetween(text, start, stop, replacement, 1);
    }

    private String replaceBetween(String text, String start, String stop, String replacement,
                                  int occurrence) {
        DebugLog.log("Model: replaceBetween() start string: '" + start + "', stop string: '" + stop
                + "', occurrence: " + occurrence + ", within text : '" + text + "'");


        int s = findStart(text, start, occurrence, "replaced");
        if (s == -1) {
            return null;
        }

        int e = findStop(text, stop, s, "replaced");
        if (e == -1) {
            return null;
        }

        DebugLog.log("Model: Replacing text between index " + s + " and index " + e + ".");
        return text.substring(0, s) + replacement + text.substring(e);
    }

    private String extractBetween(String text, String start, String stop) {
        DebugLog.log("Model: extractBetween() start string: '" + start + "', stop string: '" + stop + "' within text : '" + text + "'");

        // Extractions always take the first occurrence. ReplaceText carries an occurrence field;
        // ExtractText does not yet, and adding one is the same three line change plus a UI field.
        int s = findStart(text, start, 1, "extracted");
        if (s == -1) {
            return null;
        }

        int e = findStop(text, stop, s, "extracted");
        if (e == -1) {
            return null;
        }

        DebugLog.log("Model: Extracting text between index " + s + " and index " + e + ".");
        return text.substring(s, e);
    }

    /*
    Resolves a start string to the index the extraction or replacement should begin at, or -1 if it
    cannot be found.

    Utils.START_OF_LINE is a marker rather than real text, so there is nothing in the message to
    indexOf(). It means the very beginning of the message, which is index 0. It used to be fed straight
    into indexOf(), which always returned -1, so every selection made at the very start of a message
    quietly did nothing at all.
     */
    private int findStart(String text, String start, int occurrence, String action) {
        if (start.equals(Utils.START_OF_LINE)) {
            DebugLog.log("Model: Start string is " + Utils.START_OF_LINE + ". Starting at index 0.");
            return 0;
        }

        /*
        Walks forward to the requested occurrence. Matches are counted as non-overlapping, stepping
        past each one by the whole key, because "the 2nd time this string appears" is how someone
        reading the message counts them. Anything below 1 is treated as the first occurrence.
         */
        int wanted = Math.max(1, occurrence);
        int from = 0;
        int s = -1;

        for (int n = 0; n < wanted; n++) {
            s = text.indexOf(start, from);

            if (s == -1) {
                if (n == 0) {
                    DebugLog.log("Model: Start string not found in text. Nothing " + action + ".");
                }
                else {
                    DebugLog.log("Model: Start string found only " + n + " time(s), occurrence "
                            + wanted + " requested. Nothing " + action + ".");
                }
                return -1;
            }

            from = s + start.length();
        }

        if (wanted > 1) {
            DebugLog.log("Model: Using occurrence " + wanted + " of the start string, at index " + s + ".");
        }

        return s + start.length();
    }

    // Same as findStart(). Utils.END_OF_LINE means the very end of the message, which is index length().
    private int findStop(String text, String stop, int fromIndex, String action) {
        if (stop.equals(Utils.END_OF_LINE)) {
            DebugLog.log("Model: Stop string is " + Utils.END_OF_LINE + ". Stopping at index " + text.length() + ".");
            return text.length();
        }

        int e = text.indexOf(stop, fromIndex);
        if (e == -1) {
            DebugLog.log("Model: Stop string not found after start string. Nothing " + action + ".");
            return -1;
        }

        return e;
    }
}
