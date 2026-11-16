import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.Editor;

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

    /*
    Markers, not text that exists anywhere in the message. They mean "the very start of the message"
    and "the very end of the message". Model.extractBetween() and Model.replaceBetween() resolve them
    to index 0 and index length() instead of trying to indexOf() them.
     */
    public static final String START_OF_LINE = "[Start of Line]";
    public static final String END_OF_LINE = "[End of Line]";

    // The three choices offered by the "URL encode/decode" dropdown on the Extraction panel. NONE is the
    // default and leaves the extracted value untouched.
    public static final String URL_NONE = "None";
    public static final String URL_ENCODE = "URL Encode";
    public static final String URL_DECODE = "URL Decode";

    /*
    Applies the extraction's "URL encode/decode" choice to the value it pulled out, before that value is
    stored for a later replacement to insert. Anything other than URL_ENCODE / URL_DECODE (including a
    null mode on an extraction configured before this field was populated) leaves the value alone.
     */
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

    /*
    Percent-encodes a value for use inside a URL. URLEncoder targets application/x-www-form-urlencoded,
    which encodes a space as "+"; a literal "+" in the input is already turned into "%2B" first, so
    replacing the remaining "+" with "%20" afterwards gives real percent-encoding without corrupting an
    input that genuinely contained a plus.
     */
    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    // Reverses URL percent-encoding. URLDecoder turns both "%20" and "+" back into a space, so it round
    // trips whatever urlEncode() produced.
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


    /*
    ------------------------------------------------------------------ From Selection

    The three strings the "From Selection" button derives from a selection in an HTTP editor.

    Kept as a plain value object computed by a pure function so the derivation can be tested without a
    running Burp or any Swing component. The Extraction, Replacement and Followup panels all used to
    carry their own copy of this arithmetic.
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

    /*
    Derives the selected text, start string and stop string for a selection.

    caretPosition is where the caret sits after the selection, so the selection starts that many
    characters back. A selection running to the very start of the message therefore yields a negative
    start index, which getStartString() treats as the start of the message.
     */
    public static SelectionStrings selectionStrings(String messageString, int caretPosition,
                                                    String selectedContents) {
        String stopString = getStopString(caretPosition, messageString);

        int startIndex = caretPosition - selectedContents.length();
        String startString = getStartString(startIndex, messageString);

        return new SelectionStrings(selectedContents, startString, stopString);
    }

    /*
    Reads the current selection out of an HTTP editor and fills in the Name / Start / Stop / Selected
    input fields, returning false when there is nothing selected.

    Both HttpRequestEditor and HttpResponseEditor extend Editor, so one implementation serves the
    request and response sides of Extraction as well as Replacement and Followup. inputFields is the
    panel's field array, where index 1 is the start string, 2 the stop string and 3 the selected text.

    selection() is Optional.empty() when the user has not highlighted anything, which used to reach an
    unguarded .get() and throw NoSuchElementException out of the button listener.
     */
    public static boolean applyFromSelection(Editor editor, String messageString,
                                             JTextField[] inputFields, String context) {
        Optional<Selection> selection = editor.selection();

        if (selection.isEmpty()) {
            DebugLog.log("[Error] " + context + ": 'From Selection' clicked with nothing selected. "
                    + "Select the text first.");
            return false;
        }

        int caretPosition = editor.caretPosition();
        String selectedContents = selection.get().contents().toString();
        SelectionStrings strings = selectionStrings(messageString, caretPosition, selectedContents);

        inputFields[3].setText(strings.selectedText);
        inputFields[2].setText(strings.stopString);
        inputFields[1].setText(strings.startString);

        if (strings.selectedText.startsWith(" ")) {
            inputFields[3].setToolTipText("⚠️ Selection includes whitespace, was this intentional?");
            DebugLog.log(context + ": Selection starts with whitespace. Warning tool tip set.");
        }
        else {
            inputFields[3].setToolTipText(null);
        }

        DebugLog.log(context + ": 'From Selection' set the input fields."
                + " Message length: " + messageString.length()
                + ", content ends at: " + getContentEnd(messageString)
                + ", caret: " + caretPosition
                + ", selection length: " + selectedContents.length());
        DebugLog.log(context + ": Selected Text: " + strings.selectedText);
        DebugLog.log(context + ": Stop String: " + strings.stopString);
        DebugLog.log(context + ": Start String: " + strings.startString);

        return true;
    }

    /*
    ------------------------------------------------------------------ dropdowns

    Refills a dropdown and puts the previous selection back afterwards.

    The callers used to restore the selection inside the fill loop, which meant every item that was not
    the previous selection reset the selection to index 0, so only a match on the final item survived.
    Restoring once after the loop, by item rather than by index, fixes that and also removes the need to
    track where in the combined list an item ended up.

    setSelectedItem() is a no-op when the item is no longer present, which leaves the first item
    selected as addItem() does by default.
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

    Keeps two HTTP editors from both claiming a selection.

    Montoya editors remember their caret and selection, and nothing clears one when the user clicks
    into the other. So after working in both, BOTH report a selection, and a "From Selection" button
    that checks them in a fixed order always picks the same one regardless of where the user actually
    meant. Clicking inside an editor resets the OTHER editor's caret, which collapses its selection,
    leaving exactly one editor holding a live selection.

    Called once per pair at construction. Safe to call for a pair whose other half is not currently
    displayed: the listener simply never fires for a component nobody can click.
     */
    public static void pairEditorCarets(Editor first, Editor second, String context) {
        attachCaretResetListener(first.uiComponent(), second, context + " (first)");
        attachCaretResetListener(second.uiComponent(), first, context + " (second)");
    }

    /*
    Walks an editor's component tree looking for the text component Burp renders the message in, and
    attaches a listener that resets the OTHER editor's caret.

    Hacky by necessity: Montoya exposes an editor only as an opaque Component, so there is no supported
    way to reach its text area. Failure to find one is logged rather than thrown, because a missing
    convenience is not worth refusing to load the extension over.
     */
    public static void attachCaretResetListener(Component root, Editor otherEditor, String label) {
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

    Attaches a right-click "Delete" menu to a table whose model implements DeletableTableModel.

    This was duplicated in CorrectiveCallsController, FollowUpController and TriggerConditionController,
    and the copies had drifted: some resized the columns afterwards, some pushed state back to the
    model, some did neither. afterDelete carries whatever the individual table needs to do on top of
    the removal, and may be null.
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
