import burp.api.montoya.http.message.HttpRequestResponse;

import java.util.ArrayList;
import java.util.List;

public class CorrectiveCallsSet {
    public int index;
    public String name;
    public String description;
    public List<HttpRequestResponse> correctiveCalls = new ArrayList<>();

    CorrectiveCallsSet() {
        description = "";
    }

    public void setName () {
        name = "Set " + index;
    }
}
