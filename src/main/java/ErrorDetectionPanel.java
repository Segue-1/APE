import burp.api.montoya.MontoyaApi;
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

    public DefaultTableModel tableModel;
    public JTable table;


    // TODO Refactor, split up into smaller methods. Don't pass in montoyaApi as arg through constructor, setup an attribute.
    public ErrorDetectionPanel(MontoyaApi montoyaApi) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Label / header
        // TODO, rename label to label or something smaller.
        JLabel label = new JLabel("Error Detection");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setForeground(errorTextColor);
        label.setFont(headerFont);
        add(label, BorderLayout.NORTH);

        // Text below label / header
        // TODO Rename this label as well
        JLabel subText = new JLabel("Select error conditions which will trigger corrective calls.");
        subText.setAlignmentX(Component.LEFT_ALIGNMENT);
        subText.setFont(labelFont);
        add(subText, BorderLayout.NORTH);

        // Horizontal line below label / above editors
        // TODO Disappeared after removing max / preffered size on editors and adding max / preferred
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.LIGHT_GRAY);  // Change color
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setPreferredSize(new Dimension(Integer.MAX_VALUE, 2));
        add(sep);


        requestEditor = montoyaApi.userInterface().createHttpRequestEditor();
        responseEditor = montoyaApi.userInterface().createHttpResponseEditor();

        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorPane.setLeftComponent(requestEditor.uiComponent());
        editorPane.setRightComponent(responseEditor.uiComponent());
        editorPane.setDividerLocation(0.5);
        editorPane.setResizeWeight(0.5);
        editorPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 875));
        editorPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 875));
        add(editorPane, BorderLayout.CENTER);

        // UI elements related to adding error conditions in bottom right.
        // This includes the label, dropdown box, text field, and "Add" button.
        // TODO rename errorConditionPanel, it's confusing since this is in an ErrorDetectionClass but it is something else
        JPanel errorConditionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridy = 0;

        add(sep);

        JLabel errorConditionLabel = new JLabel("Error Condition");
        //errorConditionLabel.setForeground(errorTextColor);
        errorConditionLabel.setFont(subHeaderFont);
        errorConditionPanel.add(errorConditionLabel, gbc);

        gbc.gridy = 1;
        String[] options = {"Status Code", "Body", "Header"};
        dropdown = new JComboBox<>(options);
        errorConditionPanel.add(dropdown, gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        conditionTextField = new JTextArea();
        conditionTextField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        conditionTextField.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(conditionTextField);
        scrollPane.setMinimumSize(new Dimension(200, 50));
        scrollPane.setPreferredSize(new Dimension(200, 100));
        errorConditionPanel.add(scrollPane, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        submitButton = new JButton("Add");
        errorConditionPanel.add(submitButton, gbc);

        // Bottom left portion of UI which lists error conditions which have been added.
        String[] columns = {"Name", "Category", "Value", "Description"};
        tableModel = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setMinWidth(75);
        table.getColumnModel().getColumn(1).setMinWidth(75);

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(400, 200));

        JSplitPane bottomPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomPane.setLeftComponent(tableScrollPane);

        bottomPane.setRightComponent(errorConditionPanel);
        bottomPane.setDividerLocation(0.75);
        bottomPane.setResizeWeight(0.75);
        add(bottomPane);
    }

    public void resizeColumnsToFitContent() {
        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxWidth = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
            }
            TableColumn column = table.getColumnModel().getColumn(col);
            int calculatedWidth = maxWidth + 3;
            int minWidth = column.getMinWidth();
            table.getColumnModel().getColumn(col).setPreferredWidth(Math.max(calculatedWidth, minWidth));
        }
    }
}