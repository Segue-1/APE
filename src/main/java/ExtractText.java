public class ExtractText {
    public int index;
    public String id;
    public String name;
    public String startString;
    public String stopString;
    public String selectedText;
    public String urlEncodeDecode;


    public void setId () {
        id = "Extraction-" + index;
    }

    @Override
    public String toString() {
        return "ExtractText{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", startString='" + startString + '\'' +
                ", stopString='" + stopString + '\'' +
                ", selectedText='" + selectedText + '\'' +
                ", urlEncodeDecode='" + urlEncodeDecode + '\'' +
                '}';
    }
}
