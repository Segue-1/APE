import javax.naming.ldap.Control;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class ErrorDetectionController {
    private Controller controller;
    public ArrayList<String[]> errorConditions;
    private int numberOfConditions = 0;

    ErrorDetectionController(Controller controller){
        this.controller = controller;
        storeErrorConditions();
        createErrorConditionTableContextMenu();
    }

    // Stores error conditions into an array list.
    private void storeErrorConditions(){
        errorConditions = new ArrayList<>();

        // Get conditions type from drop down menu.
        ActionListener submitButtonListener = e -> {
            String selectedItem = (String) controller.view.errorDetectionPanel.dropdown.getSelectedItem();
            String textInput = controller.view.errorDetectionPanel.conditionTextField.getText();

            if (selectedItem.equals("Status Code") && (textInput == null || !textInput.matches("\\d{3}"))){
                return;
            }

            errorConditions.add(new String[]{selectedItem, textInput});
            Controller.montoyaApi.logging().logToOutput("Submit button clicked!");
            errorConditions.forEach(pair -> Controller.montoyaApi.logging().logToOutput(Arrays.toString(pair)));
            numberOfConditions++;
            populateErrorDetectionTable(selectedItem, textInput);
        };
        controller.view.errorDetectionPanel.submitButton.addActionListener(submitButtonListener);
    }

    // Populates the table on the bottom left hand side of the Error Detection tab.
    private void populateErrorDetectionTable(String category, String input) {
        /* TODO numberofConditions is still broken, create a list or something to track. If adding a bunch of conditions
           and something in the middle is deleted, then more conditions are added, the wrong number will appear. When
           deleting, you could decrement every after that number by 1. */
        String name = "Condition-" + numberOfConditions;
        String description;

        if (category.equals("Status Code")){
            description = "APE will trigger if Status Code is " + input;
        }
        else if (category.equals("Body")) {
            description = "APE will trigger if text '" + input + "' is found in response Body";
        }
        else if (category.equals("Header")) {
            description = "APE will trigger if text '" + input + "' is found in response Header";
        }
        else{
            description = "How did you do that?";
        }
        controller.view.errorDetectionPanel.tableModel.addRow(new Object[]{name, category, input, description});
        controller.view.errorDetectionPanel.resizeColumnsToFitContent();
    }

    // Create context menu for bottom left table to delete entries
    private void createErrorConditionTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = controller.view.errorDetectionPanel.table.getSelectedRow();
            if (selectedRow != -1){
                controller.view.errorDetectionPanel.tableModel.removeRow(selectedRow);
                controller.view.errorDetectionPanel.resizeColumnsToFitContent();

                Controller.montoyaApi.logging().logToOutput("Delete button clicked!");
                errorConditions.remove(selectedRow);

                errorConditions.forEach(pair -> Controller.montoyaApi.logging().logToOutput(Arrays.toString(pair)));

                // If last row is deleted, reset numberOfConditions count.
                if (selectedRow == 1){
                    numberOfConditions = 0;
                }
            }
        });

        popupMenu.add(deleteItem);

        // Displays popup menu and selects row.
        controller.view.errorDetectionPanel.table.addMouseListener(new MouseAdapter() {
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
                int row = controller.view.errorDetectionPanel.table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    controller.view.errorDetectionPanel.table.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
}