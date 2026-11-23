import burp.api.montoya.core.ToolType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.EnumSet;

public class SettingsController {
    private SettingsPanel settingsPanel;
    private JPanel checkBoxes;
    private Model model;
    private EnumSet<ToolType> enabledTools;


    public SettingsController(SettingsPanel settingsPanel, Model model) {
        this.settingsPanel = settingsPanel;
        this.checkBoxes = settingsPanel.checkBoxGrid;
        this.model = model;

        setEnabledToolsListener();
    }

    // Tracks the state of the checkboxes in the settings menu for scope
    private void setEnabledToolsListener () {
        // Stores state of checkboxes
        enabledTools = EnumSet.noneOf(ToolType.class);

        // Set up a listener which will track checkbox state in enabledTools
        ItemListener checkBoxListener = e -> {
            try {
                JCheckBox source = (JCheckBox) e.getSource();
                ToolType toolType = (ToolType) source.getClientProperty("ToolType");

                if (source.isSelected()) {
                    enabledTools.add(toolType);
                    DebugLog.log("ToolType " + toolType.toString() + " enabled in settings menu.");
                } else {
                    enabledTools.remove(toolType);
                    DebugLog.log("ToolType " + toolType.toString() + " disabled in settings menu.");
                }
                model.updateEnabledTools(enabledTools);
            }
            catch (Exception ex) {
                DebugLog.log("[Error] SettingsController: Updating enabled tools failed. " + ex);
            }
        };

        // Attach the listener to each checkbox and store initial state of checkboxes
        for (Component component : checkBoxes.getComponents()) {
            JCheckBox checkBox = (JCheckBox) component;
            checkBox.addItemListener(checkBoxListener);

            if (checkBox.isSelected()) {
                ToolType toolType = (ToolType) checkBox.getClientProperty("ToolType");
                enabledTools.add(toolType);
                model.updateEnabledTools(enabledTools);
            }
        }
    }
}
