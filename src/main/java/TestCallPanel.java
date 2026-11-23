import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;

/*
The Test Calls sub-panel of TriggerConditionPanel: the Test button and the table of steps.
*/
public class TestCallPanel extends JPanel {

    private static final Color testCallTextColor = new Color(255, 91, 0);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);

    public JButton testButton;
    public TestCallTableModel tableModel;
    public JTable table;

    TestCallPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.33; gbc.weighty = 0;

        add(createLabelPanel(), gbc);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        add(createButtonPanel(), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 0.5;
        gbc.gridwidth = 2;

        tableModel = new TestCallTableModel();
        table = new JTable(tableModel);
        table.setName("TestCallTable");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);

        // Short columns get a fixed width that fits their header and values. Name, Path and Detail
        // share the rest of the width and can be resized.
        fixedWidth(0, "Step", "99");
        fixedWidth(1, "Type", TestCallStep.CORRECTIVE);
        fixedWidth(3, "Method", "DELETE");
        fixedWidth(5, "Status", "503");
        fixedWidth(6, "Outcome", TestCallStep.NO_RESPONSE);
        fixedWidth(7, "Extractions", "9/9");
        fixedWidth(8, "Replacements", "9/9");
        flexibleWidth(2, 110, 120);
        flexibleWidth(4, 120, 220);
        flexibleWidth(9, 150, 320);

        add(createTablePanel("APE's Steps", table), gbc);
    }

    // Fixes a column to the width of its widest sample, header or value, plus padding.
    private void fixedWidth(int column, String... samples) {
        FontMetrics header = table.getTableHeader().getFontMetrics(table.getTableHeader().getFont());
        FontMetrics cell = table.getFontMetrics(table.getFont());

        int width = 0;
        for (String sample : samples) {
            width = Math.max(width, Math.max(header.stringWidth(sample), cell.stringWidth(sample)));
        }
        width += 16;

        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        tableColumn.setMinWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setPreferredWidth(width);
    }

    private void flexibleWidth(int column, int min, int preferred) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        tableColumn.setMinWidth(min);
        tableColumn.setPreferredWidth(preferred);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0; gbc.weighty = 0;

        testButton = new JButton("Test");

        panel.add(testButton, gbc);

        return panel;
    }

    private JPanel createLabelPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel();
        JLabel subText = new JLabel();
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        label.setText("Test Calls");
        label.setFont(headerFont);
        label.setForeground(testCallTextColor);
        panel.add(label, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        subText.setFont(subTextFont);
        subText.setText("Displays step by step what APE does. Send a test call which will execute a trigger condition.");
        panel.add(subText, gbc);

        return panel;
    }

    private JPanel createTablePanel(String header, JTable table) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(subHeaderFont);
        panel.add(headerLabel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 100));
        scrollPane.setMinimumSize(new Dimension(500, 100));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        panel.add(scrollPane, gbc);

        panel.setMinimumSize(new Dimension(400, 100));
        panel.setPreferredSize(new Dimension(400, 100));

        return panel;
    }
}
