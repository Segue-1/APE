import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;

public class FollowUpController {

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public JTextField[] inputFields;
    public JButton fromSelect;
    public JButton addButton;
    public JComboBox<ExtractText> replaceDropDown;

    public ReplacementTableModel followUpReplaceTableModel;
    public JTable followUpReplaceTable;
    
    private ExtractionTableModel extractionTableModel;

    FollowUpController (View view, Model model, Controller controller) {
        setAttributes(view);
        
        controller.setTableDeleteContextMenu(followUpReplaceTable);
        setReplaceDropDownListener();
        setReplaceAddButtonListener();
        setFromSelectionListener(fromSelect, inputFields);
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
            ReplaceText replaceText = new ReplaceText();
            for (int i = 0; i < inputFields.length - 1; i++) {
                if (inputFields[i].getText().isEmpty()) {
                    Controller.montoyaApi.logging().logToOutput("Return hit");
                    return;
                }
            }
            replaceText.name = inputFields[0].getText();
            replaceText.startString = inputFields[1].getText();
            replaceText.stopString = inputFields[2].getText();
            replaceText.selectedText = inputFields[3].getText();
            replaceText.extractText = (ExtractText) replaceDropDown.getSelectedItem();
            followUpReplaceTableModel.addRow(replaceText);
            DebugLog.log("Replace Text added to table: " + replaceText);
        };
        addButton.addActionListener(replaceAddButtonListener);
    }

    // TODO could use cleanup. 3rd arg probably not necessary. hasBody might be set in a cleaner way.
    private void setFromSelectionListener(JButton fromSelect, JTextField[] inputFields) {
        fromSelect.addActionListener(e -> {
            int position;
            boolean hasBody;
            String messageString;
            String selectedContents;

   
            position = requestEditor.caretPosition();
            messageString = requestEditor.getRequest().toString();
            selectedContents = requestEditor.selection().get().contents().toString();
            if (requestEditor.getRequest().body().length() > 0){
                hasBody = true;
            }
            else
                hasBody = false;
            
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

            DebugLog.log("'From Selection' button clicked in followup call. The following input fields were set.");
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

    private void setAttributes (View view) {

        requestEditor = view.followUpPanel.requestEditor;
        responseEditor = view.followUpPanel.responseEditor;

        inputFields = view.followUpPanel.inputFields;
        fromSelect = view.followUpPanel.fromSelect;
        addButton = view.followUpPanel.addButton;
        replaceDropDown = view.followUpPanel.dropDown;

        followUpReplaceTableModel = view.followUpPanel.followUpReplaceTableModel;
        followUpReplaceTable =  view.followUpPanel.followUpReplaceTable;
        
        extractionTableModel = view.correctiveCallsPanel.extractionTableModel;
    }
}
