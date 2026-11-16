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




    public ReplacementPanel replacementPanel;
    public ExtractionPanel extractionPanel;

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
        callSequenceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        replacementPanel = new ReplacementPanel();
        this.replacementTableModel = replacementPanel.tableModel;
        bottomPanel.add(replacementPanel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        extractionPanel = new ExtractionPanel();
        this.extractionTableModel = extractionPanel.tableModel;
        bottomPanel.add(extractionPanel, gbc);

        add(bottomPanel);
    }
}