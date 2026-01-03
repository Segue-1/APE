import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorrectiveCallsSetsTableModel extends AbstractTableModel {
    public List<CorrectiveCallsSet> correctiveCallsSets = new ArrayList<>();
    private String[] columns = {"Name", "Description"};

    CorrectiveCallsSetsTableModel() {
        CorrectiveCallsSet defaultCorrectiveCallsSet = new CorrectiveCallsSet();
        defaultCorrectiveCallsSet.index = 0;
        defaultCorrectiveCallsSet.setName();
        this.addRow(defaultCorrectiveCallsSet);
    }

    @Override
    public int getRowCount() {
        return correctiveCallsSets.size();
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
        CorrectiveCallsSet correctiveCallsSet = correctiveCallsSets.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> correctiveCallsSet.name;
            case 1 -> correctiveCallsSet.description;
            default -> null;
        };
    }

    public void removeRow(int rowIndex) {
        if (rowIndex == 0){
            return;
        }

        correctiveCallsSets.remove(rowIndex);
        resetNames(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void addRow(CorrectiveCallsSet correctiveCallSet) {
        correctiveCallSet.index = correctiveCallsSets.size();
        correctiveCallSet.setName();
        correctiveCallsSets.add(correctiveCallSet);
        fireTableRowsInserted(correctiveCallsSets.size() - 1, correctiveCallsSets.size() - 1);
    }

    private void resetNames(int start) {
        for (int i = start; i < correctiveCallsSets.size(); i++) {
            correctiveCallsSets.get(i).name = "Set " + --correctiveCallsSets.get(i).index;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            return true;
        }
        else
            return false;
    }

    @Override
    public void setValueAt(Object input, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            correctiveCallsSets.get(rowIndex).description = (String) input;
        }
        else
            return;

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void addRequestResponse(HttpRequestResponse reqResp, int rowIndex) {
        // If no row selected, select Set 0, shouldn't be possible.
        if (rowIndex == -1) {
            rowIndex = 0;
        }
        correctiveCallsSets.get(rowIndex).callSequence.add(reqResp);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public CorrectiveCallsSet getCorrectiveCallsSet(int rowIndex) {
        return correctiveCallsSets.get(rowIndex);
    }

    @Override
    public String toString() {
        return "CorrectiveCallsSetsTableModel{" +
                "correctiveCallsSets=" + correctiveCallsSets +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }
}
