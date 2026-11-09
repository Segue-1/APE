import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorrectiveCallSetTableModel extends AbstractTableModel {
    public List<CorrectiveCallSet> correctiveCallSetList = new ArrayList<>();
    private String[] columns = {"Name", "Description"};

    CorrectiveCallSetTableModel() {
        CorrectiveCallSet defaultCorrectiveCallSet = new CorrectiveCallSet();
        defaultCorrectiveCallSet.index = 0;
        defaultCorrectiveCallSet.setName();
        this.addRow(defaultCorrectiveCallSet);
    }

    @Override
    public int getRowCount() {
        return correctiveCallSetList.size();
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
        CorrectiveCallSet correctiveCallSet = correctiveCallSetList.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> correctiveCallSet.name;
            case 1 -> correctiveCallSet.description;
            default -> null;
        };
    }

    public void removeRow(int rowIndex) {
        if (rowIndex == 0){
            return;
        }

        correctiveCallSetList.remove(rowIndex);
        resetNames(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void addRow(CorrectiveCallSet correctiveCallSet) {
        correctiveCallSet.index = correctiveCallSetList.size();
        correctiveCallSet.setName();
        correctiveCallSetList.add(correctiveCallSet);
        fireTableRowsInserted(correctiveCallSetList.size() - 1, correctiveCallSetList.size() - 1);
    }

    private void resetNames(int start) {
        for (int i = start; i < correctiveCallSetList.size(); i++) {
            correctiveCallSetList.get(i).name = "Set " + --correctiveCallSetList.get(i).index;
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
            correctiveCallSetList.get(rowIndex).description = (String) input;
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

        CorrectiveCall correctiveCall = new CorrectiveCall(reqResp);
        correctiveCallSetList.get(rowIndex).correctiveCallList.add(correctiveCall);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public CorrectiveCallSet getCorrectiveCallsSet(int rowIndex) {
        return correctiveCallSetList.get(rowIndex);
    }

    @Override
    public String toString() {
        return "CorrectiveCallsSetsTableModel{" +
                "correctiveCallsSets=" + correctiveCallSetList +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }
}
