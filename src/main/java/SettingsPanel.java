import burp.api.montoya.core.ToolType;

import javax.swing.*;
import java.awt.*;

// TODO Rewrite this. Create a large panel for the entire settingsPanel. Add a smaller subpanel for
// checkboxes that has grid bag layout. Give it a header. Consider adding an expansion type functionality.
public class SettingsPanel extends JPanel {
    public JPanel checkBoxGrid;

    public SettingsPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel panelLabel = new JLabel("Scope");
        add(panelLabel);
        add(createToolSelectorPanel());
    }

    private JPanel createToolSelectorPanel() {
        JPanel panel = new JPanel();
        checkBoxGrid = new JPanel(new GridLayout(2,7));

        // Creates a checkbox for each ToolType in burp and
        // associates a ToolType with each checkbox. Adds them to checkBoxGrid.
        JCheckBox checkBox;
        for (ToolType toolType : ToolType.values()){
            if (toolType.toolName().equals("Repeater") || toolType.toolName().equals("Intruder")){
                checkBox = new JCheckBox(toolType.toolName(), true);
                checkBox.putClientProperty("ToolType", toolType);
            }
            else {
                checkBox = new JCheckBox(toolType.toolName(), false);
                checkBox.putClientProperty("ToolType", toolType);
            }
            checkBoxGrid.add(checkBox);
        }

        panel.add(checkBoxGrid);
        return panel;
    }
}
