import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.List;

public class ReplacementController {

    public JComboBox<ExtractText> dropDown;
    public JTextField[] inputFields;
    public JTable table;
    public ReplacementTableModel tableModel;
    public ExtractionTableModel extractionTableModel;
    public JButton fromSelectButton;
    public JButton addButton;
    public HttpRequestEditor requestEditor;
    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;
    
    ReplacementController (ReplacementPanel replacementPanel, HttpRequestEditor httpRequestEditor,
                           ExtractionTableModel extractionTableModel, CallSequenceTableModel callSequenceTableModel,
                           JTable callSequenceTable) {
        this.dropDown = replacementPanel.dropDown;
        this.inputFields = replacementPanel.inputFields;
        this.table = replacementPanel.table;
        this.tableModel = replacementPanel.tableModel;
        this.extractionTableModel = extractionTableModel;
        this.fromSelectButton = replacementPanel.fromSelectButton;
        this.addButton = replacementPanel.addButton;
        this.requestEditor = httpRequestEditor;
        this.callSequenceTableModel = callSequenceTableModel;
        this.callSequenceTable = callSequenceTable;

        setFromSelectionListener();
        setAddButtonListener();
        setDropDownListener();
    }

    private void setFromSelectionListener() {
        fromSelectButton.addActionListener(e -> {
            int position;
            boolean hasBody;
            String messageString;
            String selectedContents;

            position = requestEditor.caretPosition();
            messageString = requestEditor.getRequest().toString();
            selectedContents = requestEditor.selection().get().contents().toString();


            if (requestEditor.getRequest().body().length() > 0) {
                hasBody = true;
            }
            else
                hasBody = false;


            inputFields[3].setText(selectedContents);

            if (selectedContents.startsWith(" "))
                inputFields[3].setToolTipText("⚠️ Selection includes whitespace, was this intentional?");
            else
                inputFields[3].setToolTipText(null);


            String stopString = Utils.getStopString(position, messageString, hasBody);
            inputFields[2].setText(stopString);

            int offset = selectedContents.length();
            int startIndex = position - offset;

            String startString = Utils.getStartString(startIndex, messageString);
            inputFields[1].setText(startString);

            DebugLog.log("'From Selection' button clicked in Replace text section. " +
                    "The following input fields were set.");
            DebugLog.log("Selected Text: " + selectedContents);
            DebugLog.log("Stop String: " + stopString);
            DebugLog.log("Start String: " + startString);
        });
    }

    // Creates ReplaceText object
    private void setAddButtonListener() {
        ActionListener addButtonListener = e -> {

            ReplaceText replaceText = new ReplaceText();
            for (int i = 0; i < inputFields.length - 1; i++) {
                if (inputFields[i].getText().isEmpty()) {
                    DebugLog.log("Replacement 'Add' button clicked while input field empty.");
                    return;
                }
            }

            replaceText.name = inputFields[0].getText();
            replaceText.startString = inputFields[1].getText();
            replaceText.stopString = inputFields[2].getText();
            replaceText.selectedText = inputFields[3].getText();
            replaceText.extractText = (ExtractText) dropDown.getSelectedItem();
            tableModel.addRow(replaceText);
            DebugLog.log("Replacement Add button clicked in Corrective Calls: " + replaceText);
        };
        addButton.addActionListener(addButtonListener);
    }

    // Sets the listener for the dropdown menu titled "Pick from Extraction"
    // Retrieves a list of text extractions.
    public void setDropDownListener() {
        dropDown.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                DebugLog.log("Pick from Extraction dropdown clicked in Corrective Calls" );
                refreshDropDown();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {

            }
        });
    }

    // Populates "Pick from Extraction" drop down menu. Extractions in this drop down may only come from previous requests
    // in relation to the currently selected corrective call in the call sequence table.
    public void refreshDropDown() {
        ExtractText tempExtractText;

        if (dropDown.getSelectedIndex() != -1) {
            tempExtractText = dropDown.getItemAt(dropDown.getSelectedIndex());
        }
        else {
            tempExtractText = null;
        }

        dropDown.removeAllItems();

        if (callSequenceTable.getSelectedRow() > 0) {
            for (int i = 0; i < callSequenceTable.getSelectedRow(); i++) {
                List<ExtractText> extractTextList = callSequenceTableModel.correctiveCallList.get(i).extractList;
                for (int j = 0; j < extractTextList.size(); j++) {
                    dropDown.addItem(extractTextList.get(j));

                    if (extractTextList.get(j).equals(tempExtractText)) {
                        dropDown.setSelectedIndex(j);
                    }
                }
            }
        }
    }
}
