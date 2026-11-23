import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class FollowUpController {

    private HttpRequestEditor requestEditor;
    private HttpResponseEditor responseEditor;

    private JTextField[] inputFields;
    private JButton fromSelect;
    private JButton addButton;
    private JComboBox<ExtractText> replaceDropDown;

    private ReplacementTableModel followUpReplaceTableModel;
    private JTable followUpReplaceTable;
    
    private CorrectiveCallSetTableModel correctiveCallSetTableModel;
    private JTable correctiveCallsSetsTable;

    private Model model;

    FollowUpController (View view, Model model, Controller controller) {
        setAttributes(model, view);
        
        Utils.setTableDeleteContextMenu(followUpReplaceTable, null);

        // The response editor is hidden here, but paired anyway in case it's restored.
        Utils.pairEditorCarets(requestEditor, responseEditor, "Followup Call");
        setReplaceDropDownListener();
        setReplaceAddButtonListener();
        setFromSelectionListener(fromSelect, inputFields);
        setSelectedSetListener();
    }

    // The Followup tab shows the followup of the set selected on the Corrective Calls tab.
    private void setSelectedSetListener() {
        correctiveCallsSetsTable.getSelectionModel().addListSelectionListener(e -> {
            try {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedSet();
                }
            }
            catch (Exception ex) {
                DebugLog.log("[Error] FollowUpController: Loading the selected set's followup failed. " + ex);
            }
        });
    }

    // The selected corrective call set. No selection means Set 0.
    private CorrectiveCallSet selectedSet() {
        int row = correctiveCallsSetsTable.getSelectedRow();
        return correctiveCallSetTableModel.getCorrectiveCallsSet(row == -1 ? 0 : row);
    }

    // Loads the selected set's followup request and replacements into the tab.
    private void loadSelectedSet() {
        CorrectiveCallSet set = selectedSet();

        if (set.followupCall == null) {
            requestEditor.setRequest(null);
            followUpReplaceTableModel.loadReplaceList(new ArrayList<>());
            DebugLog.log("Followup Call: " + set.name + " has no followup call yet.");
            return;
        }

        requestEditor.setRequest(set.followupCall.requestResponse.request());
        followUpReplaceTableModel.loadReplaceList(set.followupCall.replaceList);
        DebugLog.log("Followup Call: loaded " + set.name + "'s followup call.");
    }
    


    private void setReplaceDropDownListener() {
        replaceDropDown.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                try {
                    Utils.repopulatePreservingSelection(replaceDropDown, pickableExtractions());
                }
                catch (Exception ex) {
                    DebugLog.log("[Error] FollowUpController: Refreshing Pick from Extraction dropdown failed. " + ex);
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

    // Errored call extractions plus every extraction in the selected set. The followup runs last, so
    // it can use any of them.
    private List<ExtractText> pickableExtractions() {
        List<ExtractText> pickable = new ArrayList<>();

        // The errored call runs before every set, so its extractions are always in scope.
        pickable.addAll(model.errorCallExtractions());
        pickable.addAll(CorrectiveCallSet.extractionsForSelectedSet(
                correctiveCallSetTableModel.correctiveCallSetList,
                correctiveCallsSetsTable.getSelectedRow()));

        return pickable;
    }

    private void setReplaceAddButtonListener() {
        ActionListener replaceAddButtonListener = e -> {
            try {
                if (selectedSet().followupCall == null) {
                    DebugLog.log("[Error] Followup replacement 'Add' clicked before any call was sent to "
                            + "Error Detection.");
                    return;
                }

                ReplaceText replaceText = new ReplaceText();
                for (int i = 0; i < inputFields.length; i++) {
                    if (inputFields[i].getText().isEmpty()) {
                        DebugLog.log("Followup replacement 'Add' clicked while an input field was empty.");
                        return;
                    }
                }
                replaceText.name = inputFields[0].getText();
                replaceText.startString = Utils.unescapeLineBreaks(inputFields[1].getText());
                replaceText.stopString = Utils.unescapeLineBreaks(inputFields[2].getText());
                replaceText.selectedText = Utils.unescapeLineBreaks(inputFields[3].getText());
                replaceText.extractText = (ExtractText) replaceDropDown.getSelectedItem();
                followUpReplaceTableModel.addRow(replaceText);
                DebugLog.log("Replace Text added to table: " + replaceText);
            }
            catch (Exception ex) {
                DebugLog.log("[Error] FollowUpController: Adding replacement failed. " + ex);
            }
        };
        addButton.addActionListener(replaceAddButtonListener);
    }

    private void setFromSelectionListener(JButton fromSelect, JTextField[] inputFields) {
        fromSelect.addActionListener(e -> {
            try {
                String messageString = requestEditor.getRequest().toString();
                if (!Utils.applyFromSelection(requestEditor, messageString, inputFields, "Followup")) {
                    DebugLog.log("[Error] Followup: 'From Selection' clicked with nothing selected in "
                            + "the request editor.");
                }
            }
            catch (Exception ex) {
                DebugLog.log("[Error] FollowUpController: From Selection failed. " + ex);
            }
        });
    }

    // Gives every set its own followup call, starting as a copy of the errored call.
    public void setFollowUpCall(HttpRequestResponse reqResp) {
        for (CorrectiveCallSet set : correctiveCallSetTableModel.correctiveCallSetList) {
            set.followupCall = new CorrectiveCall(reqResp);
        }
        loadSelectedSet();
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
        
        correctiveCallSetTableModel = view.correctiveCallsPanel.correctiveCallSetTableModel;
        correctiveCallsSetsTable = view.correctiveCallsPanel.correctiveCallsSetsTable;

        this.model = model;
    }
}
