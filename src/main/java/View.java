import javax.swing.*;
import java.awt.*;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;


public class View implements ContextMenuItemsProvider {
    private MontoyaApi montoyaApi;

    // Primary GUI panel
    public JPanel mainPanel;

    // Context menus
    public JMenuItem contextMenuErrorDetection;
    public JMenuItem contextMenuCorrectiveCalls;

    public View(MontoyaApi montoyaApi){
        this.montoyaApi = montoyaApi;
        initMainPanel();
        initContextMenus();
    }

    private void initMainPanel() {
        mainPanel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Error Detection", new ErrorDetectionPanel(montoyaApi));
        tabbedPane.addTab("Corrective Calls", new CorrectiveCallsPanel());
        tabbedPane.addTab("Followup Call", new FollowUpPanel());
        tabbedPane.addTab("Test Calls", new TestCallsPanel());
        tabbedPane.addTab("Settings", new SettingsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        montoyaApi.userInterface().registerSuiteTab("API Processing Engine", mainPanel);
    }

    private void initContextMenus(){
        contextMenuErrorDetection = new JMenuItem("Send to Error Detection");
        contextMenuCorrectiveCalls = new JMenuItem("Send to Corrective Calls");
    }
}
