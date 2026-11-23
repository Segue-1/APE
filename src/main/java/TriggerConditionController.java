import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TriggerConditionController {
    private TriggerConditionPanel triggerConditionPanel;
    private CorrectiveCallsPanel ccPanel;
    private ErrorDetectionPanel edPanel;

    private ErrorDetectionController errorDetectionController;

    private int errorConditionDropdownCount;

    private Model model;

    private TestCallController testCallController;


    TriggerConditionController(View view, Model model, Controller controller) {
        setAttributes(model, view, controller);

        setResetButtonListener();
        setCorrectiveCallsSetDropdownListener();
        errorConditionDropdownCount = 1;
        passTriggerConditionController();
        setPlusButtonListener();
        setAddButtonListener();

        // Sub-controller for the Test Calls sub-panel. The editors are shared with the condition builder.
        testCallController = new TestCallController(triggerConditionPanel.testCallPanel, model,
                triggerConditionPanel.requestEditor, triggerConditionPanel.responseEditor);

        Utils.setTableDeleteContextMenu(triggerConditionPanel.triggerConditionTable, () -> {
            updateTriggerConditions();
            DebugLog.log("Number of trigger conditions: " + this.model.triggerConditions.size());
        });
    }

    private void setAddButtonListener() {
        JPanel inputPanel = getInputPanel();
        int index = inputPanel.getComponentCount() - 2;
        JButton addButton = (JButton) inputPanel.getComponent(index);

        addButton.addActionListener(e -> {
            try {
                JComboBox<CorrectiveCallSet> correctiveCallsSetDropdown = (JComboBox<CorrectiveCallSet>) inputPanel.getComponent(1);
                CorrectiveCallSet correctiveCallSet = (CorrectiveCallSet) correctiveCallsSetDropdown.getSelectedItem();

                if (correctiveCallSet == null) {
                    DebugLog.log("TriggerConditionBuilder: Add button clicked while no corrective calls set selected");
                    DebugLog.log("A corrective calls set must be selected when creating a trigger condition");
                    return;
                }

                ArrayList<Boolean> isNegated = new ArrayList<>();
                ArrayList<ErrorCondition> errorConditions = new ArrayList<>();
                ArrayList<String> logicalOperators = new ArrayList<>();

                DebugLog.log("TriggerConditionBuilder parsing input...");
                for (Component component : inputPanel.getComponents()) {
                    if (component instanceof JPanel && ((JPanel) component).getName().equals("NegationDropdownPanel")) {
                        JPanel panel = (JPanel) component;
                        JCheckBox negationCheckBox = (JCheckBox) panel.getComponent(1);
                        if (negationCheckBox.isSelected()) {
                            isNegated.add(true);
                            DebugLog.log("Added negation");
                        }
                        else {
                            isNegated.add(false);
                            DebugLog.log("Added non-negation");
                        }
                    }
                    else if (component instanceof JComboBox && ((JComboBox) component).getName().equals("ErrorConditionDropdown")) {
                        JComboBox errorConditionDropdown = (JComboBox) component;

                        if (errorConditionDropdown.getSelectedItem() != null) {
                            try{
                                ErrorCondition errorCondition = (ErrorCondition) errorConditionDropdown.getSelectedItem();
                                errorConditions.add(errorCondition);
                                DebugLog.log("Added error condition" + errorCondition.debugString());
                            }
                            catch (Exception ex){
                                DebugLog.log("Failed to add error condition!");
                            }

                        }
                        else {
                            DebugLog.log("[Error] Add button clicked while error condition drop down has no selection");
                            DebugLog.log("Each error condition drop down must have an error condition selected when creating a trigger condition");
                            return;
                        }
                    }
                    else if (component instanceof JComboBox && ((JComboBox) component).getName().equals("LogicalOperatorDropdown")) {
                        JComboBox logicalOperatorDropdown = (JComboBox) component;
                        if (logicalOperatorDropdown.getSelectedItem() != null) {
                            try {
                                logicalOperators.add(logicalOperatorDropdown.getSelectedItem().toString());
                            }
                            catch (Exception ex) {
                                DebugLog.log("Failed to add selected logical operator!");
                                DebugLog.log(ex.getMessage());
                            }
                        }
                        else {
                            DebugLog.log("[Error] Logical operator dropdown has no selection");
                            DebugLog.log("Each logical operator dropdown must have a selection");
                            return;
                        }
                    }
                }

                DebugLog.log("Instantiating TriggerCondition");
                TriggerCondition triggerCondition = new TriggerCondition(correctiveCallSet, isNegated, errorConditions, logicalOperators);
                DebugLog.log(triggerCondition.debugString());
                triggerConditionPanel.triggerConditionTableModel.addRow(triggerCondition);
                updateTriggerConditions();
                DebugLog.log("Number of trigger conditions: " + this.model.triggerConditions.size());
                Utils.resizeColumnsToFitContent(triggerConditionPanel.triggerConditionTable);
            }
            catch (Exception ex) {
                DebugLog.log("[Error] TriggerConditionController: Adding trigger condition failed. " + ex);
            }
        });
    }

    private void setResetButtonListener() {
        JPanel parentPanel = getConditionBuilderPanel();
        JPanel childPanel = getInputPanel();
        JButton resetButton = (JButton) childPanel.getComponent(childPanel.getComponentCount() - 1);

        resetButton.addActionListener(e -> {
            try {
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
                refreshErrorConditionDropdown();
            }
            catch (Exception ex) {
                DebugLog.log("[Error] TriggerConditionController: Resetting condition builder failed. " + ex);
            }
        });
    }

    // Sets action listener which loads corrective call sets from corrective call set table model
    // TODO update this where the current selection is saved in a temp / swap value and then use equals to see if objects have changed
    // 1/5 only last item in list persists, all other selections reset to 0
    private void setCorrectiveCallsSetDropdownListener(){
        JComboBox<CorrectiveCallSet> correctiveCallsSetDropdown = getCorrectiveCallsSetDropdown();
        correctiveCallsSetDropdown.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                try {
                    Utils.repopulatePreservingSelection(correctiveCallsSetDropdown, correctiveCallSets());
                }
                catch (Exception ex) {
                    DebugLog.log("[Error] TriggerConditionController: Refreshing corrective call set dropdown failed. " + ex);
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

    // Every corrective call set currently defined, in table order.
    private List<CorrectiveCallSet> correctiveCallSets() {
        List<CorrectiveCallSet> sets = new ArrayList<>();

        for (int row = 0; row < ccPanel.correctiveCallSetTableModel.getRowCount(); row++) {
            sets.add(ccPanel.correctiveCallSetTableModel.getCorrectiveCallsSet(row));
        }

        return sets;
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

        for (Component component : inputPanel.getComponents()) {
            if (component instanceof JComboBox && "ErrorConditionDropdown".equals(((JComboBox) component).getName())) {
                JComboBox<ErrorCondition> errorConditionDropdown = (JComboBox<ErrorCondition>) component;
                errorConditionDropdown.removeAllItems();

                DefaultComboBoxModel<ErrorCondition> model = new DefaultComboBoxModel<>();
                ArrayList<ErrorCondition> errorConditions = new ArrayList<>(edPanel.errorConditionTableModel.errorConditions);

                for (ErrorCondition errorCondition : errorConditions) {
                    model.addElement(errorCondition);
                }
                errorConditionDropdown.setModel(model);
            }
        }
    }

    private void reattachListeners() {
        setResetButtonListener();
        setCorrectiveCallsSetDropdownListener();
        setPlusButtonListener();
        setAddButtonListener();
    }

    private JPanel getConditionBuilderPanel(){
        return (JPanel) triggerConditionPanel.getComponent(0);
    }

    private JPanel getInputPanel() {
        JPanel conditionBuilderPanel = getConditionBuilderPanel();
        JScrollPane inputPanelScrollPane = (JScrollPane) conditionBuilderPanel.getComponent(1);
        JViewport inputPanelViewport = (JViewport) inputPanelScrollPane.getComponent(0);
        return (JPanel) inputPanelViewport.getComponent(0);
    }

    private JScrollPane getInputPanelScrollPane() {
        JPanel conditionBuilderPanel = getConditionBuilderPanel();
        return (JScrollPane) conditionBuilderPanel.getComponent(1);
    }

    private JComboBox<CorrectiveCallSet> getCorrectiveCallsSetDropdown() {
        JPanel inputPanel = getInputPanel();
        int index = 1;

        @SuppressWarnings("unchecked") // Component only contains ErrorCondition objects
        JComboBox<CorrectiveCallSet> correctiveCallsSetDropdown = (JComboBox<CorrectiveCallSet>) inputPanel.getComponent(index);
        return correctiveCallsSetDropdown;
    }

    private JComboBox<ErrorCondition> getErrorConditionDropdown() {
        JPanel inputPanel = getInputPanel();
        int index;

        if (errorConditionDropdownCount == 1) {
            index = inputPanel.getComponentCount() - 5;
        }
        else {
            index = inputPanel.getComponentCount() - 6;
        }

        if (index <= 0){
            DebugLog.log("TriggerConditionController: getErrorConditionDropdown() failed to set index");
            return null;
        }

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

    // You could add custom logic that sets inputPanel's preferred size manually after x amount of dynamically added
    // UI elements to add more space.
    private void setPlusButtonListener() {
        JPanel inputPanel = getInputPanel();
        JButton plusButton = getPlusButton();
        JScrollPane inputPanelScrollPane = getInputPanelScrollPane();

        plusButton.addActionListener(e -> {
            try {
                Font buttonFont = new Font("Dialog", Font.PLAIN, 12);

                JButton collapseButton = new JButton("-");
                collapseButton.setFont(buttonFont);
                collapseButton.setPreferredSize(new Dimension(15, 15));
                JComboBox<ErrorCondition> errorConditionDropDown = new JComboBox<>();
                errorConditionDropDown.setName("ErrorConditionDropdown");
                errorConditionDropdownCount++;
                JComboBox<String> logicalOperator = new JComboBox<>(new String[]{"AND", "OR"});
                logicalOperator.setName("LogicalOperatorDropdown");

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

                loadErrorConditionDropdown();


                collapseButton.addActionListener(actionEvent -> {
                    try {
                        inputPanel.remove(logicalOperator);
                        inputPanel.remove(negationDropDownPanel);
                        inputPanel.remove(errorConditionDropDown);
                        errorConditionDropdownCount--;
                        DebugLog.log("errorConditionDropdownCount: " + errorConditionDropdownCount);
                        inputPanel.remove(collapseButton);

                        inputPanel.repaint();
                        inputPanel.revalidate();
                    }
                    catch (Exception ex) {
                        DebugLog.log("[Error] TriggerConditionController: Removing error condition dropdown failed. " + ex);
                    }
                });


                inputPanel.repaint();
                inputPanel.revalidate();

                inputPanelScrollPane.repaint();
                inputPanelScrollPane.revalidate();
            }
            catch (Exception ex) {
                DebugLog.log("[Error] TriggerConditionController: Adding error condition dropdown failed. " + ex);
            }
        });
    }

    private void passTriggerConditionController() {
        errorDetectionController.setTriggerConditionController(this);
    }


    private void setAttributes(Model model, View view, Controller controller) {
        this.triggerConditionPanel = view.triggerConditionPanel;
        this.ccPanel = view.correctiveCallsPanel;
        this.edPanel = view.errorDetectionPanel;
        this.errorDetectionController = controller.errorDetectionController;

        this.model = model;
    }

    private void updateTriggerConditions() {
        model.triggerConditions = triggerConditionPanel.triggerConditionTableModel.triggerConditions;
    }
    
}
