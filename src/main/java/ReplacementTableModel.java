import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ReplacementTableModel extends AbstractTableModel {
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
