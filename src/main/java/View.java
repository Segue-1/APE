import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import burp.api.montoya.MontoyaApi;

public class View {
    private MontoyaApi montoyaApi;
    private JPanel mainPanel;

    public View(MontoyaApi montoyaApi){
        this.montoyaApi = montoyaApi;
        initializeUI();
    }

    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Error Detection", createErrorDetectionPanel());
        tabbedPane.addTab("Corrective Calls", createCorrectiveCallsPanel());
        tabbedPane.addTab("Followup Call", createFollwupPanel());
        tabbedPane.addTab("Test Calls", createTestCallsPanel());
        tabbedPane.addTab("Settings", createSettingsPanel());

        mainPanel.add(tabbedPane, BorderLayout.NORTH);

        // Register mainPanel with montoyaApi
        montoyaApi.userInterface().registerSuiteTab("API Processing Engine", mainPanel);
    }

    private JPanel createErrorDetectionPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Error Detection"));
        return panel;
    }

    private JPanel createCorrectiveCallsPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Corrective Calls"));
        return panel;
    }

    private JPanel createFollwupPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Followup Call"));
        return panel;
    }

    private JPanel createTestCallsPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Test Calls"));
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Aligns entire settings menu left
        JLabel panelHeader = new JLabel("Scope");
        panel.add(panelHeader);
        panel.add(createToolSelectorPanel());
        return panel;
    }

    private JPanel createToolSelectorPanel() {
        JPanel panel = new JPanel();
        JPanel checkBoxGrid = new JPanel(new GridLayout(2,7));

        checkBoxGrid.add(new JCheckBox("Repeater", true));
        checkBoxGrid.add(new JCheckBox("Intruder", true));
        checkBoxGrid.add(new JCheckBox("Burp AI", false));
        checkBoxGrid.add(new JCheckBox("Comparer", false));
        checkBoxGrid.add(new JCheckBox("Decoder", false));
        checkBoxGrid.add(new JCheckBox("Extensions", false));
        checkBoxGrid.add(new JCheckBox("Logger", false));
        checkBoxGrid.add(new JCheckBox("Organizer", false));
        checkBoxGrid.add(new JCheckBox("Proxy", false));
        checkBoxGrid.add(new JCheckBox("Scanner", false));
        checkBoxGrid.add(new JCheckBox("Sequencer", false));
        checkBoxGrid.add(new JCheckBox("Suite", false));
        checkBoxGrid.add(new JCheckBox("Target", false));

        panel.add(checkBoxGrid);
        return panel;
    }
}
