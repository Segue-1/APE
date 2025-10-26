import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class CorrectiveCallsController {
    private CorrectiveCallsPanel correctiveCallsPanel;
    
    CorrectiveCallsController(CorrectiveCallsPanel correctiveCallsPanel, Model model) {
        this.correctiveCallsPanel = correctiveCallsPanel;
        initRadioButtons();
        callSequenceTableContextMenu();
        storeCorrectiveCalls();
        setButtonActionListeners();
        //setSelectedCorrectiveCallsSetListener();
    }

    private void storeCorrectiveCalls(){

    }

    private void initRadioButtons() {
        singleSet(); // Single set by default
        onRadioButton();
    }

    private void onRadioButton() {
        correctiveCallsPanel.singleSetRadionButton.addActionListener(e -> singleSet());
        correctiveCallsPanel.multiSetRadioButton.addActionListener(e -> multiSet());
    }

    private void singleSet() {
        correctiveCallsPanel.addBotton.setEnabled(false);
        correctiveCallsPanel.removeButton.setEnabled(false);
        correctiveCallsPanel.correctiveCallsSetsTable.setRowSelectionInterval(0,0);
        correctiveCallsPanel.correctiveCallsSetsTable.setEnabled(false);
    }

    private void multiSet() {
        correctiveCallsPanel.addBotton.setEnabled(true);
        correctiveCallsPanel.removeButton.setEnabled(true);
        correctiveCallsPanel.correctiveCallsSetsTable.setEnabled(true);
    }

    private void callSequenceTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = correctiveCallsPanel.callSequenceTable.getSelectedRow();
            if (selectedRow != -1){
                correctiveCallsPanel.callSequenceTableModel.removeRow(selectedRow);
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

    private void setButtonActionListeners(){
        ActionListener addButtonListener = e -> {
            correctiveCallsPanel.correctiveCallsSetsTableModel.addRow(new CorrectiveCallsSet());
            Utils.resizeColumnsToFitContent(correctiveCallsPanel.correctiveCallsSetsTable);
        };

        ActionListener removeButtonListener = e -> {
            int selectedRow = correctiveCallsPanel.correctiveCallsSetsTable.getSelectedRow();
            correctiveCallsPanel.correctiveCallsSetsTableModel.removeRow(selectedRow);
            Utils.resizeColumnsToFitContent(correctiveCallsPanel.correctiveCallsSetsTable);
        };
        correctiveCallsPanel.addBotton.addActionListener(addButtonListener);
        correctiveCallsPanel.removeButton.addActionListener(removeButtonListener);
    }

    private void setSelectedCorrectiveCallsSetListener() {
        correctiveCallsPanel.correctiveCallsSetsTable.getSelectionModel().addListSelectionListener(e -> {
            int index = correctiveCallsPanel.correctiveCallsSetsTable.getSelectedRow();
            if (index != -1) {
                correctiveCallsPanel.callSequenceTableModel.loadCorrectiveCalls(correctiveCallsPanel.correctiveCallsSetsTableModel.correctiveCallsSets.get(index).correctiveCalls);
            }
        });
    }

}                    

