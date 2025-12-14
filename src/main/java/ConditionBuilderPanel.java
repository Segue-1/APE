import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ConditionBuilderPanel extends JPanel {
    static Color conditionBuilderTextColor = new Color(143, 0, 197);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subHeaderFont = new Font("Dialog", Font.BOLD, 12);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    ConditionBuilderPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.PINK);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        add(createConditionBuilderPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        add(createTestCallPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 1; gbc.weighty = 2;

        add(createEditorPanel(), gbc);
    }

    private JPanel createConditionBuilderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        panel.setBackground(new Color(0, 131, 11));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = .66; gbc.weighty = 0;

        panel.add(createConditionBuilderLabel(), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = .66; gbc.weighty = 1;

        panel.add(createInputPanel(), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.gridheight = 2;

        TableModel conditionTableModel = new DefaultTableModel();
        JTable conditionTable = new JTable(conditionTableModel);

        panel.add(createSmallTable("Condition Table", conditionTable), gbc);

        return panel;
    }

    private JPanel createConditionBuilderLabel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel();
        JLabel subText = new JLabel();
        GridBagConstraints gbc = new GridBagConstraints();

        // Label
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;


        label.setText("Condition Builder");
        label.setFont(headerFont);
        label.setForeground(conditionBuilderTextColor);
        panel.add(label, gbc);

        // Sub text
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 0;

        subText.setText("Build conditions which trigger a set of corrective calls. " +
                "Select and error condition, a conditional statement, and a corrective call set to trigger.");

        subText.setFont(subTextFont);
        subText.setForeground(Color.BLACK);
        panel.add(subText, gbc);

        panel.setBackground(new Color(151, 131, 21));
        return panel;
    }

    // TODO Fix addErrorCondition method
    private JPanel createInputPanel(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //panel.setBackground(new Color(0, 255, 223));

        panel.add(new JLabel("Call set "));
        JComboBox<String> callSetDropDown = new JComboBox(new String[]{"Set 0", "Set 1", "Set N"});
        panel.add(callSetDropDown);
        panel.add(new JLabel(" will be called if "));
        panel.add(negationDropDownPanel());
        JComboBox<String> errorConditionDropDown = new JComboBox(new String[]{"Error Condition 0", "Error Condition 1", "Error Condition N"});
        panel.add(errorConditionDropDown);

        JButton addErrorConditionButton = new JButton("+");
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                addErrorCondition(panel);
            }
        };

        addErrorConditionButton.addActionListener(actionListener);
        panel.add(addErrorConditionButton);

        JLabel text3 = new JLabel(" is received in the response.");
        panel.add(text3);
        //panel.add(new JLabel(" is received in the response."));

        JButton addConditionButton = new JButton("Add condition");
        panel.add(addConditionButton);

        return panel;
    }

    public JPanel createSmallTable(String header, JTable table){
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        panel.setBackground(new Color(138, 79, 243));

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 1, 1, 1);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        JLabel headerLabel = new JLabel(header);
        headerLabel.setFont(subHeaderFont);
        panel.add(headerLabel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane(table);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;
        panel.add(scrollPane, gbc);

        return panel;
    }

    private JPanel createTestCallPanel(){
        JPanel panel = new JPanel();
        panel.setBackground(new Color(153, 41, 250));
        return panel;
    }

    private JPanel createEditorPanel(){
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(new Color(211, 173, 19));
        requestEditor = Controller.montoyaApi.userInterface().createHttpRequestEditor();
        responseEditor = Controller.montoyaApi.userInterface().createHttpResponseEditor();

        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorPane.setLeftComponent(requestEditor.uiComponent());
        editorPane.setRightComponent(responseEditor.uiComponent());
        editorPane.setDividerLocation(0.5);
        editorPane.setResizeWeight(0.5);
        editorPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 500));

        panel.add(editorPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel negationDropDownPanel() {
        JPanel panel = new JPanel();

        Font buttonFont = new Font("Dialog", Font.PLAIN, 6);

        JButton expandButton = new JButton("▶");
        expandButton.setFont(buttonFont);
        expandButton.setPreferredSize(new Dimension(15, 15));
        JCheckBox notCheckbox = new JCheckBox("NOT");
        notCheckbox.setVisible(false);

        expandButton.addActionListener(actionEvent -> {
            boolean state = notCheckbox.isVisible();
            notCheckbox.setVisible(!state);
            expandButton.setText(state ? "▶" : "▼");
        });

        panel.add(expandButton);
        panel.add(notCheckbox);

        return panel;
    }

    private void addErrorCondition(JPanel panel) {

        Font buttonFont = new Font("Dialog", Font.PLAIN, 12);

        JButton collapseButton = new JButton("-");
        collapseButton.setFont(buttonFont);
        collapseButton.setPreferredSize(new Dimension(15, 15));
        JComboBox<String> errorConditionDropDown = new JComboBox(new String[]{"Error Condition 0", "Error Condition 1", "Error Condition N"});
        JComboBox<String> logicalOperator = new JComboBox<>(new String[]{"AND", "OR"});

        // Remove last 3 components
        int stop = panel.getComponentCount() - 3;
        int start = panel.getComponentCount() - 1;

        ArrayList<Component> componentList = new ArrayList<Component>();
        
        for (int i = start; i >= stop; i--) {
            try {
                componentList.add(0, panel.getComponent(i));
                DebugLog.log("start: " + start);
                DebugLog.log("stop: " + stop);
                panel.remove(i);
                panel.repaint();
                panel.revalidate();
            }
            catch (Exception e) {
                DebugLog.log(e.getMessage());
            }
        }


        panel.add(logicalOperator);
        JPanel negationDropDownPanel = negationDropDownPanel();
        panel.add(negationDropDownPanel);
        panel.add(errorConditionDropDown);
        panel.add(collapseButton);


        for (Component component : componentList) {
            panel.add(component);
        }

        collapseButton.addActionListener(actionEvent -> {
            panel.remove(logicalOperator);
            panel.remove(negationDropDownPanel);
            panel.remove(errorConditionDropDown);
            panel.remove(collapseButton);

            panel.repaint();
            panel.revalidate();
        });


    }
}
