import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriggerConditionTableModel extends AbstractTableModel
{
    public List<TriggerCondition> triggerConditions;
    private String[] columns = {"Name", "Call Set", "Error Condition", "Logical Op"};

    TriggerConditionTableModel()
    {
        triggerConditions = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return triggerConditions.size();
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
        TriggerCondition triggerCondition = triggerConditions.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> triggerCondition.name;
            case 1 -> triggerCondition.correctiveCallsSet.name;
            case 2 -> triggerCondition.errorConditions;
            case 3 -> triggerCondition.logicalOperator;
            default -> null;
        };
    }

    public void removeRow(int rowIndex) {
        if (rowIndex == 0){
            return;
        }

        triggerConditions.remove(rowIndex);
        resetNames(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void addRow(TriggerCondition triggerCondition) {
        triggerCondition.index = triggerConditions.size();
        triggerCondition.setName();
        triggerConditions.add(triggerCondition);
        fireTableRowsInserted(triggerConditions.size() - 1, triggerConditions.size() - 1);
    }

    private void resetNames(int start) {
        for (int i = start; i < triggerConditions.size(); i++) {
            triggerConditions.get(i).name = "Set " + --triggerConditions.get(i).index;
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
    public String toString() {
        return "TriggerConditionTableModel{" +
                "triggerConditions=" + triggerConditions +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }
}
