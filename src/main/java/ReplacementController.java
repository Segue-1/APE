import burp.api.montoya.ui.editor.HttpRequestEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ReplacementController {

    private JComboBox<ExtractText> dropDown;
    private JTextField[] inputFields;
    private JTable table;
    private ReplacementTableModel tableModel;
    private JButton fromSelectButton;
    private JButton addButton;
    private HttpRequestEditor requestEditor;
    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;
    private Model model;
    
    ReplacementController (ReplacementPanel replacementPanel, HttpRequestEditor httpRequestEditor,
                           CallSequenceTableModel callSequenceTableModel,
                           JTable callSequenceTable, Model model) {
        this.model = model;
        this.dropDown = replacementPanel.dropDown;
        this.inputFields = replacementPanel.inputFields;
        this.table = replacementPanel.table;
        this.tableModel = replacementPanel.tableModel;
        this.fromSelectButton = replacementPanel.fromSelectButton;
        this.addButton = replacementPanel.addButton;
        this.requestEditor = httpRequestEditor;
        this.callSequenceTableModel = callSequenceTableModel;
        this.callSequenceTable = callSequenceTable;

        setFromSelectionListener();
        setAddButtonListener();
        setDropDownListener();
    }

    private void setFromSelectionListener() {
        fromSelectButton.addActionListener(e -> {
            try {
                String messageString = requestEditor.getRequest().toString();
                if (!Utils.applyFromSelection(requestEditor, messageString, inputFields, "Replacement")) {
                    DebugLog.log("[Error] Replacement: 'From Selection' clicked with nothing selected in "
                            + "the request editor.");
                }
            }
            catch (Exception ex) {
                DebugLog.log("[Error] ReplacementController: From Selection failed. " + ex);
            }
        });
    }

    // Creates ReplaceText object
    private void setAddButtonListener() {
        ActionListener addButtonListener = e -> {
            try {

                ReplaceText replaceText = new ReplaceText();
                for (int i = 0; i < inputFields.length; i++) {
                    if (inputFields[i].getText().isEmpty()) {
                        DebugLog.log("Replacement 'Add' button clicked while input field empty.");
                        return;
                    }
                }

                replaceText.name = inputFields[0].getText();
                replaceText.startString = Utils.unescapeLineBreaks(inputFields[1].getText());
                replaceText.stopString = Utils.unescapeLineBreaks(inputFields[2].getText());
                replaceText.selectedText = Utils.unescapeLineBreaks(inputFields[3].getText());
                replaceText.extractText = (ExtractText) dropDown.getSelectedItem();
                tableModel.addRow(replaceText);
                DebugLog.log("Replacement Add button clicked in Corrective Calls: " + replaceText);
            }
            catch (Exception ex) {
                DebugLog.log("[Error] ReplacementController: Adding replacement failed. " + ex);
            }
        };
        addButton.addActionListener(addButtonListener);
    }

    // Sets the listener for the dropdown menu titled "Pick from Extraction"
    // Retrieves a list of text extractions.
    private void setDropDownListener() {
        dropDown.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                try {
                    DebugLog.log("Pick from Extraction dropdown clicked in Corrective Calls" );
                    refreshDropDown();
                }
                catch (Exception ex) {
                    DebugLog.log("[Error] ReplacementController: Refreshing Pick from Extraction dropdown failed. " + ex);
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

    // Populates "Pick from Extraction" drop down menu. Extractions in this drop down may only come from previous requests
    // in relation to the currently selected corrective call in the call sequence table.
    public void refreshDropDown() {
        Utils.repopulatePreservingSelection(dropDown, pickableExtractions());
    }

    // Errored call extractions plus those of earlier calls in this set. Later calls haven't run yet
    // when this one is sent.
    private List<ExtractText> pickableExtractions() {
        List<ExtractText> pickable = new ArrayList<>();

        // The errored call runs first, so its extractions are always available.
        pickable.addAll(model.errorCallExtractions());

        int selectedRow = callSequenceTable.getSelectedRow();

        if (selectedRow <= 0 || callSequenceTableModel.correctiveCallList == null) {
            return pickable;
        }

        for (int i = 0; i < selectedRow && i < callSequenceTableModel.correctiveCallList.size(); i++) {
            pickable.addAll(callSequenceTableModel.correctiveCallList.get(i).extractList);
        }

        return pickable;
    }

}
