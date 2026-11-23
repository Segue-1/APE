
import java.awt.event.ActionListener;


public class ErrorDetectionController {
    private ErrorDetectionPanel errorDetectionPanel;

    private TriggerConditionController triggerConditionController;

    private Model model;


    ErrorDetectionController(ErrorDetectionPanel errorDetectionPanel, Model model){
        this.errorDetectionPanel = errorDetectionPanel;
        this.model = model;

        storeErrorConditions();
        createErrorConditionTableContextMenu();

        // Clicking one editor clears the other's selection, so From Selection reads the right one.
        Utils.pairEditorCarets(errorDetectionPanel.requestEditor, errorDetectionPanel.responseEditor,
                "Error Detection");

        setUpErroredCallExtraction();
    }

    /*
    ------------------------------------------------------------ errored call extractions

    Wires the Extract panel. Its extractions belong to Model.errorCall and run against the intercepted
    exchange, so values from the error response can be used by the corrective calls.
     */
    private void setUpErroredCallExtraction() {
        ExtractionPanel panel = errorDetectionPanel.errorExtractionPanel;

        // The table edits Model.errorCall's list directly.
        panel.tableModel.loadExtractList(model.errorCall.extractList);

        setErroredCallFromSelectionListener(panel);
        setErroredCallAddButtonListener(panel);
        Utils.setTableDeleteContextMenu(panel.table, null);
    }

    // Fills the input fields from the editor last clicked in (caret past 0).
    private void setErroredCallFromSelectionListener(ExtractionPanel panel) {
        panel.fromSelectButton.addActionListener(e -> {
            try {
                DebugLog.log("Errored call 'From Selection' clicked.");

                Utils.applyFromEitherSelection(errorDetectionPanel.requestEditor,
                        errorDetectionPanel.responseEditor, panel.inputFields, panel.radioButtons,
                        "Errored call");
            }
            catch (Exception ex) {
                DebugLog.log("[Error] ErrorDetectionController: Errored call From Selection failed. " + ex);
            }
        });
    }

    private void setErroredCallAddButtonListener(ExtractionPanel panel) {
        ActionListener addButtonListener = e -> {
            try {
                // Name, start, stop and selected text are required.
                for (int i = 0; i < panel.inputFields.length - 2; i++) {
                    if (panel.inputFields[i].getText().isEmpty()) {
                        DebugLog.log("Errored call extraction 'Add' clicked while an input field was empty.");
                        return;
                    }
                }

                ExtractText extractText = new ExtractText();
                extractText.name = panel.inputFields[0].getText();
                extractText.startString = Utils.unescapeLineBreaks(panel.inputFields[1].getText());
                extractText.stopString = Utils.unescapeLineBreaks(panel.inputFields[2].getText());
                extractText.selectedText = Utils.unescapeLineBreaks(panel.inputFields[3].getText());
                extractText.urlEncodeDecode = (String) panel.dropDown.getSelectedItem();
                extractText.extractFromResponse = panel.radioButtons[0].isSelected();
                extractText.extractFromRequest = panel.radioButtons[1].isSelected();

                panel.tableModel.addRow(extractText);
                Utils.resizeColumnsToFitContent(panel.table);

                DebugLog.log("Errored call extraction added: " + extractText.debugString());
            }
            catch (Exception ex) {
                DebugLog.log("[Error] ErrorDetectionController: Adding errored call extraction failed. " + ex);
            }
        };
        panel.addButton.addActionListener(addButtonListener);
    }

    // Stores error conditions into an array list. As error conditions are stored, populates table with a list of error
    // conditions.
    private void storeErrorConditions(){
        // Get conditions type from drop down menu.
        ActionListener submitButtonListener = e -> {
            try {
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

                // Header needs a name. The value is optional.
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
            }
            catch (Exception ex) {
                DebugLog.log("[Error] ErrorDetectionController: Adding error condition failed. " + ex);
            }
        };
        errorDetectionPanel.submitButton.addActionListener(submitButtonListener);
    }


    // Shares Utils.setTableDeleteContextMenu(), with this table's dropdown refresh as the follow-up.
    private void createErrorConditionTableContextMenu() {
        Utils.setTableDeleteContextMenu(errorDetectionPanel.errorConditionTable, this::refreshDropdowns);
    }

    // Rebuilds the trigger condition panel's error condition dropdowns.
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