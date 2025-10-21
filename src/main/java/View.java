import javax.swing.*;
import java.awt.*;


public class View {
    public JPanel mainPanel;

    public ErrorDetectionPanel errorDetectionPanel;
    public CorrectiveCallsPanel correctiveCallsPanel;
    public FollowUpPanel followUpPanel;
    public TestCallsPanel testCallsPanel;
    public SettingsPanel settingsPanel;

    public ContextMenus contextMenus;


    public View(){
        mainPanel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Error Detection", errorDetectionPanel = new ErrorDetectionPanel());
        tabbedPane.addTab("Corrective Calls", correctiveCallsPanel = new CorrectiveCallsPanel());
        tabbedPane.addTab("Followup Call", followUpPanel = new FollowUpPanel());
        tabbedPane.addTab("Test Calls", testCallsPanel = new TestCallsPanel());
        tabbedPane.addTab("Settings", settingsPanel = new SettingsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        Controller.montoyaApi.userInterface().registerSuiteTab("API Processing Engine", mainPanel);

        contextMenus = new ContextMenus();
    }
}