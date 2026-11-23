import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.*;

public class ErrorDetectionPanel extends JPanel {
    // Color / font
    private static final Color errorTextColor = new Color(198, 39, 3);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font labelFont = new Font("Dialog", Font.PLAIN, 12);
    // Error Detection HTTP Editors
    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    public JComboBox<String> dropdown;
    public JButton submitButton;
    public JTextArea conditionTextField;
    // Shown in place of conditionTextField when the "Header" category is selected.
    public JTextField headerNameField;
    public JTextField headerValueField;
    // Which half of the exchange a Body or Header condition looks at.
    private JRadioButton matchResponseButton;
    public JRadioButton matchRequestButton;
    private JLabel matchSourceLabel;
    private JPanel conditionCards;
    private static final String CARD_SINGLE = "single";
    private static final String CARD_HEADER = "header";

    public ErrorConditionTableModel errorConditionTableModel;
    public JTable errorConditionTable;

    // Extractions from the errored call. Wired by ErrorDetectionController.
    public ExtractionPanel errorExtractionPanel;


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
        // Preferred height, not a cap. Leaves the bottom row its full 250 on a shorter window.
        editorPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 600));
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

        JPanel triggerConditionPanel = createErrorConditionBuilderPanel();
        bottomPanel.add(triggerConditionPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.33;

        errorExtractionPanel = new ExtractionPanel();
        errorExtractionPanel.table.setName("Errored Call Extract Table");
        bottomPanel.add(errorExtractionPanel, gbc);

        add(bottomPanel);
    }

    private JPanel createTable(){
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

    private JPanel createErrorConditionBuilderPanel() {
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

        // Request or response. Disabled for Status Code, which only exists on a response.
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0;

        matchSourceLabel = new JLabel("Match in:");
        matchSourceLabel.setFont(labelFont);
        panel.add(matchSourceLabel, gbc);

        gbc.gridy = 3;
        panel.add(createMatchSourcePanel(), gbc);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        conditionTextField = new JTextArea();
        conditionTextField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        conditionTextField.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(conditionTextField);

        // Header needs name and value fields, the others a single text area. CardLayout swaps between them.
        conditionCards = new JPanel(new CardLayout());
        conditionCards.add(scrollPane, CARD_SINGLE);
        conditionCards.add(createHeaderFieldsPanel(), CARD_HEADER);
        panel.add(conditionCards, gbc);

        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        submitButton = new JButton("Add");
        panel.add(submitButton, gbc);

        dropdown.addActionListener(e -> showConditionCard());
        showConditionCard();

        return panel;
    }

    // Request / Response selector for Body and Header conditions.
    private JPanel createMatchSourcePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        matchResponseButton = new JRadioButton("Response", true);
        matchRequestButton = new JRadioButton("Request");

        ButtonGroup group = new ButtonGroup();
        group.add(matchResponseButton);
        group.add(matchRequestButton);

        panel.add(matchResponseButton, gbc);

        gbc.gridx = 1;
        panel.add(matchRequestButton, gbc);

        return panel;
    }

    // Header name and value fields. Only shown for the Header category.
    private JPanel createHeaderFieldsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0; gbc.weighty = 0;

        JLabel nameLabel = new JLabel("Header");
        nameLabel.setFont(labelFont);
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        headerNameField = new JTextField();
        panel.add(headerNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;

        JLabel valueLabel = new JLabel("Value");
        valueLabel.setFont(labelFont);
        panel.add(valueLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        headerValueField = new JTextField();
        panel.add(headerValueField, gbc);

        // Keeps the two rows at the top of the cell instead of stretched down it.
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    private void showConditionCard() {
        String selected = (String) dropdown.getSelectedItem();

        String card = "Header".equals(selected) ? CARD_HEADER : CARD_SINGLE;
        ((CardLayout) conditionCards.getLayout()).show(conditionCards, card);

        // A status code only exists on a response, so there is nothing to choose for it.
        boolean sourceApplies = !"Status Code".equals(selected);
        matchSourceLabel.setEnabled(sourceApplies);
        matchResponseButton.setEnabled(sourceApplies);
        matchRequestButton.setEnabled(sourceApplies);

        if (!sourceApplies) {
            matchResponseButton.setSelected(true);
        }
    }
}