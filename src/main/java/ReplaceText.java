public class ReplaceText {
    public int index;
    public String name;
    public String id;
    public String startString;
    public String stopString;
    public String selectedText;
    public ExtractText extractText;

    public void setId () {
        name = "Replacement-" + index;
    }

    @Override
    public String toString() {
        return "ReplaceText{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", startString='" + startString + '\'' +
                ", stopString='" + stopString + '\'' +
                ", selectedText='" + selectedText + '\'' +
                ", extractText='" + extractText.toString() + '\'' +
                '}';
    }
}
