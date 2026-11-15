import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class Utils {

    /*
    Markers, not text that exists anywhere in the message. They mean "the very start of the message"
    and "the very end of the message". Model.extractBetween() and Model.replaceBetween() resolve them
    to index 0 and index length() instead of trying to indexOf() them.
     */
    public static final String START_OF_LINE = "[Start of Line]";
    public static final String END_OF_LINE = "[End of Line]";

    public static void resizeColumnsToFitContent(JTable table) {
        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxWidth = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
            }
            TableColumn column = table.getColumnModel().getColumn(col);
            int calculatedWidth = maxWidth + 3;
            int minWidth = column.getMinWidth();
            table.getColumnModel().getColumn(col).setPreferredWidth(Math.max(calculatedWidth, minWidth));
        }
    }

    /*
    Used to get "Start String" for text Replacements and Extractions when clicking "From Selection" button.

    \r and \n are deliberately kept. The start string is only useful as a locator, and Model looks for it
    with a plain indexOf() against the raw message, which still has its \r\n in it. Dropping them here
    used to build a key that was not actually a substring of the message, so it matched the wrong place.
    Selecting the first character of the body in "...Content-Length: 31\r\n\r\n{"token":..." collected
    ": 31", which indexOf() found inside "Content-Length|: 31|", putting the match 4 bytes early, on the
    \r, and silently prefixing every extraction with \r\n\r\n. Keeping them gives ": 31\r\n\r\n", which
    only matches where it should.
     */
    public static String getStartString(int startIndex, String messageString){
        StringBuilder startString = new StringBuilder();
        int count = 8;

        DebugLog.log("Getting start string. Start index: " + startIndex
                     + ", message length: " + messageString.length());

        // If the selected string is at the very beginning of the request body and there are no previous characters,
        // then set to [Start of Line]
        if (startIndex == 0) {
            DebugLog.log("Start index is at start of message. Start string: " + START_OF_LINE);
            return START_OF_LINE;
        }

        // Collect the 8 previous characters from the start index
        while (count > 0){
            if (startIndex - count >= 0) { // Make sure index is not out of bounds
                startString.append(messageString.charAt(startIndex - count));
            }
            count--;
        }

        if (startString.length() < 8)
            DebugLog.log("Reached start of message before 8 characters were collected. Characters collected: " + startString.length());

        DebugLog.log("Start string: " + startString.toString());

        return startString.toString();
    }

    /*
    Returns the index just past the last character of the message a user could have selected.

    A message with a body runs all the way to the end of the string. A message without one still ends
    with the blank line that terminates the headers, so its selectable content stops where that blank
    line begins.

    Works the same for requests and responses, since only the blank line matters, not the start line.

    This is derived from the message instead of being handed in. getStopString() used to take a hasBody
    flag, computed separately at four call sites, and do "length() - 4" arithmetic with it. That assumed
    every body-less message ends with exactly one blank line, which made it wrong in both directions:
      - caret at length() of a body-less message matched neither branch, so the collection loop ran zero
        times and the stop string came back empty. An empty stop string means indexOf("", s) returns s,
        collapsing the extraction to nothing.
      - a message with no blank line at all treated length() - 4 as the end, which is 4 characters into
        the last header, so [End of Line] fired in the middle of a header value.
     */
    public static int getContentEnd(String messageString) {
        int blankLine = messageString.indexOf("\r\n\r\n");

        // No blank line, so there are no trailing header terminators to step back over.
        if (blankLine == -1) {
            return messageString.length();
        }

        // Anything after the blank line is a body, and a body runs to the end of the message.
        if (blankLine + 4 < messageString.length()) {
            return messageString.length();
        }

        return blankLine;
    }

    // Used to get "Stop String" for text Replacements and Extractions when clicking "From Selection" button.
    // \r and \n are kept here for the same reason as in getStartString above.
    public static String getStopString(int caretPosition, String messageString) {
        StringBuilder stopString = new StringBuilder();
        int count = 0;
        int contentEnd = getContentEnd(messageString);

        DebugLog.log("Getting stop string. Caret position: " + caretPosition
                     + ", message length: " + messageString.length()
                     + ", content ends at: " + contentEnd);

        // Greater-than-or-equal rather than equal, so a caret sitting past the content, anywhere inside
        // the blank line that terminates the headers, still counts as the end of the message.
        if (caretPosition >= contentEnd) {
            DebugLog.log("Caret position is at end of message. Stop string: " + END_OF_LINE);
            return END_OF_LINE;
        }

        // Collect the 8 characters from the caret position onward
        while (count < 8 && caretPosition + count < messageString.length()) {
            stopString.append(messageString.charAt(caretPosition + count));
            count++;
        }

        if (count < 8)
            DebugLog.log("Reached end of message before 8 characters were collected. Characters collected: " + count);

        DebugLog.log("Stop string: " + stopString.toString());

        return stopString.toString();
    }

}
