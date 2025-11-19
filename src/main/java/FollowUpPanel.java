import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

public class FollowUpPanel extends JPanel {
    static Color followUpCallTextColor = new Color(9, 30, 172);
    static Color conditionBuilderTextColor = new Color(143, 0, 197);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public JTextField[] inputFields;
    public JButton fromSelect;
    public JButton addButton;
    public JComboBox<ExtractText> dropDown;

    public TableModel followUpReplaceTableModel;
    public JTable followUpReplaceTable;

    public JComboBox<ErrorCondition> errorConditionDropDown;
    public JComboBox<CorrectiveCallsSet> correctiveCallSetDropDown;

    FollowUpPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.PINK);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;
        //gbc.gridwidth = 1;

        add(createLabelPanel(), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        //gbc.gridwidth = 1;

        add(createRequestEditorPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 1; gbc.weighty = 0;

        add(createReplaceTextPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 1; gbc.weighty = .4;

        add(createBottomPanel(), gbc);
    }


    private JPanel createLabelPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel();
        JLabel subText = new JLabel();
        GridBagConstraints gbc = new GridBagConstraints();

        // Main label
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;


        label.setText("Followup Call");
        label.setFont(headerFont);
        label.setForeground(followUpCallTextColor);
        panel.add(label, gbc);

        // Sub text
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 0;

        subText.setText("Resend the first call which returned an error response");
        subText.setFont(subTextFont);
        subText.setForeground(Color.BLACK);
        panel.add(subText, gbc);

        return panel;
    }

    private JPanel createRequestEditorPanel() {
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

    // TODO reorganize, put replace input fields and table underneath editor. Add response editor. Put condition builder table beneath everything.
    private JPanel createReplaceTextPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        panel.setBackground(new Color(63, 225, 37));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.5; gbc.weighty = 1;

        String[] fieldLabels = {"Name:", "Start string:", "Stop string:", "Selected Text from Request:",
                "Pick from Extraction"};
        dropDown = new JComboBox<>();
        inputFields = new JTextField[fieldLabels.length - 1];
        fromSelect = new JButton();
        addButton = new JButton();

        JPanel replaceTextInput = createInputTextFields("Replacement", fieldLabels, dropDown,
                inputFields, fromSelect, addButton);

        panel.add(replaceTextInput, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        followUpReplaceTableModel = new DefaultTableModel();
        followUpReplaceTable = new JTable(followUpReplaceTableModel);

        //followUpReplaceTable.setPreferredSize(new Dimension(300, 200));
        //followUpReplaceTable.setMaximumSize(new Dimension(300, 200));

        panel.add(createSmallTable("Replace", followUpReplaceTable), gbc);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel();
        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc = new GridBagConstraints();
        /*
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel("Error condition "));
        panel.add(new JComboBox<>(new String[]{"Error Condition 0", "Error Condition 1", "These are just placeholders"}));
        panel.add(new JLabel(" will trigger corrective call set "));
        panel.add(new JComboBox<>(new String[]{"Set 0", "Set 1", "These are just placeholders"}));

         */

        panel.setBackground(new Color(0, 131, 11));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = .66; gbc.weighty = 0;

        panel.add(createConditionBuilderLabel(), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = .66; gbc.weighty = 1;

        panel.add(createConditionBuilderPanel(), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.gridheight = 2;

        TableModel conditionTableModel = new DefaultTableModel();
        JTable conditionTable = new JTable(conditionTableModel);

        panel.add(createSmallTable("Condition Table", conditionTable), gbc);


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


        label.setText("Condition Builder");
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

    private JPanel createConditionBuilderPanel(){
        JPanel panel = new JPanel();
        panel.setBackground(new Color(0, 255, 223));
        return panel;
    }


    private JPanel createConditionTable() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(58, 158, 12));
        return panel;
    }

    public JPanel createInputTextFields(String header, String[] fieldNames, JComboBox dropDown,
                                        JTextField[] jTextFields, JButton fromSelect, JButton addButton){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        panel.setBackground(new Color(253, 83, 83));

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.gridwidth = 2;

        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(subHeaderFont);
        panel.add(headerLabel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.gridwidth = 1;

        for (int i = 0; i < jTextFields.length; i++) {
            jTextFields[i] = new JTextField();
        }

        for (int i = 0; i < fieldNames.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1;
            gbc.weightx = .5;
            panel.add(new JLabel(fieldNames[i]), gbc);

            if (i < fieldNames.length - 1) {
                gbc.gridx = 1;
                panel.add(jTextFields[i], gbc);
            }
        }

        gbc.gridx = 1; gbc.gridy = fieldNames.length;
        panel.add(dropDown, gbc);

        //gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = fieldNames.length + 1;
        gbc.weightx = 1; gbc.weighty = 1;
        fromSelect.setText("From Selection");
        panel.add(fromSelect, gbc);

        gbc.gridx = 1; gbc.gridy = fieldNames.length + 1;
        gbc.weightx = 1;
        addButton.setText("Add");
        panel.add(addButton, gbc);

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

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        panel.add(scrollPane, gbc);

        return panel;
    }
}
