import javax.swing.*;
import java.awt.*;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;

public class View {
    private MontoyaApi montoyaApi;
    public JPanel mainPanel;


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
        JLabel panelLabel = new JLabel("Scope");
        panel.add(panelLabel);
        panel.add(createToolSelectorPanel());
        return panel;
    }

    private JPanel createToolSelectorPanel() {
        JPanel panel = new JPanel();
        JPanel checkBoxGrid = new JPanel(new GridLayout(2,7));

        // Creates a checkbox for each possible ToolType in burp and
        // associates a ToolType with each checkbox.
        JCheckBox checkBox;
        for (ToolType toolType : ToolType.values()){
            if (toolType.toolName().equals("Repeater") || toolType.toolName().equals("Intruder")){
                checkBox = new JCheckBox(toolType.toolName(), true);
                checkBox.putClientProperty(toolType.toolName(), toolType);
            }
            else {
                checkBox = new JCheckBox(toolType.toolName(), false);
                checkBox.putClientProperty(toolType.toolName(), toolType);
            }
            checkBoxGrid.add(checkBox);
        }

        panel.add(checkBoxGrid);
        return panel;
    }
}
