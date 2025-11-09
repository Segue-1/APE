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

    public void addRow(ReplaceText replaceText) {
        replaceText.index = replaceTextList.size();
        replaceText.setId();
        replaceTextList.add(replaceText);
        Controller.montoyaApi.logging().logToOutput("Hellow from ReplacementTableModel.addRow");
        Controller.montoyaApi.logging().logToOutput(replaceText.toString());
        Controller.montoyaApi.logging().logToOutput(replaceTextList);

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
            default -> null;
        };
    }

    private void resetNames(int start) {
        for (int i = start; i < replaceTextList.size(); i++) {
            replaceTextList.get(i).id = "Extraction-" + --replaceTextList.get(i).index;
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
}
