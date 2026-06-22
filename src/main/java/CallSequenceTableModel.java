import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class CallSequenceTableModel extends AbstractTableModel implements DeletableTableModel {
    public List<CorrectiveCall> correctiveCallList ;
    private String[] columns = {"Call Number", "Host", "URL", "Method", "Path"};


    @Override
    public int getRowCount() {
        return correctiveCallList.size();
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
        CorrectiveCall correctiveCall = correctiveCallList.get(rowIndex);
        HttpRequestResponse reqResp = correctiveCall.getRequestResponse();
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
    public void removeRow(int rowIndex) {
        correctiveCallList.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void loadCallSequence(List<CorrectiveCall> correctiveCallList) {
        this.correctiveCallList = correctiveCallList;
        fireTableDataChanged();
    }

    public HttpRequestResponse getReqRes(int row) {
        CorrectiveCall correctiveCall = correctiveCallList.get(row);
        return correctiveCall.requestResponse;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
