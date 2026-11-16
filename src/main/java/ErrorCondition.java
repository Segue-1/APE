public class ErrorCondition {
    public int index;
    public String name;
    public String category;
    public String value;
    // Only used by the "Header" category: the name of the response header to look for. The value field
    // then holds an optional substring the header's value must contain. Null for Status Code / Body.
    public String headerName;

    /*
    Whether to match the REQUEST rather than the response. Defaults to false, the response, which is
    what every condition did before this existed.

    Body and Header conditions can meaningfully apply to either half of the exchange: the Error
    Detection panel shows both editors, so a header a user picked out of the request -- "Connection",
    say -- is a natural thing to want to match, and until now it silently never fired because only the
    response was ever examined. Status Code has no request equivalent and ignores this.
     */
    public boolean matchInRequest;

    public String description;

    ErrorCondition(String category, String input) {
        this(category, input, null);
    }

    ErrorCondition(String category, String input, String headerName) {
        this(category, input, headerName, false);
    }

    ErrorCondition(String category, String input, String headerName, boolean matchInRequest) {
        this.category = category;
        this.headerName = headerName;
        this.value = input;
        this.matchInRequest = matchInRequest;
        setDescription(input);
    }

    // "request" or "response", for the description and the table.
    public String source() {
        return matchInRequest ? "request" : "response";
    }

    public void setName() {
        name = "Error Condition " + index;
    }

    public void setDescription(String input) {
        switch (category) {
            case "Status Code" -> description = "APE will trigger if Status Code is " + input;
            case "Body" -> description = "APE will trigger if text '" + input + "' is found in the "
                    + source() + " Body";
            case "Header" -> {
                if (input == null || input.isEmpty()) {
                    description = "APE will trigger if " + source() + " header '" + headerName
                            + "' is present";
                }
                else {
                    description = "APE will trigger if " + source() + " header '" + headerName
                            + "' is present and contains '" + input + "'";
                }
            }
            default -> description = "How did you do that?";
        }
    }


    @Override
    public String toString() {
        return name;
    }

    public String debugString() {
        return "ErrorCondition{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", headerName='" + headerName + '\'' +
                ", matchInRequest=" + matchInRequest +
                ", value='" + value + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
