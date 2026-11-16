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
                extractFromRequestEditor();
                radioButtons[1].setSelected(true);
            }
            else if (responseEditorposition > 0) {

                extractFromResponseEditor();
                radioButtons[0].setSelected(true);
            }
            else{
                DebugLog.log("Extraction fromSelection button clicked while no editor selected.");
            }
        });
    }

    /*
    Both of these now just hand the editor and its raw message to Utils.applyFromSelection(), which
    reads the selection, guards the empty case and fills in the input fields. The position argument is
    no longer needed, since the helper reads the caret off the editor itself.
     */
    private void extractFromRequestEditor() {
        String messageString = requestEditor.getRequest().toString();
        Utils.applyFromSelection(requestEditor, messageString, inputFields, "Extraction (request)");
    }

    private void extractFromResponseEditor() {
        String messageString = responseEditor.getResponse().toString();
        Utils.applyFromSelection(responseEditor, messageString, inputFields, "Extraction (response)");
    }
}
