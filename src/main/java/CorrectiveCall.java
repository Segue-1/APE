import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.util.ArrayList;
import java.util.List;

import static burp.api.montoya.http.message.HttpRequestResponse.httpRequestResponse;

public class CorrectiveCall {
    public HttpRequestResponse requestResponse;
    public List<ReplaceText> replaceList = new ArrayList<>();
    public List<ExtractText> extractList = new ArrayList<>();

    CorrectiveCall(HttpRequestResponse requestResponse) {
        this.requestResponse = requestResponse;
    }


    public void sendCorrectiveCall() {
        if (!extractList.isEmpty()){

        }
        else if (!replaceList.isEmpty()){

        }

        Controller.montoyaApi.http().sendRequest(requestResponse.request());
    }

    HttpRequestResponse getRequestResponse() {
        return requestResponse;
    }

    public void setRequestResponse(HttpRequest request, HttpResponse response) {
        this.requestResponse = httpRequestResponse(request, response);
    }

}
