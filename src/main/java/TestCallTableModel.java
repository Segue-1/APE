import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
The Test Calls table: what APE did, step by step, during the last test run.

Populated from Model.runTestCall(). Each row is one request APE sent (or decided not to send), and
selecting a row loads its request/response pair into the editors below, the same way the call
sequence table works.

The columns replace the earlier "Description / Object / Object Detail" placeholders. The table is wide
and this is the only place a user can see why a trigger condition did or did not fire, so it carries
the status code, which conditions hit and missed, and how many extractions and replacements actually
landed rather than merely being configured.
*/
public class TestCallTableModel extends AbstractTableModel implements DeletableTableModel {

    public List<TestCallStep> steps = new ArrayList<>();

    private String[] columns = {
            "Step",          // 1-based order of execution
            "Type",          // Errored Call / Corrective Call / Followup Call
            "Name",          // which set and call, or Followup
            "Method",
            "Path",
            "Status",        // what came back, blank if nothing did
            "Outcome",       // Triggered / No match / Sent / No response / Not run
            "Extractions",   // hit/run, e.g. 2/3
            "Replacements",  // applied/configured, e.g. 1/2
            "Detail"         // which conditions hit and missed, or why a step did not run
    };

    @Override
    public int getRowCount() {
        return steps.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TestCallStep step = steps.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> step.step;
            case 1 -> step.type;
            case 2 -> step.name;
            case 3 -> step.method();
            case 4 -> step.path();
            case 5 -> step.status();
            case 6 -> step.outcome;
            case 7 -> step.extractions();
            case 8 -> step.replacements();
            case 9 -> step.detail;
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    // Replaces the whole table with the results of a run.
    public void loadSteps(List<TestCallStep> steps) {
        this.steps = steps == null ? new ArrayList<>() : steps;
        fireTableDataChanged();
    }

    public void clear() {
        steps = new ArrayList<>();
        fireTableDataChanged();
    }

    public TestCallStep getStep(int rowIndex) {
        return steps.get(rowIndex);
    }

    /*
    The exchange behind a row, or null for a step that never sent anything -- a followup that was not
    configured, or a run that could not start. The controller checks for null before loading the
    editors.
     */
    public HttpRequestResponse getReqRes(int rowIndex) {
        return steps.get(rowIndex).requestResponse;
    }

    @Override
    public void removeRow(int rowIndex) {
        steps.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    @Override
    public String toString() {
        return "TestCallTableModel{" +
                "steps=" + steps +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }
}
