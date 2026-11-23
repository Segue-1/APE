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


    HttpRequestResponse getRequestResponse() {
        return requestResponse;
    }

}
