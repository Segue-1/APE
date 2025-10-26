import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CorrectiveCallsSetTableModel extends AbstractTableModel {
    public List<HttpRequestResponse> correctiveCalls;
    private String[] columns = {"Call Number", "Host", "URL", "Method", "Path"};

    CorrectiveCallsSetTableModel (List<HttpRequestResponse> set0CorrectiveCalls) {
        this.correctiveCalls = set0CorrectiveCalls;
    }

    @Override
    public int getRowCount() {
        return correctiveCalls.size();
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
        HttpRequestResponse reqResp = correctiveCalls.get(rowIndex);
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
        correctiveCalls.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void addRow(HttpRequestResponse reqResp) {
        correctiveCalls.add(reqResp);
        fireTableRowsInserted(correctiveCalls.size() - 1, correctiveCalls.size() - 1);
    }

    public void loadCorrectiveCalls(List<HttpRequestResponse> correctiveCalls) {
        this.correctiveCalls = correctiveCalls;
    }


    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
