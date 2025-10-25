import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

public class CorrectiveCallsPanel extends JPanel {
    static Color correctiveCallsTextColor = new Color(26, 90, 2);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public JRadioButton disableRadioButton;
    public JRadioButton enableRadioButton;

    public JPanel saveCallSequencePanel;

    public RequestResponseTableModel callSequenceTableModel;
    public JTable callSequenceTable;
    public JScrollPane callSequenceScrollPane;

    public DefaultTableModel saveCallSequenceTableModel;
    public JTable saveCallSequenceTable;
    public JScrollPane saveCallSequenceScrollPane;

    public JButton saveButton;
    public JButton removeButton;

    public CorrectiveCallsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        createLabel();
        createTopPanel();
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

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 150));
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        topPanel.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        topPanel.add(createCallSequenceTable(), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.125; gbc.weighty = 1;

        topPanel.add(createSaveCallSequencePanel(), gbc);

        add(topPanel);
    }

    private JScrollPane createCallSequenceTable() {
        callSequenceTableModel = new RequestResponseTableModel();
        callSequenceTable = new JTable(callSequenceTableModel);

        callSequenceTable.getColumnModel().getColumn(0).setMinWidth(75);
        callSequenceTable.getTableHeader().setReorderingAllowed(false);

        callSequenceScrollPane = new JScrollPane(callSequenceTable);
        callSequenceScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        return callSequenceScrollPane;
    }

    private JPanel createSaveCallSequencePanel() {
        saveCallSequencePanel = new JPanel(new GridBagLayout());
        saveCallSequencePanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        saveCallSequencePanel.add(createRadioButtonPanel(), gbc);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(disableRadioButton);
        buttonGroup.add(enableRadioButton);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        String[] columns = {"Name", "Description"};
        saveCallSequenceTableModel = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        saveCallSequenceTable = new JTable(saveCallSequenceTableModel);
        saveCallSequenceScrollPane = new JScrollPane(saveCallSequenceTable);
        saveCallSequencePanel.add(saveCallSequenceScrollPane, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;

        saveCallSequencePanel.add(createSaveCallSequenceButtonPanel(), gbc);
        return saveCallSequencePanel;
    }

    private JPanel createRadioButtonPanel(){
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        disableRadioButton = new JRadioButton("Single Set", true);
        panel.add(disableRadioButton, gbc);

        gbc.gridx = 1;

        enableRadioButton = new JRadioButton("Mulitple Sets");
        panel.add(enableRadioButton, gbc);

        return panel;
    }

    private JPanel createSaveCallSequenceButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        saveButton = new JButton("Save");
        panel.add(saveButton, gbc);

        gbc.gridx = 1;

        removeButton = new JButton("Remove");
        panel.add(removeButton, gbc);

        return panel;
    }

    private void createHTTPEditors() {
        requestEditor = Controller.montoyaApi.userInterface().createHttpRequestEditor();
        responseEditor = Controller.montoyaApi.userInterface().createHttpResponseEditor();

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
        bottomPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        String[] replaceFieldsLabels = {"Name:", "Start string:", "Stop string:", "Selected Text from Reqest:", "Pick from Extraction"};
        JPanel replaceFields = createInputFields("Replacement", replaceFieldsLabels);
        bottomPanel.add(replaceFields, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        String[] columns = {"Name", "Id"};

        JPanel replaceTable = createSmallTable("Replace List", columns);
        bottomPanel.add(replaceTable, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        String[] extractFieldsLabels  = {"Name:", "Start string:", "Stop string:", "Selected Text from Response:", "URL encode/decode"};
        JPanel extractFields = createInputFields("Extraction", extractFieldsLabels);
        bottomPanel.add(extractFields, gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        JPanel extractTable = createSmallTable("Extract List", columns);
        bottomPanel.add(extractTable, gbc);

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
            gbc.weightx = 1;
            panel.add(new JLabel(fieldNames[i]), gbc);

            gbc.weightx = 1;
            gbc.gridx = 1;
            JTextField jtextField = new JTextField("placeholder text");
            panel.add(jtextField, gbc);
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