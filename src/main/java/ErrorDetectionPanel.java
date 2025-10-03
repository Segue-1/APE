import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.*;

public class ErrorDetectionPanel extends JPanel {
    // Color / font
    static Color errorTextColor = new Color(198, 39, 3);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);
    // Error Detection HTTP Editors
    public static HttpRequestEditor errorDetectionRequestEditor;
    public static HttpResponseEditor errorDetectionResponseEditor;


    public ErrorDetectionPanel(MontoyaApi montoyaApi) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Label / header
        JLabel errorDetectionLabel = new JLabel("Error Detection");
        errorDetectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorDetectionLabel.setForeground(errorTextColor);
        errorDetectionLabel.setFont(headerFont);
        add(errorDetectionLabel, BorderLayout.NORTH);

        // Text below label / header
        JLabel errorDetectionSubText = new JLabel("Select error conditions which will trigger corrective calls.");
        errorDetectionSubText.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorDetectionSubText.setFont(subTextFont);
        add(errorDetectionSubText, BorderLayout.NORTH);

        // Horizontal line below subText / above editors
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.LIGHT_GRAY);  // Change color
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        add(sep);

        errorDetectionRequestEditor = montoyaApi.userInterface().createHttpRequestEditor();
        errorDetectionResponseEditor = montoyaApi.userInterface().createHttpResponseEditor();
        errorDetectionRequestEditor.uiComponent().setPreferredSize(new Dimension(600, 600));
        errorDetectionResponseEditor.uiComponent().setPreferredSize(new Dimension(600, 600));

        JSplitPane editorPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editorPane.setLeftComponent(errorDetectionRequestEditor.uiComponent());
        editorPane.setRightComponent(errorDetectionResponseEditor.uiComponent());
        editorPane.setDividerLocation(0.5);
        editorPane.setResizeWeight(0.5);
        add(editorPane, BorderLayout.CENTER);

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
        add(bottomPane);
    }
}
