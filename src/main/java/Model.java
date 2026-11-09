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

        DebugLog.log("Model toolType.toolName(): " + toolType.toString());

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
            DebugLog.log("detectErrorCondition: no trigger conditions configured -> false");
            return;
        }



        for (TriggerCondition triggerCondition : triggerConditions) {
            DebugLog.log("detectErrorCondition: evaluating " + triggerCondition.name);
            if (evaluateTriggerCondition(triggerCondition, response)) {
                // Remember which one fired so the corrective call step knows what to run.

                DebugLog.log("detectErrorCondition: " + triggerCondition.name + " MATCHED -> true");

                // Make corrective calls
                for (CorrectiveCall correctiveCall : triggerCondition.correctiveCallSet.correctiveCallList){
                    Controller.montoyaApi.http().sendRequest(correctiveCall.requestResponse.request());
                }
            }
        }
        DebugLog.log("detectErrorCondition: no trigger condition matched -> false");
    }

    /*
    Evaluates one trigger condition in three steps, which is the only logic the UI can express:
      1. evaluate each error condition on its own and apply its negation checkbox
      2. collapse every AND (AND binds tighter than OR)
      3. collapse every remaining OR
    The working lists are local, so nothing carries over between responses or between trigger conditions.
     */
    Boolean evaluateTriggerCondition(TriggerCondition triggerCondition, HttpResponse response) {
        DebugLog.log("evaluateTriggerCondition: " + triggerCondition.name
                + " [" + triggerCondition.conditionString + "]");

        List<Boolean> conditions = new ArrayList<>();
        List<String> operators = new ArrayList<>();

        // Step 1: each error condition on its own, negation applied immediately.
        for (int i = 0; i < triggerCondition.errorConditions.size(); i++) {
            ErrorCondition errorCondition = triggerCondition.errorConditions.get(i);

            Boolean raw = evaluateErrorCondidition(errorCondition, response);
            Boolean isNegated = triggerCondition.isNegated.get(i);
            Boolean result = evaluateNegation(raw, isNegated);

            DebugLog.log("  condition[" + i + "] " + errorCondition.name
                    + " (" + errorCondition.category + " = '" + errorCondition.value + "')"
                    + " raw=" + raw + " negated=" + isNegated + " -> " + result);

            conditions.add(result);
        }

        // TriggerCondition leaves logicalOperator null when there is only one error condition.
        if (triggerCondition.logicalOperator != null) {
            operators.addAll(triggerCondition.logicalOperator);
        }

        DebugLog.log("  parsed conditions=" + conditions + " operators=" + operators);

        // One operator joins each adjacent pair of error conditions, so there is always one fewer
        // operator than condition. Anything else means the UI handed us something malformed.
        if (operators.size() != conditions.size() - 1) {
            DebugLog.log("  [Error] malformed trigger condition: " + conditions.size() + " condition(s) with "
                    + operators.size() + " operator(s), expected " + (conditions.size() - 1) + " -> false");
            return false;
        }

        // Step 2 then step 3.
        evalAND(conditions, operators);
        evalOR(conditions, operators);

        if (conditions.size() != 1 || !operators.isEmpty()) {
            DebugLog.log("  [Error] did not collapse to a single value, conditions=" + conditions
                    + " operators=" + operators + " -> false");
            return false;
        }

        DebugLog.log("  => " + triggerCondition.name + " evaluates to " + conditions.getFirst());
        return conditions.getFirst();
    }

    /*
    Collapses every AND in place, left to right. operators.get(i) joins conditions.get(i) and
    conditions.get(i+1), so folding an AND replaces that pair with a single value at index i and
    drops the operator. Mutates both lists.
     */
    void evalAND(List<Boolean> conditions, List<String> operators) {
        DebugLog.log("  evalAND  in: conditions=" + conditions + " operators=" + operators);

        int i = 0;
        while (i < operators.size()) {
            if (!operators.get(i).equals(AND)) {
                i++; // leave ORs alone, evalOR handles them
                continue;
            }

            Boolean left = conditions.get(i);
            Boolean right = conditions.get(i + 1);
            Boolean result = left && right;

            DebugLog.log("    AND at operator[" + i + "]: " + left + " AND " + right + " = " + result);

            operators.remove(i);       // operator is consumed
            conditions.remove(i + 1);  // right operand folds into the left one
            conditions.set(i, result); // which now holds the result

            // i is deliberately not advanced. operators.get(i) is now whatever followed the pair we
            // just folded, e.g. the second AND of "A AND B AND C", which must fold into this result.
            DebugLog.log("    collapsed to: conditions=" + conditions + " operators=" + operators);
        }

        DebugLog.log("  evalAND out: conditions=" + conditions + " operators=" + operators);
    }

    // Same collapse as evalAND. Runs second, so by now every remaining operator should be an OR.
    void evalOR(List<Boolean> conditions, List<String> operators) {
        DebugLog.log("  evalOR   in: conditions=" + conditions + " operators=" + operators);

        int i = 0;
        while (i < operators.size()) {
            if (!operators.get(i).equals(OR)) {
                DebugLog.log("    [Error] unrecognized operator[" + i + "] '" + operators.get(i)
                        + "', skipping it");
                i++;
                continue;
            }

            Boolean left = conditions.get(i);
            Boolean right = conditions.get(i + 1);
            Boolean result = left || right;

            DebugLog.log("    OR at operator[" + i + "]: " + left + " OR " + right + " = " + result);

            operators.remove(i);
            conditions.remove(i + 1);
            conditions.set(i, result);

            DebugLog.log("    collapsed to: conditions=" + conditions + " operators=" + operators);
        }

        DebugLog.log("  evalOR  out: conditions=" + conditions + " operators=" + operators);
    }

    private Boolean evaluateErrorCondidition(ErrorCondition errorCondition, HttpResponse response) {
        boolean result = false; //Should use only primitive boolean not obj Boolean because there is no use for it and it is more expensive

        switch (errorCondition.category) {
            case "Status Code":
                result = String.valueOf(response.statusCode()).equals(errorCondition.value);
                return result;
            case "Body":
                result = response.body().toString().contains(errorCondition.value);
                return result;
            case "Header":
                for (HttpHeader header : response.headers()) {
                    if (header.value().contains(errorCondition.value)) result = true;
                }
                return result;
            default:
                DebugLog.log("  [Error] unknown error condition category '" + errorCondition.category + "'");
                return false;
        }
    }


    // Applies the negation checkbox that belongs to a single error condition. Through the UI, negation
    // can only be attached to an individual error condition, never to a group of them.
    Boolean evaluateNegation(Boolean result, Boolean isNegated) {
        if (isNegated) {
            return !result;
        }
        else {
            return result;
        }
    }
}
