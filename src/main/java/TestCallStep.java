import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

/*
One row of the Test Calls table, with the request/response behind it. Built by Model.runTestCall().
*/
public class TestCallStep {

    // Values for type, in the order they occur in a run.
    public static final String ERRORED = "Errored Call";
    public static final String CORRECTIVE = "Corrective Call";
    public static final String FOLLOWUP = "Followup Call";

    // Values for outcome.
    public static final String TRIGGERED = "Triggered";
    public static final String NO_MATCH = "No match";
    public static final String SENT = "Sent";
    public static final String NO_RESPONSE = "No response";
    public static final String NOT_RUN = "Not run";
    public static final String ERROR = "Error";

    public int step;
    public String type;
    public String name;
    public HttpRequestResponse requestResponse;
    public String outcome;
    public String detail = "";

    public int extractionsRun;
    public int extractionsHit;
    public int replacementsTotal;
    public int replacementsApplied;

    TestCallStep(int step, String type, String name, HttpRequestResponse requestResponse, String outcome) {
        this.step = step;
        this.type = type;
        this.name = name;
        this.requestResponse = requestResponse;
        this.outcome = outcome;
    }

    public String method() {
        HttpRequest request = request();
        return request == null ? "" : request.method();
    }

    public String path() {
        HttpRequest request = request();
        return request == null ? "" : request.path();
    }

    public HttpRequest request() {
        return requestResponse == null ? null : requestResponse.request();
    }

    // The status code of whatever came back, or an empty string when nothing did.
    public String status() {
        if (requestResponse == null || requestResponse.response() == null) {
            return "";
        }
        return String.valueOf(requestResponse.response().statusCode());
    }

    // "2/3" meaning two of three extractions found something. Empty when the step had none.
    public String extractions() {
        return extractionsRun == 0 ? "" : extractionsHit + "/" + extractionsRun;
    }

    // "1/2" meaning one of two replacements actually applied. Empty when the step had none.
    public String replacements() {
        return replacementsTotal == 0 ? "" : replacementsApplied + "/" + replacementsTotal;
    }

    public String debugString() {
        return "TestCallStep{" +
                "step=" + step +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", outcome='" + outcome + '\'' +
                ", status='" + status() + '\'' +
                ", extractions='" + extractions() + '\'' +
                ", replacements='" + replacements() + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }

    @Override
    public String toString() {
        return step + ". " + type + (name == null || name.isEmpty() ? "" : " (" + name + ")");
    }
}
