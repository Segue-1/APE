import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCallTableModel extends AbstractTableModel {
    public List<String> placeholder = new ArrayList<>();
    private String[] columns = {"Step", "Description", "Object", "Object Detail"};

    TestCallTableModel() {
        CorrectiveCallSet defaultCorrectiveCallSet = new CorrectiveCallSet();
        defaultCorrectiveCallSet.index = 0;
        defaultCorrectiveCallSet.setName();
        //this.addRow(defaultCorrectiveCallSet);
    }

    @Override
    public int getRowCount() {
        return placeholder.size();
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
        /*
        CorrectiveCallSet correctiveCallSet = placeholder.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> correctiveCallSet.name;
            case 1 -> correctiveCallSet.description;
            default -> null;
        };
         */
        System.out.println("Placeholder");
        Object value = null;
        return value;
    }

    public void removeRow(int rowIndex) {
        if (rowIndex == 0){
            return;
        }

        placeholder.remove(rowIndex);
        resetNames(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void addRow(String placeholder) {
        /*
        placeholder.index = placeholder.size();
        placeholder.setName();
        placeholder.add(placeholder);
        fireTableRowsInserted(placeholder.size() - 1, placeholder.size() - 1);
         */
    }

    private void resetNames(int start) {
        for (int i = start; i < placeholder.size(); i++) {
            //placeholder.get(i).name = "Set " + --placeholder.get(i).index;
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
        /*
        if (columnIndex == 1) {
            placeholder.get(rowIndex).description = (String) input;
        }
        else
            return;

        fireTableCellUpdated(rowIndex, columnIndex);

         */
    }

    public void addRequestResponse(HttpRequestResponse reqResp, int rowIndex) {
        // If no row selected, select Set 0, shouldn't be possible.
        if (rowIndex == -1) {
            rowIndex = 0;
        }

        //placeholder.get(rowIndex).callSequence.add(reqResp);

        CorrectiveCall correctiveCall = new CorrectiveCall(reqResp);
        //placeholder.get(rowIndex).correctiveCallList.add(correctiveCall);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public Object getCorrectiveCallsSet(int rowIndex) {
        //return placeholder.get(rowIndex);
        System.out.println("placeholder");
        Object value = null;
        return value;
    }

    @Override
    public String toString() {
        return "TestCallTableModel{" +
                "correctiveCallsSets=" + placeholder +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }
}
