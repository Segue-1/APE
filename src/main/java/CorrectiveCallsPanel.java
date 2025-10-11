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
        gbc.insets = new Insets(2, 2, 2, 2);

        // Set headers
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.2; gbc.weighty = 0;
        gbc.gridwidth = 2;
        bottomPanel.add(new JLabel("First and second column label"), gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        gbc.weightx = 1; ; gbc.weighty = 0;
        gbc.gridwidth = 1;
        bottomPanel.add(new JLabel("Replace Table"), gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        gbc.weightx = 0.2; gbc.weighty = 0;
        gbc.gridwidth = 2;
        bottomPanel.add(new JLabel("Third and fourth column label"), gbc);

        gbc.gridx = 5; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;
        gbc.gridwidth = 1;
        bottomPanel.add(new JLabel("Extract Table"), gbc);

        // Left side text
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.2; gbc.weighty = 0;
        gbc.gridwidth = 1;

        JLabel label1 = new JLabel("Text");
        bottomPanel.add(label1, gbc);

        gbc.gridy = 2;

        JLabel label2 = new JLabel("Text");
        bottomPanel.add(label2, gbc);

        gbc.gridy = 3;

        JLabel label3 = new JLabel("Text");
        bottomPanel.add(label3, gbc);

        gbc.gridy = 4;

        JLabel label4 = new JLabel("Text");
        bottomPanel.add(label4, gbc);

        gbc.gridy = 5;

        JLabel label5 = new JLabel("Text");
        bottomPanel.add(label5, gbc);

        gbc.gridy = 6; gbc.weighty = 1;

        // filler area
        bottomPanel.add(new JPanel(), gbc);

        gbc.gridy = 7; gbc.weighty = 0;

        JButton addButton = new JButton("Add");
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        addButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        bottomPanel.add(addButton, gbc);
        //gbc.fill = GridBagConstraints.BOTH;

        // Text fields on right
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.weighty = 0;

        JTextField smallTextBox1 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox1, gbc);

        gbc.gridy = 2;

        JTextField smallTextBox2 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox2, gbc);

        gbc.gridy = 3;

        JTextField smallTextBox3 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox3, gbc);

        gbc.gridy = 4;

        JTextField smallTextBox4 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox4, gbc);

        gbc.gridy = 5;

        JTextField smallTextBox5 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox5, gbc);

        gbc.gridy = 6; gbc.weighty = 1;

        // filler area
        bottomPanel.add(new JPanel(), gbc);

        gbc.gridy = 7; gbc.weighty = 0;

        JButton fromSelectionButton = new JButton("From Selection");
        fromSelectionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        fromSelectionButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        bottomPanel.add(fromSelectionButton, gbc);

        gbc.weighty = 0;
        gbc.weightx = 1;

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

        gbc.gridx = 2; gbc.gridy = 1;
        gbc.gridwidth = 1; gbc.gridheight = 7;

        bottomPanel.add(replaceTableScrollPane, gbc);

        // Left side text
        gbc.gridx = 3; gbc.gridy = 1;
        gbc.weightx = 0.2; gbc.weighty = 0;
        gbc.gridwidth = 1; gbc.gridheight = 1;

        JLabel label6 = new JLabel("Text");
        bottomPanel.add(label6, gbc);

        gbc.gridy = 2;

        JLabel label7 = new JLabel("Text");
        bottomPanel.add(label7, gbc);

        gbc.gridy = 3;

        JLabel label8 = new JLabel("Text");
        bottomPanel.add(label8, gbc);

        gbc.gridy = 4;

        JLabel label9 = new JLabel("Text");
        bottomPanel.add(label9, gbc);

        gbc.gridy = 5;

        JLabel label10 = new JLabel("Text");
        bottomPanel.add(label10, gbc);

        gbc.gridy = 6; gbc.weighty = 1;

        // filler area
        JPanel filler = new JPanel();

        // TODO Fix how this filler panel resizes. Not resizing properly.
        filler.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        //filler.setMinimumSize(new Dimension(Integer.MAX_VALUE, 20));
        filler.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
        bottomPanel.add(filler, gbc);

        gbc.gridy = 7; gbc.weighty = 0;

        JButton addButton2 = new JButton("Add");
        addButton2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        addButton2.setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        bottomPanel.add(addButton2, gbc);

        // Text fields on right
        gbc.gridx = 4; gbc.gridy = 1;
        gbc.weighty = 0;

        JTextField smallTextBox6 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox6, gbc);

        gbc.gridy = 2;

        JTextField smallTextBox7 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox7, gbc);

        gbc.gridy = 3;

        JTextField smallTextBox8 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox8, gbc);

        gbc.gridy = 4;

        JTextField smallTextBox9 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox9, gbc);

        gbc.gridy = 5;

        JTextField smallTextBox10 = new JTextField("Text", 15);
        bottomPanel.add(smallTextBox10, gbc);

        gbc.gridy = 6; gbc.weighty = 1;

        // filler area
        bottomPanel.add(filler, gbc);

        gbc.gridy = 7; gbc.weighty = 0;

        JButton fromSelectionButton2 = new JButton("From Selection");
        fromSelectionButton2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        fromSelectionButton2.setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
        bottomPanel.add(fromSelectionButton2, gbc);

        gbc.weighty = 0;
        gbc.weightx = 1;

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
        //extractTableScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT); // For some reason, this was shifting the label right.

        gbc.gridx = 5; gbc.gridy = 1;
        gbc.gridwidth = 1; gbc.gridheight = 7;

        bottomPanel.add(extractTableScrollPane, gbc);
        add(bottomPanel);
    }
}