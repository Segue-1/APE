public class ReplaceText {
    public int index;
    public String name;
    public String id;
    public String startString;
    public String stopString;
    public String selectedText;
    public ExtractText extractText;

    /*
    Which occurrence of startString to act on, counting from 1.

    replaceBetween() locates the start string with indexOf(), so without this it always acted on the
    FIRST occurrence. An 8 character key taken from "From Selection" can easily also appear earlier in
    the message, typically in a header, and the replacement then silently rewrote the wrong place.

    Defaults to 1, which is the old behaviour.
     */
    public int occurrence = 1;

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
                ", occurrence=" + occurrence +
                ", extractText='" + (extractText == null ? "null" : extractText.debugString()) + '\'' +
                '}';
    }
}
