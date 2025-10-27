import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;


public class CorrectiveCallsController {
    private CorrectiveCallsSetsTableModel correctiveCallsSetsTableModel;
    private JTable correctiveCallsSetsTable;

    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;

    private JRadioButton singleSetRadioButton;
    private JRadioButton multiSetRadioButton;

    private JButton addBotton;
    private JButton removeButton;
    

    CorrectiveCallsController(CorrectiveCallsPanel correctiveCallsPanel, Model model) {
        correctiveCallsSetsTableModel = correctiveCallsPanel.correctiveCallsSetsTableModel;
        correctiveCallsSetsTable = correctiveCallsPanel.correctiveCallsSetsTable;

        callSequenceTableModel = correctiveCallsPanel.callSequenceTableModel;
        callSequenceTable = correctiveCallsPanel.callSequenceTable;

        singleSetRadioButton = correctiveCallsPanel.singleSetRadionButton;
        multiSetRadioButton = correctiveCallsPanel.multiSetRadioButton;

        addBotton = correctiveCallsPanel.addBotton;
        removeButton = correctiveCallsPanel.removeButton;

        setRadioButtons();
        setCallSequenceTableContextMenu();
        setButtonActionListeners();
        setSelectedCorrectiveCallsSetListener();
        loadSet0();
    }

    private void loadSet0() {
        List<HttpRequestResponse> set0 = correctiveCallsSetsTableModel.correctiveCallsSets.getFirst().callSequence;
        callSequenceTableModel.loadCallSequence(set0);
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
        addBotton.setEnabled(false);
        removeButton.setEnabled(false);
        correctiveCallsSetsTable.setRowSelectionInterval(0, 0);
        correctiveCallsSetsTable.setEnabled(false);
    }

    private void multiSet() {
        addBotton.setEnabled(true);
        removeButton.setEnabled(true);
        correctiveCallsSetsTable.setEnabled(true);
    }

    private void setCallSequenceTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = callSequenceTable.getSelectedRow();
            if (selectedRow != -1) {
                callSequenceTableModel.removeRow(selectedRow);
            }
        });

        popupMenu.add(deleteItem);

        // Displays popup menu and selects row.
        callSequenceTable.addMouseListener(new MouseAdapter() {
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
                int row = callSequenceTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    callSequenceTable.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void setButtonActionListeners() {
        ActionListener addButtonListener = e -> {
            correctiveCallsSetsTableModel.addRow(new CorrectiveCallsSet());
            Utils.resizeColumnsToFitContent(correctiveCallsSetsTable);
        };

        ActionListener removeButtonListener = e -> {
            int selectedRow = correctiveCallsSetsTable.getSelectedRow();
            correctiveCallsSetsTableModel.removeRow(selectedRow);
            callSequenceTableModel.fireTableDataChanged();
            Utils.resizeColumnsToFitContent(correctiveCallsSetsTable);
            correctiveCallsSetsTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        };
        addBotton.addActionListener(addButtonListener);
        removeButton.addActionListener(removeButtonListener);
    }

    private void setSelectedCorrectiveCallsSetListener() {
        correctiveCallsSetsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = correctiveCallsSetsTable.getSelectedRow();

                if (selectedRow != -1) {
                    callSequenceTableModel.loadCallSequence(correctiveCallsSetsTableModel.correctiveCallsSets.get(selectedRow).callSequence);
                    Controller.montoyaApi.logging().logToOutput("\n------ FROM SELECTION -----");
                    Controller.montoyaApi.logging().logToOutput(correctiveCallsSetsTableModel.correctiveCallsSets.get(selectedRow).toString());
                }
            }
        });
    }
}

