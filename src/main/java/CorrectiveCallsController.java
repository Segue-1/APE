import burp.api.montoya.http.message.HttpRequestResponse;

import burp.api.montoya.repeater.HttpEditor;
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

    public JButton extractFromSelect;
    public JButton replaceFromSelect;

    public JTextField[] extractInputFields;
    public JTextField[] replaceInputFields;

    public ExtractionTableModel extractionTableModel;
    public JTable extractionTable;

    public ReplacementTableModel replacementTableModel;
    public JTable replacementTable;

    public JComboBox<String> extractDropDown;
    public JComboBox<ExtractText> replaceDropDown;

    public HttpEditor httpEditor;

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    private ReplacementController replacementController;
    private ExtractionController extractionController;


    CorrectiveCallsController(CorrectiveCallsPanel correctiveCallsPanel, Model model) {
        setAttributes(correctiveCallsPanel);

        setRadioButtons();
        setTableDeleteContextMenu(callSequenceTable);
        setTableDeleteContextMenu(extractionTable);
        setTableDeleteContextMenu(replacementTable);
        setCallSetsButtonActionListeners();
        setSelectedCorrectiveCallsSetListener();
        setSelectedCorrectiveCallCallListener();
        initCallSequenceTable();
        setEditorFocusListener();

        replacementController = new ReplacementController(correctiveCallsPanel.replacementPanel,
                requestEditor, extractionTableModel, callSequenceTableModel, callSequenceTable);

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

    private void setTableDeleteContextMenu(JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                DeletableTableModel model = (DeletableTableModel) table.getModel();
                DebugLog.log(table.getName() + ": User deleted row " + selectedRow);
                model.removeRow(selectedRow);
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
            correctiveCallSetTableModel.removeRow(selectedRow);
            callSequenceTableModel.fireTableDataChanged();
            Utils.resizeColumnsToFitContent(correctiveCallsSetsTable);

            correctiveCallsSetsTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);

            DebugLog.log("Remove button clicked on corrective call set table. Call set "
                    + correctiveCallSetTableModel.correctiveCallSetList.get(selectedRow).name
                    + " has been deleted.");
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

    private void setEditorFocusListener() {
        MouseAdapter requestListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                responseEditor.setCaretPosition(0);
                DebugLog.log("Mouse pressed on request editor.");
                DebugLog.log("Caret position of request editor: " + requestEditor.caretPosition());
                DebugLog.log("Caret position of response editor: " + responseEditor.caretPosition());
            }
        };

        MouseAdapter responseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestEditor.setCaretPosition(0);
                DebugLog.log("Mouse pressed on response editor.");
                DebugLog.log("Caret position of request editor: " + requestEditor.caretPosition());
                DebugLog.log("Caret position of response editor: " + responseEditor.caretPosition());
            }
        };

        // Very hacky way of attaching a mouse listener to the text field within montoya http editors
        // Iterates through all components of the editor and looks for a JTextComponent with a parent JViewport
        try {
            boolean found = false;
            Deque<Component> stack = new ArrayDeque<>();
            stack.push(requestEditor.uiComponent());
            while (!stack.isEmpty()) {
                Component c = stack.pop();
                if (c instanceof JTextComponent && c.getParent() instanceof JViewport) {
                    c.addMouseListener(requestListener);
                    found = true;
                }
                if (c instanceof Container) {
                    for (Component child : ((Container) c).getComponents()) stack.push(child);
                }
            }
            if (!found)
                DebugLog.log("Corrective Call Controller: Could not attach listener to request editor text component.");
        } catch (Exception ex) {
            DebugLog.log("Corrective Call Controller: Error attaching request editor listener: " + ex);
        }

        try {
            boolean found = false;
            Deque<Component> stack = new ArrayDeque<>();
            stack.push(responseEditor.uiComponent());
            while (!stack.isEmpty()) {
                Component c = stack.pop();
                if (c instanceof JTextComponent && c.getParent() instanceof JViewport) {
                    DebugLog.log("Component name " + c.getName());
                    c.addMouseListener(responseListener);
                    found = true;
                }
                if (c instanceof Container) {
                    for (Component child : ((Container) c).getComponents()) stack.push(child);
                }
            }
            if (!found)
                DebugLog.log("Corrective Call Controller: Could not attach listener to response editor text component.");
        } catch (Exception ex) {
            DebugLog.log("Corrective Call Controller: Error attaching response editor listener: " + ex);
        }
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

        extractFromSelect = correctiveCallsPanel.extractFromSelect;
        replaceFromSelect = correctiveCallsPanel.replaceFromSelect;

        extractInputFields = correctiveCallsPanel.extractInputFields;
        replaceInputFields = correctiveCallsPanel.replaceInputFields;

        extractionTableModel = correctiveCallsPanel.extractionPanel.tableModel;
        replacementTableModel = correctiveCallsPanel.replacementPanel.tableModel;

        extractDropDown = correctiveCallsPanel.extractDropDown;
        replaceDropDown = correctiveCallsPanel.replaceDropDown;

        extractionTable = correctiveCallsPanel.extractionPanel.table;
        replacementTable = correctiveCallsPanel.replacementPanel.table;

        requestEditor = correctiveCallsPanel.requestEditor;
        responseEditor = correctiveCallsPanel.responseEditor;
    }
}

