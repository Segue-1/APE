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

        if (startIndex == 0)
            return "[Start of Line]";

        while (count > 0){
            if (startIndex - count >= 0) {
                char c = messageString.charAt(startIndex - count);
                if (c != '\r' && c != '\n') {
                    startString.append(c);
                    count--;
                }
            }
            else {
                count--;
            }
        }
        return startString.toString();
    }

    // Used to get "Stop String" for text Replacements and Extractions when clicking "From Selection" button.
    public static String getStopString(int caretPosition, String messageString, Boolean hasBody) {
        StringBuilder stopString = new StringBuilder();
        int index = 0;
        int count = 0;

        if (hasBody && caretPosition == messageString.length())
            return "[End of Line]";
        else if (!hasBody && caretPosition == messageString.length() - 4) // - 2x \r\n
            return "[End of Line]";

        while (count < 8 && caretPosition + index < messageString.length()) {
            char c = messageString.charAt(caretPosition + index);
            if (c != '\r' && c != '\n') {
                stopString.append(c);
                count++;
            }
            index++;
        }
        return stopString.toString();
    }

}
