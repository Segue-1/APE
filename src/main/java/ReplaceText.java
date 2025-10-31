public class ReplaceText {
    public int index;
    public String name;
    public String id;
    private String startString;
    private String stopString;
    private String selectedText;
    //private Extraction extraction;

    public void setId () {
        name = "Extraction-" + index;
    }

    @Override
    public String toString() {
        return "ReplaceText{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", startString='" + startString + '\'' +
                ", stopString='" + stopString + '\'' +
                ", selectedText='" + selectedText + '\'' +
                '}';
    }
}
