import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static burp.api.montoya.http.message.HttpRequestResponse.httpRequestResponse;

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

    private Model model;

    FollowUpController (View view, Model model, Controller controller) {
        setAttributes(model, view);
        
        setTableDeleteContextMenu(followUpReplaceTable);
        setReplaceDropDownListener();
        setReplaceAddButtonListener();
        setFromSelectionListener(fromSelect, inputFields);
        //setfollowUpCall();
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

    // TODO could use cleanup. The hasBody argument is gone, Utils.getContentEnd() derives it from the message.
    private void setFromSelectionListener(JButton fromSelect, JTextField[] inputFields) {
        fromSelect.addActionListener(e -> {
            int position;
            String messageString;
            String selectedContents;

            position = requestEditor.caretPosition();
            messageString = requestEditor.getRequest().toString();
            selectedContents = requestEditor.selection().get().contents().toString();

            inputFields[3].setText(selectedContents);

            if (selectedContents.startsWith(" "))
                inputFields[3].setToolTipText("⚠️ Selection includes whitespace, was this intentional?");
            else
                inputFields[3].setToolTipText(null);


            String stopString = Utils.getStopString(position, messageString);
            inputFields[2].setText(stopString);

            int offset = selectedContents.length();
            int startIndex = position - offset;

            String startString = Utils.getStartString(startIndex, messageString);
            inputFields[1].setText(startString);

            DebugLog.log("'From Selection' button clicked in followup call. The following input fields were set.");
            DebugLog.log("Selected Text: " + selectedContents);
            DebugLog.log("Stop String: " + stopString);
            DebugLog.log("Start String: " + startString);
        });
    }


    // TODO need to go through all controllers and update them to use this method
    public void setTableDeleteContextMenu(JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                DeletableTableModel model = (DeletableTableModel) table.getModel();
                DebugLog.log(table.getName() + ": User deleted row " + selectedRow);
                model.removeRow(selectedRow);
                Utils.resizeColumnsToFitContent(table);
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

    public void setfollowUpCall(){
        model.followupCall = new CorrectiveCall(httpRequestResponse(requestEditor.getRequest(), responseEditor.getResponse()));
        followUpReplaceTableModel.loadReplaceList(model.followupCall.replaceList);
    }


    private void setAttributes (Model model, View view) {

        requestEditor = view.followUpPanel.requestEditor;
        responseEditor = view.followUpPanel.responseEditor;

        inputFields = view.followUpPanel.inputFields;
        fromSelect = view.followUpPanel.fromSelect;
        addButton = view.followUpPanel.addButton;
        replaceDropDown = view.followUpPanel.dropDown;

        followUpReplaceTableModel = view.followUpPanel.followUpReplaceTableModel;
        followUpReplaceTable = view.followUpPanel.followUpReplaceTable;
        
        extractionTableModel = view.correctiveCallsPanel.extractionTableModel;

        this.model = model;
    }
}
