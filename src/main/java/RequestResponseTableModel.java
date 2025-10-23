import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RequestResponseTableModel extends AbstractTableModel {
    private List<HttpRequestResponse> reqRespList = new ArrayList<>();
    private String[] columns = {"Call Number", "Host", "URL", "Method", "Path"};

    @Override
    public int getRowCount() {
        return reqRespList.size();
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
        HttpRequestResponse reqResp = reqRespList.get(rowIndex);
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

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void removeRow(int rowIndex) {
        reqRespList.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void addRow(HttpRequestResponse reqResp) {
        reqRespList.add(reqResp);
        fireTableRowsInserted(reqRespList.size() - 1, reqRespList.size() - 1);
    }
}
