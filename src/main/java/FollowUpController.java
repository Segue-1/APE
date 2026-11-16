import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
    
    private CorrectiveCallSetTableModel correctiveCallSetTableModel;
    private JTable correctiveCallsSetsTable;

    private Model model;

    FollowUpController (View view, Model model, Controller controller) {
        setAttributes(model, view);
        
        Utils.setTableDeleteContextMenu(followUpReplaceTable, null);

        /*
        Paired for the same reason as the other tabs, though it has no effect while this tab's response
        editor is hidden behind the empty placeholder panel: a component nobody can click never fires
        the listener, and only the request editor is read by "From Selection" here. Wired now so that
        restoring the response editor does not silently reintroduce the stale-selection bug.
         */
        Utils.pairEditorCarets(requestEditor, responseEditor, "Followup Call");
        setReplaceDropDownListener();
        setReplaceAddButtonListener();
        setFromSelectionListener(fromSelect, inputFields);
        //setfollowUpCall();
    }
    


    public void setReplaceDropDownListener() {
        replaceDropDown.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                Utils.repopulatePreservingSelection(replaceDropDown, pickableExtractions());
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {

            }
        });
    }

    /*
    Every extraction on the currently selected corrective call set. The followup runs after all of that
    set's calls, so it may use any of their extractions, not just the earlier ones.

    Sourced from the set rather than from the Corrective Calls panel's extraction table, whose list is
    swapped to whichever single call is selected there. Reading that table meant the followup could
    only reference the extractions of the one call that happened to be highlighted.
     */
    List<ExtractText> pickableExtractions() {
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
            ReplaceText replaceText = new ReplaceText();
            for (int i = 0; i < inputFields.length - 1; i++) {
                if (inputFields[i].getText().isEmpty()) {
                    DebugLog.log("Followup replacement 'Add' clicked while an input field was empty.");
                    return;
                }
            }
            replaceText.name = inputFields[0].getText();
            replaceText.startString = inputFields[1].getText();
            replaceText.stopString = inputFields[2].getText();
            replaceText.selectedText = inputFields[3].getText();
            replaceText.extractText = (ExtractText) replaceDropDown.getSelectedItem();
            replaceText.occurrence = ReplacementController.parseOccurrence(
                    inputFields[4].getText(), replaceText.name);
            followUpReplaceTableModel.addRow(replaceText);
            DebugLog.log("Replace Text added to table: " + replaceText);
        };
        addButton.addActionListener(replaceAddButtonListener);
    }

    private void setFromSelectionListener(JButton fromSelect, JTextField[] inputFields) {
        fromSelect.addActionListener(e -> {
            String messageString = requestEditor.getRequest().toString();
            Utils.applyFromSelection(requestEditor, messageString, inputFields, "Followup");
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
        
        correctiveCallSetTableModel = view.correctiveCallsPanel.correctiveCallSetTableModel;
        correctiveCallsSetsTable = view.correctiveCallsPanel.correctiveCallsSetsTable;

        this.model = model;
    }
}
