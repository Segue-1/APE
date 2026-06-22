import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.*;

public class CorrectiveCallsPanel extends JPanel {
    static Color correctiveCallsTextColor = new Color(26, 90, 2);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public JRadioButton singleSetRadionButton;
    public JRadioButton multiSetRadioButton;

    public JPanel saveCallSequencePanel;

    public CallSequenceTableModel callSequenceTableModel;
    public JTable callSequenceTable;
    public JScrollPane callSequenceScrollPane;

    public CorrectiveCallSetTableModel correctiveCallSetTableModel;
    public JTable correctiveCallsSetsTable;
    public JScrollPane saveCallSequenceScrollPane;

    public JButton addSetBotton;
    public JButton removeSetBotton;

    public ExtractionTableModel extractionTableModel;
    public ReplacementTableModel replacementTableModel;

    public JTable extractionTable;
    public JTable replacementTable;

    public JTextField[] extractInputFields;
    public JTextField[] replaceInputFields;

    public JButton extractFromSelect;
    public JButton replaceFromSelect;

    public JButton extractAddButton;
    public JButton replaceAddButton;

    //public String[] extractDropDownOptions;

    public JComboBox<String> extractDropDown;
    public JComboBox<ExtractText> replaceDropDown;


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
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.125; gbc.weighty = 1;

        topPanel.add(createCorrectiveCallsSetsPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        topPanel.add(createCallSequenceScrollPane(), gbc);

        add(topPanel);
    }

    private JScrollPane createCallSequenceScrollPane() {
        callSequenceTableModel = new CallSequenceTableModel();
        callSequenceTable = new JTable(callSequenceTableModel);
        callSequenceTable.setName("Call Sequence Table"); // Not used

        callSequenceTable.getColumnModel().getColumn(0).setMinWidth(90);
        callSequenceTable.getTableHeader().setReorderingAllowed(false);

        callSequenceScrollPane = new JScrollPane(callSequenceTable);
        callSequenceScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        return callSequenceScrollPane;
    }

    private JPanel createCorrectiveCallsSetsPanel() {
        saveCallSequencePanel = new JPanel(new GridBagLayout());
        saveCallSequencePanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        saveCallSequencePanel.add(createRadioButtonPanel(), gbc);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(singleSetRadionButton);
        buttonGroup.add(multiSetRadioButton);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        
        correctiveCallSetTableModel = new CorrectiveCallSetTableModel();

        correctiveCallsSetsTable = new JTable(correctiveCallSetTableModel);
        correctiveCallsSetsTable.setRowSelectionInterval(0,0);
        correctiveCallsSetsTable.getColumnModel().getColumn(0).setMaxWidth(75);
        correctiveCallsSetsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saveCallSequenceScrollPane = new JScrollPane(correctiveCallsSetsTable);
        saveCallSequencePanel.add(saveCallSequenceScrollPane, gbc);

        gbc.gridy = 2;
        gbc.weighty = 0;

        saveCallSequencePanel.add(createCorrectiveCallsSetsButtonPanel(), gbc);
        return saveCallSequencePanel;
    }

    private JPanel createRadioButtonPanel(){
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        singleSetRadionButton = new JRadioButton("Single Set", true);
        panel.add(singleSetRadionButton, gbc);

        gbc.gridx = 1;

        multiSetRadioButton = new JRadioButton("Mulitple Sets");
        panel.add(multiSetRadioButton, gbc);

        return panel;
    }

    private JPanel createCorrectiveCallsSetsButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        addSetBotton = new JButton("Add");
        panel.add(addSetBotton, gbc);

        gbc.gridx = 1;

        removeSetBotton = new JButton("Remove");
        panel.add(removeSetBotton, gbc);

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

        JPanel extractionPanel = new ExtractionPanel();

        bottomPanel.add(extractionPanel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        JPanel replacementPanel = new ReplacementPanel();

        bottomPanel.add(replacementPanel, gbc);

        add(bottomPanel);
        /*

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        String[] replaceFieldsLabels = {"Name:", "Start string:", "Stop string:", "Selected Text from Request:",
                                        "Pick from Extraction"};
        replaceDropDown = new JComboBox<>();
        replaceInputFields = new JTextField[replaceFieldsLabels.length - 1];
        replaceFromSelect = new JButton();
        replaceAddButton = new JButton();

        JPanel replaceFields = createInputTextFields("Replacement", replaceFieldsLabels, replaceDropDown,
                replaceInputFields, replaceFromSelect, replaceAddButton);

        bottomPanel.add(replaceFields, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        replacementTableModel = new ReplacementTableModel();
        replacementTable = new JTable(replacementTableModel);
        replacementTable.setName("Replacement Table");

        JPanel replaceTablePanel = createSmallTable("Replace List", replacementTable);
        bottomPanel.add(replaceTablePanel, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        String[] extractFieldsLabels  = {"Name:", "Start string:", "Stop string:", "Selected Text from Response:",
                                         "URL encode/decode"};
        String[] extractDropDownOptions = new String[]{"N/A", "Encode", "Decode"};
        extractInputFields = new JTextField[replaceFieldsLabels.length - 1];
        extractFromSelect = new JButton();
        extractAddButton = new JButton();
        extractDropDown = new JComboBox<>(extractDropDownOptions);

        JPanel extractFields = createInputTextFields("Extraction", extractFieldsLabels, extractDropDown,
                extractInputFields, extractFromSelect, extractAddButton);

        bottomPanel.add(extractFields, gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        extractionTableModel = new ExtractionTableModel();
        extractionTable = new JTable(extractionTableModel);
        extractionTable.setName("Extraction Table");

        JPanel extractTablePanel = createSmallTable("Extract List", extractionTable);
        bottomPanel.add(extractTablePanel, gbc);

        add(bottomPanel);

         */
    }

    public JPanel createSmallTable(String header, JTable table){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(subHeaderFont);
        panel.add(headerLabel, gbc);

        JScrollPane scrollPane = new JScrollPane(table);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        panel.add(scrollPane, gbc);

        return panel;
    }

    public JPanel createInputTextFields(String header, String[] fieldNames, JComboBox dropDown,
                                        JTextField[] jTextFields, JButton fromSelect, JButton addButton){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.gridwidth = 2;

        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(subHeaderFont);
        panel.add(headerLabel, gbc);

        gbc.weighty = 1;
        gbc.gridwidth = 1;

        for (int i = 0; i < jTextFields.length; i++) {
            jTextFields[i] = new JTextField();
        }

        for (int i = 0; i < fieldNames.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1;
            gbc.weightx = 1;
            panel.add(new JLabel(fieldNames[i]), gbc);

            if (i < fieldNames.length - 1) {
                gbc.gridx = 1;
                panel.add(jTextFields[i], gbc);
            }
        }

        gbc.gridx = 1; gbc.gridy = fieldNames.length;
        panel.add(dropDown, gbc);

        gbc.gridx = 0; gbc.gridy = fieldNames.length + 1;
        gbc.weightx = 0; gbc.weighty = 1;
        fromSelect.setText("From Selection");
        panel.add(fromSelect, gbc);

        gbc.gridx = 1; gbc.gridy = fieldNames.length + 1;
        gbc.weightx = 0;
        addButton.setText("Add");
        panel.add(addButton, gbc);

        return panel;
    }
}