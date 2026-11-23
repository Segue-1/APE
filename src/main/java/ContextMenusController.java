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
            try {
                if (contextMenuEvent.messageEditorRequestResponse().isPresent()){
                    HttpRequestResponse reqResp = contextMenuEvent.messageEditorRequestResponse().get().requestResponse();

                    // Pass call to error detection panel
                    view.errorDetectionPanel.requestEditor.setRequest(reqResp.request());
                    view.errorDetectionPanel.responseEditor.setResponse(reqResp.response());
                    // Also set on the Model's error call, which its extractions run against.
                    model.errorCall.requestResponse = reqResp;

                    // Each set's followup call starts as a copy of the errored call.
                    controller.followUpController.setFollowUpCall(reqResp);

                    DebugLog.log("Context menu 'Send to Error Detection' was clicked. Call sent to Error Detection panel.");
                }
            }
            catch (Exception ex) {
                DebugLog.log("[Error] ContextMenusController: 'Send to Error Detection' failed. " + ex);
            }
        });

        view.contextMenus.sendCorrectiveCalls.addActionListener(e -> {
            try {
                if (contextMenuEvent.messageEditorRequestResponse().isPresent()){
                    HttpRequestResponse reqResp = contextMenuEvent.messageEditorRequestResponse().get().requestResponse();
                    view.correctiveCallsPanel.requestEditor.setRequest(reqResp.request());
                    view.correctiveCallsPanel.responseEditor.setResponse(reqResp.response());

                    // No selection maps to Set 0, same as addRequestResponse().
                    int targetRow = correctiveCallsSetsTable.getSelectedRow();
                    if (targetRow == -1) {
                        targetRow = 0;
                    }

                    // Stores call in currently selected corrective calls set.
                    CorrectiveCallSet targetSet = correctiveCallSetTableModel.getCorrectiveCallsSet(targetRow);
                    correctiveCallSetTableModel.addRequestResponse(reqResp, targetRow);
                    DebugLog.log("Context menu 'Send to Corrective Calls' was clicked. " +
                                 "Call sent to Corrective Calls panel and stored in corrective call set: " +
                                  targetSet);

                    // Show the set the call was added to, which may not be the one displayed.
                    callSequenceTableModel.loadCallSequence(targetSet.correctiveCallList);

                    int lastRow = callSequenceTableModel.getRowCount() - 1;
                    if (lastRow >= 0) {
                        callSequenceTable.setRowSelectionInterval(lastRow, lastRow);
                    }
                    Utils.resizeColumnsToFitContent(callSequenceTable);
                }
            }
            catch (Exception ex) {
                DebugLog.log("[Error] ContextMenusController: 'Send to Corrective Calls' failed. " + ex);
            }
        });
    }

    // Loads context menus.
    private void registerContextMenus() {
        Controller.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }
}
