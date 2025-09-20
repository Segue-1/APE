import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;


public class Model implements BurpExtension, HttpHandler{
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("APE");
        montoyaApi.logging().logToOutput("Welcome to the API Processing Engine (APE)");
        montoyaApi.http().registerHttpHandler(this);
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        HttpRequest request = requestToBeSent.withAddedHeader("lol", "lmao");
        return RequestToBeSentAction.continueWith(request);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return null;
    }
}