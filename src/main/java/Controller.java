import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.naming.Context;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.EnumSet;


public class Controller {
    private MontoyaApi montoyaApi;
    private Model model;
    private View view;

    public EnumSet<ToolType> enabledTools;

    public Controller (MontoyaApi montoyaApi){
        this.montoyaApi = montoyaApi;
        model = new Model(montoyaApi);
        view = new View(montoyaApi);

        initSettingsPanel();
        initErrorDetectionPanel();
    }

    
    private void initSettingsPanel() {
        getScopeCheckBoxStates();
    }
    
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

    private void initErrorDetectionPanel() {
        // TODO
    }
}