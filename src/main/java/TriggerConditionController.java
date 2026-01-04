import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;

public class TriggerConditionController {
    private TriggerConditionPanel triggerConditionPanel;
    private CorrectiveCallsPanel ccPanel;

    private JButton addButton;


    TriggerConditionController(View view, Model model) {
        setAttributes(view);

        setAddButtonListener();
        setResetButtonListener();
        setCcsDropDown();
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

            reattachListeners();
        });
    }

    // Sets action listener which loads corrective call sets from corrective call set table model
    // TODO update this where the current selection is saved in a temp / swap value and then use equals to see if objects have changed
    private void setCcsDropDown(){
        triggerConditionPanel.ccsDropdown.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                CorrectiveCallsSet tempCcs;

                if (triggerConditionPanel.ccsDropdown.getSelectedIndex() != -1) {
                    tempCcs = triggerConditionPanel.ccsDropdown.getItemAt(triggerConditionPanel.ccsDropdown.getSelectedIndex());
                }
                else {
                    tempCcs = null;
                }

                triggerConditionPanel.ccsDropdown.removeAllItems();

                for (int row = 0; row < ccPanel.correctiveCallsSetsTableModel.getRowCount(); row++) {
                    CorrectiveCallsSet ccs = ccPanel.correctiveCallsSetsTableModel.getCorrectiveCallsSet(row);
                    triggerConditionPanel.ccsDropdown.addItem(ccs);

                    if (ccs.equals(tempCcs)){
                        triggerConditionPanel.ccsDropdown.setSelectedIndex(row);
                    }
                    else{
                        triggerConditionPanel.ccsDropdown.setSelectedIndex(0);
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
    }

    private void reattachListeners() {
        setResetButtonListener();
        setCcsDropDown();
    }

    private void setAttributes(View view) {
        this.triggerConditionPanel = view.triggerConditionPanel;
        this.ccPanel = view.correctiveCallsPanel;



        this.addButton = triggerConditionPanel.addButton;
    }
}
