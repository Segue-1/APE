import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public class CorrectiveCallSet {
    public int index;
    public String name;
    public String description;
    public List<CorrectiveCall> correctiveCallList = new ArrayList<>();

    // This set's final call, sent after its corrective calls. Starts as a copy of the errored call.
    public CorrectiveCall followupCall;
    //public List<HttpRequestResponse> callSequence = new ArrayList<>();

    CorrectiveCallSet() {
        description = "";
    }

    public void setName () {
        name = "Set " + index;
    }

    // Every extraction configured on this set's corrective calls, in call order.
    public List<ExtractText> allExtractions() {
        List<ExtractText> extractions = new ArrayList<>();

        for (CorrectiveCall correctiveCall : correctiveCallList) {
            extractions.addAll(correctiveCall.extractList);
        }

        return extractions;
    }

    // Every extraction in the selected set. The followup runs last, so it can use any of them.
    // A selectedRow of -1 is treated as Set 0.
    public static List<ExtractText> extractionsForSelectedSet(List<CorrectiveCallSet> sets, int selectedRow) {
        if (sets == null || sets.isEmpty()) {
            return new ArrayList<>();
        }

        int row = selectedRow == -1 ? 0 : selectedRow;

        if (row < 0 || row >= sets.size()) {
            return new ArrayList<>();
        }

        return sets.get(row).allExtractions();
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
