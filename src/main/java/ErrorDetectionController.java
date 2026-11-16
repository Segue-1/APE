import burp.api.montoya.http.message.HttpRequestResponse;

import java.awt.event.ActionListener;


public class ErrorDetectionController {
    private ErrorDetectionPanel errorDetectionPanel;
    public HttpRequestResponse errorCall; // not used

    private TriggerConditionController triggerConditionController;

    private Model model;


    ErrorDetectionController(ErrorDetectionPanel errorDetectionPanel, Model model){
        this.errorDetectionPanel = errorDetectionPanel;
        this.model = model;

        storeErrorConditions();
        createErrorConditionTableContextMenu();

        /*
        Both editors on this tab remember their selection, and nothing clears one when the user clicks
        into the other, so after working in both BOTH report a selection. The extraction's
        "From Selection" below asks the request first, so without this it would always take the
        request's stale selection no matter which editor the user meant.
         */
        Utils.pairEditorCarets(errorDetectionPanel.requestEditor, errorDetectionPanel.responseEditor,
                "Error Detection");

        setUpErroredCallExtraction();
    }

    /*
    ------------------------------------------------------------ errored call extractions

    Wires the "Extract" panel in the bottom right of this tab.

    Extractions added there belong to Model.errorCall and run against the exchange that actually
    triggered APE, which makes a value the error response itself supplied -- the version on a 409, a
    nonce on a 401 -- usable by the corrective calls that follow. Nothing else can read it: a
    corrective call only ever extracts from its own request or response.

    The panel is an ExtractionPanel, the same class the Corrective Calls tab uses, so the two cannot
    drift apart visually. Only the wiring below differs: which exchange it reads, and which list it
    writes to.
     */
    private void setUpErroredCallExtraction() {
        ExtractionPanel panel = errorDetectionPanel.errorExtractionPanel;

        /*
        Point the table's model at the error call's own list, so adding a row here adds to the list
        Model reads at request time. No copying and nothing to keep in sync.
         */
        panel.tableModel.loadExtractList(model.errorCall.extractList);

        setErroredCallFromSelectionListener(panel);
        setErroredCallAddButtonListener(panel);
        Utils.setTableDeleteContextMenu(panel.table, null);
    }

    /*
    Fills the extraction input fields from whichever editor the user has a selection in.

    Asks the request first, then the response, rather than inferring from caret positions:
    applyFromSelection() reports whether there was a selection at all, which sidesteps the "caret
    reads 0 in both editors" case the caret-based approach has.

    This only picks the right editor because the two are caret-paired in the constructor. Without that,
    both would hold a selection after the user had worked in each, and asking in a fixed order would
    always return the request's.
     */
    private void setErroredCallFromSelectionListener(ExtractionPanel panel) {
        panel.fromSelectButton.addActionListener(e -> {
            DebugLog.log("Errored call 'From Selection' clicked.");

            if (Utils.applyFromSelection(errorDetectionPanel.requestEditor,
                    errorDetectionPanel.requestEditor.getRequest().toString(),
                    panel.inputFields, "Errored call (request)")) {
                panel.radioButtons[1].setSelected(true);
                return;
            }

            if (Utils.applyFromSelection(errorDetectionPanel.responseEditor,
                    errorDetectionPanel.responseEditor.getResponse().toString(),
                    panel.inputFields, "Errored call (response)")) {
                panel.radioButtons[0].setSelected(true);
                return;
            }

            DebugLog.log("[Error] Errored call 'From Selection' clicked with nothing selected in "
                    + "either editor.");
        });
    }

    private void setErroredCallAddButtonListener(ExtractionPanel panel) {
        ActionListener addButtonListener = e -> {
            // Name, start, stop and selected text are required; the dropdown and radios always have a
            // value. Mirrors the check on the Corrective Calls extraction panel.
            for (int i = 0; i < panel.inputFields.length - 2; i++) {
                if (panel.inputFields[i].getText().isEmpty()) {
                    DebugLog.log("Errored call extraction 'Add' clicked while an input field was empty.");
                    return;
                }
            }

            ExtractText extractText = new ExtractText();
            extractText.name = panel.inputFields[0].getText();
            extractText.startString = panel.inputFields[1].getText();
            extractText.stopString = panel.inputFields[2].getText();
            extractText.selectedText = panel.inputFields[3].getText();
            extractText.urlEncodeDecode = (String) panel.dropDown.getSelectedItem();
            extractText.extractFromResponse = panel.radioButtons[0].isSelected();
            extractText.extractFromRequest = panel.radioButtons[1].isSelected();

            panel.tableModel.addRow(extractText);
            Utils.resizeColumnsToFitContent(panel.table);

            DebugLog.log("Errored call extraction added: " + extractText.debugString());
        };
        panel.addButton.addActionListener(addButtonListener);
    }

    // Stores error conditions into an array list. As error conditions are stored, populates table with a list of error
    // conditions.
    private void storeErrorConditions(){
        // Get conditions type from drop down menu.
        ActionListener submitButtonListener = e -> {
            String selectedItem = (String) errorDetectionPanel.dropdown.getSelectedItem();
            boolean isHeader = selectedItem.equals("Header");

            // Header reads its own two fields; every other category reads the single text area.
            String input = isHeader
                    ? errorDetectionPanel.headerValueField.getText()
                    : errorDetectionPanel.conditionTextField.getText();
            String headerName = errorDetectionPanel.headerNameField.getText();

            if (selectedItem.equals("Status Code") && (input == null || !input.matches("\\d{3}"))){
                DebugLog.log("User attempted to submit an invalid status code. " +
                             "Status code cannot be null and must be 3 digits");
                DebugLog.log("input: " + input);
                return;
            }

            // A Header condition needs a header name to look for. The value is optional, so presence of
            // the header alone can trigger.
            if (isHeader && (headerName == null || headerName.isBlank())) {
                DebugLog.log("User attempted to submit a Header condition with no header name. "
                             + "A header name is required.");
                return;
            }

            // Status Code ignores this; the panel disables the choice for it.
            boolean matchInRequest = errorDetectionPanel.matchRequestButton.isSelected();

            ErrorCondition errorCondition = isHeader
                    ? new ErrorCondition(selectedItem, input.trim(), headerName.trim(), matchInRequest)
                    : new ErrorCondition(selectedItem, input, null, matchInRequest);
            errorDetectionPanel.errorConditionTableModel.addRow(errorCondition);

            if (isHeader) {
                errorDetectionPanel.headerNameField.setText("");
                errorDetectionPanel.headerValueField.setText("");
            }

            DebugLog.log("Error condition added. Type: '" + selectedItem + "' header: '" + headerName
                         + "' input: '" + input + "' matching the " + errorCondition.source());
            Utils.resizeColumnsToFitContent(errorDetectionPanel.errorConditionTable);
            refreshDropdowns();
        };
        errorDetectionPanel.submitButton.addActionListener(submitButtonListener);
    }


    // Shares Utils.setTableDeleteContextMenu(), with this table's dropdown refresh as the follow-up.
    private void createErrorConditionTableContextMenu() {
        Utils.setTableDeleteContextMenu(errorDetectionPanel.errorConditionTable, this::refreshDropdowns);
    }

    // The trigger condition panel's error condition dropdowns list what is in this table, so they have
    // to be rebuilt whenever a row is added or removed.
    private void refreshDropdowns() {
        try {
            triggerConditionController.refreshErrorConditionDropdown();
            DebugLog.log("Refreshed TriggerConditionPanel's error condition dropdown menus.");
        }
        catch (Exception ex) {
            DebugLog.log("Failed to refresh TriggerConditionPanel's error condition dropdown menus.");
            DebugLog.log(ex.getMessage());
        }
    }

    public void setTriggerConditionController(TriggerConditionController triggerConditionController){
        this.triggerConditionController = triggerConditionController;
    }
}