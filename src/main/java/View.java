import javax.swing.*;
import java.awt.*;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import java.util.List;

public class View implements ContextMenuItemsProvider {
    private MontoyaApi montoyaApi;

    // Primary GUI panel
    public JPanel mainPanel;
    // Context Menus
    public JMenuItem contextMenuErrorDetection;
    public JMenuItem contextMenuCorrectiveCalls;
    public HttpRequestResponse reqResp;
    // Error Detection HTTP Editors
    public HttpRequestEditor errorDetectionRequestEditor;
    public HttpResponseEditor errorDetectionResponseEditor;
    // Color / font
    static Color errorTextColor = new Color(198, 39, 3);
    static Color correctiveCallsTextColor = new Color(26, 90, 2);
    static Color followUpCallTextColor = new Color(9, 30, 172);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);


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

        // Label / header
        JLabel errorDetectionLabel = new JLabel("Error Detection");
        errorDetectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorDetectionLabel.setForeground(errorTextColor);
        errorDetectionLabel.setFont(headerFont);
        panel.add(errorDetectionLabel, BorderLayout.NORTH);

        // Text below label / header
        JLabel errorDetectionSubText = new JLabel("Select error conditions which will trigger corrective calls.");
        errorDetectionSubText.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorDetectionSubText.setFont(subTextFont);
        panel.add(errorDetectionSubText, BorderLayout.NORTH);

        // Horizontal line below subText / above editors
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.LIGHT_GRAY);  // Change color
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        panel.add(sep);

        // HTTP editors
        /*
        errorDetectionRequestEditor = montoyaApi.userInterface().createRawEditor();
        errorDetectionResponseEditor = montoyaApi.userInterface().createRawEditor();
        */
        errorDetectionRequestEditor = montoyaApi.userInterface().createHttpRequestEditor();
        errorDetectionResponseEditor = montoyaApi.userInterface().createHttpResponseEditor();


        errorDetectionRequestEditor.uiComponent().setPreferredSize(new Dimension(600, 600));
        errorDetectionResponseEditor.uiComponent().setPreferredSize(new Dimension(600, 600));

        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorPane.setLeftComponent(errorDetectionRequestEditor.uiComponent());
        editorPane.setRightComponent(errorDetectionResponseEditor.uiComponent());
        editorPane.setDividerLocation(0.5);
        editorPane.setResizeWeight(0.5);
        panel.add(editorPane, BorderLayout.CENTER);

        // UI elements related to adding error conditions in bottom right.
        // This includes the label, dropdown box, text field, and "Add" button.
        JPanel setErrorConditionPanel = new JPanel();
        setErrorConditionPanel.setLayout(new BoxLayout(setErrorConditionPanel, BoxLayout.Y_AXIS));

        JLabel errorConditionLabel = new JLabel("Error Condition");
        errorConditionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] options = {"Status Code", "Body", "Header"};
        JComboBox<String> dropdown = new JComboBox<>(options);
        dropdown.setMaximumSize(new Dimension(150,30));

        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(100,100));
        textField.setMinimumSize(new Dimension(100,100));

        JButton button = new JButton("Add");

        setErrorConditionPanel.add(errorConditionLabel);
        setErrorConditionPanel.add(dropdown);
        setErrorConditionPanel.add(textField);
        setErrorConditionPanel.add(button);

        // Bottom left portion of UI which lists error conditions which have been added.
        JPanel listErrorConditions = new JPanel();
        listErrorConditions.setLayout(new BoxLayout(listErrorConditions, BoxLayout.Y_AXIS));
        JTextField textField2 = new JTextField();

        listErrorConditions.add(textField2);

        JSplitPane bottomPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomPane.setLeftComponent(listErrorConditions);
        bottomPane.setRightComponent(setErrorConditionPanel);
        bottomPane.setDividerLocation(0.75);
        bottomPane.setResizeWeight(0.75);
        panel.add(bottomPane);

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
    // Consider moving to controller
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        contextMenuErrorDetection = new JMenuItem("Send to Error Detection");

        contextMenuErrorDetection.addActionListener(e -> {
            if (!event.messageEditorRequestResponse().isEmpty()){
                reqResp = event.messageEditorRequestResponse().get().requestResponse();
                errorDetectionRequestEditor.setRequest(reqResp.request());
                errorDetectionResponseEditor.setResponse(reqResp.response());
            }
        });

        contextMenuCorrectiveCalls = new JMenuItem("Send to Corrective Calls");

        return List.of(contextMenuErrorDetection, contextMenuCorrectiveCalls);
    }

}
