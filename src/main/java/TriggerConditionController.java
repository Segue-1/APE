import javax.swing.*;

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
            inputPanel = triggerConditionPanel.createInputPanel();
        });
    }

    private void setAttributes(TriggerConditionPanel triggerConditionPanel) {
        this.triggerConditionPanel = triggerConditionPanel;

        this.addButton = triggerConditionPanel.addButton;
        this.resetButton = triggerConditionPanel.resetButton;

        this.inputPanel = triggerConditionPanel.inputPanel;
    }
}
