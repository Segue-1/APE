import javax.swing.*;
import java.awt.*;

public class ExtractionPanel extends JPanel {

    public String[] labels;
    public JComboBox<String> dropDown;
    public JTextField[] inputFields;
    public JTable table;
    public ExtractionTableModel tableModel;
    public ButtonGroup buttonGroup;
    public JRadioButton[] radioButtons;
    public JButton fromSelectButton;
    public JButton addButton;
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    /*
    Column count for the input fields. A JTextField built with no column count reports a preferred width
    based on whatever text it currently holds, so "From Selection" dropping a long selected string into a
    field grew that field's preferred width and GridBagLayout re-laid out the whole panel. With a column
    count the preferred width is fixed and setText() no longer moves anything. fill = BOTH still expands
    the fields to the full cell, so this is only a floor, not the displayed width.
     */
    private static final int FIELD_COLUMNS = 8;



    ExtractionPanel(){
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        labels = new String[]{"Name:", "Start string:", "Stop string:", "Selected Text from Response:",
                "URL encode/decode:", "Extract from: "};

        // The "URL encode/decode:" selector. Was created empty, so getSelectedItem() returned null and
        // the feature did nothing. Populated here; the controller stores the choice on the ExtractText
        // and Model applies it to the extracted value.
        dropDown = new JComboBox<>(new String[]{Utils.URL_NONE, Utils.URL_ENCODE, Utils.URL_DECODE});
        inputFields = new JTextField[labels.length - 1];
        fromSelectButton = new JButton();
        addButton = new JButton();

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        JPanel extractFields = createInputTextFields("Extract", labels, dropDown, createRadionButtonPanel(),
                inputFields, fromSelectButton, addButton);

        add(extractFields, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        tableModel = new ExtractionTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setName("Extract Table");

        JPanel tablePanel = createSmallTable("Extract List", table);

        add(tablePanel, gbc);
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

    public JPanel createInputTextFields(String header, String[] fieldNames, JComboBox dropDown, JPanel radioButtonPanel,
                                        JTextField[] jTextFields, JButton fromSelect, JButton addButton){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(subHeaderFont);

        panel.add(headerLabel, gbc);

        gbc.weighty = 1;

        for (int i = 0; i < jTextFields.length; i++) {
            jTextFields[i] = new JTextField(FIELD_COLUMNS);
        }

        for (int i = 0; i < fieldNames.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1;
            panel.add(new JLabel(fieldNames[i]), gbc);

            if (i < fieldNames.length - 2) {
                gbc.gridx = 1;
                panel.add(jTextFields[i], gbc);
            }
        }

        gbc.gridx = 1; gbc.gridy = fieldNames.length - 1;
        panel.add(dropDown, gbc);

        gbc.gridx = 1; gbc.gridy = fieldNames.length;
        panel.add(radioButtonPanel, gbc);

        gbc.gridx = 0; gbc.gridy = fieldNames.length + 2;
        gbc.weightx = 0; gbc.weighty = 1;
        fromSelect.setText("From Selection");
        panel.add(fromSelect, gbc);

        gbc.gridx = 1; gbc.gridy = fieldNames.length + 2;
        gbc.weightx = 0;
        addButton.setText("Add");
        panel.add(addButton, gbc);

        return panel;
    }

    private JPanel createRadionButtonPanel(){
        JPanel panel = new JPanel(new GridBagLayout());

        radioButtons = new JRadioButton[3];
        radioButtons[0] = new JRadioButton("Response", true);
        radioButtons[1] = new JRadioButton("Request");
        buttonGroup = new ButtonGroup();
        buttonGroup.add(radioButtons[0]);
        buttonGroup.add(radioButtons[1]);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        panel.add(radioButtons[0], gbc);

        gbc.gridx = 1;
        panel.add(radioButtons[1], gbc);


        return panel;
    }
}
