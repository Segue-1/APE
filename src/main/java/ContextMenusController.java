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
                // Also on the Model's error call, so the object the extractions belong to carries the
                // exchange they were configured against.
                model.errorCall.requestResponse = reqResp;

                // Pass call to follow up call panel
                view.followUpPanel.requestEditor.setRequest(reqResp.request());
                controller.followUpController.setfollowUpCall();
               // view.followUpPanel.responseEditor.setResponse(reqResp.response());

                DebugLog.log("Context menu 'Send to Error Detection' was clicked. Call sent to Error Detection panel.");
            }
        });

        view.contextMenus.sendCorrectiveCalls.addActionListener(e -> {
            if (contextMenuEvent.messageEditorRequestResponse().isPresent()){
                HttpRequestResponse reqResp = contextMenuEvent.messageEditorRequestResponse().get().requestResponse();
                view.correctiveCallsPanel.requestEditor.setRequest(reqResp.request());
                view.correctiveCallsPanel.responseEditor.setResponse(reqResp.response());

                /*
                Resolve the target row once. addRequestResponse() maps -1 to Set 0, and the log below
                used to re-read the selection without that mapping and index the list with -1.
                 */
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

                /*
                Point the call sequence table at the set the call was actually added to. It could still
                be showing a different set's list, in which case fireTableDataChanged() on its own
                redrew the wrong calls and the row selection below could land out of range.
                 */
                callSequenceTableModel.loadCallSequence(targetSet.correctiveCallList);

                int lastRow = callSequenceTableModel.getRowCount() - 1;
                if (lastRow >= 0) {
                    callSequenceTable.setRowSelectionInterval(lastRow, lastRow);
                }
                Utils.resizeColumnsToFitContent(callSequenceTable);
            }
        });
    }

    // Loads context menus.
    private void registerContextMenus() {
        Controller.montoyaApi.userInterface().registerContextMenuItemsProvider(this);
    }
}
