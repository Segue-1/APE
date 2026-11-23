public class ExtractText {
    public int index;
    public String id;
    public String name;
    public String startString;
    public String stopString;
    public String selectedText;
    public String urlEncodeDecode;
    public boolean extractFromResponse;
    public boolean extractFromRequest;


    public void setId () {
        id = "Extraction-" + index;
    }

    // This needs to just return name so that the replace text drop down can populate
    @Override
    public String toString() {
        return name;
    }

    public String debugString() {
        return "ExtractText{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", startString='" + startString + '\'' +
                ", stopString='" + stopString + '\'' +
                ", selectedText='" + selectedText + '\'' +
                ", urlEncodeDecode='" + urlEncodeDecode + '\'' +
                ", extractFromResponse=" + extractFromResponse +
                ", extractFromRequest=" + extractFromRequest +
                '}';
    }
}
