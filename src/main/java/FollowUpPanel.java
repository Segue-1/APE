import javax.swing.*;
import java.awt.*;

public class FollowUpPanel extends JPanel {
    static Color followUpCallTextColor = new Color(9, 30, 172);
    private Font headerFont = new Font("Dialog", Font.BOLD, 14);
    private Font subTextFont = new Font("Dialog", Font.PLAIN, 12);

    FollowUpPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.PINK);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 0;

        add(createLabelPanel(), gbc);

        //gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        add(createConditionBuilderPanel(), gbc);
    }


    private JPanel createLabelPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel();
        JLabel subText = new JLabel();
        GridBagConstraints gbc = new GridBagConstraints();

        // Main label
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;


        label.setText("Followup Call");
        label.setFont(headerFont);
        label.setForeground(followUpCallTextColor);
        panel.add(label, gbc);

        // Sub text
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 1; gbc.weighty = 1;

        subText.setText("Resend the first call which returned an error response");
        subText.setFont(subTextFont);
        subText.setForeground(Color.BLACK);
        panel.add(subText, gbc);

        return panel;
    }

    private JPanel createConditionBuilderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel();
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        label.setText("Select a corrective call set: ");
        panel.setBackground(new Color(129, 216, 112));
        panel.add(label, gbc);

        return panel;
    }
}
