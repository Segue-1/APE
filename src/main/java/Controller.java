import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


public class Controller implements ContextMenuItemsProvider {
    private MontoyaApi montoyaApi;
    private Model model;
    private View view;

    // Stores which burp suite tools are enabled on the Settings tab
    public EnumSet<ToolType> enabledTools;

    // Stores error conditions set in the Error Conditions tab
    public ArrayList<String[]> errorConditions;
    private int numberOfConditions = 0;

    public Controller (MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        model = new Model(montoyaApi);
        view = new View(montoyaApi);

        // Add listeners, extract data from GUI
        settingsPanel();
        contextMenus();
        errorConditionsPanel();
        correctiveCallPanel();
    }
    
    private void settingsPanel() {
        getScopeCheckBoxStates();
    }

    // Tracks the state of the checkboxes in the settings menu for scope
    // TODO: Maybe consider removing GUI elements and find a way to reference them directly from view.
    private void getScopeCheckBoxStates () {
        // Get the parent panel of the checkboxes.
        JPanel checkBoxGrid = ((JPanel) ((JPanel) ((JPanel) ((JTabbedPane) view.mainPanel.
                getComponent(0)).getComponent(4)).getComponent(1)).getComponent(0));

        // Stores state of checkboxes
        enabledTools = EnumSet.noneOf(ToolType.class);

        // Set up a listener which will track checkbox state in enabledTools
        ItemListener scopeCheckBoxListener = e -> {
            JCheckBox source = (JCheckBox) e.getSource();
            ToolType toolType = (ToolType) source.getClientProperty("ToolType");

            if (source.isSelected()) {
                enabledTools.add(toolType);
            } else {
                enabledTools.remove(toolType);
            }
            model.updateEnabledTools(enabledTools);
        };

        // Attach the listener to each checkbox and store initial state of checkboxes
        for (Component component : checkBoxGrid.getComponents()) {
            JCheckBox checkBox = (JCheckBox) component;
            checkBox.addItemListener(scopeCheckBoxListener);

            if (checkBox.isSelected()) {
                ToolType toolType = (ToolType) checkBox.getClientProperty("ToolType");
                enabledTools.add(toolType);
                model.updateEnabledTools(enabledTools);
            }
        }
    }

    // Sets up context menus (right click menu)
    @Override
    public java.util.List<Component> provideMenuItems(ContextMenuEvent event) {
        view.contextMenus.errorDetection.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = event.messageEditorRequestResponse().get().requestResponse();
                view.errorDetectionPanel.requestEditor.setRequest(reqResp.request());
                view.errorDetectionPanel.responseEditor.setResponse(reqResp.response());
            }
        });
        return List.of(view.contextMenus.errorDetection, view.contextMenus.correctiveCalls);
    }

    // Loads context menus. Perhaps consider using a nested class or anonymous class
    // for ContextMenuItemsProvider and pass that only.
    private void contextMenus() {
        montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }

    private void errorConditionsPanel(){
        storeErrorConditions();
        createErrorConditionTableContextMenu();
    }

    // Stores error conditions into an array list.
    private void storeErrorConditions(){
        errorConditions = new ArrayList<>();

        // Get conditions type from drop down menu.
        ActionListener submitButtonListener = e -> {
            String selectedItem = (String) view.errorDetectionPanel.dropdown.getSelectedItem();
            String textInput = view.errorDetectionPanel.conditionTextField.getText();

            if (selectedItem.equals("Status Code") && (textInput == null || !textInput.matches("\\d{3}"))){
                return;
            }

            errorConditions.add(new String[]{selectedItem, textInput});
            montoyaApi.logging().logToOutput("Submit button clicked!");
            errorConditions.forEach(pair -> montoyaApi.logging().logToOutput(Arrays.toString(pair)));
            numberOfConditions++;
            populateErrorDetectionTable(selectedItem, textInput);
        };
        view.errorDetectionPanel.submitButton.addActionListener(submitButtonListener);
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
        view.errorDetectionPanel.tableModel.addRow(new Object[]{name, category, input, description});
        view.errorDetectionPanel.resizeColumnsToFitContent();
    }
    
    // Create context menu for bottom left table to delete entries
    private void createErrorConditionTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = view.errorDetectionPanel.table.getSelectedRow();
            if (selectedRow != -1){
                view.errorDetectionPanel.tableModel.removeRow(selectedRow);
                view.errorDetectionPanel.resizeColumnsToFitContent();

                montoyaApi.logging().logToOutput("Delete button clicked!");
                errorConditions.remove(selectedRow);

                errorConditions.forEach(pair -> montoyaApi.logging().logToOutput(Arrays.toString(pair)));

                // If last row is deleted, reset numberOfConditions count.
                if (selectedRow == 1){
                    numberOfConditions = 0;
                }
            }
        });

        popupMenu.add(deleteItem);

        // Displays popup menu and selects row.
        view.errorDetectionPanel.table.addMouseListener(new MouseAdapter() {
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
                int row = view.errorDetectionPanel.table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    view.errorDetectionPanel.table.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void correctiveCallPanel() {
        callSequenceRadioButtons();
    }

    //TODO Doesn't do shit, should gray out entire CallSequencePanel
    private void callSequenceRadioButtons() {
        view.correctiveCallsPanel.disableRadioButton.addActionListener(e -> {
            view.correctiveCallsPanel.saveCallSequencePanel.setEnabled(false);
        });

        view.correctiveCallsPanel.enableRadioButton.addActionListener(e -> {
            view.correctiveCallsPanel.saveCallSequencePanel.setEnabled(true);
        });
    }
}