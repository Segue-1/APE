import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.editor.RawEditor;

import java.util.List;

public class View implements ContextMenuItemsProvider {
    private MontoyaApi montoyaApi;

    // Primary GUI panel
    public JPanel mainPanel;
    // Context Menus
    public JMenuItem contextMenuErrorDetection;
    public JMenuItem contextMenuCorrectiveCalls;
    public HttpRequestResponse reqResp;
    // errorHeaderColor / font
    static Color errorHeaderColor = new Color(255, 50, 0);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);


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

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        montoyaApi.userInterface().registerSuiteTab("API Processing Engine", mainPanel);
    }

    private JPanel createErrorDetectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Set up header
        JLabel errorDetectionLabel = new JLabel("Error Detection");
        errorDetectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorDetectionLabel.setForeground(errorHeaderColor);
        errorDetectionLabel.setFont(headerFont);
        errorDetectionLabel.setBorder(new EmptyBorder(5, 0, 20, 0));

        // Set up text under header


        // Set up HTTP editors
        RawEditor requestEditor = montoyaApi.userInterface().createRawEditor();
        RawEditor responseEditor = montoyaApi.userInterface().createRawEditor();

        requestEditor.uiComponent().setPreferredSize(new Dimension(600, 600));
        responseEditor.uiComponent().setPreferredSize(new Dimension(600, 600));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(requestEditor.uiComponent());
        splitPane.setRightComponent(responseEditor.uiComponent());
        splitPane.setDividerLocation(0.5); // Initial 50/50 split
        splitPane.setResizeWeight(0.5); // Both sides resize proportionally

        panel.add(errorDetectionLabel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);



        JPanel setErrorConditionPanel = new JPanel();
        setErrorConditionPanel.setLayout(new BoxLayout(setErrorConditionPanel, BoxLayout.Y_AXIS));

        JLabel testLabel = new JLabel("Test");
        String[] options = {"Option 1", "Option 2", "Option 3"};
        JComboBox<String> dropdown = new JComboBox<>(options);
        JTextField textField = new JTextField();
        JButton button = new JButton("Submit");

        setErrorConditionPanel.add(testLabel);
        setErrorConditionPanel.add(dropdown);
        setErrorConditionPanel.add(textField);
        setErrorConditionPanel.add(button);

        panel.add(setErrorConditionPanel);

        JPanel listErrorConditions = new JPanel();
        listErrorConditions.setLayout(new BoxLayout(listErrorConditions, BoxLayout.Y_AXIS));
        JTextField textField2 = new JTextField();

        listErrorConditions.add(textField2);

        panel.add(listErrorConditions);



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

        contextMenuErrorDetection.addActionListener(e -> {
            if (!event.messageEditorRequestResponse().isEmpty()){
                reqResp = event.messageEditorRequestResponse().get().requestResponse();
            }
        });

        contextMenuCorrectiveCalls = new JMenuItem("Send to Corrective Calls");

        return List.of(contextMenuErrorDetection, contextMenuCorrectiveCalls);
    }

}
