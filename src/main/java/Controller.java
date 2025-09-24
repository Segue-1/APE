import burp.api.montoya.MontoyaApi;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    private MontoyaApi montoyaApi;
    private Model model;
    private View view;

    public Controller(MontoyaApi montoyaApi){
        this.montoyaApi = montoyaApi;
        model = new Model(montoyaApi);
        view = new View(montoyaApi);

        getToolType();
    }

    // Either get data from view.mainPanel or update View to have a bunch of checkbox attributes which are set in createToolSelectorPanel
    private void getToolType() {
        Map<String, Boolean> checkboxStates = new HashMap<>();

        // view.mainPanel is the root panel "API Processing Engine"
        // getComponent(0) will get the first child of a panel
        // mainPanel > tabbedPane > panel returned from method calls in tabbedPane
        JTabbedPane zero = (JTabbedPane) view.mainPanel.getComponent(0); // tabbedPane
        montoyaApi.logging().logToOutput(zero.getTitleAt(0)); // Error Detection
        montoyaApi.logging().logToOutput(zero.getTitleAt(1)); // Corrective Calls
        montoyaApi.logging().logToOutput(zero.getTitleAt(2)); // Followup Call
        montoyaApi.logging().logToOutput(zero.getTitleAt(3)); // Test Calls
        montoyaApi.logging().logToOutput(zero.getTitleAt(4)); // Settings
        JPanel one = (JPanel) zero.getComponent(4); // gets the panel returned from createSettingsPanel()
        //JLabel two = (JLabel) one.getComponent(0); // Scope (first child of panel returned from createSettingsPanel())
        JPanel two = (JPanel) one.getComponent(1); // Gets panel returned from createToolSelectorPanel()
        JPanel three = (JPanel) two.getComponent(0); // checkBoxGrid. panel from createToolSelectorPanel() has this as its first child.
        JCheckBox four = (JCheckBox) three.getComponent(0); // JCheckBox which are the children of checkBoxGrid.
        montoyaApi.logging().logToOutput(four.getText()); // Repeater


        for (int i = 0; i < 13; i++) {
            //JCheckBox checkbox = (JCheckBox) ((JPanel) ((JTabbedPane) ((JPanel) view.mainPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(i);
            //checkboxStates.put(checkbox.getText(), checkbox.isSelected());
            //System.out.println(checkbox.getText());


            //JCheckBox three = (JCheckBox) two.getComponent(0);
            //System.out.println(three.getText());

        }
    }
}
