import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.List;


public class ContextMenusController implements ContextMenuItemsProvider {
    private View view;
    private Model model;
    private Controller controller;
    private ContextMenuEvent contextMenuEvent;

    private CorrectiveCallSetTableModel correctiveCallSetTableModel;
    private JTable correctiveCallsSetsTable;

    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;

    
    ContextMenusController(View view, Model model, Controller controller) {
        this.view = view;
        this.model = model;
        this.controller = controller;

        correctiveCallSetTableModel = view.correctiveCallsPanel.correctiveCallSetTableModel;
        correctiveCallsSetsTable = view.correctiveCallsPanel.correctiveCallsSetsTable;

        callSequenceTableModel = view.correctiveCallsPanel.callSequenceTableModel;
        callSequenceTable = view.correctiveCallsPanel.callSequenceTable;

        attachListeners();
        registerContextMenus();
    }


    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        this.contextMenuEvent = event;
        return List.of(view.contextMenus.sendToErrorDetection, view.contextMenus.sendCorrectiveCalls);
    }

    private void attachListeners() {
        view.contextMenus.sendToErrorDetection.addActionListener(e -> {
            if (contextMenuEvent.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = contextMenuEvent.messageEditorRequestResponse().get().requestResponse();

                // Pass call to error detection panel
                view.errorDetectionPanel.requestEditor.setRequest(reqResp.request());
                view.errorDetectionPanel.responseEditor.setResponse(reqResp.response());
                controller.errorDetectionController.errorCall = reqResp;

                // Pass call to follow up call panel
                view.followUpPanel.requestEditor.setRequest(reqResp.request());
               // view.followUpPanel.responseEditor.setResponse(reqResp.response());

                DebugLog.log("Context menu 'Send to Error Detection' was clicked. Call sent to Error Detection panel.");
            }
        });

        view.contextMenus.sendCorrectiveCalls.addActionListener(e -> {
            if (contextMenuEvent.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = contextMenuEvent.messageEditorRequestResponse().get().requestResponse();
                view.correctiveCallsPanel.requestEditor.setRequest(reqResp.request());
                view.correctiveCallsPanel.responseEditor.setResponse(reqResp.response());

                // Stores call in currently selected corrective calls set.
                correctiveCallSetTableModel.addRequestResponse(reqResp, correctiveCallsSetsTable.getSelectedRow());
                DebugLog.log("Context menu 'Send to Corrective Calls' was clicked. " +
                             "Call sent to Corrective Calls panel and stored in corrective call set: " +
                              correctiveCallSetTableModel.correctiveCallSetList.get(getSelectedRow()).toString());

                callSequenceTableModel.fireTableDataChanged();
                callSequenceTable.setRowSelectionInterval(callSequenceTableModel.correctiveCallList.size() - 1, callSequenceTableModel.correctiveCallList.size() - 1);
                Utils.resizeColumnsToFitContent(callSequenceTable);
            }
        });
    }

    int getSelectedRow() {
        int index;
        index = correctiveCallsSetsTable.getSelectedRow();
        return index;
    }

    // Loads context menus.
    private void registerContextMenus() {
        Controller.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }
}
