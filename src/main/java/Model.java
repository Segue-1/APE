import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;


public class Model implements HttpHandler{
    private MontoyaApi montoyaApi;


    public Model (MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;

        montoyaApi.extension().setName("APE");
        montoyaApi.logging().logToOutput("Welcome to the API Processing Engine (APE)");

        montoyaApi.http().registerHttpHandler(this);
    }

    // HTTP Handler, this is an example that will globally modify all requests being sent with stupid headers.
    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {

        // An object that can identify what burp tool was used for the request.
        ToolType toolType = requestToBeSent.toolSource().toolType();

        HttpRequest request = requestToBeSent.withAddedHeader("lol", "lmao");
        return RequestToBeSentAction.continueWith(request);
    }

    // Method comes from HttpHandler interface, does nothing.
    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return null;
    }
}