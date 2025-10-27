import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class CallSequenceTableModel extends AbstractTableModel {
    public List<HttpRequestResponse> callSequence;
    private String[] columns = {"Call Number", "Host", "URL", "Method", "Path"};


    @Override
    public int getRowCount() {
        return callSequence.size();
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
        HttpRequestResponse reqResp = callSequence.get(rowIndex);
        HttpRequest request = reqResp.request();

        switch (columnIndex) {
            case 0: return String.valueOf(rowIndex);
            case 1: return request.httpService().host();
            case 2: return request.url();
            case 3: return request.method();
            case 4: return request.path();
            default: return null;
        }
    }

    public void removeRow(int rowIndex) {
        callSequence.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void loadCallSequence(List<HttpRequestResponse> callSequence) {
        this.callSequence = callSequence;
        fireTableDataChanged();
    }


    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
