import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ExtractionController {
    
    
    public JTextField[] inputFields;
    public JTable table;
    public ExtractionTableModel tableModel;
    public JButton fromSelectButton;
    public JButton addButton;
    public HttpRequestEditor requestEditor;
    public JComboBox<String> dropDown;

    ExtractionController(ExtractionPanel extractionPanel, HttpRequestEditor httpRequestEditor,
                         HttpResponseEditor httpResponseEditor) {

        this.dropDown = extractionPanel.dropDown;
        this.inputFields = extractionPanel.inputFields;
        this.table = extractionPanel.table;
        this.tableModel = extractionPanel.tableModel;
        this.fromSelectButton = extractionPanel.fromSelectButton;
        this.addButton = extractionPanel.addButton;
        this.requestEditor = httpRequestEditor;

        setFromSelectionListener();
        setAddButtonListener();
        //setDropDownListener();
    }


    private void setAddButtonListener() {
        ActionListener addButtonListener = e -> {
            ExtractText extractText = new ExtractText();
            for (int i = 0; i < inputFields.length - 1; i++) {
                if (inputFields[i].getText().isEmpty())
                    return;
            }
            extractText.name = inputFields[0].getText();
            extractText.startString = inputFields[1].getText();
            extractText.stopString = inputFields[2].getText();
            extractText.selectedText = inputFields[3].getText();
            extractText.urlEncodeDecode = (String) dropDown.getSelectedItem();
            tableModel.addRow(extractText);
            DebugLog.log("Extract Text added to table: " + extractText.debugString());
        };
        addButton.addActionListener(addButtonListener);
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

            DebugLog.log("'From Selection' button clicked in corrective calls panel's Replace text section. " +
                    "The following input fields were set.");
            DebugLog.log("Selected Text: " + selectedContents);
            DebugLog.log("Stop String: " + stopString);
            DebugLog.log("Start String: " + startString);
        });
    }



}
