public class ErrorCondition {
    public int index;
    public String name;
    public String category;
    public String value;
    // Header category only: the header name to match. value is then an optional substring of its value.
    public String headerName;

    // Match the request instead of the response. Ignored by Status Code.
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

    private void setDescription(String input) {
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
