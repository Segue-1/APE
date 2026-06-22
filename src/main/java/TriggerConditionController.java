import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class TriggerConditionController extends Controller {
    private TriggerConditionPanel triggerConditionPanel;
    private CorrectiveCallsPanel ccPanel;
    private ErrorDetectionPanel edPanel;

    private ErrorDetectionController errorDetectionController;

    private int errorConditionDropdownCount;

    private Model model;


    TriggerConditionController(View view, Model model, Controller controller) {
        setAttributes(model, view, controller);

        setResetButtonListener();
        setCorrectiveCallsSetDropdownListener();
        errorConditionDropdownCount = 1;
        setErrorConditionDropdownListener();
        passTriggerConditionController();
        setPlusButtonListener();
        setAddButtonListener();
        setTableDeleteContextMenu(triggerConditionPanel.triggerConditionTable);
    }

    public void setAddButtonListener() {
        JPanel inputPanel = getInputPanel();
        int index = inputPanel.getComponentCount() - 2;
        JButton addButton = (JButton) inputPanel.getComponent(index);

        addButton.addActionListener(e -> {
            JComboBox<CorrectiveCallSet> correctiveCallsSetDropdown = (JComboBox<CorrectiveCallSet>) inputPanel.getComponent(1);
            CorrectiveCallSet correctiveCallSet = (CorrectiveCallSet) correctiveCallsSetDropdown.getSelectedItem();

            if (correctiveCallSet == null) {
                DebugLog.log("TriggerConditionBuilder: Add button clicked while no corrective calls set selected");
                DebugLog.log("A corrective calls set must be selected when creating a trigger condition");
                return;
            }
            else if (correctiveCallSet.correctiveCallList.size() == 0) {
                DebugLog.log("Empty call sequence in currently selected corrective calls set");
                warningPopup("No corrective calls populated in call set! Send 1 or more calls to the call set.");
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
            UpdateIsTriggerConditionSet();
            Utils.resizeColumnsToFitContent(triggerConditionPanel.triggerConditionTable);
        });
    }

    public void setResetButtonListener() {
        JPanel parentPanel = getConditionBuilderPanel();
        JPanel childPanel = getInputPanel();
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
            refreshErrorConditionDropdown();
        });
    }

    // Sets action listener which loads corrective call sets from corrective call set table model
    // TODO update this where the current selection is saved in a temp / swap value and then use equals to see if objects have changed
    // 1/5 only last item in list persists, all other selections reset to 0
    private void setCorrectiveCallsSetDropdownListener(){
        JComboBox<CorrectiveCallSet> correctiveCallsSetDropdown = getCorrectiveCallsSetDropdown();
        correctiveCallsSetDropdown.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                CorrectiveCallSet tempCcs;

                if (correctiveCallsSetDropdown.getSelectedIndex() != -1) {
                    tempCcs = correctiveCallsSetDropdown.getItemAt(correctiveCallsSetDropdown.getSelectedIndex());
                }
                else {
                    tempCcs = null;
                }

                correctiveCallsSetDropdown.removeAllItems();

                for (int row = 0; row < ccPanel.correctiveCallSetTableModel.getRowCount(); row++) {
                    CorrectiveCallSet ccs = ccPanel.correctiveCallSetTableModel.getCorrectiveCallsSet(row);
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
        setErrorConditionDropdownListener();
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


            inputPanel.repaint();
            inputPanel.revalidate();

            inputPanelScrollPane.repaint();
            inputPanelScrollPane.revalidate();


        });
    }

    private void passTriggerConditionController() {
        errorDetectionController.setTriggerConditionController(this);
    }

    private void warningPopup(String message) {
        JFrame warningFrame = new JFrame("Warning");
        warningFrame.setSize(500, 150);
        warningFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(new JLabel(message, JLabel.CENTER), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> warningFrame.dispose());
        panel.add(closeButton, BorderLayout.SOUTH);

        warningFrame.add(panel);
        warningFrame.setVisible(true);
    }

    private void setAttributes(Model model, View view, Controller controller) {
        this.triggerConditionPanel = view.triggerConditionPanel;
        this.ccPanel = view.correctiveCallsPanel;
        this.edPanel = view.errorDetectionPanel;
        this.errorDetectionController = controller.errorDetectionController;

        this.model = model;
    }

    private void UpdateIsTriggerConditionSet() {
        model.triggerConditionIsSet = triggerConditionPanel.triggerConditionTableModel.triggerConditionIsSet;
    }
    
    public void setTableDeleteContextMenu(JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                DeletableTableModel model = (DeletableTableModel) table.getModel();
                DebugLog.log(table.getName() + ": User deleted row " + selectedRow);
                model.removeRow(selectedRow);
                UpdateIsTriggerConditionSet();
                Utils.resizeColumnsToFitContent(table);
            }
        });

        popupMenu.add(deleteItem);

        // Displays popup menu and selects row.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
}
