import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class TriggerConditionPanel extends JPanel {
    static Color conditionBuilderTextColor = new Color(143, 0, 197);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public JButton addButton;
    public JButton resetButton;


    TriggerConditionPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.PINK);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        add(createConditionBuilderPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        add(createTestCallPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 1; gbc.weighty = 2;

        add(createEditorPanel(), gbc);
    }

    private JPanel createConditionBuilderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        panel.setBackground(new Color(0, 131, 11));
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

        TableModel conditionTableModel = new DefaultTableModel();
        JTable conditionTable = new JTable(conditionTableModel);
        JPanel conditionTablePanel = createSmallTable("Trigger Condition Table", conditionTable);

        panel.add(conditionTablePanel, gbc);

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
        gbc.weightx = 1; gbc.weighty = 0;

        subText.setText("Build conditions which trigger a set of corrective calls. " +
                "Select and error condition, a conditional statement, and a corrective call set to trigger.");

        subText.setFont(subTextFont);
        subText.setForeground(Color.BLACK);
        panel.add(subText, gbc);

        panel.setBackground(new Color(151, 131, 21));
        return panel;
    }

    // Rename this shit. Even you have no clue what this is referring to.
    public JPanel createInputPanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        DebugLog.log("createInputPanel() called");

        panel.add(new JLabel("Call set "));
        JComboBox<CorrectiveCallsSet> correctiveCallsSetDropdown = new JComboBox<>();
        correctiveCallsSetDropdown.setName("CorrectiveCallsSetDropdown");
        panel.add(correctiveCallsSetDropdown);
        panel.add(new JLabel(" will be called if "));
        JPanel negationDropdownPanel = createNegationDropDownPanel();
        negationDropdownPanel.setName("NegationDropdownPanel");
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

        panel.setMaximumSize(new Dimension(350, 200));
        panel.setPreferredSize(new Dimension(350, 200));

        return panel;
    }

    public JPanel createSmallTable(String header, JTable table){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        panel.setBackground(new Color(138, 79, 243));

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(subHeaderFont);
        panel.add(headerLabel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        scrollPane.setMinimumSize(new Dimension(350, 200));

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        panel.add(scrollPane, gbc);
        panel.setMinimumSize(new Dimension(100, 100));
        panel.setPreferredSize(new Dimension(100, 100));

        return panel;
    }

    private JPanel createTestCallPanel(){
        JPanel panel = new JPanel();
        panel.setBackground(new Color(153, 41, 250));
        return panel;
    }

    private JPanel createEditorPanel(){
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(new Color(211, 173, 19));
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
        notCheckbox.setVisible(false);

        // Should be moved to controller
        expandButton.addActionListener(actionEvent -> {
            boolean state = notCheckbox.isVisible();
            notCheckbox.setVisible(!state);
            expandButton.setText(state ? "▶" : "▼");
        });

        panel.add(expandButton);
        panel.add(notCheckbox);

        return panel;
    }
}
