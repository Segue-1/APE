import javax.swing.*;
import java.awt.*;

public class ExtractionPanel extends JPanel {

    public String[] labels;
    public JComboBox<String> dropDown;
    public JTextField[] inputFields;
    public JTable table;
    public ExtractionTableModel tableModel;
    public JButton fromSelectButton;
    public JButton addButton;
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);


    ExtractionPanel(){
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        labels = new String[]{"Name:", "Start string:", "Stop string:", "Selected Text from Response:",
                "URL encode/decode"};

        dropDown = new JComboBox<>();
        inputFields = new JTextField[labels.length - 1];
        fromSelectButton = new JButton();
        addButton = new JButton();

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        JPanel extractFields = createInputTextFields("Extract", labels, dropDown,
                inputFields, fromSelectButton, addButton);

        add(extractFields, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        tableModel = new ExtractionTableModel();
        table = new JTable(tableModel);
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
            jTextFields[i] = new JTextField();
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
