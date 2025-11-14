import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public class CorrectiveCallsSet {
    public int index;
    public String name;
    public String description;
    public List<HttpRequestResponse> callSequence = new ArrayList<>();

    CorrectiveCallsSet() {
        description = "";
    }

    public void setName () {
        name = "Set " + index;
    }

    @Override
    public String toString() {
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

        for (HttpRequestResponse call : callSequence) {
            HttpRequest req = call.request();
            strings.add("Call number: " + count++);
            strings.add(req.httpService().host());
            strings.add(req.url());
            strings.add(req.method());
            strings.add(req.path());
        }
        return strings.toString();
    }
}
