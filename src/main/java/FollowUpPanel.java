import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.*;

public class FollowUpPanel extends JPanel {
    private static final Color followUpCallTextColor = new Color(9, 30, 172);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);
    // Fixed column count so setText() from From Selection doesn't resize the field.
    private static final int FIELD_COLUMNS = 8;


    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public JTextField[] inputFields;
    public JButton fromSelect;
    public JButton addButton;
    public JComboBox<ExtractText> dropDown;

    public ReplacementTableModel followUpReplaceTableModel;
    public JTable followUpReplaceTable;
    

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

        // Only the request is edited, so an empty panel takes the response editor's place. The editor
        // still exists because setFollowUpCall() reads it.
        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorPane.setLeftComponent(requestEditor.uiComponent());
        editorPane.setRightComponent(new JPanel());
        editorPane.setDividerLocation(0.5);
        editorPane.setResizeWeight(0.5);
        editorPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 500));

        panel.add(editorPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return panel;
    }

    private JPanel createReplaceTextPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        panel.setBackground(new Color(63, 225, 37));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = .75; gbc.weighty = 1;

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

        followUpReplaceTableModel = new ReplacementTableModel();
        followUpReplaceTable = new JTable(followUpReplaceTableModel);

        panel.add(createReplaceTable("Followup Replaced Text", followUpReplaceTable), gbc);

        panel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 200));

        return panel;
    }

    public JPanel createInputTextFields(String header, String[] fieldNames, JComboBox dropDown,
                                        JTextField[] jTextFields, JButton fromSelect, JButton addButton){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

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
            jTextFields[i] = new JTextField(FIELD_COLUMNS);
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

    private JPanel createReplaceTable(String header, JTable table){
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

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        panel.add(scrollPane, gbc);

        return panel;
    }
}
