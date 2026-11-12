import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class Utils {
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

    // Used to get "Start String" for text Replacements and Extractions when clicking "From Selection" button.
    public static String getStartString(int startIndex, String messageString){
        StringBuilder startString = new StringBuilder();
        int count = 8;

        DebugLog.log("Getting start string. Start index: " + startIndex
                     + ", message length: " + messageString.length());

        // If the selected string is at the very beginning of the request body and there are no previous characters,
        // then set to [Start of Line]
        if (startIndex == 0) {
            DebugLog.log("Start index is at start of message. Start string: [Start of Line]");
            return "[Start of Line]";
        }

        // Collect the 8 previous characters from the start index
        while (count > 0){
            if (startIndex - count >= 0) { // Make sure index is not out of bounds
                char c = messageString.charAt(startIndex - count);
                if (c != '\r' && c != '\n') {  // Append non \r and \n chars
                    startString.append(c);
                }
            }
            count--;
        }

        if (startString.length() < 8)
            DebugLog.log("Reached start of message before 8 characters were collected. Characters collected: " + startString.length());

        DebugLog.log("Start string: " + startString.toString());

        return startString.toString();
    }

    // Used to get "Stop String" for text Replacements and Extractions when clicking "From Selection" button.
    public static String getStopString(int caretPosition, String messageString, Boolean hasBody) {
        StringBuilder stopString = new StringBuilder();
        int index = 0;
        int count = 0;

        DebugLog.log("Getting stop string. Caret position: " + caretPosition
                     + ", message length: " + messageString.length()
                     + ", has body: " + hasBody);

        if (hasBody && caretPosition == messageString.length()) {
            DebugLog.log("Caret position is at end of message with body. Stop string: [End of Line]");
            return "[End of Line]";
        }
        else if (!hasBody && caretPosition == messageString.length() - 4) { // - 2x \r\n
            DebugLog.log("Caret position is at end of message without body. Stop string: [End of Line]");
            return "[End of Line]";
        }

        while (count < 8 && caretPosition + index < messageString.length()) {
            char c = messageString.charAt(caretPosition + index);
            if (c != '\r' && c != '\n') {
                stopString.append(c);
                count++;
            }
            index++;
        }

        if (count < 8)
            DebugLog.log("Reached end of message before 8 characters were collected. Characters collected: " + count);

        DebugLog.log("Stop string: " + stopString.toString());

        return stopString.toString();
    }

}
