import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import java.awt.*;
import java.util.List;

public class ContextMenusController implements ContextMenuItemsProvider {
    private View view;
    private Model model;
    
    ContextMenusController(View view, Model model) {
        this.view = view;
        this.model = model;
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
            }
        });

        view.contextMenus.sendTocorrectiveCalls.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = event.messageEditorRequestResponse().get().requestResponse();
                view.correctiveCallsPanel.requestEditor.setRequest(reqResp.request());
                view.correctiveCallsPanel.responseEditor.setResponse(reqResp.response());
            }
        });
        return List.of(view.contextMenus.sendToErrorDetection, view.contextMenus.sendTocorrectiveCalls);
    }

    // Loads context menus.
    private void contextMenus() {
        Controller.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }
}
