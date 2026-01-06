import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;

public class TriggerConditionController {
    private TriggerConditionPanel triggerConditionPanel;
    private CorrectiveCallsPanel ccPanel;
    private ErrorDetectionPanel edPanel;

    private JButton addButton;


    TriggerConditionController(View view, Model model) {
        setAttributes(view);

        setAddButtonListener();
        setResetButtonListener();
        setCcsDropDown();
        setEcDropDown();
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
    // 1/5 only last item in list persists, all other selections reset to 0
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

    private void setEcDropDown(){
        triggerConditionPanel.ecDropDown.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                ErrorCondition tempEc;

                if (triggerConditionPanel.ecDropDown.getSelectedIndex() != -1) {
                    tempEc = triggerConditionPanel.ecDropDown.getItemAt(triggerConditionPanel.ecDropDown.getSelectedIndex());
                }
                else {
                    tempEc = null;
                }

                triggerConditionPanel.ecDropDown.removeAllItems();

                for (int row = 0; row < edPanel.errorConditionTableModel.getRowCount(); row++) {
                    ErrorCondition ec = edPanel.errorConditionTableModel.getErrorCondition(row);
                    triggerConditionPanel.ecDropDown.addItem(ec);

                    if (ec.equals(tempEc)){
                        triggerConditionPanel.ecDropDown.setSelectedIndex(row);
                    }
                    else{
                        triggerConditionPanel.ecDropDown.setSelectedIndex(0);
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
        setEcDropDown();
    }

    private void setAttributes(View view) {
        this.triggerConditionPanel = view.triggerConditionPanel;
        this.ccPanel = view.correctiveCallsPanel;
        this.edPanel = view.errorDetectionPanel;



        this.addButton = triggerConditionPanel.addButton;
    }
}
