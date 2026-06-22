import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;

public class ReplacementController {

    ReplacementController (){

    }

    private void setFromSelectionListener(JButton fromSelect, JTextField[] inputFields,
                                          HttpRequestEditor requestEditor) {
        fromSelect.addActionListener(e -> {
            int position;
            boolean hasBody;
            String messageString;
            String selectedContents;

            position = requestEditor.caretPosition();
            messageString = requestEditor.getRequest().toString();
            selectedContents = requestEditor.selection().get().contents().toString();


            if (requestEditor.getRequest().body().length() > 0) {
                hasBody = true;
            } else
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

            DebugLog.log("'From Selection' button clicked in corrective calls panel. The following input fields were set.");
            DebugLog.log("Selected Text: " + selectedContents);
            DebugLog.log("Stop String: " + stopString);
            DebugLog.log("Start String: " + startString);

        });
    }

    private void setAddButtonListener(JTextField[] inputFields, JComboBox<String> dropDown, JButton addButton,
                                      ReplacementTableModel tableModel) {
        ActionListener replaceAddButtonListener = e -> {

            // Create ReplaceText object
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
            DebugLog.log("Replace Text added to table: " + replaceText);

            // Associate ReplaceText object with currently selected corrective call.


        };
        addButton.addActionListener(replaceAddButtonListener);
    }

    public void setReplaceDropDownListener(JComboBox<ExtractText> replaceDropDown, ExtractionTableModel tableModel) {
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

                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    ExtractText extractText = tableModel.getExtractText(row);
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
}
