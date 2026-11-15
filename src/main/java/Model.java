import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public class Model implements HttpHandler{

    // Logical operators as they appear in the UI dropdown (TriggerConditionController).
    public static final String AND = "AND";
    public static final String OR = "OR";

    public EnumSet<ToolType> enabledTools;
    public Boolean triggerConditionIsSet;
    public List<TriggerCondition> triggerConditions;
    public CorrectiveCall followupCall;

    // The trigger condition that most recently evaluated to true. Set by detectErrorCondition so the
    // caller knows which corrective call set to run.
    TriggerCondition matchedTriggerCondition;

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
        return null;
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {

        // This determines which tool (Repeater, Intruder, etc.) a request came from.
        // TODO make sure this check makes sense on the response.
        ToolType toolType = responseReceived.toolSource().toolType();

        /*
        TODO: Make this work with extensions in an elegant way. Extensions currently disabled.
         Since APE itself is an extension, it was triggering the processResponse function multiple
         times. Need a way to make sure processResponse is not called for requests made by APE.
         Currently no way to identify which extension sends a request.
         */
        if (!enabledTools.contains(toolType) || !triggerConditionIsSet || toolType.toolName().equals("Extension")) {
            DebugLog.log("Model: Not processing response!");
            return ResponseReceivedAction.continueWith(responseReceived);
        }

        DebugLog.log("Model: Processing response...");

        detectErrorCondition(responseReceived);

        return ResponseReceivedAction.continueWith(responseReceived);
    }

    // Allows controller to update model's enabledTools
    public void updateEnabledTools(EnumSet<ToolType> enabledTools) {
        this.enabledTools = EnumSet.copyOf(enabledTools);
    }



    // Detects error conditions and returns the corrective call set to be called.
    // A response triggers APE if ANY of the configured trigger conditions evaluates to true.
    void detectErrorCondition(HttpResponse response) {

        if (triggerConditions == null || triggerConditions.isEmpty()) {
            DebugLog.log("Model: No trigger conditions configured. Not processing response further.");
            return;
        }



        for (TriggerCondition triggerCondition : triggerConditions) {
            DebugLog.log("Model: Evaluating trigger condition: " + triggerCondition.name);
            if (evaluateTriggerCondition(triggerCondition, response)) {
                // Remember which one fired so the corrective call step knows what to run.

                DebugLog.log("Model: Trigger condition matched: " + triggerCondition.name
                        + ". Making corrective calls.");

                processCorrectiveCalls(triggerCondition);
            }
        }
        DebugLog.log("Model: Finished evaluating " + triggerConditions.size() + " trigger condition(s).");
    }

    /*
    Evaluates one trigger condition in three steps, which is the only logic the UI can express:
      1. evaluate each error condition on its own and apply its negation checkbox
      2. collapse every AND (AND binds tighter than OR)
      3. collapse every remaining OR
    The working lists are local, so nothing carries over between responses or between trigger conditions.
     */
    Boolean evaluateTriggerCondition(TriggerCondition triggerCondition, HttpResponse response) {
        DebugLog.log("Model: Evaluating trigger condition " + triggerCondition.name
                + ". Condition string: " + triggerCondition.conditionString);

        List<Boolean> conditions = new ArrayList<>();
        List<String> operators = new ArrayList<>();

        // Step 1: each error condition on its own, negation applied immediately.
        for (int i = 0; i < triggerCondition.errorConditions.size(); i++) {
            ErrorCondition errorCondition = triggerCondition.errorConditions.get(i);

            Boolean raw = evaluateErrorCondidition(errorCondition, response);
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

            // i is deliberately not advanced. operators.get(i) is now whatever followed the pair we
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

    private Boolean evaluateErrorCondidition(ErrorCondition errorCondition, HttpResponse response) {
        boolean result = false; //Should use only primitive boolean not obj Boolean because there is no use for it and it is more expensive

        switch (errorCondition.category) {
            case "Status Code":
                result = String.valueOf(response.statusCode()).equals(errorCondition.value);
                DebugLog.log("Model: Status code " + response.statusCode() + " compared to '"
                        + errorCondition.value + "'. Result: " + result);
                return result;
            case "Body":
                result = response.body().toString().contains(errorCondition.value);
                DebugLog.log("Model: Response body searched for '" + errorCondition.value
                        + "'. Result: " + result);
                return result;
            case "Header":
                for (HttpHeader header : response.headers()) {
                    if (header.value().contains(errorCondition.value)) result = true;
                }
                DebugLog.log("Model: " + response.headers().size() + " response header(s) searched for '"
                        + errorCondition.value + "'. Result: " + result);
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
    private void processCorrectiveCalls(TriggerCondition triggerCondition) {
        DebugLog.log("Model: Processing corrective call set: " + triggerCondition.correctiveCallSet.name
                + ". Call sequence size: " + triggerCondition.correctiveCallSet.correctiveCallList.size());

        int callIndex = 0;

        for (CorrectiveCall correctiveCall : triggerCondition.correctiveCallSet.correctiveCallList){
            HttpRequestResponse sentRequestResponse = null;
            String updatedRequest = null;
            HttpRequest correctiveCallRequest = correctiveCall.requestResponse.request();

            DebugLog.log("Model: Processing corrective call at row " + callIndex + ". Replacements: "
                    + correctiveCall.replaceList.size() + ", extractions: " + correctiveCall.extractList.size());
            callIndex++;

            if (!correctiveCall.replaceList.isEmpty()) {
                for (ReplaceText replaceText : correctiveCall.replaceList) {

                    DebugLog.log("Model: Applying replacement to corrective call request: " + replaceText);

                    String text = correctiveCallRequest.bodyToString();
                    updatedRequest = replaceBetween(text, replaceText.startString, replaceText.stopString, replaceText.extractText.extractedText);

                    if (updatedRequest == null) {
                        DebugLog.log("[Error] Replacement not applied. Start string '" + replaceText.startString
                                + "' and stop string '" + replaceText.stopString + "' were not found in the request body.");
                    }
                }
            }

            if (updatedRequest != null) {
                DebugLog.log("Model: Sending corrective call with replaced request body.");
                sentRequestResponse = Controller.montoyaApi.http().sendRequest(correctiveCallRequest.withBody(updatedRequest));
            }
            else {
                DebugLog.log("Model: Sending corrective call with unmodified request body.");
                sentRequestResponse = Controller.montoyaApi.http().sendRequest(correctiveCallRequest);
            }

            if (!correctiveCall.extractList.isEmpty()) {
                for (ExtractText extractText : correctiveCall.extractList) {
                    if (extractText.extractFromResponse) { // Needs to come AFTER corrective call is made, this extracts from whatever is sent from repeater
                        DebugLog.log("Model: Extracting from corrective call response: " + extractText.debugString());

                        HttpResponse sentResponse = sentRequestResponse.response();
                        String text = sentResponse.bodyToString();
                        String extractedText = extractBetween(text, extractText.startString, extractText.stopString);
                        if (!extractedText.isEmpty())
                                extractText.extractedText = extractedText;

                        DebugLog.log("Model: Extracted text: " + extractText.extractedText);
                    }
                    else if (extractText.extractFromRequest) {
                        DebugLog.log("Model: Extracting from corrective call request: " + extractText.debugString());

                        HttpRequest sentRequest = sentRequestResponse.request();
                        String text = sentRequest.bodyToString();

                        String extractedText = extractBetween(text, extractText.startString, extractText.stopString);
                        if (!extractedText.isEmpty())
                            extractText.extractedText = extractedText;

                        DebugLog.log("Model: Extracted text: " + extractText.extractedText);
                    }
                    else {
                        // Not possible but whatever
                        DebugLog.log("[Error] Extraction is set to extract from neither the request nor the response: "
                                + extractText.debugString());
                    }
                }
            }
        }

        DebugLog.log("Model: Finished processing corrective call set: " + triggerCondition.correctiveCallSet.name);
        DebugLog.log("Model: Processing followup call...");

        HttpRequestResponse sentRequestResponse = null;
        String updatedRequest = null;
        HttpRequest correctiveCallRequest = followupCall.requestResponse.request();


        if (!followupCall.replaceList.isEmpty()) {
            for (ReplaceText replaceText : followupCall.replaceList) {
                DebugLog.log("Model: Applying replacement to followupCall call request: " + replaceText);

                String rawRequest = correctiveCallRequest.toString();
                updatedRequest = replaceBetween(rawRequest, replaceText.startString, replaceText.stopString, replaceText.extractText.extractedText);

                if (updatedRequest == null) {
                    DebugLog.log("[Error] Replacement not applied. Start string '" + replaceText.startString
                            + "' and stop string '" + replaceText.stopString + "' were not found in the request body.");
                }
            }
        }

        if (updatedRequest != null) {
            DebugLog.log("Model: Sending corrective call with replaced request body.");
            HttpRequest rebuiltRequest = HttpRequest.httpRequest(correctiveCallRequest.httpService(), updatedRequest);
            rebuiltRequest = rebuiltRequest.withBody(rebuiltRequest.bodyToString()); // recalculates content-length
            Controller.montoyaApi.http().sendRequest(rebuiltRequest);
        }
        else {
            DebugLog.log("Model: Sending corrective call with unmodified request body.");
            Controller.montoyaApi.http().sendRequest(correctiveCallRequest);
        }
    }

    private String replaceBetween(String text, String start, String stop, String replacement) {
        DebugLog.log("Model: replaceBetween() start string: '" + start + "', stop string: '" + stop
                + "', within text : '" + text + "'");


        int s = text.indexOf(start);
        if (s == -1) {
            DebugLog.log("Model: Start string not found in text. Nothing replaced.");
            return null;
        }


        s += start.length();
        int e = text.indexOf(stop, s);
        if (e == -1) {
            DebugLog.log("Model: Stop string not found after start string. Nothing replaced.");
            return null;
        }

        DebugLog.log("Model: Replacing text between index " + s + " and index " + e + ".");
        return text.substring(0, s) + replacement + text.substring(e);
    }

    private String extractBetween(String text, String start, String stop) {
        DebugLog.log("Model: extractBetween() start string: '" + start + "', stop string: '" + stop + "' within text : '" + text + "'");

        int s = text.indexOf(start);
        if (s == -1) {
            DebugLog.log("Model: Start string not found in text. Nothing extracted.");
            return null;
        }

        s += start.length();
        int e = text.indexOf(stop, s);
        if (e == -1) {
            DebugLog.log("Model: Stop string not found after start string. Nothing extracted.");
            return null;
        }

        DebugLog.log("Model: Extracting text between index " + s + " and index " + e + ".");
        return text.substring(s, e);
    }
}
