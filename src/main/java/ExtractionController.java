import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ExtractionController {
    
    
    private JTextField[] inputFields;
    private JTable table;
    private ExtractionTableModel tableModel;
    private JButton fromSelectButton;
    private JButton addButton;
    private HttpRequestEditor requestEditor;
    private HttpResponseEditor responseEditor;
    private JComboBox<String> dropDown;
    private JRadioButton[] radioButtons;
    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;


    ExtractionController(ExtractionPanel extractionPanel, HttpRequestEditor httpRequestEditor,
                         HttpResponseEditor httpResponseEditor, CallSequenceTableModel callSequenceTableModel,
                         JTable callSequenceTable) {

        this.dropDown = extractionPanel.dropDown;
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
            try {
                ExtractText extractText = new ExtractText();
                for (int i = 0; i < inputFields.length - 2; i++) {
                    if (inputFields[i].getText().isEmpty())
                        return;
                }
                extractText.name = inputFields[0].getText();
                extractText.startString = Utils.unescapeLineBreaks(inputFields[1].getText());
                extractText.stopString = Utils.unescapeLineBreaks(inputFields[2].getText());
                extractText.selectedText = Utils.unescapeLineBreaks(inputFields[3].getText());
                extractText.urlEncodeDecode = (String) dropDown.getSelectedItem();
                extractText.extractFromResponse = radioButtons[0].isSelected();
                extractText.extractFromRequest = radioButtons[1].isSelected();

                DebugLog.log("Extract Text added to table: " + extractText.debugString());

                tableModel.addRow(extractText);

                DebugLog.log("Extract Text added to corrective call" + callSequenceTableModel.getCorrectiveCall(callSequenceTable.getSelectedRow()).toString());
            }
            catch (Exception ex) {
                DebugLog.log("[Error] ExtractionController: Adding extraction failed. " + ex);
            }
        };
        addButton.addActionListener(addButtonListener);
    }
    
    private void setFromSelectionListener() {
        fromSelectButton.addActionListener(e -> {
            try {
                DebugLog.log("Extraction 'From Selection' clicked.");
                Utils.applyFromEitherSelection(requestEditor, responseEditor, inputFields, radioButtons,
                        "Extraction");
            }
            catch (Exception ex) {
                DebugLog.log("[Error] ExtractionController: From Selection failed. " + ex);
            }
        });
    }
}
