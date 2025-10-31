public class ExtractText {
    private int index;
    public String name;
    public String startString;
    public String stopString;
    public String selectedText;
    public String urlEncodeDecode;

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
