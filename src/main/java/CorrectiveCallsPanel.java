import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
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
        createBottomPanel();
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
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        topPanel.setBackground(Color.ORANGE);

        String[] columns = {"Call number", "Host", "Method", "Path"};
        tableModel = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        JScrollPane replaceTableScrollPane = new JScrollPane(table);
        replaceTableScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        replaceTableScrollPane.setPreferredSize(new Dimension(400, 160));
        replaceTableScrollPane.setMaximumSize(new Dimension(400, 160));

        topPanel.add(replaceTableScrollPane, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        JPanel testPanel = new JPanel();
        testPanel.setBackground(Color.PINK);
        topPanel.add(testPanel, gbc);

        /*
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton disableRadioButton = new JRadioButton("Single Set", true);
        JRadioButton enableRadioButton = new JRadioButton("Mulitple Sets");

        buttonGroup.add(disableRadioButton);
        buttonGroup.add(enableRadioButton);

        topPanel.add(disableRadioButton, gbc);
        //topPanel.add(enableRadioButton, gbc);

         */

        add(topPanel);
    }

    private void createHTTPEditors() {
        requestEditor = montoyaApi.userInterface().createHttpRequestEditor();
        responseEditor = montoyaApi.userInterface().createHttpResponseEditor();

        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorPane.setLeftComponent(requestEditor.uiComponent());
        editorPane.setRightComponent(responseEditor.uiComponent());
        editorPane.setDividerLocation(0.5);
        editorPane.setResizeWeight(0.5);
        editorPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 500));
        add(editorPane, BorderLayout.CENTER);
    }

    public void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 250));
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        String[] fields = {"Fiedl 1", "Fiedl 2", "Fiedl 3", "Fiedl 4", "Fiedl 5", "Fiedl 6"};

        JPanel test1 = createInputFields("Input fields header placehold", fields);
        bottomPanel.add(test1, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        String[] columns = {"Name", "Id"};

        JPanel test2 = createSmallTable("Placeholder Table 1", columns);
        bottomPanel.add(test2, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        JPanel test3 = createInputFields("Input fields header placehold", fields);
        bottomPanel.add(test3, gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        JPanel test4 = createSmallTable("Placeholder Table 2", columns);
        bottomPanel.add(test4, gbc);

        add(bottomPanel);
    }


    public JPanel createSmallTable(String header, String[] columns){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        panel.add(new JLabel(header), gbc);


        TableModel tableModel = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        panel.add(scrollPane, gbc);
        return panel;
    }


    public JPanel createInputFields(String header, String[] fieldNames){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.gridwidth=2;

        panel.add(new JLabel(header), gbc);

        gbc.weighty = 1;
        gbc.gridwidth=1;

        for (int i = 0; i < fieldNames.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1;
            gbc.weightx = 0;

            panel.add(new JLabel(fieldNames[i]), gbc);
            gbc.weightx = 1;
            gbc.gridx = 1;
            panel.add(new JTextField("Placeholder Text"), gbc);
        }

        gbc.gridx = 0; gbc.gridy = fieldNames.length + 1;
        gbc.weightx = 0; gbc.weighty = 1;
        JButton fromSelectionButton = new JButton("From Selection");
        panel.add(fromSelectionButton, gbc);

        gbc.gridx = 1; gbc.gridy = fieldNames.length + 1;
        gbc.weightx = 0;
        JButton addButton = new JButton("Add");
        panel.add(addButton, gbc);

        return panel;
    }
}