import javax.swing.*;
import java.awt.*;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import java.util.List;

public class View implements ContextMenuItemsProvider {
    private MontoyaApi montoyaApi;

    // Primary GUI panel
    public JPanel mainPanel;
    // Context Menus
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
        tabbedPane.addTab("Error Detection", createErrorDetectionPanel());
        tabbedPane.addTab("Corrective Calls", createCorrectiveCallsPanel());
        tabbedPane.addTab("Followup Call", createFollwupPanel());
        tabbedPane.addTab("Test Calls", createTestCallsPanel());
        tabbedPane.addTab("Settings", createSettingsPanel());

        mainPanel.add(tabbedPane, BorderLayout.NORTH);

        montoyaApi.userInterface().registerSuiteTab("API Processing Engine", mainPanel);
    }

    private JPanel createErrorDetectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea requestDisplay = new JTextArea(20,50);
        JTextArea responseDisplay = new JTextArea(20, 50);

        panel.add(new JScrollPane(requestDisplay), BorderLayout.NORTH);
        panel.add(new JScrollPane(responseDisplay), BorderLayout.SOUTH);

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

        // Creates a checkbox for each ToolType in burp and
        // associates a ToolType with each checkbox. Adds them to checkBoxGrid.
        JCheckBox checkBox;
        for (ToolType toolType : ToolType.values()){
            if (toolType.toolName().equals("Repeater") || toolType.toolName().equals("Intruder")){
                checkBox = new JCheckBox(toolType.toolName(), true);
                checkBox.putClientProperty("ToolType", toolType);
            }
            else {
                checkBox = new JCheckBox(toolType.toolName(), false);
                checkBox.putClientProperty("ToolType", toolType);
            }
            checkBoxGrid.add(checkBox);
        }

        panel.add(checkBoxGrid);
        return panel;
    }

    // Loads context menus. Perhaps consider using a nested class or anonymous class
    // for ContextMenuItemsProvider and pass that only.
    private void initContextMenus() {
        montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }

    // Sets up context menus (right click menu)
    // This method comes from the ContextMenuItemsProvider interface.
    // montoya ties the ContextMenuEvent to this method, therefore listeners needed to
    // be added here to access the event.
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        contextMenuErrorDetection = new JMenuItem("Send to Error Detection");
        /*
        contextMenuErrorDetection.addActionListener(e -> {
            if (!event.messageEditorRequestResponse())
        });
         */
        contextMenuCorrectiveCalls = new JMenuItem("Send to Corrective Calls");

        return List.of(contextMenuErrorDetection, contextMenuCorrectiveCalls);
    }

}
