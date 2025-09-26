import burp.api.montoya.MontoyaApi;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    private MontoyaApi montoyaApi;
    private Model model;
    private View view;
    
    public Map<String, Boolean> checkBoxStates;

    public Controller(MontoyaApi montoyaApi){
        this.montoyaApi = montoyaApi;
        model = new Model(montoyaApi);
        view = new View(montoyaApi);

        getToolType();
    }

    
    private void getToolType() {

        checkBoxStates = new HashMap<>();

        JPanel checkBoxGrid = ((JPanel) ((JPanel) ((JPanel) ((JTabbedPane) view.mainPanel.
                getComponent(0)).getComponent(4)).getComponent(1)).getComponent(0));


        for (Component c : checkBoxGrid.getComponents()) {
            JCheckBox cb = (JCheckBox) c;
            montoyaApi.logging().logToOutput(cb.getText() + " " + cb.isSelected());
            checkBoxStates.put(cb.getText(), cb.isSelected());
        }

        montoyaApi.logging().logToOutput("Hashmap check");
        montoyaApi.logging().logToOutput(checkBoxStates);

        // find out how to iterate through stupid fing hashmap
        //checkBoxStates.forEach
    }
}