import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CorrectiveCallsController {
    private CorrectiveCallsPanel correctiveCallsPanel;
    public List<HttpRequestResponse> correctiveCalls;
    private int callNumber;
    
    CorrectiveCallsController(CorrectiveCallsPanel correctiveCallsPanel, Model model) {
        this.correctiveCallsPanel = correctiveCallsPanel;
        callNumber = 0;
        saveCallSequence();
        callSequenceTableContextMenu();
        storeCorrectiveCalls();
    }

    private void storeCorrectiveCalls(){

    }

    private void saveCallSequence() {
        disableSaveCallSequence(); // Disable by default
        onRadioButton();
    }

    private void onRadioButton() {
        correctiveCallsPanel.disableRadioButton.addActionListener(e -> disableSaveCallSequence());
        correctiveCallsPanel.enableRadioButton.addActionListener(e -> enableSaveCallSequence());
    }

    private void disableSaveCallSequence() {
        correctiveCallsPanel.saveButton.setEnabled(false);
        correctiveCallsPanel.removeButton.setEnabled(false);
    }

    private void enableSaveCallSequence() {
        correctiveCallsPanel.saveButton.setEnabled(true);
        correctiveCallsPanel.removeButton.setEnabled(true);
    }

    private void callSequenceTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = correctiveCallsPanel.callSequenceTable.getSelectedRow();
            if (selectedRow != -1){
                correctiveCallsPanel.callSequenceTableModel.removeRow(selectedRow);

                // Some data structure which stores each call
                correctiveCalls.remove(selectedRow);

                // If last row is deleted, reset numberOfConditions count.
                if (selectedRow == 1){
                    callNumber = 0;
                }
            }
        });

        popupMenu.add(deleteItem);

        // Displays popup menu and selects row.
        correctiveCallsPanel.callSequenceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e){
                if (e.isPopupTrigger()){
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = correctiveCallsPanel.callSequenceTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    correctiveCallsPanel.callSequenceTable.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

}                    

