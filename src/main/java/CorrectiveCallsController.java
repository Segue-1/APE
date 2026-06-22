import burp.api.montoya.http.message.HttpRequestResponse;

import burp.api.montoya.repeater.HttpEditor;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;


public class CorrectiveCallsController {
    private CorrectiveCallSetTableModel correctiveCallSetTableModel;
    private JTable correctiveCallsSetsTable;

    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;

    private JRadioButton singleSetRadioButton;
    private JRadioButton multiSetRadioButton;

    private JButton addSetBotton;
    private JButton removeSetBotton;

    public JButton extractAddButton;
    public JButton replaceAddButton;

    public JButton extractFromSelect;
    public JButton replaceFromSelect;

    public JTextField[] extractInputFields;
    public JTextField[] replaceInputFields;

    public ExtractionTableModel extractionTableModel;
    public JTable extractionTable;

    public ReplacementTableModel replacementTableModel;
    public JTable replacementTable;

    public JComboBox<String> extractDropDown;
    public JComboBox<ExtractText> replaceDropDown;

    public HttpEditor httpEditor;

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    CorrectiveCallsController(CorrectiveCallsPanel correctiveCallsPanel, Model model) {
        setAttributes(correctiveCallsPanel);

        setRadioButtons();
        setTableDeleteContextMenu(callSequenceTable);
        //
        //setTableDeleteContextMenu(extractionTable);
        //setTableDeleteContextMenu(replacementTable);
        setCallSetsButtonActionListeners();
        setSelectedCorrectiveCallsSetListener();
        setSelectedCorrectiveCallCallListener();
        initCallSequenceTable();
        setExtractAddButtonListener();
        setReplaceDropDownListener();
        setReplaceAddButtonListener();
        // Split these into two. setExtractFromSelectButton and setReplaceFromSelectButton
        setFromSelectionListener(extractFromSelect, extractInputFields, "response");
        setFromSelectionListener(replaceFromSelect, replaceInputFields, "request");
    }

    private void initCallSequenceTable() {
        CorrectiveCallSet set0 = correctiveCallSetTableModel.correctiveCallSetList.getFirst();
        callSequenceTableModel.loadCallSequence(set0.correctiveCallList);
    }

    private void setRadioButtons() {
        singleSet(); // Single set by default
        onRadioButton();
    }

    private void onRadioButton() {
        singleSetRadioButton.addActionListener(e -> singleSet());
        multiSetRadioButton.addActionListener(e -> multiSet());
    }

    private void singleSet() {
        addSetBotton.setEnabled(false);
        removeSetBotton.setEnabled(false);
        correctiveCallsSetsTable.setRowSelectionInterval(0, 0);
        correctiveCallsSetsTable.setEnabled(false);
    }

    private void multiSet() {
        addSetBotton.setEnabled(true);
        removeSetBotton.setEnabled(true);
        correctiveCallsSetsTable.setEnabled(true);
    }

    private void setTableDeleteContextMenu(JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                DeletableTableModel model = (DeletableTableModel) table.getModel();
                DebugLog.log(table.getName() + ": User deleted row " + selectedRow);
                model.removeRow(selectedRow);
            }
        });

        popupMenu.add(deleteItem);

        // Displays popup menu and selects row.
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
                }
            }
        });
    }

    private void setCallSetsButtonActionListeners() {
        ActionListener addButtonListener = e -> {
            correctiveCallSetTableModel.addRow(new CorrectiveCallSet());
            Utils.resizeColumnsToFitContent(correctiveCallsSetsTable);
            DebugLog.log("Add button clicked on corrective call set table. Call set "
                         + correctiveCallSetTableModel.correctiveCallSetList.getLast().name
                         + " has been added");
        };

        ActionListener removeBottonListener = e -> {
            int selectedRow = correctiveCallsSetsTable.getSelectedRow();
            correctiveCallSetTableModel.removeRow(selectedRow);
            callSequenceTableModel.fireTableDataChanged();
            Utils.resizeColumnsToFitContent(correctiveCallsSetsTable);

            correctiveCallsSetsTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);

            DebugLog.log("Remove button clicked on corrective call set table. Call set "
                         + correctiveCallSetTableModel.correctiveCallSetList.get(selectedRow).name
                         + " has been deleted.");
        };
        addSetBotton.addActionListener(addButtonListener);
        removeSetBotton.addActionListener(removeBottonListener);
    }

    private void setSelectedCorrectiveCallsSetListener() {
        correctiveCallsSetsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = correctiveCallsSetsTable.getSelectedRow();

                if (selectedRow != -1) {
                    callSequenceTableModel.loadCallSequence(correctiveCallSetTableModel.correctiveCallSetList.get(selectedRow).correctiveCallList);
                    DebugLog.log("Corrective call set selected. Loaded to call sequence table: " +
                                  correctiveCallSetTableModel.correctiveCallSetList.get(selectedRow).toString());
                }
            }
        });
    }

    private void setSelectedCorrectiveCallCallListener() {
        callSequenceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = callSequenceTable.getSelectedRow();

                if (selectedRow != -1) {
                    HttpRequestResponse reqResp = callSequenceTableModel.getReqRes(selectedRow);
                    requestEditor.setRequest(reqResp.request());
                    responseEditor.setResponse(reqResp.response());
                    DebugLog.log("Corrective call selected. Loaded call at row " + selectedRow + " to http editors.");
                }
            }
        });
    }

    private void setExtractAddButtonListener() {
        ActionListener extractAddButtonListener = e -> {
            ExtractText extractText = new ExtractText();
            for (int i = 0; i < extractInputFields.length - 1; i++) {
                if (extractInputFields[i].getText().isEmpty())
                    return;
            }
            extractText.name = extractInputFields[0].getText();
            extractText.startString = extractInputFields[1].getText();
            extractText.stopString = extractInputFields[2].getText();
            extractText.selectedText = extractInputFields[3].getText();
            extractText.urlEncodeDecode = (String) extractDropDown.getSelectedItem();
            extractionTableModel.addRow(extractText);
            DebugLog.log("Extract Text added to table: " + extractText.debugString());
        };
        extractAddButton.addActionListener(extractAddButtonListener);
    }

    // TODO could use cleanup. 3rd arg probably not necessary. hasBody might be set in a cleaner way.

    // Sets the action listener for the "From Selection" button for Extractions.
    /*
    private void setExtractFromSelListener(JButton fromSelect, JTextField[] inputFields) {
        fromSelect.addActionListener(e -> {
            int position;
            boolean hasBody;
            String messageString;
            String selectedContents;

            // Separate out into its own function
            if (editor.equals("response")) {
                position = responseEditor.caretPosition();
                messageString = responseEditor.getResponse().toString();
                selectedContents = responseEditor.selection().get().contents().toString();
                if (responseEditor.getResponse().body().length() > 0){
                    hasBody = true;
                }
                else
                    hasBody = false;
            }
            else if (editor.equals("request")){
                position = requestEditor.caretPosition();
                messageString = requestEditor.getRequest().toString();
                selectedContents = requestEditor.selection().get().contents().toString();
                if (requestEditor.getRequest().body().length() > 0){
                    hasBody = true;
                }
                else
                    hasBody = false;
            }
            else
                return;

        });
    }

     */

    private void setFromSelectionListener(JButton fromSelect, JTextField[] inputFields, String editor) {
        fromSelect.addActionListener(e -> {
            int position;
            boolean hasBody;
            String messageString;
            String selectedContents;

            // Separate out into its own function
            if (editor.equals("response")) {
                position = responseEditor.caretPosition();
                messageString = responseEditor.getResponse().toString();
                selectedContents = responseEditor.selection().get().contents().toString();
                if (responseEditor.getResponse().body().length() > 0){
                    hasBody = true;
                }
                else
                    hasBody = false;
            }
            else if (editor.equals("request")){
                position = requestEditor.caretPosition();
                messageString = requestEditor.getRequest().toString();
                selectedContents = requestEditor.selection().get().contents().toString();
                if (requestEditor.getRequest().body().length() > 0){
                    hasBody = true;
                }
                else
                    hasBody = false;
            }
            else
                return;


            inputFields[3].setText(selectedContents);

            if (selectedContents.startsWith(" "))
                inputFields[3].setToolTipText("⚠️ Selection includes whitespace, was this intentional?");
            else
                inputFields[3].setToolTipText(null);


            String stopString = getStopString(position, messageString, hasBody);
            inputFields[2].setText(stopString);

            int offset = selectedContents.length();
            int startIndex = position - offset;

            String startString = getStartString(startIndex, messageString);
            inputFields[1].setText(startString);

            DebugLog.log("'From Selection' button clicked in corrective calls panel. The following input fields were set.");
            DebugLog.log("Selected Text: " + selectedContents);
            DebugLog.log("Stop String: " + stopString);
            DebugLog.log("Start String: " + startString);
        });
    }

    private String getStopString(int caretPosition, String messageString, Boolean hasBody) {
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

    private String getStartString(int startIndex, String messageString){
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

    private Boolean hasBody(String editor){
        int position;
        boolean hasBody;
        String messageString;
        String selectedContents;

        if (editor.equals("response")) {
            position = responseEditor.caretPosition();
            messageString = responseEditor.getResponse().toString();
            selectedContents = responseEditor.selection().get().contents().toString();
            if (responseEditor.getResponse().body().length() > 0){
                hasBody = true;
            }
            else
                hasBody = false;
        }
        else if (editor.equals("request")){
            position = requestEditor.caretPosition();
            messageString = requestEditor.getRequest().toString();
            selectedContents = requestEditor.selection().get().contents().toString();
            if (requestEditor.getRequest().body().length() > 0){
                hasBody = true;
            }
            else
                hasBody = false;
        }
        return false;
    }

    public void setReplaceDropDownListener() {
        replaceDropDown.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {

                ExtractText tempExtractText;

                if (replaceDropDown.getSelectedIndex() != -1) {
                    tempExtractText = replaceDropDown.getItemAt(replaceDropDown.getSelectedIndex());
                }
                else {
                    tempExtractText = null;
                }

                replaceDropDown.removeAllItems();

                for (int row = 0; row < extractionTableModel.getRowCount(); row++) {
                    ExtractText extractText = extractionTableModel.getExtractText(row);
                    replaceDropDown.addItem(extractText);

                    if (extractText.equals(tempExtractText)) {
                        replaceDropDown.setSelectedIndex(row);
                    }
                }


            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {

            }
        });
    }

    private void setReplaceAddButtonListener() {
        ActionListener replaceAddButtonListener = e -> {

            // Create ReplaceText object
            ReplaceText replaceText = new ReplaceText();
            for (int i = 0; i < replaceInputFields.length - 1; i++) {
                if (replaceInputFields[i].getText().isEmpty()) {
                    DebugLog.log("Replacement 'Add' button clicked while input field empty.");
                    return;
                }
            }
            replaceText.name = replaceInputFields[0].getText();
            replaceText.startString = replaceInputFields[1].getText();
            replaceText.stopString = replaceInputFields[2].getText();
            replaceText.selectedText = replaceInputFields[3].getText();
            replaceText.extractText = (ExtractText) replaceDropDown.getSelectedItem();
            replacementTableModel.addRow(replaceText);
            DebugLog.log("Replace Text added to table: " + replaceText);

            // Associate ReplaceText object with currently selected corrective call.


        };
        replaceAddButton.addActionListener(replaceAddButtonListener);
    }


    private void setAttributes(CorrectiveCallsPanel correctiveCallsPanel) {
        correctiveCallSetTableModel = correctiveCallsPanel.correctiveCallSetTableModel;
        correctiveCallsSetsTable = correctiveCallsPanel.correctiveCallsSetsTable;

        callSequenceTableModel = correctiveCallsPanel.callSequenceTableModel;
        callSequenceTable = correctiveCallsPanel.callSequenceTable;

        singleSetRadioButton = correctiveCallsPanel.singleSetRadionButton;
        multiSetRadioButton = correctiveCallsPanel.multiSetRadioButton;

        addSetBotton = correctiveCallsPanel.addSetBotton;
        removeSetBotton = correctiveCallsPanel.removeSetBotton;

        extractAddButton = correctiveCallsPanel.extractAddButton;
        replaceAddButton = correctiveCallsPanel.replaceAddButton;

        extractFromSelect = correctiveCallsPanel.extractFromSelect;
        replaceFromSelect = correctiveCallsPanel.replaceFromSelect;

        extractInputFields = correctiveCallsPanel.extractInputFields;
        replaceInputFields = correctiveCallsPanel.replaceInputFields;

        extractionTableModel = correctiveCallsPanel.extractionTableModel;
        replacementTableModel = correctiveCallsPanel.replacementTableModel;

        extractDropDown = correctiveCallsPanel.extractDropDown;
        replaceDropDown = correctiveCallsPanel.replaceDropDown;

        extractionTable = correctiveCallsPanel.extractionTable;
        replacementTable = correctiveCallsPanel.replacementTable;

        requestEditor = correctiveCallsPanel.requestEditor;
        responseEditor = correctiveCallsPanel.responseEditor;
    }
}

