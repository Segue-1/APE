import javax.swing.*;
import java.awt.*;


public class View {
    public JPanel mainPanel;
    public ErrorDetectionPanel errorDetectionPanel;
    public CorrectiveCallsPanel correctiveCallsPanel;
    public FollowUpPanel followUpPanel;
    public ConditionBuilderPanel conditionBuilderPanel;
    public SettingsPanel settingsPanel;
    public ContextMenus contextMenus;
    public TestPanel testPanel;


    public View(){
        mainPanel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Error Detection", errorDetectionPanel = new ErrorDetectionPanel());
        tabbedPane.addTab("Corrective Calls", correctiveCallsPanel = new CorrectiveCallsPanel());
        tabbedPane.addTab("Followup Call", followUpPanel = new FollowUpPanel());
        tabbedPane.addTab("Condition Builder", conditionBuilderPanel = new ConditionBuilderPanel());
        tabbedPane.addTab("Settings", settingsPanel = new SettingsPanel());
        tabbedPane.addTab("Test Panel", testPanel = new TestPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        contextMenus = new ContextMenus();

        Controller.montoyaApi.userInterface().registerSuiteTab("API Processing Engine", mainPanel);
    }
}