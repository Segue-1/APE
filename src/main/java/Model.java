import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.EnumSet;


public class Model implements HttpHandler{

    public EnumSet<ToolType> enabledTools;
    public Boolean triggerConditionIsSet;

    public Model () {
        // TODO Maybe just move this to controller
        Controller.montoyaApi.extension().setName("APE");
        Controller.montoyaApi.http().registerHttpHandler(this);
    }

    // HTTP Handler which will intercept all traffic generated in burp
    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {

        // This determines which tool (Repeater, Intruder, etc.) a request came from.
        ToolType toolType = requestToBeSent.toolSource().toolType();

        if (!enabledTools.contains(toolType) || !triggerConditionIsSet) {
            return null;
        }

        //HttpRequest request = processRequest(requestToBeSent);
        HttpRequest request = requestToBeSent.withAddedHeader("lol", "lmao");

        return RequestToBeSentAction.continueWith(request);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return null;
    }

    // Allows controller to update model's enabledTools
    public void updateEnabledTools(EnumSet<ToolType> enabledTools) {
        this.enabledTools = EnumSet.copyOf(enabledTools);
    }

    private HttpRequest processRequest(HttpRequest httpRequest) {
        HttpRequest followupRequest = null;



        return followupRequest;
    }
}