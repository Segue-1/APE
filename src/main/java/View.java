import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;


public class View {
    public JPanel mainPanel;

    public ErrorDetectionPanel errorDetectionPanel;
    public CorrectiveCallsPanel correctiveCallsPanel;
    public FollowUpPanel followUpPanel;
    public TestCallsPanel testCallsPanel;
    public SettingsPanel settingsPanel;

    public ContextMenus contextMenus;


    public View(){
        mainPanel = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Error Detection", errorDetectionPanel = new ErrorDetectionPanel());
        tabbedPane.addTab("Corrective Calls", correctiveCallsPanel = new CorrectiveCallsPanel());
        tabbedPane.addTab("Followup Call", followUpPanel = new FollowUpPanel());
        tabbedPane.addTab("Test Calls", testCallsPanel = new TestCallsPanel());
        tabbedPane.addTab("Settings", settingsPanel = new SettingsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        contextMenus = new ContextMenus();

        Controller.montoyaApi.userInterface().registerSuiteTab("API Processing Engine", mainPanel);
    }

    public void resizeColumnsToFitContent(JTable table) {
        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxWidth = 0;
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, col);
                Component comp = table.prepareRenderer(renderer, row, col);
                maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
            }
            TableColumn column = table.getColumnModel().getColumn(col);
            int calculatedWidth = maxWidth + 3;
            int minWidth = column.getMinWidth();
            table.getColumnModel().getColumn(col).setPreferredWidth(Math.max(calculatedWidth, minWidth));
        }
    }
}