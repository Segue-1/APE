import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;


public class TriggerConditionPanel extends JPanel {
    static Color conditionBuilderTextColor = new Color(143, 0, 197);
    static Color testCallTextColor = new Color(255, 91, 0);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public JButton addButton;
    public JButton resetButton;

    public TriggerConditionTableModel triggerConditionTableModel;
    public JTable triggerConditionTable;

    public TestCallTableModel testCallTableModel;
    public JTable testCallTable;


    TriggerConditionPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0.25;

        add(createConditionBuilderPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 0.1;

        add(createTestCallPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 1; gbc.weighty = 1;

        add(createEditorPanel(), gbc);
    }

    private JPanel createConditionBuilderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0; gbc.weighty = 0;

        panel.add(createConditionBuilderLabel(), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        panel.add(createInputPanel(), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.gridheight = 2;

        triggerConditionTableModel = new TriggerConditionTableModel();
        triggerConditionTable = new JTable(triggerConditionTableModel);
        triggerConditionTable.setName("TriggerConditionTable");
        JPanel triggerConditionTablePanel = createTriggerConditionTable("Trigger Condition Table", triggerConditionTable);

        panel.add(triggerConditionTablePanel, gbc);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return panel;
    }

    private JPanel createConditionBuilderLabel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel();
        JLabel subText = new JLabel();
        GridBagConstraints gbc = new GridBagConstraints();

        // Label
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;


        label.setText("Trigger Condition Builder");
        label.setFont(headerFont);
        label.setForeground(conditionBuilderTextColor);
        panel.add(label, gbc);

        // Sub text
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        subText.setFont(subTextFont);
        subText.setForeground(Color.BLACK);

        subText.setText("Build a conditional statement which will trigger a set of corrective calls.");

        panel.add(subText, gbc);

        return panel;
    }

    // Rename this shit. Even you have no clue what this is referring to.
    public JScrollPane createInputPanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        panel.add(new JLabel("Call set "));
        JComboBox<CorrectiveCallSet> correctiveCallsSetDropdown = new JComboBox<>();
        correctiveCallsSetDropdown.setName("CorrectiveCallsSetDropdown");
        panel.add(correctiveCallsSetDropdown);
        panel.add(new JLabel(" will be called if "));
        JPanel negationDropdownPanel = createNegationDropDownPanel();
        panel.add(negationDropdownPanel);
        JComboBox<ErrorCondition> errorConditionDropdown = new JComboBox<>();
        errorConditionDropdown.setName("ErrorConditionDropdown");
        panel.add(errorConditionDropdown);

        JButton addTriggerConditionButton = new JButton("+");
        addTriggerConditionButton.setPreferredSize(new Dimension(15, 15));

        panel.add(addTriggerConditionButton);
        panel.add(new JLabel(" is received in the response."));
        panel.add(new JButton("Add"));
        panel.add(new JButton("Reset"));
        panel.setName("InputPanel");

        //panel.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
        panel.setPreferredSize(new Dimension(700, 600));

        return scrollPane;
    }

    public JPanel createTriggerConditionTable(String header, JTable table){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(subHeaderFont);
        panel.add(headerLabel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 100));
        scrollPane.setMinimumSize(new Dimension(500, 100));

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        panel.add(scrollPane, gbc);
        panel.setMinimumSize(new Dimension(400, 100));
        panel.setPreferredSize(new Dimension(400, 100));

        table.getColumnModel().getColumn(0).setMinWidth(125);
        table.getColumnModel().getColumn(1).setMinWidth(420);

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        return panel;
    }

    private JPanel createTestCallPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.33; gbc.weighty = 0;

        panel.add(createTestCallLabel(), gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        panel.add(createTestCallButtonPanel(), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 0.5;
        gbc.gridwidth = 2;

        testCallTableModel = new TestCallTableModel();
        testCallTable = new JTable(testCallTableModel);
        testCallTable.setName("TestCallTable");
        panel.add(createTestCallTable("APE's Steps", testCallTable), gbc);

        return panel;
    }

    private JPanel createTestCallButtonPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0; gbc.weighty = 0;

        JButton testButton = new JButton("Test");

        panel.add(testButton, gbc);

        return panel;
    }

    private JPanel createTestCallLabel(){
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel();
        JLabel subText = new JLabel();
        GridBagConstraints gbc = new GridBagConstraints();

        // Label
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;


        label.setText("Test Calls");
        label.setFont(headerFont);
        label.setForeground(testCallTextColor);
        panel.add(label, gbc);

        // Sub text
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        subText.setFont(subTextFont);
        subText.setForeground(Color.BLACK);

        subText.setText("Displays step by step what APE does. Send a test call which will execute a trigger condition.");

        panel.add(subText, gbc);

        return panel;
    }

    private JPanel createTestCallTable(String header, JTable table) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(subHeaderFont);
        panel.add(headerLabel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 100));
        scrollPane.setMinimumSize(new Dimension(500, 100));

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        panel.add(scrollPane, gbc);
        panel.setMinimumSize(new Dimension(400, 100));
        panel.setPreferredSize(new Dimension(400, 100));

        //table.getColumnModel().getColumn(0).setMinWidth(125);
        //table.getColumnModel().getColumn(1).setMinWidth(420);

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        return panel;
    }

    private JPanel createEditorPanel(){
        JPanel panel = new JPanel(new BorderLayout());

        requestEditor = Controller.montoyaApi.userInterface().createHttpRequestEditor();
        responseEditor = Controller.montoyaApi.userInterface().createHttpResponseEditor();

        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorPane.setLeftComponent(requestEditor.uiComponent());
        editorPane.setRightComponent(responseEditor.uiComponent());
        editorPane.setDividerLocation(0.5);
        editorPane.setResizeWeight(0.5);
        editorPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 500));

        panel.add(editorPane, BorderLayout.CENTER);
        return panel;
    }

    public JPanel createNegationDropDownPanel() {
        JPanel panel = new JPanel();
        Font buttonFont = new Font("Dialog", Font.PLAIN, 6);

        JButton expandButton = new JButton("▶");
        expandButton.setFont(buttonFont);
        expandButton.setPreferredSize(new Dimension(15, 15));
        JCheckBox notCheckbox = new JCheckBox("NOT");
        notCheckbox.setName("NegationCheckBox");
        notCheckbox.setVisible(false);

        // Should be moved to controller
        expandButton.addActionListener(actionEvent -> {
            boolean state = notCheckbox.isVisible();
            notCheckbox.setVisible(!state);
            expandButton.setText(state ? "▶" : "▼");
        });

        panel.add(expandButton);
        panel.add(notCheckbox);
        panel.setName("NegationDropdownPanel");

        return panel;
    }
}
