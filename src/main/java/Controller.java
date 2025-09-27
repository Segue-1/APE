import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    private MontoyaApi montoyaApi;
    private Model model;
    private View view;

    // Settings related
    public EnumSet<ToolType> enabledTools;

    public Controller(MontoyaApi montoyaApi){
        this.montoyaApi = montoyaApi;
        model = new Model(montoyaApi);
        view = new View(montoyaApi);

        getSettings();
    }

    
    private void getSettings() {
        getScopeCheckBoxStates();
    }
    
    private void getScopeCheckBoxStates () {

        // Gets the parent panel of the checkboxes.
        JPanel checkBoxGrid = ((JPanel) ((JPanel) ((JPanel) ((JTabbedPane) view.mainPanel.
                getComponent(0)).getComponent(4)).getComponent(1)).getComponent(0));

        // Setup a listener
        ItemListener scopeCheckBoxListener = e -> {
            JCheckBox source = (JCheckBox) e.getSource();
            String checkBoxText = source.getText().toUpperCase();
            boolean isSelected = source.isSelected();

            montoyaApi.logging().logToOutput("Checkbox: " + checkBoxText + " was clicked on and set to " + isSelected);
            if (isSelected){
                enabledTools.add(ToolType.INTRUDER);
                //montoyaApi.logging().logToOutput(enabledTools);
            }

        };

        // Attach the listener to each checkbox
        for (Component c : checkBoxGrid.getComponents()) {
            JCheckBox cb = (JCheckBox) c;
            cb.addItemListener(scopeCheckBoxListener);
        }

        /*
        for (Map.Entry<String, Boolean> entry : scopeCheckBoxStates.entrySet()){
            montoyaApi.logging().logToOutput(entry);
        }
        */
    }
}