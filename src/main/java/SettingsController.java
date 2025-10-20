import burp.api.montoya.core.ToolType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.EnumSet;

public class SettingsController {
    private JPanel checkBoxGrid;

    public SettingsController(SettingsPanel settingsPanel, EnumSet<ToolType> enabledTools) {
        this.checkBoxGrid = settingsPanel.checkBoxGrid;
        getScopeCheckBoxStates();
    }


    // Tracks the state of the checkboxes in the settings menu for scope
    // TODO: Maybe consider removing GUI elements and find a way to reference them directly from view.
    private void getScopeCheckBoxStates () {
        /*
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

         */
    }
}
