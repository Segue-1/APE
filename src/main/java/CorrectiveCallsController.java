import burp.api.montoya.http.message.HttpRequestResponse;

import burp.api.montoya.ui.editor.Editor;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;


public class CorrectiveCallsController {
    private CorrectiveCallSetTableModel correctiveCallSetTableModel;
    private JTable correctiveCallsSetsTable;

    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;

    private JRadioButton singleSetRadioButton;
    private JRadioButton multiSetRadioButton;

    private JButton addSetBotton;
    private JButton removeSetBotton;

    public JButton extractAddButton;
    public JButton replaceAddButton;

    public ExtractionTableModel extractionTableModel;
    public JTable extractionTable;

    public ReplacementTableModel replacementTableModel;
    public JTable replacementTable;

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

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
                requestEditor, extractionTableModel, callSequenceTableModel, callSequenceTable, model);

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
        addSetBotton.setEnabled(false);
        removeSetBotton.setEnabled(false);
        correctiveCallsSetsTable.setRowSelectionInterval(0, 0);
        correctiveCallsSetsTable.setEnabled(false);
    }

    private void multiSet() {
        addSetBotton.setEnabled(true);
        removeSetBotton.setEnabled(true);
        correctiveCallsSetsTable.setEnabled(true);
    }

    private void setCallSetsButtonActionListeners() {
        ActionListener addButtonListener = e -> {
            correctiveCallSetTableModel.addRow(new CorrectiveCallSet());
            Utils.resizeColumnsToFitContent(correctiveCallsSetsTable);
            DebugLog.log("Add button clicked on corrective call set table. Call set "
                    + correctiveCallSetTableModel.correctiveCallSetList.getLast().name
                    + " has been added");
        };

        ActionListener removeBottonListener = e -> {
            int selectedRow = correctiveCallsSetsTable.getSelectedRow();

            if (selectedRow == -1) {
                DebugLog.log("Remove button clicked on corrective call set table with no set selected.");
                return;
            }

            // CorrectiveCallSetTableModel.removeRow() refuses to delete Set 0, so stop here rather than
            // carrying on and moving the selection for a removal that did not happen.
            if (selectedRow == 0) {
                DebugLog.log("Set 0 cannot be removed. Remove button ignored.");
                return;
            }

            // Read the name BEFORE removing the row. This used to be logged afterwards, which named the
            // wrong set, or threw IndexOutOfBounds when the last row was the one removed.
            String removedName = correctiveCallSetTableModel.getCorrectiveCallsSet(selectedRow).name;

            correctiveCallSetTableModel.removeRow(selectedRow);
            Utils.resizeColumnsToFitContent(correctiveCallsSetsTable);

            /*
            Move to the previous row and load its calls. selectedRow is at least 1 here, so
            selectedRow - 1 is a valid row; the old code could be handed row 0 and call
            setRowSelectionInterval(-1, -1), which throws.

            Loading explicitly rather than relying on fireTableDataChanged(), because the call sequence
            table model still pointed at the deleted set's list and would redraw it.
             */
            int newSelection = selectedRow - 1;
            correctiveCallsSetsTable.setRowSelectionInterval(newSelection, newSelection);
            callSequenceTableModel.loadCallSequence(
                    correctiveCallSetTableModel.getCorrectiveCallsSet(newSelection).correctiveCallList);

            DebugLog.log("Remove button clicked on corrective call set table. Call set "
                    + removedName + " has been deleted.");
        };
        addSetBotton.addActionListener(addButtonListener);
        removeSetBotton.addActionListener(removeBottonListener);
    }

    // TODO rename: poor naming between the following two methods
    private void setSelectedCorrectiveCallsSetListener() {
        correctiveCallsSetsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = correctiveCallsSetsTable.getSelectedRow();

                if (selectedRow != -1) {
                    callSequenceTableModel.loadCallSequence(correctiveCallSetTableModel.correctiveCallSetList.get(selectedRow).correctiveCallList);
                    DebugLog.log("Corrective call set selected. Loaded to call sequence table: " +
                            correctiveCallSetTableModel.correctiveCallSetList.get(selectedRow).toString());
                }
            }
        });
    }

    private void setSelectedCorrectiveCallCallListener() {
        callSequenceTable.getSelectionModel().addListSelectionListener(e -> {
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
        });
    }

    private void setAttributes(CorrectiveCallsPanel correctiveCallsPanel) {
        correctiveCallSetTableModel = correctiveCallsPanel.correctiveCallSetTableModel;
        correctiveCallsSetsTable = correctiveCallsPanel.correctiveCallsSetsTable;

        callSequenceTableModel = correctiveCallsPanel.callSequenceTableModel;
        callSequenceTable = correctiveCallsPanel.callSequenceTable;

        singleSetRadioButton = correctiveCallsPanel.singleSetRadionButton;
        multiSetRadioButton = correctiveCallsPanel.multiSetRadioButton;

        addSetBotton = correctiveCallsPanel.addSetBotton;
        removeSetBotton = correctiveCallsPanel.removeSetBotton;

        extractAddButton = correctiveCallsPanel.extractionPanel.addButton;
        replaceAddButton = correctiveCallsPanel.replacementPanel.addButton;

        extractionTableModel = correctiveCallsPanel.extractionPanel.tableModel;
        replacementTableModel = correctiveCallsPanel.replacementPanel.tableModel;

        extractionTable = correctiveCallsPanel.extractionPanel.table;
        replacementTable = correctiveCallsPanel.replacementPanel.table;

        requestEditor = correctiveCallsPanel.requestEditor;
        responseEditor = correctiveCallsPanel.responseEditor;
    }
}

