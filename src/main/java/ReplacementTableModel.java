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
    public Object getValueAt(int i, int i1) {
        return null;
    }
}
