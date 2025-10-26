import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
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

    // Tie into "Save" button which should be renamed "Add"
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
    public boolean isCellEditable(int row, int column) {
        if (column == 1) {
            return true;
        }
        else
            return false;
    }
}
