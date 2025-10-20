import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import java.awt.*;
import java.util.List;

public class ContextMenusController implements ContextMenuItemsProvider {
    private ContextMenus contextMenus;
    private ErrorDetectionPanel errorDetectionPanel;
    private CorrectiveCallsPanel correctiveCallsPanel;
    
    ContextMenusController(ContextMenus contextMenus,
                           ErrorDetectionPanel errorDetectionPanel,
                           CorrectiveCallsPanel correctiveCallsPanel)
    {
        this.contextMenus = contextMenus;
        this.errorDetectionPanel = errorDetectionPanel;
        this.correctiveCallsPanel = correctiveCallsPanel;
        contextMenus();
    }

    // Sets up context menus (right click menu)
    @Override
    public java.util.List<Component> provideMenuItems(ContextMenuEvent event) {
        contextMenus.sendToErrorDetection.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = event.messageEditorRequestResponse().get().requestResponse();
                errorDetectionPanel.requestEditor.setRequest(reqResp.request());
                errorDetectionPanel.responseEditor.setResponse(reqResp.response());
            }
        });

        contextMenus.sendTocorrectiveCalls.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = event.messageEditorRequestResponse().get().requestResponse();
                correctiveCallsPanel.requestEditor.setRequest(reqResp.request());
                correctiveCallsPanel.responseEditor.setResponse(reqResp.response());
            }
        });
        return List.of(contextMenus.sendToErrorDetection, contextMenus.sendTocorrectiveCalls);
    }

    // Loads context menus.
    private void contextMenus() {
        Controller.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }
}
