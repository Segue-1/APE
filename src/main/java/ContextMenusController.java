import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import java.awt.*;
import java.util.List;

import static burp.api.montoya.http.HttpService.httpService;

public class ContextMenusController implements ContextMenuItemsProvider {
    private View view;
    private Model model;
    private Controller controller;
    
    ContextMenusController(View view, Model model, Controller controller) {
        this.view = view;
        this.model = model;
        this.controller = controller;
        contextMenus();
    }

    // Sets up context menus (right click menu)
    @Override
    public java.util.List<Component> provideMenuItems(ContextMenuEvent event) {
        view.contextMenus.sendToErrorDetection.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = event.messageEditorRequestResponse().get().requestResponse();
                view.errorDetectionPanel.requestEditor.setRequest(reqResp.request());
                view.errorDetectionPanel.responseEditor.setResponse(reqResp.response());
                controller.errorDetectionController.errorCall = reqResp;
            }
        });

        view.contextMenus.sendTocorrectiveCalls.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = event.messageEditorRequestResponse().get().requestResponse();
                view.correctiveCallsPanel.requestEditor.setRequest(reqResp.request());
                view.correctiveCallsPanel.responseEditor.setResponse(reqResp.response());
                controller.correctiveCallsController.correctiveCalls.add(reqResp);
                // TODO Fix this, needs to populate table
                //HttpService httpService = reqResp.httpService();
                //view.correctiveCallsPanel.callSequenceTableModel.addRow(new Object[]{"1", httpService.host(), httpService.toString(), httpService.port()});
                view.correctiveCallsPanel.callSequenceTableModel.addRow(new Object[]{"test", "test", "test", "test"});
            }
        });
        return List.of(view.contextMenus.sendToErrorDetection, view.contextMenus.sendTocorrectiveCalls);
    }

    // Loads context menus.
    private void contextMenus() {
        Controller.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }
}
