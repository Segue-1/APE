import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ReplacementTableModel extends AbstractTableModel implements DeletableTableModel {
    private List<ReplaceText> replaceTextList = new ArrayList<>();
    private String[] columns = {"Name", "Id"};

    @Override
    public int getRowCount() {
        return replaceTextList.size();
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
        ReplaceText replaceText = replaceTextList.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> replaceText.name;
            case 1 -> replaceText.id;
            default -> null;
        };
    }

    public void addRow(ReplaceText replaceText) {
        replaceTextList.add(replaceText);
        fireTableRowsInserted(replaceTextList.size() - 1, replaceTextList.size() - 1);
    }

    @Override
    public void removeRow(int rowIndex) {
        replaceTextList.remove(rowIndex);
        resetNames(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    private void resetNames(int start) {
        for (int i = start; i < replaceTextList.size(); i++) {
            replaceTextList.get(i).id = "Extraction-" + --replaceTextList.get(i).index;
        }
    }

    @Override
    public void setValueAt(Object input, int rowIndex, int columnIndex) {
        //TODO Should grab from input text field after hitting add button...
        if (columnIndex == 1) {
            replaceTextList.get(rowIndex).name = (String) input;
        }
        else
            return;

        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
