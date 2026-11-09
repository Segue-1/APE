import burp.api.montoya.http.message.HttpRequestResponse;

import java.util.ArrayList;
import java.util.List;

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

}
