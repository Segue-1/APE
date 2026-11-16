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

    // Every extraction configured on this set's corrective calls, in call order.
    public List<ExtractText> allExtractions() {
        List<ExtractText> extractions = new ArrayList<>();

        for (CorrectiveCall correctiveCall : correctiveCallList) {
            extractions.addAll(correctiveCall.extractList);
        }

        return extractions;
    }

    /*
    Every extraction on the selected set, which is what the followup call may reference.

    The followup always runs last, after every call in the set has returned, so it may use the
    extractions of ANY call in that set, including the final one. It is scoped to the selected set
    because only one set runs per intercepted request, so another set's extractions would never have a
    value to insert.

    The followup has no extractions of its own, only replacements; there is no UI to add any and
    Model.sendFollowupCall() does not process them.

    Contrast with a corrective call's own replacements, which may only reference extractions from
    EARLIER calls in the same set: replacements are applied to a call before it is sent and extractions
    happen after it returns, so a later call's extraction cannot exist yet.

    selectedRow of -1 means nothing is selected, which is treated as Set 0, the same default
    CorrectiveCallSetTableModel.addRequestResponse() applies.
     */
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
