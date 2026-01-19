import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Controller implements BurpExtension {
    // TODO don't need to pass around montoyaAPI since it is static
    public static MontoyaApi montoyaApi;
    private Model model;
    private View view;

    public SettingsController settingsController;
    public ContextMenusController contextMenusController;
    public ErrorDetectionController errorDetectionController;
    public CorrectiveCallsController correctiveCallsController;
    public TriggerConditionController triggerConditionController;


    // This class is automatically instantiated by burp suite. Burp calls initialize first and
    // passes the monotoyaApi object.
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        Controller.montoyaApi = montoyaApi;
        Controller.montoyaApi.logging().logToOutput("Welcome to the API Processing Engine (APE)");
        DebugLog.init(montoyaApi);

        view = new View();
        model = new Model();

        settingsController = new SettingsController(view.settingsPanel, model);
        contextMenusController = new ContextMenusController(view, model, this);
        errorDetectionController = new ErrorDetectionController(view.errorDetectionPanel, model);
        correctiveCallsController = new CorrectiveCallsController(view.correctiveCallsPanel, model);
        triggerConditionController = new TriggerConditionController(view, model, this);
    }

    public void setTableDeleteContextMenu(JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                DeletableTableModel model = (DeletableTableModel) table.getModel();
                DebugLog.log(table.getName() + ": User deleted row " + selectedRow);
                model.removeRow(selectedRow);
                Utils.resizeColumnsToFitContent(table);
            }
        });

        popupMenu.add(deleteItem);

        // Displays popup menu and selects row.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
}