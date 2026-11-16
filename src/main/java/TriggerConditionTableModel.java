import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriggerConditionTableModel extends AbstractTableModel implements DeletableTableModel
{
    public List<TriggerCondition> triggerConditions;
    public Boolean triggerConditionIsSet = false;
    private String[] columns = {"Name", "Condition"};

    TriggerConditionTableModel()
    {
        triggerConditions = new ArrayList<>();
        DebugLog.log("TriggerConditionTableModel: triggerConditionIsSet " + triggerConditionIsSet);
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
            case 1 -> triggerCondition.conditionString;
            default -> null;
        };
    }

    public void removeRow(int rowIndex) {
        triggerConditions.remove(rowIndex);
        resetNames(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);

        if (triggerConditions.isEmpty()) {
            triggerConditionIsSet = false;
            DebugLog.log("TriggerConditionTableModel: No trigger condition set. " + triggerConditionIsSet);
        }
    }

    public void addRow(TriggerCondition triggerCondition) {
        triggerCondition.index = triggerConditions.size();
        triggerCondition.setName();
        triggerConditions.add(triggerCondition);
        fireTableRowsInserted(triggerConditions.size() - 1, triggerConditions.size() - 1);

        if (triggerConditions.size() == 1) {
            triggerConditionIsSet = true;
            DebugLog.log("TriggerConditionTableModel: triggerConditionIsSet " + triggerConditionIsSet);
        }
    }

    /*
    Re-numbers the rows after a deletion. Calls the object's own setName() rather than rebuilding
    the string here, which is how ErrorCondition rows ended up renamed from "Error Condition N" to
    "Condition N" after a delete.
     */
    private void resetNames(int start) {
        for (int i = start; i < triggerConditions.size(); i++) {
            triggerConditions.get(i).index--;
            triggerConditions.get(i).setName();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
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
