import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

public class ErrorDetectionController {
    private ErrorDetectionPanel errorDetectionPanel;
    public ArrayList<String[]> errorConditions;
    public HttpRequestResponse errorCall;

    // TODO implement custom table model. no need to track numberOfConditions, just use index of data structure in custom table model
    private int numberOfConditions = 0;

    ErrorDetectionController(ErrorDetectionPanel errorDetectionPanel, Model model){
        this.errorDetectionPanel = errorDetectionPanel;
        storeErrorConditions();
        createErrorConditionTableContextMenu();
    }

    // Stores error conditions into an array list. As error conditions are stored, populates table with a list of error
    // conditions.
    private void storeErrorConditions(){
        errorConditions = new ArrayList<>();

        // Get conditions type from drop down menu.
        ActionListener submitButtonListener = e -> {
            String selectedItem = (String) errorDetectionPanel.dropdown.getSelectedItem();
            String textInput = errorDetectionPanel.conditionTextField.getText();

            if (selectedItem.equals("Status Code") && (textInput == null || !textInput.matches("\\d{3}"))){
                return;
            }

            errorConditions.add(new String[]{selectedItem, textInput});
            errorConditions.forEach(pair -> Controller.montoyaApi.logging().logToOutput(Arrays.toString(pair)));
            numberOfConditions++;
            populateErrorDetectionTable(selectedItem, textInput);
        };
        errorDetectionPanel.submitButton.addActionListener(submitButtonListener);
    }

    // Populates the table on the bottom left hand side of the Error Detection tab.
    private void populateErrorDetectionTable(String category, String input) {
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
        errorDetectionPanel.tableModel.addRow(new Object[]{name, category, input, description});
        errorDetectionPanel.resizeColumnsToFitContent();
    }

    // Create context menu for bottom left table to delete entries
    private void createErrorConditionTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = errorDetectionPanel.table.getSelectedRow();
            if (selectedRow != -1){
                errorDetectionPanel.tableModel.removeRow(selectedRow);
                errorDetectionPanel.resizeColumnsToFitContent();

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
        errorDetectionPanel.table.addMouseListener(new MouseAdapter() {
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
                int row = errorDetectionPanel.table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    errorDetectionPanel.table.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
}