import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import java.awt.*;
import java.util.List;

public class ContextMenusController implements ContextMenuItemsProvider {
    private Controller controller;
    
    ContextMenusController(Controller controller) {
        this.controller = controller;
        contextMenus();
    }

    // Sets up context menus (right click menu)
    @Override
    public java.util.List<Component> provideMenuItems(ContextMenuEvent event) {
        controller.view.contextMenus.sendToErrorDetection.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = event.messageEditorRequestResponse().get().requestResponse();
                controller.view.errorDetectionPanel.requestEditor.setRequest(reqResp.request());
                controller.view.errorDetectionPanel.responseEditor.setResponse(reqResp.response());
            }
        });

        controller.view.contextMenus.sendTocorrectiveCalls.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = event.messageEditorRequestResponse().get().requestResponse();
                controller.view.correctiveCallsPanel.requestEditor.setRequest(reqResp.request());
                controller.view.correctiveCallsPanel.responseEditor.setResponse(reqResp.response());
            }
        });
        return List.of(controller.view.contextMenus.sendToErrorDetection, controller.view.contextMenus.sendTocorrectiveCalls);
    }

    // Loads context menus.
    private void contextMenus() {
        Controller.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }
}
