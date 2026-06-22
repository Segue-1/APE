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

    CorrectiveCall(HttpRequestResponse requestResponse, List<ReplaceText> replaceList) {
        this.requestResponse = requestResponse;
        this.replaceList = replaceList;
    }

    CorrectiveCall(HttpRequestResponse requestResponse, List<ReplaceText> replaceList,  List<ExtractText> extractList) {
        this.requestResponse = requestResponse;
        this.replaceList = replaceList;
        this.extractList = extractList;
    }

    HttpRequestResponse getRequestResponse() {
        return requestResponse;
    }

}
