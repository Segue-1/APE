import javax.swing.*;
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

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.add(createToolSelectorPanel(), BorderLayout.NORTH);
        return panel;

    }

    private JPanel createToolSelectorPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Add your checkboxes
        contentPanel.add(new JCheckBox("Repeater", true));
        contentPanel.add(new JCheckBox("Intruder", true));
        // etc...

        // Add to WEST (left) or use LINE_START
        outerPanel.add(contentPanel, BorderLayout.WEST);

        return outerPanel;
    }
}
