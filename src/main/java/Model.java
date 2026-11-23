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


public class Model implements HttpHandler {

    // Logical operators as they appear in the trigger condition builder dropdown.
    private static final String AND = "AND";
    private static final String OR = "OR";

    // Empty rather than null, since requests can arrive before SettingsController sets it.
    private EnumSet<ToolType> enabledTools = EnumSet.noneOf(ToolType.class);
    public List<TriggerCondition> triggerConditions;

    // The call sent to the Error Detection tab. Only its extractList is used, to extract from the
    // intercepted exchange before any corrective calls run.
    public CorrectiveCall errorCall = new CorrectiveCall(null);

    // Result of applying a replace list to a request. request is null when nothing applied.
    private record Replaced(String request, int applied) {}

    public Model() {
        if (Controller.montoyaApi != null) {
            Controller.montoyaApi.extension().setName("API Processing Engine");
            Controller.montoyaApi.http().registerHttpHandler(this);
        }
    }


    // HTTP Handler which will intercept all traffic generated in burp
    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        // This determines which tool (Repeater, Intruder, etc.) a request came from.
        ToolType toolType = requestToBeSent.toolSource().toolType();

        /*
        TODO: Does not currently work with Extensions. There is no way to identify which Extension is associated with
        a request. This will make APE endlessly process its own requests.
         */
        if (!enabledTools.contains(toolType) || toolType.toolName().equals("Extension")) {
            DebugLog.log("Model: Not processing response!");
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        // Extracted values for this request only. Keyed by identity since ExtractText is shared config.
        Map<ExtractText, String> extracted = new IdentityHashMap<>();

        // Nothing went out if the send throws, so it is safe to let Burp send it instead.
        HttpRequestResponse requestResponse;
        try {
            requestResponse = Controller.montoyaApi.http().sendRequest(requestToBeSent);
        }
        catch (Exception e) {
            DebugLog.log("[Error] Model: Sending the intercepted request threw: " + e
                    + ". Handing it back to Burp.");
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        // No response (connection refused, timeout) means nothing to evaluate. Let Burp report the error.
        if (requestResponse.response() == null) {
            DebugLog.log("[Error] Model: Request produced no response. Handing it back to Burp.");
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        // The request has already been sent, so from here on always spoof. continueWith() would send it twice.
        try {
            List<TriggerCondition> matched = matchingTriggerConditions(requestResponse.request(),
                    requestResponse.response(), null);

            if (matched.isEmpty()) {
                DebugLog.log("Model: No trigger condition matched. Returning the original response.");
                return RequestToBeSentAction.spoof(requestResponse.response());
            }

            HttpResponse followupCallResponse = correct(matched, requestResponse, extracted, null);

            if (followupCallResponse == null) {
                DebugLog.log("[Error] Model: No followup call returned a response. Returning the original response.");
                return RequestToBeSentAction.spoof(requestResponse.response());
            }

            return RequestToBeSentAction.spoof(followupCallResponse);
        }
        catch (Exception e) {
            DebugLog.log("[Error] Model: Processing the intercepted request threw: " + e
                    + ". Returning the original response.");
            return RequestToBeSentAction.spoof(requestResponse.response());
        }
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return ResponseReceivedAction.continueWith(responseReceived);
    }

    // Allows controller to update model's enabledTools
    public void updateEnabledTools(EnumSet<ToolType> enabledTools) {
        this.enabledTools = EnumSet.copyOf(enabledTools);
    }

    // Extractions on the errored call. Available to every corrective call and the followup.
    public List<ExtractText> errorCallExtractions() {
        if (errorCall == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(errorCall.extractList);
    }


    /*
    ------------------------------------------------------------------ Test Call

    Replays the errored call through the same pipeline as handleHttpRequestToBeSent(), recording each
    send as a TestCallStep. Never returns null; a run that can't start returns one step saying why.
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

        // Keeps the steps that ran and adds a row saying what broke.
        try {
            runTestCall(trace);
        }
        catch (Exception e) {
            DebugLog.log("[Error] Model: Test call threw: " + e);
            TestCallStep step = new TestCallStep(trace.size() + 1,
                    trace.isEmpty() ? TestCallStep.ERRORED : trace.getLast().type,
                    "Error", null, TestCallStep.ERROR);
            step.detail = "test call stopped: " + e;
            trace.add(step);
        }

        return trace;
    }

    private void runTestCall(List<TestCallStep> trace) {
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
            return;
        }

        List<String> conditionResults = new ArrayList<>();
        List<TriggerCondition> matched = matchingTriggerConditions(requestResponse.request(),
                requestResponse.response(), conditionResults);

        first.outcome = matched.isEmpty() ? TestCallStep.NO_MATCH : TestCallStep.TRIGGERED;
        first.detail = String.join(", ", conditionResults);

        if (matched.isEmpty()) {
            DebugLog.log("Model: Test call matched no trigger condition. Stopping after one step.");
            return;
        }

        correct(matched, requestResponse, extracted, trace);

        DebugLog.log("Model: Test call finished in " + trace.size() + " step(s).");
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
    ------------------------------------------------------------------ Trigger conditions
     */

    /*
    First match wins: only the first matched trigger condition, in table order, fires. Runs the errored
    call's extractions, that trigger condition's corrective calls, then its set's followup call. Returns
    the followup's response, or null if none came back.
     */
    private HttpResponse correct(List<TriggerCondition> matched, HttpRequestResponse requestResponse,
                                 Map<ExtractText, String> extracted, List<TestCallStep> trace) {
        TriggerCondition triggerCondition = matched.getFirst();
        CorrectiveCallSet set = triggerCondition.correctiveCallSet;

        if (matched.size() > 1) {
            DebugLog.log("Model: " + matched.size() + " trigger conditions matched. Only the first, "
                    + triggerCondition.name + ", fires.");
        }

        int hits = extractFromErrorCall(requestResponse, extracted);
        if (trace != null && !trace.isEmpty()) {
            trace.getFirst().extractionsHit = hits;
        }

        DebugLog.log("Model: Trigger condition matched: " + triggerCondition.name + ". Making corrective calls.");
        processCorrectiveCalls(triggerCondition, extracted, trace);

        CorrectiveCall followupCall = set.followupCall;

        if (followupCall == null || followupCall.requestResponse == null) {
            DebugLog.log("[Error] Model: " + set.name + " has no followup call configured.");

            if (trace != null) {
                TestCallStep step = new TestCallStep(trace.size() + 1, TestCallStep.FOLLOWUP,
                        set.name + " / Followup", null, TestCallStep.NOT_RUN);
                step.detail = "no followup call configured for " + set.name;
                trace.add(step);
            }
            return null;
        }

        return sendFollowupCall(followupCall, set.name, extracted, trace);
    }

    // Which trigger conditions match, without sending anything. results, if given, gets one line per
    // condition for the Test Calls table. Package-private for the test harnesses.
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

        // A copy, so adding or deleting a trigger condition in the UI can't disturb this loop.
        for (TriggerCondition triggerCondition : new ArrayList<>(triggerConditions)) {
            DebugLog.log("Model: Evaluating trigger condition: " + triggerCondition.name);

            // A condition that throws counts as a miss. The rest are still evaluated.
            boolean hit;
            try {
                hit = evaluateTriggerCondition(triggerCondition, request, response);
            }
            catch (Exception e) {
                DebugLog.log("[Error] Model: Trigger condition '" + triggerCondition.name
                        + "' threw while evaluating. Treating it as not matched. " + e);
                hit = false;
            }

            if (results != null) {
                results.add(triggerCondition.name + (hit ? ": HIT" : ": miss"));
            }

            if (hit) {
                matched.add(triggerCondition);
            }
        }

        return matched;
    }

    // Package-private for the test harnesses. Request-side conditions never match without a request.
    boolean evaluateTriggerCondition(TriggerCondition triggerCondition, HttpResponse response) {
        return evaluateTriggerCondition(triggerCondition, null, response);
    }

    /*
    Evaluates in three steps:
      1. each error condition on its own, with its negation applied
      2. collapse every AND (AND binds tighter than OR)
      3. collapse every remaining OR
    Package-private for the test harnesses.
     */
    boolean evaluateTriggerCondition(TriggerCondition triggerCondition, HttpRequest request,
                                             HttpResponse response) {
        DebugLog.log("Model: Evaluating trigger condition " + triggerCondition.name
                + ". Condition string: " + triggerCondition.conditionString);

        List<Boolean> conditions = new ArrayList<>();
        List<String> operators = new ArrayList<>();

        // Step 1
        for (int i = 0; i < triggerCondition.errorConditions.size(); i++) {
            ErrorCondition errorCondition = triggerCondition.errorConditions.get(i);

            boolean raw = evaluateErrorCondition(errorCondition, request, response);
            boolean isNegated = triggerCondition.isNegated.get(i);
            boolean result = isNegated != raw;

            DebugLog.log("Model: Error condition " + i + " evaluated: " + errorCondition.name
                    + ". Category: '" + errorCondition.category + "', value: '" + errorCondition.value + "'");
            DebugLog.log("Model: Result: " + raw + ", negated: " + isNegated + ", result after negation: " + result);

            conditions.add(result);
        }

        // logicalOperator is null when there is only one error condition.
        if (triggerCondition.logicalOperator != null) {
            operators.addAll(triggerCondition.logicalOperator);
        }

        DebugLog.log("Model: Parsed trigger condition. Conditions: " + conditions + ", operators: " + operators);

        // There must be exactly one operator between each pair of conditions.
        if (operators.size() != conditions.size() - 1) {
            DebugLog.log("[Error] Malformed trigger condition: " + triggerCondition.name + ". Found "
                    + conditions.size() + " condition(s) and " + operators.size() + " operator(s).");
            return false;
        }

        // Steps 2 and 3
        evalAND(conditions, operators);
        evalOR(conditions, operators);

        if (conditions.size() != 1 || !operators.isEmpty()) {
            DebugLog.log("[Error] Trigger condition did not collapse to a single value: " + triggerCondition.name
                    + ". Conditions: " + conditions + ", operators: " + operators);
            return false;
        }

        DebugLog.log("Model: Trigger condition " + triggerCondition.name + " evaluates to "
                + conditions.getFirst());
        return conditions.getFirst();
    }

    /*
    Collapses every AND in place, left to right. operators.get(i) joins conditions.get(i) and
    conditions.get(i + 1), so each AND folds that pair into index i. Package-private for the test harnesses.
     */
    void evalAND(List<Boolean> conditions, List<String> operators) {
        DebugLog.log("Model: evalAND() input. Conditions: " + conditions + ", operators: " + operators);

        int i = 0;
        while (i < operators.size()) {
            if (!operators.get(i).equals(AND)) {
                i++; // leave ORs alone, evalOR handles them
                continue;
            }

            boolean result = conditions.get(i) && conditions.get(i + 1);

            DebugLog.log("Model: Collapsing AND at operator " + i + ". " + conditions.get(i) + " AND "
                    + conditions.get(i + 1) + " = " + result);

            operators.remove(i);
            conditions.remove(i + 1);
            conditions.set(i, result);

            // i is not advanced, so a following AND (A AND B AND C) folds into this result.
        }

        DebugLog.log("Model: evalAND() output. Conditions: " + conditions + ", operators: " + operators);
    }

    // Same as evalAND. Runs second, so every remaining operator should be an OR.
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

            boolean result = conditions.get(i) || conditions.get(i + 1);

            DebugLog.log("Model: Collapsing OR at operator " + i + ". " + conditions.get(i) + " OR "
                    + conditions.get(i + 1) + " = " + result);

            operators.remove(i);
            conditions.remove(i + 1);
            conditions.set(i, result);
        }

        DebugLog.log("Model: evalOR() output. Conditions: " + conditions + ", operators: " + operators);
    }

    private boolean evaluateErrorCondition(ErrorCondition errorCondition, HttpRequest request,
                                           HttpResponse response) {
        boolean result = false;
        boolean useRequest = errorCondition.matchInRequest;

        // Status Code always reads the response, so only Body and Header need a request.
        if (useRequest && request == null && !errorCondition.category.equals("Status Code")) {
            DebugLog.log("[Error] Model: Error condition '" + errorCondition.name + "' matches the "
                    + "request, but no request was supplied. Treating it as not matched.");
            return false;
        }

        switch (errorCondition.category) {
            case "Status Code":
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
                boolean anyValue = errorCondition.value == null || errorCondition.value.isEmpty();

                // Header names are case-insensitive. A blank value matches on the header's presence alone.
                for (HttpHeader header : headers) {
                    if (header.name().equalsIgnoreCase(errorCondition.headerName)
                            && (anyValue || header.value().contains(errorCondition.value))) {
                        result = true;
                    }
                }
                DebugLog.log("Model: " + headers.size() + " " + errorCondition.source()
                        + " header(s) searched for name '" + errorCondition.headerName + "'"
                        + (anyValue ? "" : " containing '" + errorCondition.value + "'")
                        + ". Result: " + result);
                return result;
            default:
                DebugLog.log("[Error] Unknown error condition category: '" + errorCondition.category + "'");
                return false;
        }
    }


    /*
    ------------------------------------------------------------------ Corrective and followup calls
     */

    // Sends each call in the trigger condition's set. trace, if given, gets one TestCallStep per call.
    private void processCorrectiveCalls(TriggerCondition triggerCondition, Map<ExtractText, String> extracted,
                                        List<TestCallStep> trace) {
        DebugLog.log("Model: Processing corrective call set: " + triggerCondition.correctiveCallSet.name
                + ". Call sequence size: " + triggerCondition.correctiveCallSet.correctiveCallList.size());

        int callIndex = 0;

        for (CorrectiveCall correctiveCall : new ArrayList<>(triggerCondition.correctiveCallSet.correctiveCallList)) {
            // A call that throws is skipped. The rest of the set still runs.
            try {
                processCorrectiveCall(triggerCondition, correctiveCall, callIndex, extracted, trace);
            }
            catch (Exception e) {
                DebugLog.log("[Error] Model: Corrective call at row " + callIndex + " threw, skipping it. " + e);

                if (trace != null) {
                    TestCallStep step = new TestCallStep(trace.size() + 1, TestCallStep.CORRECTIVE,
                            triggerCondition.correctiveCallSet.name + " / Call " + callIndex,
                            null, TestCallStep.ERROR);
                    step.detail = "threw: " + e;
                    trace.add(step);
                }
            }

            callIndex++;
        }
    }

    // One corrective call: apply its replacements, send it, run its extractions.
    private void processCorrectiveCall(TriggerCondition triggerCondition, CorrectiveCall correctiveCall,
                                       int callIndex, Map<ExtractText, String> extracted,
                                       List<TestCallStep> trace) {
        HttpRequest correctiveCallRequest = correctiveCall.requestResponse.request();
        String rawRequest = correctiveCallRequest.toString();

        DebugLog.log("Model: Processing corrective call at row " + callIndex + ". Replacements: "
                + correctiveCall.replaceList.size() + ", extractions: " + correctiveCall.extractList.size());

        Replaced replaced = applyReplacements(rawRequest, correctiveCall.replaceList, extracted);
        HttpRequestResponse sentRequestResponse = send(correctiveCallRequest, replaced.request());
        HttpResponse sentResponse = sentRequestResponse.response();

        // Added before the extractions run, so the row exists even if they fail.
        TestCallStep step = null;
        if (trace != null) {
            step = new TestCallStep(trace.size() + 1, TestCallStep.CORRECTIVE,
                    triggerCondition.correctiveCallSet.name + " / Call " + callIndex,
                    sentRequestResponse,
                    sentResponse == null ? TestCallStep.NO_RESPONSE : TestCallStep.SENT);
            step.replacementsTotal = correctiveCall.replaceList.size();
            step.replacementsApplied = replaced.applied();
            step.extractionsRun = correctiveCall.extractList.size();
            step.detail = "fired by " + triggerCondition.name;
            trace.add(step);
        }

        // No response means only request extractions can run. The rest of the set still gets its chance.
        if (sentResponse == null) {
            DebugLog.log("[Error] Model: Corrective call at row " + callIndex
                    + " produced no response. Its response extractions cannot run.");
        }

        for (ExtractText extractText : new ArrayList<>(correctiveCall.extractList)) {
            String source;

            if (extractText.extractFromResponse) {
                if (sentResponse == null) {
                    DebugLog.log("[Error] Model: No response to extract from, skipping extraction: "
                            + extractText.name);
                    continue;
                }
                source = sentResponse.toString();
            }
            else if (extractText.extractFromRequest) {
                source = rawRequest;
            }
            else {
                DebugLog.log("[Error] Extraction is set to extract from neither the request nor the response: "
                        + extractText.debugString());
                continue;
            }

            if (extract(extractText, source, extracted, "corrective call") && step != null) {
                step.extractionsHit++;
            }
        }
    }

    // The followup has replacements only, no extractions of its own.
    private HttpResponse sendFollowupCall(CorrectiveCall followupCall, String setName,
                                          Map<ExtractText, String> extracted, List<TestCallStep> trace) {
        HttpRequest followupCallRequest = followupCall.requestResponse.request();

        DebugLog.log("Model: Processing followup call for " + setName + ". Replacements: " + followupCall.replaceList.size());

        Replaced replaced = applyReplacements(followupCallRequest.toString(), followupCall.replaceList, extracted);

        // Callers already treat a null response as "return the original response".
        HttpRequestResponse sentRequestResponse;
        try {
            sentRequestResponse = send(followupCallRequest, replaced.request());
        }
        catch (Exception e) {
            DebugLog.log("[Error] Model: Sending the followup call threw: " + e);

            if (trace != null) {
                TestCallStep step = new TestCallStep(trace.size() + 1, TestCallStep.FOLLOWUP, setName + " / Followup",
                        null, TestCallStep.ERROR);
                step.replacementsTotal = followupCall.replaceList.size();
                step.replacementsApplied = replaced.applied();
                step.detail = "send threw: " + e;
                trace.add(step);
            }

            return null;
        }
        HttpResponse sentResponse = sentRequestResponse.response();

        if (trace != null) {
            TestCallStep step = new TestCallStep(trace.size() + 1, TestCallStep.FOLLOWUP, setName + " / Followup",
                    sentRequestResponse,
                    sentResponse == null ? TestCallStep.NO_RESPONSE : TestCallStep.SENT);
            step.replacementsTotal = followupCall.replaceList.size();
            step.replacementsApplied = replaced.applied();
            step.detail = "response returned to the caller";
            trace.add(step);
        }

        return sentResponse;
    }

    // Sends original, or updatedRequest rebuilt against original's service when there is one.
    private HttpRequestResponse send(HttpRequest original, String updatedRequest) {
        if (updatedRequest == null) {
            DebugLog.log("Model: Sending request unmodified.");
            return Controller.montoyaApi.http().sendRequest(original);
        }

        DebugLog.log("Model: Sending request with replacements applied.");
        HttpRequest rebuiltRequest = HttpRequest.httpRequest(original.httpService(), updatedRequest);
        rebuiltRequest = rebuiltRequest.withBody(rebuiltRequest.bodyToString()); // recalculates content-length
        return Controller.montoyaApi.http().sendRequest(rebuiltRequest);
    }

    /*
    Applies each replacement in turn, each working on the previous one's result. A replacement whose
    extraction found nothing is skipped rather than inserting "null".
     */
    private Replaced applyReplacements(String rawRequest, List<ReplaceText> replaceList,
                                       Map<ExtractText, String> extracted) {
        String updatedRequest = null;
        int applied = 0;

        for (ReplaceText replaceText : new ArrayList<>(replaceList)) {
            DebugLog.log("Model: Applying replacement: " + replaceText);

            // extractText is null when nothing was picked from the "Pick from Extraction" dropdown.
            String replacement = replaceText.extractText == null ? null : extracted.get(replaceText.extractText);

            if (replacement == null) {
                DebugLog.log("[Error] Model: Replacement has no extracted text to insert, skipping it: "
                        + replaceText.name);
                continue;
            }

            String textToReplace = updatedRequest != null ? updatedRequest : rawRequest;
            String replacedRequest = replaceBetween(textToReplace, replaceText.startString,
                    replaceText.stopString, replacement);

            if (replacedRequest != null) {
                updatedRequest = replacedRequest;
                applied++;
            }
            else {
                DebugLog.log("[Error] Model: Replacement did not apply, leaving request as it was: "
                        + replaceText.name);
            }
        }

        return new Replaced(updatedRequest, applied);
    }


    /*
    ------------------------------------------------------------------ Extraction
     */

    // Runs the errored call's extractions against the intercepted exchange. Returns how many found something.
    private int extractFromErrorCall(HttpRequestResponse requestResponse, Map<ExtractText, String> extracted) {
        if (errorCall == null || errorCall.extractList.isEmpty()) {
            return 0;
        }

        String rawRequest = requestResponse.request().toString();
        String rawResponse = requestResponse.response().toString();
        int hits = 0;

        DebugLog.log("Model: Running " + errorCall.extractList.size()
                + " extraction(s) against the errored call.");

        for (ExtractText extractText : new ArrayList<>(errorCall.extractList)) {
            String source = extractText.extractFromRequest ? rawRequest : rawResponse;
            if (extract(extractText, source, extracted, "errored call")) {
                hits++;
            }
        }

        return hits;
    }

    // Runs one extraction against source and stores the result. Returns false if nothing was extracted.
    private boolean extract(ExtractText extractText, String source, Map<ExtractText, String> extracted,
                            String from) {
        try {
            DebugLog.log("Model: Extracting from " + from + ": " + extractText.debugString());
            String extractedText = extractBetween(source, extractText.startString, extractText.stopString);

            if (extractedText == null) {
                DebugLog.log("[Error] Model: Extraction from " + from + " found nothing: " + extractText.name);
                return false;
            }

            extractedText = Utils.applyUrlTransform(extractedText, extractText.urlEncodeDecode);
            extracted.put(extractText, extractedText);
            DebugLog.log("Model: Extracted text: " + extractedText);
            return true;
        }
        catch (Exception e) {
            DebugLog.log("[Error] Model: Extraction from " + from + " threw, skipping it: "
                    + extractText.name + ". " + e);
            return false;
        }
    }

    // Returns null if the start or stop string isn't found.
    private String replaceBetween(String text, String start, String stop, String replacement) {
        DebugLog.log("Model: replaceBetween() start string: '" + start + "', stop string: '" + stop
                + "' within text : '" + text + "'");

        try {
            int s = findStart(text, start, "replaced");
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
        catch (Exception ex) {
            DebugLog.log("[Error] Model: replaceBetween() threw. Nothing replaced. " + ex);
            return null;
        }
    }

    // Returns null if the start or stop string isn't found.
    private String extractBetween(String text, String start, String stop) {
        DebugLog.log("Model: extractBetween() start string: '" + start + "', stop string: '" + stop + "' within text : '" + text + "'");

        try {
            int s = findStart(text, start, "extracted");
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
        catch (Exception ex) {
            DebugLog.log("[Error] Model: extractBetween() threw. Nothing extracted. " + ex);
            return null;
        }
    }

    // Index just past the first occurrence of start, or -1 if not found. Utils.START_OF_MESSAGE means index 0.
    private int findStart(String text, String start, String action) {
        if (start.equals(Utils.START_OF_MESSAGE)) {
            DebugLog.log("Model: Start string is " + Utils.START_OF_MESSAGE + ". Starting at index 0.");
            return 0;
        }

        int s = text.indexOf(start);
        if (s == -1) {
            DebugLog.log("Model: Start string not found in text. Nothing " + action + ".");
            return -1;
        }

        return s + start.length();
    }

    // Index of stop at or after fromIndex, or -1 if not found. Utils.END_OF_MESSAGE means the end of text.
    private int findStop(String text, String stop, int fromIndex, String action) {
        if (stop.equals(Utils.END_OF_MESSAGE)) {
            DebugLog.log("Model: Stop string is " + Utils.END_OF_MESSAGE + ". Stopping at index " + text.length() + ".");
            return text.length();
        }

        int e = text.indexOf(stop, fromIndex);
        if (e == -1) {
            DebugLog.log("Model: Stop string not found after start string. Nothing " + action + ".");
        }

        return e;
    }
}
