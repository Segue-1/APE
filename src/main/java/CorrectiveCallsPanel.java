import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CorrectiveCallsPanel extends JPanel {
    private MontoyaApi montoyaApi;

    static Color correctiveCallsTextColor = new Color(26, 90, 2);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);

    public DefaultTableModel tableModel;
    public JTable table;

    public DefaultTableModel replaceTableModel;
    public JTable replaceTable;

    public DefaultTableModel extractTableModel;
    public JTable extractTable;

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public CorrectiveCallsPanel(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createLabel();
        createTable();
        createHTTPEditors();
        createReplaceExtract();
    }

    private void createLabel() {
        JLabel label = new JLabel("Corrective Calls");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setForeground(correctiveCallsTextColor);
        label.setFont(headerFont);
        add(label, BorderLayout.NORTH);

        JLabel subText = new JLabel("Set up a series of corrective calls which automatically trigger upon detecting " +
                "an error condition.");
        subText.setAlignmentX(Component.LEFT_ALIGNMENT);
        subText.setFont(subTextFont);
        add(subText, BorderLayout.NORTH);


        JSeparator sep = new JSeparator();
        sep.setForeground(Color.LIGHT_GRAY);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        add(sep);


    }

    private void createTable() {
        String[] columns = {"Call number", "Host", "Method", "Path"};
        tableModel = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        //table.getColumnModel().getColumn(0).setMinWidth(75);
        //table.getColumnModel().getColumn(1).setMinWidth(75);

        JScrollPane replaceTableScrollPane = new JScrollPane(table);
        replaceTableScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT); // For some reason, this was shifting the label right.
        replaceTableScrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 125));
        replaceTableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));

        add(replaceTableScrollPane);
    }

    private void createHTTPEditors() {
        requestEditor = montoyaApi.userInterface().createHttpRequestEditor();
        responseEditor = montoyaApi.userInterface().createHttpResponseEditor();
        /*
        requestEditor.uiComponent().setPreferredSize(new Dimension(300, 300));
        requestEditor.uiComponent().setMaximumSize(new Dimension(300,300));
        responseEditor.uiComponent().setPreferredSize(new Dimension(300, 300));
        responseEditor.uiComponent().setMaximumSize(new Dimension(300,300));

         */

        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorPane.setLeftComponent(requestEditor.uiComponent());
        editorPane.setRightComponent(responseEditor.uiComponent());
        editorPane.setDividerLocation(0.5);
        editorPane.setResizeWeight(0.5);
        editorPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 400));
        //editorPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));
        add(editorPane, BorderLayout.CENTER);
    }

    private void createReplaceExtract() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; // Each component within this GridbagLayout fills its own space
        gbc.weightx = 1; // All cells grow along X axis equally.
        gbc.insets = new Insets(2, 2, 2, 2);

        // Row 0: Labels (don't grow vertically)
        gbc.gridy = 0; gbc.weighty = 0; gbc.gridwidth = 2;
        gbc.gridx = 0; bottomPanel.add(new JLabel("Text"), gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 2; bottomPanel.add(new JLabel("Replace Table"), gbc);
        gbc.gridwidth = 2;
        gbc.gridx = 3; bottomPanel.add(new JLabel("Text"), gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 5; bottomPanel.add(new JLabel("Extract Table"), gbc);

        // Row 1: Components (grow vertically)
        gbc.gridy = 1; gbc.weighty = 1.0;
        gbc.gridx = 0; bottomPanel.add(new JScrollPane(new JTextArea()), gbc);

        gbc.gridx = 1; bottomPanel.add(new JScrollPane(new JTextArea()), gbc);

        // Table
        String[] replaceColumns = {"Name", "Id"};
        replaceTableModel = new DefaultTableModel(replaceColumns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        replaceTable = new JTable(replaceTableModel);
        JScrollPane replaceTableScrollPane = new JScrollPane(replaceTable);
        replaceTableScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT); // For some reason, this was shifting the label right.

        gbc.gridwidth = 1;
        gbc.gridx = 2; bottomPanel.add(replaceTableScrollPane, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 3; bottomPanel.add(new JScrollPane(new JTextArea()), gbc);
        gbc.gridx = 4; bottomPanel.add(new JScrollPane(new JTextArea()), gbc);

        // Table
        String[] columns = {"Name", "Id"};
        extractTableModel = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        extractTable = new JTable(extractTableModel);
        JScrollPane extractTableScrollPane = new JScrollPane(extractTable);
        extractTableScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT); // For some reason, this was shifting the label right.

        gbc.gridwidth = 1;
        gbc.gridx = 5; bottomPanel.add(extractTableScrollPane, gbc);
        add(bottomPanel);
    }
}