import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.ArrayList;

public class TriggerConditionController {
    private TriggerConditionPanel triggerConditionPanel;
    private CorrectiveCallsPanel ccPanel;
    private ErrorDetectionPanel edPanel;

    private ErrorDetectionController errorDetectionController;

    private int errorConditionDropdownCount;


    TriggerConditionController(View view, Model model, Controller controller) {
        setAttributes(view, controller);

        setResetButtonListener();
        setCorrectiveCallsSetDropdownListener();
        errorConditionDropdownCount = 1;
        setErrorConditionDropdownListener();
        passTriggerConditionController();
        setPlusButtonListener();
        setAddButtonListener();
    }

    public void setAddButtonListener() {
        JPanel inputPanel = getInputPanel();
        int index = inputPanel.getComponentCount() - 2;
        JButton addButton = (JButton) inputPanel.getComponent(index);

        addButton.addActionListener(e -> {
            DebugLog.log("Add Button clicked");

        });
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

            errorConditionDropdownCount = 1;
            reattachListeners();
        });
    }

    // Sets action listener which loads corrective call sets from corrective call set table model
    // TODO update this where the current selection is saved in a temp / swap value and then use equals to see if objects have changed
    // 1/5 only last item in list persists, all other selections reset to 0
    private void setCorrectiveCallsSetDropdownListener(){
        JComboBox<CorrectiveCallsSet> correctiveCallsSetDropdown = getCorrectiveCallsSetDropdown();

        correctiveCallsSetDropdown.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                CorrectiveCallsSet tempCcs;

                if (correctiveCallsSetDropdown.getSelectedIndex() != -1) {
                    tempCcs = correctiveCallsSetDropdown.getItemAt(correctiveCallsSetDropdown.getSelectedIndex());
                }
                else {
                    tempCcs = null;
                }

                correctiveCallsSetDropdown.removeAllItems();

                for (int row = 0; row < ccPanel.correctiveCallsSetsTableModel.getRowCount(); row++) {
                    CorrectiveCallsSet ccs = ccPanel.correctiveCallsSetsTableModel.getCorrectiveCallsSet(row);
                    correctiveCallsSetDropdown.addItem(ccs);

                    if (ccs.equals(tempCcs)){
                        correctiveCallsSetDropdown.setSelectedIndex(row);
                    }
                    else{
                        correctiveCallsSetDropdown.setSelectedIndex(0);
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

    // remove
    private void setErrorConditionDropdownListener(){
        JComboBox<ErrorCondition> errorConditionDropdown = getErrorConditionDropdown();
        errorConditionDropdown.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
    }

    private void loadErrorConditionDropdown() {
        JComboBox<ErrorCondition> errorConditionDropdown = getErrorConditionDropdown();
        DefaultComboBoxModel<ErrorCondition> model = new DefaultComboBoxModel<>();
        ArrayList<ErrorCondition> errorConditions = new ArrayList<>(edPanel.errorConditionTableModel.errorConditions);

        for (ErrorCondition errorCondition : errorConditions) {
            model.addElement(errorCondition);
        }

        if (errorConditionDropdown != null)
            errorConditionDropdown.setModel(model);
    }

    // Refreshes all error condition drop down menus
    public void refreshErrorConditionDropdown() {
        JPanel inputPanel = getInputPanel();

        DebugLog.log("refreshErrorConditionDropdown iterating through inputPanel");
        for (Component component : inputPanel.getComponents()) {
            if (component instanceof JComboBox && "ErrorConditionDropdown".equals(((JComboBox) component).getName())) {
                DebugLog.log("refreshErrorConditionDropdown found ErrorConditionDropdown");
                JComboBox<ErrorCondition> errorConditionDropdown = (JComboBox<ErrorCondition>) component;
                errorConditionDropdown.removeAllItems();

                DefaultComboBoxModel<ErrorCondition> model = new DefaultComboBoxModel<>();
                ArrayList<ErrorCondition> errorConditions = new ArrayList<>(edPanel.errorConditionTableModel.errorConditions);

                DebugLog.log("refreshErrorConditionDropdown iterating through new list of errorConditions" );
                for (ErrorCondition errorCondition : errorConditions) {
                    DebugLog.log("refreshErrorConditionDropdown adding to model: " + errorCondition.toString());
                    model.addElement(errorCondition);
                }
                errorConditionDropdown.setModel(model);
            }
        }
    }

    private void reattachListeners() {
        setResetButtonListener();
        setCorrectiveCallsSetDropdownListener();
        setErrorConditionDropdownListener();
        setPlusButtonListener();
        setAddButtonListener();
    }

    private JPanel getConditionBuilderPanel(){
        return (JPanel) triggerConditionPanel.getComponent(0);
    }

    private JPanel getInputPanel() {
        JPanel conditionBuilderPanel = (JPanel) triggerConditionPanel.getComponent(0);
        return (JPanel) conditionBuilderPanel.getComponent(1);
    }

    private JComboBox<CorrectiveCallsSet> getCorrectiveCallsSetDropdown() {
        JPanel inputPanel = getInputPanel();
        int index = 1;

        @SuppressWarnings("unchecked") // Component only contains ErrorCondition objects
        JComboBox<CorrectiveCallsSet> correctiveCallsSetDropdown = (JComboBox<CorrectiveCallsSet>) inputPanel.getComponent(index);
        return correctiveCallsSetDropdown;
    }

    private JComboBox<ErrorCondition> getErrorConditionDropdown() {
        JPanel inputPanel = getInputPanel();
        int index;

        if (errorConditionDropdownCount == 1) {
            index = inputPanel.getComponentCount() - 5;
        }
        else {
            index = inputPanel.getComponentCount() - 6; // Doesn't seem to work
        }

        if (index <= 0){
            DebugLog.log("TriggerConditionController: getErrorConditionDropdown() failed to set index");
            return null;
        }
        
        DebugLog.log("errorConditionDropdownCount = " + errorConditionDropdownCount);

        @SuppressWarnings("unchecked") // Component only contains ErrorCondition objects
        JComboBox<ErrorCondition> errorConditionDropDown = (JComboBox<ErrorCondition>) inputPanel.getComponent(index);
        return errorConditionDropDown;
    }

    private JButton getPlusButton() {
        JPanel inputPanel = getInputPanel();

        // index of the plusButton. Always 3rd to last.
        // + (-1) for going from count (starts at 1) to list (starts at 0)
        int index = inputPanel.getComponentCount() - 4;

        return (JButton) inputPanel.getComponent(index);
    }

    private void setPlusButtonListener() {
        JPanel inputPanel = getInputPanel();
        JButton plusButton = getPlusButton();

        plusButton.addActionListener(e -> {
            Font buttonFont = new Font("Dialog", Font.PLAIN, 12);

            JButton collapseButton = new JButton("-");
            collapseButton.setFont(buttonFont);
            collapseButton.setPreferredSize(new Dimension(15, 15));
            JComboBox<ErrorCondition> errorConditionDropDown = new JComboBox<>();
            errorConditionDropDown.setName("ErrorConditionDropdown");
            errorConditionDropdownCount++;
            JComboBox<String> logicalOperator = new JComboBox<>(new String[]{"AND", "OR"});
            logicalOperator.setName("LogicalOperator");

            // Store last 4 components, temporarily remove them.
            int stop = inputPanel.getComponentCount() - 5;
            int start = inputPanel.getComponentCount() - 1;
            ArrayList<Component> componentList = new ArrayList<>();

            for (int i = start; i > stop; i--) {
                try {
                    componentList.add(0, inputPanel.getComponent(i));
                    inputPanel.remove(i);
                    inputPanel.repaint();
                    inputPanel.revalidate();
                }
                catch (Exception ex) {
                    DebugLog.log(ex.getMessage());
                }
            }

            inputPanel.add(logicalOperator);
            JPanel negationDropDownPanel = triggerConditionPanel.createNegationDropDownPanel();
            inputPanel.add(negationDropDownPanel);
            inputPanel.add(errorConditionDropDown);
            inputPanel.add(collapseButton);

            for (Component component : componentList) {
                inputPanel.add(component);
            }

            setErrorConditionDropdownListener();
            loadErrorConditionDropdown();

            collapseButton.addActionListener(actionEvent -> {
                inputPanel.remove(logicalOperator);
                inputPanel.remove(negationDropDownPanel);
                inputPanel.remove(errorConditionDropDown);
                errorConditionDropdownCount--;
                DebugLog.log("errorConditionDropdownCount: " + errorConditionDropdownCount);
                inputPanel.remove(collapseButton);

                inputPanel.repaint();
                inputPanel.revalidate();
            });
        });
    }

    private void passTriggerConditionController() {
        errorDetectionController.setTriggerConditionController(this);
    }

    private void setAttributes(View view, Controller controller) {
        this.triggerConditionPanel = view.triggerConditionPanel;
        this.ccPanel = view.correctiveCallsPanel;
        this.edPanel = view.errorDetectionPanel;

        this.errorDetectionController = controller.errorDetectionController;
    }
}
