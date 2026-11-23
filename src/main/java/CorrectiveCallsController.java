import burp.api.montoya.http.message.HttpRequestResponse;

import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;


public class CorrectiveCallsController {
    private CorrectiveCallSetTableModel correctiveCallSetTableModel;
    private JTable correctiveCallsSetsTable;

    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;

    private JRadioButton singleSetRadioButton;
    private JRadioButton multiSetRadioButton;

    private JButton addSetButton;
    private JButton removeSetButton;


    private ExtractionTableModel extractionTableModel;
    private JTable extractionTable;

    private ReplacementTableModel replacementTableModel;
    private JTable replacementTable;

    private HttpRequestEditor requestEditor;
    private HttpResponseEditor responseEditor;

    private ReplacementController replacementController;
    private ExtractionController extractionController;

    // Needed so ReplacementController can offer the errored call's extractions in its dropdown.
    private Model model;


    CorrectiveCallsController(CorrectiveCallsPanel correctiveCallsPanel, Model model) {
        this.model = model;
        setAttributes(correctiveCallsPanel);

        setRadioButtons();
        Utils.setTableDeleteContextMenu(callSequenceTable, null);
        Utils.setTableDeleteContextMenu(extractionTable, null);
        Utils.setTableDeleteContextMenu(replacementTable, null);
        setCallSetsButtonActionListeners();
        setSelectedCorrectiveCallsSetListener();
        setSelectedCorrectiveCallCallListener();
        initCallSequenceTable();
        Utils.pairEditorCarets(requestEditor, responseEditor, "Corrective Calls");

        replacementController = new ReplacementController(correctiveCallsPanel.replacementPanel,
                requestEditor, callSequenceTableModel, callSequenceTable, model);

        extractionController = new ExtractionController(correctiveCallsPanel.extractionPanel,
                requestEditor, responseEditor, callSequenceTableModel, callSequenceTable);

    }

    private void initCallSequenceTable() {
        CorrectiveCallSet set0 = correctiveCallSetTableModel.correctiveCallSetList.getFirst();
        callSequenceTableModel.loadCallSequence(set0.correctiveCallList);
    }

    private void setRadioButtons() {
        singleSet(); // Single set by default
        onRadioButton();
    }

    private void onRadioButton() {
        singleSetRadioButton.addActionListener(e -> singleSet());
        multiSetRadioButton.addActionListener(e -> multiSet());
    }

    private void singleSet() {
        addSetButton.setEnabled(false);
        removeSetButton.setEnabled(false);
        correctiveCallsSetsTable.setRowSelectionInterval(0, 0);
        correctiveCallsSetsTable.setEnabled(false);
    }

    private void multiSet() {
        addSetButton.setEnabled(true);
        removeSetButton.setEnabled(true);
        correctiveCallsSetsTable.setEnabled(true);
    }

    private void setCallSetsButtonActionListeners() {
        ActionListener addButtonListener = e -> {
            try {
                CorrectiveCallSet newSet = new CorrectiveCallSet();

                // A set added after Send to Error Detection gets its own followup call too.
                if (model.errorCall.requestResponse != null) {
                    newSet.followupCall = new CorrectiveCall(model.errorCall.requestResponse);
                }

                correctiveCallSetTableModel.addRow(newSet);
                DebugLog.log("Add button clicked on corrective call set table. Call set "
                        + correctiveCallSetTableModel.correctiveCallSetList.getLast().name
                        + " has been added");
            }
            catch (Exception ex) {
                DebugLog.log("[Error] CorrectiveCallsController: Adding corrective call set failed. " + ex);
            }
        };

        ActionListener removeButtonListener = e -> {
            try {
                int selectedRow = correctiveCallsSetsTable.getSelectedRow();

                if (selectedRow == -1) {
                    DebugLog.log("Remove button clicked on corrective call set table with no set selected.");
                    return;
                }

                // Set 0 can't be removed.
                if (selectedRow == 0) {
                    DebugLog.log("Set 0 cannot be removed. Remove button ignored.");
                    return;
                }

                // Read before the row is removed.
                String removedName = correctiveCallSetTableModel.getCorrectiveCallsSet(selectedRow).name;

                correctiveCallSetTableModel.removeRow(selectedRow);

                // Select the previous set and load its calls. The call sequence table still shows the deleted set.
                int newSelection = selectedRow - 1;
                correctiveCallsSetsTable.setRowSelectionInterval(newSelection, newSelection);
                callSequenceTableModel.loadCallSequence(
                        correctiveCallSetTableModel.getCorrectiveCallsSet(newSelection).correctiveCallList);

                DebugLog.log("Remove button clicked on corrective call set table. Call set "
                        + removedName + " has been deleted.");
            }
            catch (Exception ex) {
                DebugLog.log("[Error] CorrectiveCallsController: Removing corrective call set failed. " + ex);
            }
        };
        addSetButton.addActionListener(addButtonListener);
        removeSetButton.addActionListener(removeButtonListener);
    }

    // TODO rename: poor naming between the following two methods
    private void setSelectedCorrectiveCallsSetListener() {
        correctiveCallsSetsTable.getSelectionModel().addListSelectionListener(e -> {
            try {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = correctiveCallsSetsTable.getSelectedRow();

                    if (selectedRow != -1) {
                        callSequenceTableModel.loadCallSequence(correctiveCallSetTableModel.correctiveCallSetList.get(selectedRow).correctiveCallList);
                        DebugLog.log("Corrective call set selected. Loaded to call sequence table: " +
                                correctiveCallSetTableModel.correctiveCallSetList.get(selectedRow).toString());

                        // Select the set's first call, which loads its editors, extractions and replacements.
                        // An empty set clears them instead of leaving the previous set's showing.
                        if (callSequenceTableModel.getRowCount() > 0) {
                            callSequenceTable.setRowSelectionInterval(0, 0);
                        }
                        else {
                            requestEditor.setRequest(null);
                            responseEditor.setResponse(null);
                            extractionTableModel.loadExtractList(new ArrayList<>());
                            replacementTableModel.loadReplaceList(new ArrayList<>());
                        }
                    }
                }
            }
            catch (Exception ex) {
                DebugLog.log("[Error] CorrectiveCallsController: Loading selected corrective call set failed. " + ex);
            }
        });
    }

    private void setSelectedCorrectiveCallCallListener() {
        callSequenceTable.getSelectionModel().addListSelectionListener(e -> {
            try {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = callSequenceTable.getSelectedRow();

                    if (selectedRow != -1) {
                        HttpRequestResponse reqResp = callSequenceTableModel.getReqRes(selectedRow);
                        requestEditor.setRequest(reqResp.request());
                        responseEditor.setResponse(reqResp.response());
                        DebugLog.log("Corrective call selected. Loaded call at row " + selectedRow + " to http editors.");
                        extractionTableModel.loadExtractList(callSequenceTableModel.getCorrectiveCall(selectedRow).extractList);
                        replacementTableModel.loadReplaceList(callSequenceTableModel.getCorrectiveCall(selectedRow).replaceList);
                        replacementController.refreshDropDown();
                    }
                }
            }
            catch (Exception ex) {
                DebugLog.log("[Error] CorrectiveCallsController: Loading selected corrective call failed. " + ex);
            }
        });
    }

    private void setAttributes(CorrectiveCallsPanel correctiveCallsPanel) {
        correctiveCallSetTableModel = correctiveCallsPanel.correctiveCallSetTableModel;
        correctiveCallsSetsTable = correctiveCallsPanel.correctiveCallsSetsTable;

        callSequenceTableModel = correctiveCallsPanel.callSequenceTableModel;
        callSequenceTable = correctiveCallsPanel.callSequenceTable;

        singleSetRadioButton = correctiveCallsPanel.singleSetRadioButton;
        multiSetRadioButton = correctiveCallsPanel.multiSetRadioButton;

        addSetButton = correctiveCallsPanel.addSetButton;
        removeSetButton = correctiveCallsPanel.removeSetButton;


        extractionTableModel = correctiveCallsPanel.extractionPanel.tableModel;
        replacementTableModel = correctiveCallsPanel.replacementPanel.tableModel;

        extractionTable = correctiveCallsPanel.extractionPanel.table;
        replacementTable = correctiveCallsPanel.replacementPanel.table;

        requestEditor = correctiveCallsPanel.requestEditor;
        responseEditor = correctiveCallsPanel.responseEditor;
    }
}

