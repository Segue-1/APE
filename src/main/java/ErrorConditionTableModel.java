
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class ErrorConditionTableModel extends AbstractTableModel implements DeletableTableModel {
    public ArrayList<ErrorCondition> errorConditions = new ArrayList<>();
    private String[] columns = {"Name", "Category", "Match in", "Header", "Value", "Description"};

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
            // Status Code has no request form, so the source is not meaningful for it.
            case 2: return errorCondition.category.equals("Status Code") ? "" : errorCondition.source();
            case 3: return errorCondition.headerName == null ? "" : errorCondition.headerName;
            case 4: return errorCondition.value;
            case 5: return errorCondition.description;
            default: return null;
        }
    }

    @Override
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

    // Re-numbers the rows after a deletion.
    private void resetNames(int start) {
        for (int i = start; i < errorConditions.size(); i++) {
            errorConditions.get(i).index--;
            errorConditions.get(i).setName();
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
