import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class ErrorDetectionPanel extends JPanel {
    // Color / font
    static Color errorTextColor = new Color(198, 39, 3);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font labelFont = new Font("Dialog", Font.PLAIN, 12);
    // Error Detection HTTP Editors
    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public JComboBox<String> dropdown;
    public JButton submitButton;
    public JTextArea conditionTextField;

    public ErrorConditionTableModel errorConditionTableModel;
    public JTable errorConditionTable;


    public ErrorDetectionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Label / header
        JLabel label = new JLabel("Error Detection");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setForeground(errorTextColor);
        label.setFont(headerFont);
        add(label, BorderLayout.NORTH);

        // Text below label / header
        JLabel subText = new JLabel("Select error conditions which will trigger corrective calls.");
        subText.setAlignmentX(Component.LEFT_ALIGNMENT);
        subText.setFont(labelFont);
        add(subText, BorderLayout.NORTH);

        // Horizontal line below label / above editors
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.LIGHT_GRAY);  // Change color
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setPreferredSize(new Dimension(Integer.MAX_VALUE, 2));
        add(sep);
        
        requestEditor = Controller.montoyaApi.userInterface().createHttpRequestEditor();
        responseEditor = Controller.montoyaApi.userInterface().createHttpResponseEditor();

        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorPane.setLeftComponent(requestEditor.uiComponent());
        editorPane.setRightComponent(responseEditor.uiComponent());
        editorPane.setDividerLocation(0.5);
        editorPane.setResizeWeight(0.5);
        editorPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 875));
        editorPane.setMinimumSize(new Dimension(Integer.MAX_VALUE, 200));
        add(editorPane, BorderLayout.CENTER);

        createBottomPanel();
    }

    public void createBottomPanel(){
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 250));
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        bottomPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 125));
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        JPanel tablePanel = createTable();
        bottomPanel.add(tablePanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.33;

        JPanel addErrorConditions = createAddErrorConditions();
        bottomPanel.add(addErrorConditions, gbc);

        add(bottomPanel);
    }

    public JPanel createTable(){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        JLabel label = new JLabel("Current Error Conditions");
        label.setFont(subHeaderFont);
        panel.add(label, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        errorConditionTableModel = new ErrorConditionTableModel();

        errorConditionTable = new JTable(errorConditionTableModel);
        errorConditionTable.setName("Error Condition Table");
        errorConditionTable.getColumnModel().getColumn(0).setMinWidth(75);
        errorConditionTable.getColumnModel().getColumn(1).setMinWidth(75);
        errorConditionTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScrollPane = new JScrollPane(errorConditionTable);
        panel.add(tableScrollPane, gbc);

        return panel;
    }

    public JPanel createAddErrorConditions() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel errorConditionLabel = new JLabel("Add Error Condition");
        errorConditionLabel.setFont(subHeaderFont);
        panel.add(errorConditionLabel, gbc);

        gbc.gridy = 1;
        String[] options = {"Status Code", "Body", "Header"};
        dropdown = new JComboBox<>(options);
        panel.add(dropdown, gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        conditionTextField = new JTextArea();
        conditionTextField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        conditionTextField.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(conditionTextField);

        panel.add(scrollPane, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        submitButton = new JButton("Add");
        panel.add(submitButton, gbc);

        return panel;
    }
}