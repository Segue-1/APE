import javax.swing.*;
import java.awt.*;

public class ReplacementPanel extends JPanel {

    public String[] labels;
    public JComboBox<ExtractText> dropDown;
    public JTextField[] inputFields;
    public JTable table;
    public ReplacementTableModel tableModel;
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



    ReplacementPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        labels = new String[]{"Name:", "Start string:", "Stop string:", "Selected Text from Request:",
                "Occurrence:", "Pick from Extraction"};

        dropDown = new JComboBox<>();
        inputFields = new JTextField[labels.length - 1];
        fromSelectButton = new JButton();
        addButton = new JButton();

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        JPanel replaceFields = createInputTextFields("Replacement", labels, dropDown,
                inputFields, fromSelectButton, addButton);

        add(replaceFields, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        tableModel = new ReplacementTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setName("Replacement Table");

        JPanel tablePanel = createSmallTable("Replace List", table);

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

    public JPanel createInputTextFields(String header, String[] fieldNames, JComboBox dropDown,
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
            // "Occurrence:" is optional and defaults to the first match, so it carries a placeholder.
            // Matched on the label rather than a fixed index so reordering the fields cannot break it.
            jTextFields[i] = "Occurrence:".equals(fieldNames[i])
                    ? new HintTextField("Optional", FIELD_COLUMNS)
                    : new JTextField(FIELD_COLUMNS);
        }

        for (int i = 0; i < fieldNames.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1;
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
