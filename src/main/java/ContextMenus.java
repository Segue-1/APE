import javax.swing.*;

public class ContextMenus extends JPanel {
    public JMenuItem sendToErrorDetection;
    public JMenuItem sendCorrectiveCalls; // wtf is this

    ContextMenus() {
        sendToErrorDetection = new JMenuItem("Send to Error Detection");
        sendCorrectiveCalls = new JMenuItem("Send to Corrective Calls");
    }
}
