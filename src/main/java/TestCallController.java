import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/*
Controller for TestCallPanel. The editors belong to TriggerConditionPanel and are passed in.
*/
public class TestCallController {

    private final TestCallPanel panel;
    private final Model model;
    private final HttpRequestEditor requestEditor;
    private final HttpResponseEditor responseEditor;

    TestCallController(TestCallPanel panel, Model model, HttpRequestEditor requestEditor,
                       HttpResponseEditor responseEditor) {
        this.panel = panel;
        this.model = model;
        this.requestEditor = requestEditor;
        this.responseEditor = responseEditor;

        setTestButtonListener();
        setSelectionListener();
    }

    // Runs on a background thread so the HTTP doesn't freeze the UI. Results are loaded on the EDT.
    private void setTestButtonListener() {
        panel.testButton.addActionListener(e -> {
            panel.testButton.setEnabled(false);
            panel.tableModel.clear();
            DebugLog.log("Test button clicked. Running a test call.");

            new Thread(() -> {
                List<TestCallStep> steps;

                try {
                    steps = model.runTestCall();
                }
                catch (Exception ex) {
                    DebugLog.error("Test call threw.", ex);
                    steps = new ArrayList<>();
                }

                List<TestCallStep> result = steps;

                SwingUtilities.invokeLater(() -> {
                    try {
                        panel.tableModel.loadSteps(result);

                        for (TestCallStep step : result) {
                            DebugLog.log("Test call step: " + step.debugString());
                        }

                        // Land on the first row so the editors are not left blank after a run.
                        if (!result.isEmpty()) {
                            panel.table.setRowSelectionInterval(0, 0);
                        }
                    }
                    catch (Exception ex) {
                        DebugLog.error("TestCallController: Showing test call results failed.", ex);
                    }
                    finally {
                        // Always, or one failed run would leave the Test button disabled for good.
                        panel.testButton.setEnabled(true);
                    }
                });
            }, "ape-test-call").start();
        });
    }

    // Loads the selected step into the editors, or clears them if it has no exchange.
    private void setSelectionListener() {
        panel.table.getSelectionModel().addListSelectionListener(e -> {
            try {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                int selectedRow = panel.table.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                HttpRequestResponse reqResp = panel.tableModel.getReqRes(selectedRow);

                if (reqResp == null) {
                    requestEditor.setRequest(null);
                    responseEditor.setResponse(null);
                    DebugLog.log("Test call step " + selectedRow + " has no exchange to display.");
                    return;
                }

                requestEditor.setRequest(reqResp.request());
                responseEditor.setResponse(reqResp.response());
                DebugLog.log("Loaded test call step " + selectedRow + " into the editors.");
            }
            catch (Exception ex) {
                DebugLog.log("[Error] TestCallController: Loading selected test call step failed. " + ex);
            }
        });
    }
}
