public class ErrorCondition {
    public int index;
    public String name;
    public String category;
    public String value;
    public String description;

    ErrorCondition(String category, String input) {
        this.category = category;
        setDescription(input);
        this.value = input;
    }

    public void setName() {
        name = "Error Condition " + index;
    }

    public void setDescription(String input) {
        switch (category) {
            case "Status Code" -> description = "APE will trigger if Status Code is " + input;
            case "Body" -> description = "APE will trigger if text '" + input + "' is found in response Body";
            case "Header" -> description = "APE will trigger if text '" + input + "' is found in response Header";
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
                ", value='" + value + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
