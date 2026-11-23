import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.Editor;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class Utils {

    // Markers for the start and end of the message. Model resolves them to index 0 and length().
    public static final String START_OF_MESSAGE = "[Start of Message]";
    public static final String END_OF_MESSAGE = "[End of Message]";

    // Choices for the URL encode/decode dropdown. NONE leaves the value untouched.
    public static final String URL_NONE = "None";
    public static final String URL_ENCODE = "URL Encode";
    public static final String URL_DECODE = "URL Decode";

    // Applies an extraction's URL encode/decode choice. Any other mode leaves the value alone.
    public static String applyUrlTransform(String value, String mode) {
        if (value == null || mode == null) {
            return value;
        }

        switch (mode) {
            case URL_ENCODE:
                return urlEncode(value);
            case URL_DECODE:
                return urlDecode(value);
            default:
                return value;
        }
    }

    // Percent-encodes a value. Spaces become %20 rather than URLEncoder's +.
    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    // Reverses urlEncode().
    public static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

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
    // \r and \n are kept so the string still matches the raw message.
    public static String getStartString(int startIndex, String messageString){
        StringBuilder startString = new StringBuilder();
        int count = 8;

        DebugLog.log("Getting start string. Start index: " + startIndex
                     + ", message length: " + messageString.length());

        // Nothing before the selection, so there are no characters to collect.
        if (startIndex <= 0) {
            DebugLog.log("Start index is at start of message. Start string: " + START_OF_MESSAGE);
            return START_OF_MESSAGE;
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

    public static String getStopString(int caretPosition, String messageString) {
        StringBuilder stopString = new StringBuilder();
        int count = 0;
        DebugLog.log("Getting stop string. Caret position: " + caretPosition
                     + ", message length: " + messageString.length());

        // Nothing after the selection, so there are no characters to collect.
        if (caretPosition >= messageString.length()) {
            DebugLog.log("Caret position is at end of message. Stop string: " + END_OF_MESSAGE);
            return END_OF_MESSAGE;
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


    /*
    ------------------------------------------------------------------ From Selection

    The selected text, start string and stop string derived from an editor selection.
     */
    public static class SelectionStrings {
        public final String selectedText;
        public final String startString;
        public final String stopString;

        SelectionStrings(String selectedText, String startString, String stopString) {
            this.selectedText = selectedText;
            this.startString = startString;
            this.stopString = stopString;
        }
    }

    // caretPosition is the end of the selection. A negative start index means the start of the message.
    public static SelectionStrings selectionStrings(String messageString, int caretPosition,
                                                    String selectedContents) {
        String stopString = getStopString(caretPosition, messageString);

        int startIndex = caretPosition - selectedContents.length();
        String startString = getStartString(startIndex, messageString);

        return new SelectionStrings(selectedContents, startString, stopString);
    }

    /*
    JTextField turns line breaks into spaces, which corrupts any start or stop string that spans a
    line. They are shown as a visible \r and \n instead, and turned back with unescapeLineBreaks().
     */
    public static String escapeLineBreaks(String text) {
        return text.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
    }

    public static String unescapeLineBreaks(String text) {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == 'r') { out.append('\r'); i++; continue; }
                if (next == 'n') { out.append('\n'); i++; continue; }
                if (next == '\\') { out.append('\\'); i++; continue; }
            }

            out.append(c);
        }

        return out.toString();
    }

    /*
    From Selection for an extraction, which can come from either editor. Uses the editor whose caret is
    past 0 and sets the Response / Request radio buttons to match. Clicking in one editor resets the
    other's caret to 0, so this is the editor the user touched last. Returns false if neither applies.
     */
    public static boolean applyFromEitherSelection(HttpRequestEditor requestEditor,
                                                   HttpResponseEditor responseEditor,
                                                   JTextField[] inputFields, JRadioButton[] radioButtons,
                                                   String context) {
        int requestEditorPosition = requestEditor.caretPosition();
        int responseEditorPosition = responseEditor.caretPosition();
        DebugLog.log(context + ": caret position of request editor: " + requestEditorPosition
                + ", response editor: " + responseEditorPosition);

        if (requestEditorPosition > 0) {
            if (!applyFromSelection(requestEditor, requestEditor.getRequest().toString(), inputFields,
                    context + " (request)")) {
                DebugLog.log("[Error] " + context + ": 'From Selection' clicked with nothing selected in "
                        + "the request editor.");
                return false;
            }
            radioButtons[1].setSelected(true);
            return true;
        }

        if (responseEditorPosition > 0) {
            if (!applyFromSelection(responseEditor, responseEditor.getResponse().toString(), inputFields,
                    context + " (response)")) {
                DebugLog.log("[Error] " + context + ": 'From Selection' clicked with nothing selected in "
                        + "the response editor.");
                return false;
            }
            radioButtons[0].setSelected(true);
            return true;
        }

        DebugLog.log("[Error] " + context + ": 'From Selection' clicked while no editor selected.");
        return false;
    }

    // Fills the Start / Stop / Selected input fields from an editor's selection. Returns false if
    // nothing is selected.
    public static boolean applyFromSelection(Editor editor, String messageString,
                                             JTextField[] inputFields, String context) {
        Optional<Selection> selection = editor.selection();

        if (selection.isEmpty()) {
            DebugLog.log(context + ": no selection in this editor.");
            return false;
        }

        int caretPosition = editor.caretPosition();
        String selectedContents = selection.get().contents().toString();
        SelectionStrings strings = selectionStrings(messageString, caretPosition, selectedContents);

        inputFields[3].setText(escapeLineBreaks(strings.selectedText));
        inputFields[2].setText(escapeLineBreaks(strings.stopString));
        inputFields[1].setText(escapeLineBreaks(strings.startString));

        if (strings.selectedText.startsWith(" ")) {
            inputFields[3].setToolTipText("⚠️ Selection includes whitespace, was this intentional?");
            DebugLog.log(context + ": Selection starts with whitespace. Warning tool tip set.");
        }
        else {
            inputFields[3].setToolTipText(null);
        }

        DebugLog.log(context + ": 'From Selection' set the input fields."
                + " Message length: " + messageString.length()
                + ", caret: " + caretPosition
                + ", selection length: " + selectedContents.length());
        DebugLog.log(context + ": Selected Text: " + strings.selectedText);
        DebugLog.log(context + ": Stop String: " + strings.stopString);
        DebugLog.log(context + ": Start String: " + strings.startString);

        return true;
    }

    /*
    ------------------------------------------------------------------ dropdowns

    Refills a dropdown, then restores the previous selection if it's still there.
     */
    public static <T> void repopulatePreservingSelection(JComboBox<T> comboBox, List<T> items) {
        Object previous = comboBox.getSelectedItem();

        comboBox.removeAllItems();

        for (T item : items) {
            comboBox.addItem(item);
        }

        if (previous != null) {
            comboBox.setSelectedItem(previous);
        }
    }

    /*
    ------------------------------------------------------------------ editor caret pairing

    Clicking in one editor clears the other's selection, so only one of the pair holds a selection.
     */
    public static void pairEditorCarets(Editor first, Editor second, String context) {
        attachCaretResetListener(first.uiComponent(), second, context + " (first)");
        attachCaretResetListener(second.uiComponent(), first, context + " (second)");
    }

    // Attaches the caret reset listener to the editor's text component. Montoya doesn't expose it,
    // so the component tree is searched.
    private static void attachCaretResetListener(Component root, Editor otherEditor, String label) {
        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                otherEditor.setCaretPosition(0);
                DebugLog.log("Mouse pressed on " + label + " editor. Reset the other editor's caret.");
            }
        };

        try {
            boolean found = false;
            Deque<Component> stack = new ArrayDeque<>();
            stack.push(root);

            while (!stack.isEmpty()) {
                Component c = stack.pop();

                if (c instanceof JTextComponent && c.getParent() instanceof JViewport) {
                    c.addMouseListener(listener);
                    found = true;
                }
                if (c instanceof Container) {
                    for (Component child : ((Container) c).getComponents()) stack.push(child);
                }
            }

            if (!found) {
                DebugLog.log("Utils: Could not attach caret reset listener to the " + label
                        + " editor's text component.");
            }
        }
        catch (Exception ex) {
            DebugLog.log("Utils: Error attaching caret reset listener to the " + label
                    + " editor: " + ex);
        }
    }

    /*
    ------------------------------------------------------------------ table context menu

    Right-click Delete menu for a table whose model implements DeletableTableModel. afterDelete may be null.
     */
    public static void setTableDeleteContextMenu(JTable table, Runnable afterDelete) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                return;
            }

            DeletableTableModel model = (DeletableTableModel) table.getModel();
            DebugLog.log(table.getName() + ": User deleted row " + selectedRow);
            model.removeRow(selectedRow);
            resizeColumnsToFitContent(table);

            if (afterDelete != null) {
                afterDelete.run();
            }
        });

        popupMenu.add(deleteItem);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    DebugLog.log(table.getName() + ": User right clicked row " + row
                            + " and displayed context menu.");
                }
            }
        });
    }
}
