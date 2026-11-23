import burp.api.montoya.core.ToolType;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    public JPanel checkBoxGrid;

    public SettingsPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();


        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        add(createCheckboxePanel(), gbc);
    }

    private JPanel createCheckboxePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;


        JLabel label = new JLabel("Enabled Tools");

        panel.add(label, gbc);

        JLabel subLabel = new JLabel("Select which Burp Suite tools APE will work with");

        gbc.gridy = 1;

        panel.add(subLabel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 1;

        checkBoxGrid = new JPanel(new GridLayout(5,4));

        // Creates a checkbox for each ToolType in burp and
        // associates a ToolType with each checkbox. Adds them to checkBoxGrid.
        JCheckBox checkBox;
        for (ToolType toolType : ToolType.values()){
            // APE's own calls come from the Extensions tool, so processing it recurses. Not offered.
            if (toolType == ToolType.EXTENSIONS) {
                continue;
            }

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

        panel.add(checkBoxGrid, gbc);
        return panel;
    }
}
