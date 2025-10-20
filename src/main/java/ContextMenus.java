import javax.swing.*;

public class ContextMenus extends JPanel {
    public JMenuItem sendToErrorDetection;
    public JMenuItem sendTocorrectiveCalls;

    ContextMenus() {
        sendToErrorDetection = new JMenuItem("Send to Error Detection");
        sendTocorrectiveCalls = new JMenuItem("Send to Corrective Calls");
    }
}
