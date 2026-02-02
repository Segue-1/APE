import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class ErrorDetectionController {
    private ErrorDetectionPanel errorDetectionPanel;
    public HttpRequestResponse errorCall; // not used

    private TriggerConditionController triggerConditionController;


    ErrorDetectionController(ErrorDetectionPanel errorDetectionPanel, Model model){
        this.errorDetectionPanel = errorDetectionPanel;
        storeErrorConditions();
        createErrorConditionTableContextMenu();
    }

    // Stores error conditions into an array list. As error conditions are stored, populates table with a list of error
    // conditions.
    private void storeErrorConditions(){
        // Get conditions type from drop down menu.
        ActionListener submitButtonListener = e -> {
            String selectedItem = (String) errorDetectionPanel.dropdown.getSelectedItem();
            String input = errorDetectionPanel.conditionTextField.getText();

            if (selectedItem.equals("Status Code") && (input == null || !input.matches("\\d{3}"))){
                DebugLog.log("User attempted to submit an invalid status code. " +
                             "Status code cannot be null and must be 3 digits");
                DebugLog.log("input: " + input);
                return;
            }
            errorDetectionPanel.errorConditionTableModel.addRow(new ErrorCondition(selectedItem, input));
            DebugLog.log("Error condition added. Type: '" + selectedItem + "' input: '" + input + "'");
            Utils.resizeColumnsToFitContent(errorDetectionPanel.errorConditionTable);

            try {
                triggerConditionController.refreshErrorConditionDropdown();
                DebugLog.log("Refreshed TriggerConditionPanel's error condition dropdown menus.");
            }
            catch (Exception ex) {
                DebugLog.log("Failed to refresh TriggerConditionPanel's error condition dropdown menus.");
                DebugLog.log(ex.getMessage());
            }

        };
        errorDetectionPanel.submitButton.addActionListener(submitButtonListener);
    }


    // Create context menu for bottom left table to delete entries
    private void createErrorConditionTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = errorDetectionPanel.errorConditionTable.getSelectedRow();
            if (selectedRow != -1){
                DebugLog.log("User deleted error condition at row " + selectedRow);
                errorDetectionPanel.errorConditionTableModel.removeRow(selectedRow);
                Utils.resizeColumnsToFitContent(errorDetectionPanel.errorConditionTable);
                try {
                    triggerConditionController.refreshErrorConditionDropdown();
                    DebugLog.log("Refreshed TriggerConditionPanel's error condition dropdown menus.");
                }
                catch (Exception ex) {
                    DebugLog.log("Failed to refresh TriggerConditionPanel's error condition dropdown menus.");
                    DebugLog.log(ex.getMessage());
                }
            }
        });

        popupMenu.add(deleteItem);

        // Displays popup menu and selects row.
        errorDetectionPanel.errorConditionTable.addMouseListener(new MouseAdapter() {
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
                int row = errorDetectionPanel.errorConditionTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    errorDetectionPanel.errorConditionTable.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    DebugLog.log(errorDetectionPanel.errorConditionTable.getName() + ": User right clicked row " + row + " and displayed context menu.");
                }
            }
        });
    }

    public void setTriggerConditionController(TriggerConditionController triggerConditionController){
        this.triggerConditionController = triggerConditionController;
    }
}