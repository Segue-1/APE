import javax.swing.*;
import java.awt.*;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;


public class View implements ContextMenuItemsProvider {

    // Primary GUI panel
    public JPanel mainPanel;
    public ErrorDetectionPanel errorDetectionPanel;
    public CorrectiveCallsPanel correctiveCallsPanel;
    public FollowUpPanel followUpPanel;
    public TestCallsPanel testCallsPanel;
    public SettingsPanel settingsPanel;

    // Context menus
    public ContextMenus contextMenus;

    public View(){
        contextMenus = new ContextMenus();
        initMainPanel();
    }

    private void initMainPanel() {
        mainPanel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Error Detection", errorDetectionPanel = new ErrorDetectionPanel());
        tabbedPane.addTab("Corrective Calls", correctiveCallsPanel = new CorrectiveCallsPanel());
        tabbedPane.addTab("Followup Call", followUpPanel = new FollowUpPanel());
        tabbedPane.addTab("Test Calls", testCallsPanel = new TestCallsPanel());
        tabbedPane.addTab("Settings", settingsPanel = new SettingsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        Controller.montoyaApi.userInterface().registerSuiteTab("API Processing Engine", mainPanel);
    }
}