import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
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

    public Controller (MontoyaApi montoyaApi){
        this.montoyaApi = montoyaApi;
        model = new Model(montoyaApi);
        view = new View(montoyaApi);

        initSettingsPanel();
        initContextMenus();
        storeErrorConditions();
    }
    
    private void initSettingsPanel() {
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
                ErrorDetectionPanel.errorDetectionRequestEditor.setRequest(reqResp.request());
                ErrorDetectionPanel.errorDetectionResponseEditor.setResponse(reqResp.response());
            }
        });
        return List.of(view.contextMenus.errorDetection, view.contextMenus.correctiveCalls);
    }

    // Loads context menus. Perhaps consider using a nested class or anonymous class
    // for ContextMenuItemsProvider and pass that only.
    private void initContextMenus() {
        montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }

    private void storeErrorConditions(){
        errorConditions = new ArrayList<>();

        // Get conditions type from drop down menu.
        ActionListener submitButtonListener = e -> {
            String selectedItem = (String) ErrorDetectionPanel.dropdown.getSelectedItem();
            String textInput = ErrorDetectionPanel.conditionTextField.getText();
            errorConditions.add(new String[]{selectedItem, textInput});
            montoyaApi.logging().logToOutput("Submit button clicked!\n");
            errorConditions.forEach(pair -> montoyaApi.logging().logToOutput(Arrays.toString(pair)));
            populateErrorDetectionTable(selectedItem, textInput);
        };
        ErrorDetectionPanel.submitButton.addActionListener(submitButtonListener);
    }

    // Populates the table on the bottom left hand side of the Error Detection tab.
    private void populateErrorDetectionTable(String category, String input) {
        String numberOfConditions = String.valueOf(ErrorDetectionPanel.tableModel.getRowCount());
        String name = "Condition-" + numberOfConditions;
        String description;

        if (category.equals("Status Code")){
            description = "APE will trigger if Status Code is " + input;
        }
        else if (category.equals("Body")) {
            description = "APE will trigger if text '" + input + " is found in response Body";
        }
        else if (category.equals("Header")) {
            description = "APE will trigger if text '" + input + " is found in response Header";
        }
        else{
            description = "How did you do that?";
        }


        ErrorDetectionPanel.tableModel.addRow(new Object[]{name, category, input, description});
    }
}