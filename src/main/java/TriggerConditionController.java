import javax.swing.*;
import java.awt.*;

public class TriggerConditionController {
    private TriggerConditionPanel triggerConditionPanel;
    private JButton addButton;
    //private JButton resetButton;

    //private JPanel inputPanel;

    TriggerConditionController(TriggerConditionPanel triggerConditionPanel, Model model) {
        setAttributes(triggerConditionPanel);

        setAddButtonListener();
        setResetButtonListener();
    }

    public void setAddButtonListener() {

    }

    public void setResetButtonListener() {
        JPanel parentPanel = (JPanel) triggerConditionPanel.getComponent(0);
        JPanel childPanel = (JPanel) parentPanel.getComponent(1);
        JButton resetButton = (JButton) childPanel.getComponent(childPanel.getComponentCount() - 1);

        resetButton.addActionListener(e -> {
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridx = 0; gbc.gridy = 1;
            gbc.weightx = 1; gbc.weighty = 1;

            Component tempComponent = parentPanel.getComponent(2);
            parentPanel.remove(parentPanel.getComponentCount() - 1);
            parentPanel.remove(parentPanel.getComponentCount() - 1);

            parentPanel.add(triggerConditionPanel.createInputPanel(), gbc);

            gbc.gridx = 1; gbc.gridy = 0;
            gbc.weightx = 1; gbc.weighty = 1;
            gbc.gridheight = 2;

            parentPanel.add(tempComponent, gbc);

            parentPanel.revalidate();
            parentPanel.repaint();

            setResetButtonListener();
        });
    }

    private void setAttributes(TriggerConditionPanel triggerConditionPanel) {
        this.triggerConditionPanel = triggerConditionPanel;

        this.addButton = triggerConditionPanel.addButton;
        //this.resetButton = triggerConditionPanel.resetButton;

        //this.inputPanel = triggerConditionPanel.inputPanel;
    }
}
