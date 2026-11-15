import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public class CorrectiveCallSet {
    public int index;
    public String name;
    public String description;
    public List<CorrectiveCall> correctiveCallList = new ArrayList<>();
    //public List<HttpRequestResponse> callSequence = new ArrayList<>();

    CorrectiveCallSet() {
        description = "";
    }

    public void setName () {
        name = "Set " + index;
    }

    @Override
    public String toString() {
        return name;
    }

    public String debugString() {
        return "CorrectiveCallsSet{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", callSequence=" + printCorrectiveCalls() +
                '}';
    }

    private String printCorrectiveCalls() {
        List<String> strings = new ArrayList<>();
        int count = 1;

        for (CorrectiveCall call : correctiveCallList) {
            HttpRequest req = call.requestResponse.request();
            strings.add("Call number: " + count++);
            strings.add(req.httpService().host());
            strings.add(req.url());
            strings.add(req.method());
            strings.add(req.path());
        }
        return strings.toString();
    }
}
