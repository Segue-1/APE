import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ReplacementTableModel extends AbstractTableModel implements DeletableTableModel {
    private List<ReplaceText> replaceTextList = new ArrayList<>();
    private String[] columns = {"Name", "Id", "Extracted Text", "Occurrence"};

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

    public void addRow(ReplaceText replaceText) {
        replaceText.index = replaceTextList.size();
        replaceText.setId();
        replaceTextList.add(replaceText);
        fireTableRowsInserted(replaceTextList.size() - 1, replaceTextList.size() - 1);
    }

    @Override
    public void removeRow(int rowIndex) {
        replaceTextList.remove(rowIndex);
        resetNames(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ReplaceText replaceText = replaceTextList.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> replaceText.name;
            case 1 -> replaceText.id;
            // extractText is null when the replacement was added without picking anything from the
            // "Pick from Extraction" dropdown, which both add buttons allow. Dereferencing it threw a
            // NullPointerException out of the table renderer, which breaks the whole panel rather than
            // just the one cell.
            case 2 -> replaceText.extractText == null ? "(none)" : replaceText.extractText.name;
            case 3 -> replaceText.occurrence;
            default -> null;
        };
    }

    // Re-numbers the rows after a deletion, via the object's own setId() so the format cannot drift.
    private void resetNames(int start) {
        for (int i = start; i < replaceTextList.size(); i++) {
            replaceTextList.get(i).index--;
            replaceTextList.get(i).setId();
        }
    }

    @Override
    public void setValueAt(Object input, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            replaceTextList.get(rowIndex).name = (String) input;
        }
        else
            return;

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void loadReplaceList(List<ReplaceText> replaceList) {
        this.replaceTextList = replaceList;
        fireTableDataChanged();
    }
}
