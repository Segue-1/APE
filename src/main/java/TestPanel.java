import javax.swing.*;
import java.awt.*;

public class TestPanel extends JPanel {
    public TestPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        setBackground(new Color(232, 122, 122));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1; gbc.weighty = 1;

        add(testPanel1(), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        //gbc.weightx = 1; gbc.weighty = 1;

        add(testPanel2(), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        //gbc.weightx = 1; gbc.weighty = 1;

        add(testPanel3(), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        //gbc.weightx = 1; gbc.weighty = 1;

        add(testPanel4() , gbc);
    }

    public JPanel testPanel1 () {
        JPanel testPanel1 = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        testPanel1.setBackground(new Color(234, 199, 55)); // Labels don't have a background

        gbc.weightx = 1.0; // Distribute horizontal space equally
        gbc.weighty = 1.0; // Distribute vertical space equally

        // Create buttons
        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        JButton button3 = new JButton("Button 3");
        JButton button4 = new JButton("Button 4");

        // Button 1 - Top Left (row 0, col 0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        testPanel1.add(button1, gbc);

        // Button 2 - Top Right (row 0, col 1)
        gbc.gridx = 1;
        gbc.gridy = 0;
        testPanel1.add(button2, gbc);

        // Button 3 - Bottom Left (row 1, col 0)
        gbc.gridx = 0;
        gbc.gridy = 1;
        testPanel1.add(button3, gbc);

        // Button 4 - Bottom Right (row 1, col 1)
        gbc.gridx = 1;
        gbc.gridy = 1;
        testPanel1.add(button4, gbc);

        return testPanel1;
    }

    public JPanel testPanel2 () {
        JPanel testPanel2 = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel testLabel2 = new JLabel("testLabel2");
        testPanel2.setBackground(new Color(177, 239, 97)); // Labels don't have a background

        gbc.weightx = 1.0; // Distribute horizontal space equally
        gbc.weighty = 1.0; // Distribute vertical space equally

        // Create buttons
        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        JButton button3 = new JButton("Button 3");
        JButton button4 = new JButton("Button 4");

        // Button 1 - Top Left (row 0, col 0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        testPanel2.add(button1, gbc);

        // Button 2 - Top Right (row 0, col 1)
        gbc.gridx = 1;
        gbc.gridy = 0;
        testPanel2.add(button2, gbc);

        // Button 3 - Bottom Left (row 1, col 0)
        gbc.gridx = 0;
        gbc.gridy = 1;
        testPanel2.add(button3, gbc);

        // Button 4 - Bottom Right (row 1, col 1)
        gbc.gridx = 1;
        gbc.gridy = 1;
        testPanel2.add(button4, gbc);

        return testPanel2;
    }

    public JPanel testPanel3 () {
        JPanel testPanel3 = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        testPanel3.setBackground(new Color(59, 230, 216)); // Labels don't have a background

        gbc.weightx = 1.0; // Distribute horizontal space equally
        gbc.weighty = 1.0; // Distribute vertical space equally

        // Create buttons
        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        JButton button3 = new JButton("Button 3");
        JButton button4 = new JButton("Button 4");

        // Button 1 - Top Left (row 0, col 0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        testPanel3.add(button1, gbc);

        // Button 2 - Top Right (row 0, col 1)
        gbc.gridx = 1;
        gbc.gridy = 0;
        testPanel3.add(button2, gbc);

        // Button 3 - Bottom Left (row 1, col 0)
        gbc.gridx = 0;
        gbc.gridy = 1;
        testPanel3.add(button3, gbc);

        // Button 4 - Bottom Right (row 1, col 1)
        gbc.gridx = 1;
        gbc.gridy = 1;
        testPanel3.add(button4, gbc);

        return testPanel3;
    }

    public JPanel testPanel4 () {
        JPanel testPanel4 = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        testPanel4.setBackground(new Color(55, 91, 234)); // Labels don't have a background

        gbc.weightx = 1.0; // Distribute horizontal space equally
        gbc.weighty = 1.0; // Distribute vertical space equally

        // Create buttons
        JButton button1 = new JButton("Button 1");
        JButton button2 = new JButton("Button 2");
        JButton button3 = new JButton("Button 3");
        JButton button4 = new JButton("Button 4");

        // Button 1 - Top Left (row 0, col 0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        testPanel4.add(button1, gbc);

        // Button 2 - Top Right (row 0, col 1)
        gbc.gridx = 1;
        gbc.gridy = 0;
        testPanel4.add(button2, gbc);

        // Button 3 - Bottom Left (row 1, col 0)
        gbc.gridx = 0;
        gbc.gridy = 1;
        testPanel4.add(button3, gbc);

        // Button 4 - Bottom Right (row 1, col 1)
        gbc.gridx = 1;
        gbc.gridy = 1;
        testPanel4.add(button4, gbc);

        return testPanel4;
    }
}
