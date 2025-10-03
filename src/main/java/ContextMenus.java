import javax.swing.*;

public class ContextMenus extends JPanel {
    public JMenuItem errorDetection;
    public JMenuItem correctiveCalls;

    ContextMenus() {
        errorDetection = new JMenuItem("Send to Error Detection");
        correctiveCalls = new JMenuItem("Send to Corrective Calls");
    }
}
