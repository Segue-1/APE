import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.EnumSet;
import java.util.List;


public class Controller implements ContextMenuItemsProvider {
    private MontoyaApi montoyaApi;
    private Model model;
    private View view;

    public EnumSet<ToolType> enabledTools;

    public Controller (MontoyaApi montoyaApi){
        this.montoyaApi = montoyaApi;
        model = new Model(montoyaApi);
        view = new View(montoyaApi);

        initSettingsPanel();
        initContextMenus();
    }
    
    private void initSettingsPanel() {
        getScopeCheckBoxStates();
    }

    // Tracks the state of the checkboxes in the settings menu for scope
    // TODO: Maybe consider removing GUI elements and find a way to reference them directly from view.
    private void getScopeCheckBoxStates () {
        // Get the parent panel of the checkboxes.
        JPanel checkBoxGrid = ((JPanel) ((JPanel) ((JPanel) ((JTabbedPane) view.mainPanel.
                getComponent(0)).getComponent(4)).getComponent(1)).getComponent(0));

        // Stores state of checkboxes
        enabledTools = EnumSet.noneOf(ToolType.class);

        // Set up a listener which will track checkbox state in enabledTools
        ItemListener scopeCheckBoxListener = e -> {
            JCheckBox source = (JCheckBox) e.getSource();
            ToolType toolType = (ToolType) source.getClientProperty("ToolType");

            if (source.isSelected()) {
                enabledTools.add(toolType);
            } else {
                enabledTools.remove(toolType);
            }
            model.updateEnabledTools(enabledTools);
        };

        // Attach the listener to each checkbox and store initial state of checkboxes
        for (Component component : checkBoxGrid.getComponents()) {
            JCheckBox checkBox = (JCheckBox) component;
            checkBox.addItemListener(scopeCheckBoxListener);

            if (checkBox.isSelected()) {
                ToolType toolType = (ToolType) checkBox.getClientProperty("ToolType");
                enabledTools.add(toolType);
                model.updateEnabledTools(enabledTools);
            }
        }
    }

    // Sets up context menus (right click menu)
    @Override
    public java.util.List<Component> provideMenuItems(ContextMenuEvent event) {
        view.contextMenuErrorDetection.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = event.messageEditorRequestResponse().get().requestResponse();
                ErrorDetectionPanel.errorDetectionRequestEditor.setRequest(reqResp.request());
                ErrorDetectionPanel.errorDetectionResponseEditor.setResponse(reqResp.response());
            }
        });
        return List.of(view.contextMenuErrorDetection, view.contextMenuCorrectiveCalls);
    }

    // Loads context menus. Perhaps consider using a nested class or anonymous class
    // for ContextMenuItemsProvider and pass that only.
    private void initContextMenus() {
        montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }
}