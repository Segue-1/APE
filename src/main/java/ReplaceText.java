public class ReplaceText {
    public int index;
    public String name;
    public String id;
    public String startString;
    public String stopString;
    public String selectedText;
    public ExtractText extractText;

    public void setId () {
        id = "Replacement-" + index;
    }

    @Override
    public String toString() {
        return "ReplaceText{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", startString='" + startString + '\'' +
                ", stopString='" + stopString + '\'' +
                ", selectedText='" + selectedText + '\'' +
                ", extractText='" + extractText.debugString() + '\'' +
                '}';
    }
}
