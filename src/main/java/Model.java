import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.EnumSet;
import java.util.List;


public class Model implements HttpHandler{

    public EnumSet<ToolType> enabledTools;
    public Boolean triggerConditionIsSet;
    public List<TriggerCondition> triggerConditions;

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
        HttpRequest request = responseReceived.initiatingRequest();

        // This determines which tool (Repeater, Intruder, etc.) a request came from.
        // TODO make sure this check makes sense on the response.
        ToolType toolType = responseReceived.toolSource().toolType();

        DebugLog.log("Model toolType.toolName(): " + toolType.toString());

        /*
        TODO: Make this work with extensions in an elegant way. Extensions currently disabled.
         Since APE itself is an extension, it was triggering the processRequest function multiple
         times. Need a way to make sure processRequest is not called for requests made by APE.
         Currently no way to identify which extension sends a request.
         */
        if (!enabledTools.contains(toolType) || !triggerConditionIsSet || toolType.toolName().equals("Extension")) {
            DebugLog.log("Model: Not processing request!");
            return ResponseReceivedAction.continueWith(responseReceived);
        }

        DebugLog.log("Model: Processing request...");
        processRequest(request);

        return ResponseReceivedAction.continueWith(responseReceived);
    }

    // Allows controller to update model's enabledTools
    public void updateEnabledTools(EnumSet<ToolType> enabledTools) {
        this.enabledTools = EnumSet.copyOf(enabledTools);
    }


    private void processRequest(HttpRequest request) {

        HttpRequestResponse requestResponse = Controller.montoyaApi.http().sendRequest(request);
        HttpResponse response = requestResponse.response();

        DebugLog.log("Model: Test Request \n" + requestResponse.request().toString());
        DebugLog.log("Model: Test Response \n" + response.toString());

        // Need to process trigger conditions in order
        CorrectiveCallSet correctiveCallSet = detectTriggerCondition(response);

    }

    // Detects error conditions and returns the corrective call set to be called.
    private CorrectiveCallSet detectTriggerCondition(HttpResponse response) {
        CorrectiveCallSet correctiveCallSet = new CorrectiveCallSet();

        for (TriggerCondition triggerCondition : triggerConditions){

            DebugLog.log("Fucking off urgently");
        }

        return correctiveCallSet;
    }
}
