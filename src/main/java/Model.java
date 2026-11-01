import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import org.xml.sax.ErrorHandler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;


public class Model implements HttpHandler{

    public EnumSet<ToolType> enabledTools;
    public Boolean triggerConditionIsSet;
    public List<TriggerCondition> triggerConditions;

    private List<Boolean> conditions;
    public ArrayList<ErrorCondition> errorConditionsBuffer;
    public ArrayList<Boolean> negationBuffer;
    public ArrayList<String> logicalOperatorsBuffer;
    
    private TriggerCondition triggerCondition;
    private HttpResponse response;

    public Model () {
        // TODO Maybe just move this to controller
        Controller.montoyaApi.extension().setName("APE");
        Controller.montoyaApi.http().registerHttpHandler(this);
    }

    // HTTP Handler which will intercept all traffic generated in burp
    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        return null;
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        response = responseReceived;


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
        
        if (detectErrorCondition()){
            // Make corrective calls
        }

        return ResponseReceivedAction.continueWith(responseReceived);
    }

    // Allows controller to update model's enabledTools
    public void updateEnabledTools(EnumSet<ToolType> enabledTools) {
        this.enabledTools = EnumSet.copyOf(enabledTools);
    }


    // Detects error conditions and returns the corrective call set to be called.
    private Boolean detectErrorCondition() {
        for (TriggerCondition triggerCondition : triggerConditions){
            // If only 1 error condition, evaluate the single error condition
            if (triggerCondition.errorConditions.size() == 1) {
                if (triggerCondition.isNegated.get(0)) {
                    return !evaluateErrorCondidition(triggerCondition.errorConditions.get(0), response);
                }
                return evaluateErrorCondidition(triggerCondition.errorConditions.get(0), response);
            }
            // If many error conditions, we need to evaluate the error conditions and logical operators which
            // belong to the trigger condition
            else{

                for (int i = 0; i < triggerCondition.errorConditions.size(); i++) {
                    boolean result = evaluateErrorCondidition(triggerCondition.errorConditions.get(i), response);
                    result = evaluateNegation(result, i);
                    conditions.add(result);
                }

                initBuffers();
                evalAND();
                evalOR();

                /*
                Boolean result = false; // ASSUMED FALSE BUT IDK IF THIS IS CORRECT
                this.triggerCondition = triggerCondition; // This is a bit disorganized... sets the class attribute to the iterated obj
                return evaluateErrorConditions(result);
                */
            }
        }
        return false; // Make sure this is correct
    }

    private void evalAND() {
        for (int i = 0; i < logicalOperatorsBuffer.size(); i++ ) {
            if (logicalOperatorsBuffer.get(i).equals("AND")) {
                // Get boolean before and after index
                Boolean result = conditions.get(i) && conditions.get(i+1);
                // need to collapse / erase / pop the AND into a single boolean now
                logicalOperatorsBuffer.remove(i);
                conditions.remove(i);
                conditions.remove(i+1);
                conditions.addFirst(result);
            }
        }
    }

    private void evalOR() {
        for (int i = 0; i < logicalOperatorsBuffer.size(); i++ ) {
            if (logicalOperatorsBuffer.get(i).equals("OR")) {
                // Get boolean before and after index
                Boolean result = conditions.get(i) || conditions.get(i+1);
                // need to collapse / erase / pop the AND into a single boolean now
                logicalOperatorsBuffer.remove(i);
                conditions.remove(i);
                conditions.remove(i+1);
                conditions.addFirst(result);
            }
        }
    }

    // TODO this was already merged into one method.... cleanup this
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
                return false;
        }
    }



    /*
    private Boolean evaluateErrorConditions(Boolean result) {
        // what is base case? when buffer is empty
        if (triggerCondition.errorConditionsBuffer.isEmpty()) {
            return result;
        }
        else if (triggerCondition.errorConditionsBuffer.size() == 1) {
            switch (triggerCondition.errorConditionsBuffer.getFirst().category) {
                case "Status Code":
                    result = (String.valueOf(response.statusCode()).equals(triggerCondition.errorConditionsBuffer.getFirst().value));
                    triggerCondition.errorConditionsBuffer.removeFirst();
                    return result;
                case "Body":
                    result = (response.body().toString().contains(triggerCondition.errorConditionsBuffer.getFirst().value));
                    triggerCondition.errorConditionsBuffer.removeFirst();
                    return result;
                case "Header":
                    for (HttpHeader header : response.headers()) {
                        if (header.value().contains(triggerCondition.errorConditionsBuffer.getFirst().value)) result = true;
                    }
                    triggerCondition.errorConditionsBuffer.removeFirst();
                    return result;
            }
        }
        else {
            switch (triggerCondition.errorConditionsBuffer.getFirst().category) {
                case "Status Code":
                    result = (String.valueOf(response.statusCode()).equals(triggerCondition.errorConditionsBuffer.getFirst().value));
                    result = evaluateNegation(result);

                    if (Objects.equals(errorConditionsBuffer.getFirst(), "AND")){
                        errorConditionsBuffer.removeFirst();
                        triggerCondition.errorConditionsBuffer.removeFirst();
                        return result && evaluateErrorConditions(result);
                    }
                    else if (Objects.equals(errorConditionsBuffer.getFirst(), "OR")){
                        errorConditionsBuffer.removeFirst();
                        triggerCondition.errorConditionsBuffer.removeFirst();
                        return result || evaluateErrorConditions(result);
                    }
                    else {
                        return evaluateErrorConditions(result);
                    }

                case "Body":
                    result = (response.body().toString().contains(triggerCondition.errorConditionsBuffer.getFirst().value));
                    result = evaluateNegation(result);

                    if (Objects.equals(errorConditionsBuffer.getFirst(), "AND")){
                        errorConditionsBuffer.removeFirst();
                        triggerCondition.errorConditionsBuffer.removeFirst();
                        return result && evaluateErrorConditions(result);
                    }
                    else if (Objects.equals(errorConditionsBuffer.getFirst(), "OR")){
                        errorConditionsBuffer.removeFirst();
                        triggerCondition.errorConditionsBuffer.removeFirst();
                        return result || evaluateErrorConditions(result);
                    }
                    else {
                        return evaluateErrorConditions(result);
                    }

                case "Header":
                    for (HttpHeader header : response.headers()) {
                        if (header.value().contains(triggerCondition.errorConditionsBuffer.getFirst().value)) result = true;
                    }

                    result = evaluateNegation(result);

                    if (Objects.equals(errorConditionsBuffer.getFirst(), "AND")){
                        errorConditionsBuffer.removeFirst();
                        triggerCondition.errorConditionsBuffer.removeFirst();
                        return result && evaluateErrorConditions(result);
                    }
                    else if (Objects.equals(errorConditionsBuffer.getFirst(), "OR")){
                        errorConditionsBuffer.removeFirst();
                        triggerCondition.errorConditionsBuffer.removeFirst();
                        return result || evaluateErrorConditions(result);
                    }
                    else {
                        return evaluateErrorConditions(result);
                    }
            }
        }
        return result;
    }
     */

    // don't know what to do with this yet, placing logic directly in method above for now
    private Boolean evaluateLogicalOperator() {
        return true;
    }

    private void initBuffers() {
        errorConditionsBuffer = new ArrayList<>(triggerCondition.errorConditions);
        negationBuffer = new ArrayList<>(triggerCondition.isNegated);
        logicalOperatorsBuffer = new ArrayList<>(triggerCondition.logicalOperator);
        
    }

    private Boolean evaluateNegation(Boolean result, int index) {
        Boolean negation = negationBuffer.get(index);

        if (negation){
            return !result;
        }
        else {
            return result;
        }
    }
}
