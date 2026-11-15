import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class ErrorConditionTableModel extends AbstractTableModel {
    public ArrayList<ErrorCondition> errorConditions = new ArrayList<>();
    private String[] columns = {"Name", "Category", "Value", "Description"};

    @Override
    public int getRowCount() {
        return errorConditions.size();
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
        ErrorCondition errorCondition = errorConditions.get(rowIndex);

        switch(columnIndex) {
            case 0: return errorCondition.name;
            case 1: return errorCondition.category;
            case 2: return errorCondition.value;
            case 3: return errorCondition.description;
            default: return null;
        }
    }

    public void removeRow(int rowIndex) {
        errorConditions.remove(rowIndex);
        resetNames(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void addRow(ErrorCondition errorCondition) {
        errorCondition.index =  errorConditions.size();
        errorCondition.setName();
        errorConditions.add(errorCondition);

        fireTableRowsInserted(errorConditions.size() - 1, errorConditions.size() - 1);
    }

    private void resetNames(int start) {
        for (int i = start; i < errorConditions.size(); i++) {
            errorConditions.get(i).name = "Condition " + --errorConditions.get(i).index;
        }
    }

    public ErrorCondition getErrorCondition(int row) {
        return errorConditions.get(row);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
