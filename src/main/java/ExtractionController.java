import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ExtractionController {


    private void setExtractionFromSelectionListener(JButton fromSelect, JTextField[] inputFields,
                                                    HttpResponseEditor responseEditor, HttpRequestEditor requestEditor) {

    }


    private void setAddButtonListener(JTextField[] inputFields, JComboBox<String> dropDown, JButton addButton,
                                      ExtractionTableModel tableModel) {
        ActionListener extractAddButtonListener = e -> {
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
        addButton.addActionListener(extractAddButtonListener);
    }
}
