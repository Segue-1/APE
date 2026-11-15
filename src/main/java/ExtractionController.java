import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ExtractionController {
    
    
    public JTextField[] inputFields;
    public JTable table;
    public ExtractionTableModel tableModel;
    public JButton fromSelectButton;
    public JButton addButton;
    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;
    public JComboBox<String> dropDown;
    public ButtonGroup buttonGroup;
    public JRadioButton[] radioButtons;
    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;


    ExtractionController(ExtractionPanel extractionPanel, HttpRequestEditor httpRequestEditor,
                         HttpResponseEditor httpResponseEditor, CallSequenceTableModel callSequenceTableModel,
                         JTable callSequenceTable) {

        this.dropDown = extractionPanel.dropDown;
        this.buttonGroup = extractionPanel.buttonGroup;
        this.radioButtons = extractionPanel.radioButtons;
        this.inputFields = extractionPanel.inputFields;
        this.table = extractionPanel.table;
        this.tableModel = extractionPanel.tableModel;
        this.fromSelectButton = extractionPanel.fromSelectButton;
        this.addButton = extractionPanel.addButton;
        this.requestEditor = httpRequestEditor;
        this.responseEditor = httpResponseEditor;
        this.callSequenceTableModel = callSequenceTableModel;
        this.callSequenceTable = callSequenceTable;

        setFromSelectionListener();
        setAddButtonListener();
        //setDropDownListener();
    }


    private void setAddButtonListener() {
        ActionListener addButtonListener = e -> {
            ExtractText extractText = new ExtractText();
            for (int i = 0; i < inputFields.length - 2; i++) {
                if (inputFields[i].getText().isEmpty())
                    return;
            }
            extractText.name = inputFields[0].getText();
            extractText.startString = inputFields[1].getText();
            extractText.stopString = inputFields[2].getText();
            extractText.selectedText = inputFields[3].getText();
            extractText.urlEncodeDecode = (String) dropDown.getSelectedItem();
            extractText.extractFromResponse = radioButtons[0].isSelected();
            extractText.extractFromRequest = radioButtons[1].isSelected();

            DebugLog.log("Extract Text added to table: " + extractText.debugString());

            tableModel.addRow(extractText);

            DebugLog.log("Extract Text added to corrective call" + callSequenceTableModel.getCorrectiveCall(callSequenceTable.getSelectedRow()).toString());
        };
        addButton.addActionListener(addButtonListener);
    }
    
    private void setFromSelectionListener() {
        fromSelectButton.addActionListener(e -> {
            DebugLog.log("From Select button pressed");
            int requestEditorposition;
            int responseEditorposition;

            requestEditorposition = requestEditor.caretPosition();
            DebugLog.log("Caret position of request editor: " + requestEditorposition);
            responseEditorposition = responseEditor.caretPosition();
            DebugLog.log("Caret position of response editor: " + responseEditorposition);


            if (requestEditorposition > 0) {
                extractFromRequestEditor(requestEditorposition);
                radioButtons[1].setSelected(true);
            }
            else if (responseEditorposition > 0) {

                extractFromResponseEditor(responseEditorposition);
                radioButtons[0].setSelected(true);
            }
            else{
                DebugLog.log("Extraction fromSelection button clicked while no editor selected.");
            }
        });
    }

    private void extractFromRequestEditor(int position) {
        DebugLog.log("Extracting from request editor at caret position " + position);

        String messageString = requestEditor.getRequest().toString();
        String selectedContents = requestEditor.selection().get().contents().toString();

        DebugLog.log("Request message length: " + messageString.length()
                + ", content ends at: " + Utils.getContentEnd(messageString));

        inputFields[3].setText(selectedContents);

        if (selectedContents.startsWith(" ")) {
            inputFields[3].setToolTipText("⚠️ Selection includes whitespace, was this intentional?");
            DebugLog.log("Selection starts with whitespace. Whitespace warning tool tip set on selected text field.");
        }
        else
            inputFields[3].setToolTipText(null);


        String stopString = Utils.getStopString(position, messageString);
        inputFields[2].setText(stopString);

        int offset = selectedContents.length();
        int startIndex = position - offset;

        DebugLog.log("Selection length: " + offset + ", calculated start index: " + startIndex);

        String startString = Utils.getStartString(startIndex, messageString);
        inputFields[1].setText(startString);

        DebugLog.log("Selected Text: " + selectedContents);
        DebugLog.log("Stop String: " + stopString);
        DebugLog.log("Start String: " + startString);
    }

    private void extractFromResponseEditor(int position) {
        DebugLog.log("Extracting from response editor at caret position " + position);

        String messageString = responseEditor.getResponse().toString();
        String selectedContents = responseEditor.selection().get().contents().toString();

        DebugLog.log("Response message length: " + messageString.length()
                + ", content ends at: " + Utils.getContentEnd(messageString));

        inputFields[3].setText(selectedContents);

        if (selectedContents.startsWith(" ")) {
            inputFields[3].setToolTipText("⚠️ Selection includes whitespace, was this intentional?");
            DebugLog.log("Selection starts with whitespace. Whitespace warning tool tip set on selected text field.");
        }
        else
            inputFields[3].setToolTipText(null);


        String stopString = Utils.getStopString(position, messageString);
        inputFields[2].setText(stopString);

        int offset = selectedContents.length();
        int startIndex = position - offset;

        DebugLog.log("Selection length: " + offset + ", calculated start index: " + startIndex);

        String startString = Utils.getStartString(startIndex, messageString);
        inputFields[1].setText(startString);

        DebugLog.log("Selected Text: " + selectedContents);
        DebugLog.log("Stop String: " + stopString);
        DebugLog.log("Start String: " + startString);
    }
}
