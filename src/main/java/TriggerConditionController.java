import javax.swing.*;
import java.awt.*;

public class TriggerConditionController {
    private TriggerConditionPanel triggerConditionPanel;
    private JButton addButton;
    private JButton resetButton;

    private JPanel inputPanel;

    TriggerConditionController(TriggerConditionPanel triggerConditionPanel, Model model) {
        setAttributes(triggerConditionPanel);

        setAddButtonListener();
        setResetButtonListener();
    }

    public void setAddButtonListener() {

    }

    // TODO does not work, intended to reset / clear inputPanel.
    public void setResetButtonListener() {
        resetButton.addActionListener(e -> {
            try{
                DebugLog.log("Attempting to use Reset button...");

                JPanel parentPanel = (JPanel) triggerConditionPanel.getComponent(0);
                parentPanel.remove(1);

                GridBagConstraints gbc = new GridBagConstraints();

                gbc.fill = GridBagConstraints.BOTH;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.gridx = 0; gbc.gridy = 1;
                gbc.weightx = 1; gbc.weighty = 1;
                
                parentPanel.add(triggerConditionPanel.createInputPanel(), gbc);
                
                parentPanel.revalidate();
                parentPanel.repaint();
                
            }
            catch(Exception ex){
                DebugLog.log("Error in TriggerConditionPanel's Reset button!");
                DebugLog.log(ex.getMessage());
            }

        });
    }

    private void setAttributes(TriggerConditionPanel triggerConditionPanel) {
        this.triggerConditionPanel = triggerConditionPanel;

        this.addButton = triggerConditionPanel.addButton;
        this.resetButton = triggerConditionPanel.resetButton;

        this.inputPanel = triggerConditionPanel.inputPanel;
    }
}
