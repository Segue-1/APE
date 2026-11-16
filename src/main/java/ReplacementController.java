import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ReplacementController {

    public JComboBox<ExtractText> dropDown;
    public JTextField[] inputFields;
    public JTable table;
    public ReplacementTableModel tableModel;
    public ExtractionTableModel extractionTableModel;
    public JButton fromSelectButton;
    public JButton addButton;
    public HttpRequestEditor requestEditor;
    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;
    private Model model;
    
    ReplacementController (ReplacementPanel replacementPanel, HttpRequestEditor httpRequestEditor,
                           ExtractionTableModel extractionTableModel, CallSequenceTableModel callSequenceTableModel,
                           JTable callSequenceTable, Model model) {
        this.model = model;
        this.dropDown = replacementPanel.dropDown;
        this.inputFields = replacementPanel.inputFields;
        this.table = replacementPanel.table;
        this.tableModel = replacementPanel.tableModel;
        this.extractionTableModel = extractionTableModel;
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
            String messageString = requestEditor.getRequest().toString();
            Utils.applyFromSelection(requestEditor, messageString, inputFields, "Replacement");
        });
    }

    // Creates ReplaceText object
    private void setAddButtonListener() {
        ActionListener addButtonListener = e -> {

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
            replaceText.occurrence = parseOccurrence(inputFields[4].getText(), replaceText.name);
            tableModel.addRow(replaceText);
            DebugLog.log("Replacement Add button clicked in Corrective Calls: " + replaceText);
        };
        addButton.addActionListener(addButtonListener);
    }

    // Sets the listener for the dropdown menu titled "Pick from Extraction"
    // Retrieves a list of text extractions.
    public void setDropDownListener() {
        dropDown.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                DebugLog.log("Pick from Extraction dropdown clicked in Corrective Calls" );
                refreshDropDown();
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

    /*
    Every extraction belonging to a corrective call BEFORE the one currently selected in the call
    sequence table, within this set only.

    Replacements are applied to a call before it is sent and extractions happen after it returns, so
    only an earlier call can have produced a value this replacement could insert. The first call in a
    set therefore has nothing to pick and the dropdown is empty. Only one set runs per intercepted
    request, so another set's extractions are never available either.

    The followup call is the opposite case and may use any extraction, since it always runs last. See
    CorrectiveCallSet.allExtractionsFrom().
     */
    List<ExtractText> pickableExtractions() {
        List<ExtractText> pickable = new ArrayList<>();

        /*
        The errored call comes before every corrective call, so its extractions are always available --
        including to the FIRST call in a set, which has no earlier call to draw on and so used to have
        an empty dropdown.
         */
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

    /*
    The "Occurrence:" field, defaulting to 1 when blank or not a number.

    Left out of the mandatory-field check above on purpose: a blank occurrence means "the first one",
    which is what every replacement configured before this field existed meant.
     */
    static int parseOccurrence(String input, String replacementName) {
        if (input == null || input.isBlank()) {
            return 1;
        }

        try {
            int parsed = Integer.parseInt(input.trim());

            if (parsed < 1) {
                DebugLog.log("[Error] Occurrence must be 1 or greater, got " + parsed
                        + " for replacement '" + replacementName + "'. Using 1.");
                return 1;
            }

            return parsed;
        }
        catch (NumberFormatException e) {
            DebugLog.log("[Error] Occurrence '" + input + "' is not a number for replacement '"
                    + replacementName + "'. Using 1.");
            return 1;
        }
    }
}
