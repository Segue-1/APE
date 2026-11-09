import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ExtractionTableModel extends AbstractTableModel implements DeletableTableModel {
    public List<ExtractText> extractTextList;
    private String[] columns;

    ExtractionTableModel() {
        extractTextList =  new ArrayList<>();
        columns = new String[]{"Name", "Id"};
    }

    @Override
    public int getRowCount() {
        return extractTextList.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }

    public void addRow(ExtractText extractText) {
        extractText.index = extractTextList.size();
        extractText.setId();
        extractTextList.add(extractText);
        fireTableRowsInserted(extractTextList.size() - 1, extractTextList.size() - 1);
    }

    @Override
    public void removeRow(int rowIndex) {
        extractTextList.remove(rowIndex);
        resetNames(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    private void resetNames(int start) {
        for (int i = start; i < extractTextList.size(); i++) {
            extractTextList.get(i).id = "Extraction-" + --extractTextList.get(i).index;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ExtractText extractText = extractTextList.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> extractText.name;
            case 1 -> extractText.id;
            default -> null;
        };
    }

    public void loadExtractList(List<ExtractText> extractList) {
        this.extractTextList = extractList;
        fireTableDataChanged();
    }

    public ExtractText getExtractText(int rowIndex) {
        return extractTextList.get(rowIndex);
    }

    public void refresh() {
        fireTableDataChanged();
    }
}
