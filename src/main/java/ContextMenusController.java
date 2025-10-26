import burp.api.montoya.http.*;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import java.awt.*;
import java.util.List;


public class ContextMenusController implements ContextMenuItemsProvider {
    private View view;
    private Model model;
    private Controller controller;
    private ContextMenuEvent contextMenuEvent;
    
    ContextMenusController(View view, Model model, Controller controller) {
        this.view = view;
        this.model = model;
        this.controller = controller;

        attachListeners();
        registerContextMenus();
    }


    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        this.contextMenuEvent = event;
        return List.of(view.contextMenus.sendToErrorDetection, view.contextMenus.sendTocorrectiveCalls);
    }

    private void attachListeners() {
        view.contextMenus.sendToErrorDetection.addActionListener(e -> {
            if (contextMenuEvent.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = contextMenuEvent.messageEditorRequestResponse().get().requestResponse();
                view.errorDetectionPanel.requestEditor.setRequest(reqResp.request());
                view.errorDetectionPanel.responseEditor.setResponse(reqResp.response());
                controller.errorDetectionController.errorCall = reqResp;
            }
        });

        view.contextMenus.sendTocorrectiveCalls.addActionListener(e -> {
            if (contextMenuEvent.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = contextMenuEvent.messageEditorRequestResponse().get().requestResponse();
                view.correctiveCallsPanel.requestEditor.setRequest(reqResp.request());
                view.correctiveCallsPanel.responseEditor.setResponse(reqResp.response());
                view.correctiveCallsPanel.correctiveCallsSetsTableModel.addRequestResponse(reqResp, view.correctiveCallsPanel.correctiveCallsSetsTable.getSelectedRow());
                Controller.montoyaApi.logging().logToOutput(view.correctiveCallsPanel.correctiveCallsSetsTableModel.correctiveCallsSets.get(getSelectedRow()).toString());
                Utils.resizeColumnsToFitContent(view.correctiveCallsPanel.callSequenceTable);
            }
        });
    }

    int getSelectedRow() {
        int index;
        index = view.correctiveCallsPanel.correctiveCallsSetsTable.getSelectedRow();
        return index;
    }

    // Loads context menus.
    private void registerContextMenus() {
        Controller.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }
}
