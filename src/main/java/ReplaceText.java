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
        // extractText is null when a replacement was added without picking anything from the
        // "Pick from Extraction" dropdown, so it cannot be dereferenced unguarded here. This is only
        // ever called to build a debug line, and it used to throw a NullPointerException from inside
        // the logging itself.
        return "ReplaceText{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", startString='" + startString + '\'' +
                ", stopString='" + stopString + '\'' +
                ", selectedText='" + selectedText + '\'' +
                ", extractText='" + (extractText == null ? "null" : extractText.debugString()) + '\'' +
                '}';
    }
}
