import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.naming.ldap.Control;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;


public class CorrectiveCallsController {
    private CorrectiveCallsSetsTableModel correctiveCallsSetsTableModel;
    private JTable correctiveCallsSetsTable;

    private CallSequenceTableModel callSequenceTableModel;
    private JTable callSequenceTable;

    private JRadioButton singleSetRadioButton;
    private JRadioButton multiSetRadioButton;

    private JButton addSetBotton;
    private JButton removeSetBotton;

    public JButton extractAddButton;
    public JButton replaceAddButton;

    public JButton extractFromSelect;
    public JButton replaceFromSelect;

    public JTextField[] extractInputFields;
    public JTextField[] replaceInputFields;

    public ExtractionTableModel extractionTableModel;
    public JTable extractionTable;

    public ReplacementTableModel replacementTableModel;
    public JTable replacementTable;

    public JComboBox<String> extractDropDown;
    public JComboBox<String> replaceDropDown;

    public HttpRequestEditor requestEditor;
    public HttpResponseEditor responseEditor;

    CorrectiveCallsController(CorrectiveCallsPanel correctiveCallsPanel, Model model) {
        setAttributes(correctiveCallsPanel);

        setRadioButtons();
        setTableDeleteContextMenu(callSequenceTable);
        setTableDeleteContextMenu(extractionTable);
        setTableDeleteContextMenu(replacementTable);
        setButtonActionListeners();
        setSelectedCorrectiveCallsSetListener();
        initCallSequenceTable();
        setExtractAddButtonListener();
        setFromSelectionListener();
    }

    private void initCallSequenceTable() {
        List<HttpRequestResponse> set0 = correctiveCallsSetsTableModel.correctiveCallsSets.getFirst().callSequence;
        callSequenceTableModel.loadCallSequence(set0);
    }

    private void setRadioButtons() {
        singleSet(); // Single set by default
        onRadioButton();
    }

    private void onRadioButton() {
        singleSetRadioButton.addActionListener(e -> singleSet());
        multiSetRadioButton.addActionListener(e -> multiSet());
    }

    private void singleSet() {
        addSetBotton.setEnabled(false);
        removeSetBotton.setEnabled(false);
        correctiveCallsSetsTable.setRowSelectionInterval(0, 0);
        correctiveCallsSetsTable.setEnabled(false);
    }

    private void multiSet() {
        addSetBotton.setEnabled(true);
        removeSetBotton.setEnabled(true);
        correctiveCallsSetsTable.setEnabled(true);
    }

    private void setTableDeleteContextMenu(JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");

        // Deletes selected row when "Delete" is clicked.
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                DeletableTableModel model = (DeletableTableModel) table.getModel();
                model.removeRow(selectedRow);
            }
        });

        popupMenu.add(deleteItem);

        // Displays popup menu and selects row.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void setButtonActionListeners() {
        ActionListener addButtonListener = e -> {
            correctiveCallsSetsTableModel.addRow(new CorrectiveCallsSet());
            Utils.resizeColumnsToFitContent(correctiveCallsSetsTable);
        };

        ActionListener removeBottonListener = e -> {
            int selectedRow = correctiveCallsSetsTable.getSelectedRow();
            correctiveCallsSetsTableModel.removeRow(selectedRow);
            callSequenceTableModel.fireTableDataChanged();
            Utils.resizeColumnsToFitContent(correctiveCallsSetsTable);
            correctiveCallsSetsTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        };
        addSetBotton.addActionListener(addButtonListener);
        removeSetBotton.addActionListener(removeBottonListener);
    }

    private void setSelectedCorrectiveCallsSetListener() {
        correctiveCallsSetsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = correctiveCallsSetsTable.getSelectedRow();

                if (selectedRow != -1) {
                    callSequenceTableModel.loadCallSequence(correctiveCallsSetsTableModel.correctiveCallsSets.get(selectedRow).callSequence);
                    Controller.montoyaApi.logging().logToOutput("\n------ FROM SELECTION -----");
                    Controller.montoyaApi.logging().logToOutput(correctiveCallsSetsTableModel.correctiveCallsSets.get(selectedRow).toString());
                }
            }
        });
    }

    private void setExtractAddButtonListener() {
        ActionListener extractAddButtonListener = e -> {
            ExtractText extractText = new ExtractText();
            for (int i = 0; i < extractInputFields.length - 1; i++) {
                if (extractInputFields[i].getText().isEmpty())
                    return;
            }
            extractText.name = extractInputFields[0].getText();
            extractText.startString = extractInputFields[1].getText();
            extractText.stopString = extractInputFields[2].getText();
            extractText.selectedText = extractInputFields[3].getText();
            extractText.urlEncodeDecode = (String) extractDropDown.getSelectedItem();
            extractionTableModel.addRow(extractText);
            Controller.montoyaApi.logging().logToOutput(extractText.toString());
        };
        extractAddButton.addActionListener(extractAddButtonListener);
    }

    // TODO get text from editor which is selected and place into selectedText field
    public void setFromSelectionListener() {
        extractDropDown.addActionListener(e -> {
            String selection = responseEditor.selection().get().contents().toString();
        });
    }


    private void setAttributes(CorrectiveCallsPanel correctiveCallsPanel) {
        correctiveCallsSetsTableModel = correctiveCallsPanel.correctiveCallsSetsTableModel;
        correctiveCallsSetsTable = correctiveCallsPanel.correctiveCallsSetsTable;

        callSequenceTableModel = correctiveCallsPanel.callSequenceTableModel;
        callSequenceTable = correctiveCallsPanel.callSequenceTable;

        singleSetRadioButton = correctiveCallsPanel.singleSetRadionButton;
        multiSetRadioButton = correctiveCallsPanel.multiSetRadioButton;

        addSetBotton = correctiveCallsPanel.addSetBotton;
        removeSetBotton = correctiveCallsPanel.removeSetBotton;

        extractAddButton = correctiveCallsPanel.extractAddButton;
        replaceAddButton = correctiveCallsPanel.replaceAddButton;

        extractFromSelect = correctiveCallsPanel.extractFromSelect;
        replaceFromSelect = correctiveCallsPanel.replaceFromSelect;

        extractInputFields = correctiveCallsPanel.extractInputFields;
        replaceInputFields = correctiveCallsPanel.replaceInputFields;

        extractionTableModel = correctiveCallsPanel.extractionTableModel;
        replacementTableModel = correctiveCallsPanel.replacementTableModel;

        extractDropDown = correctiveCallsPanel.extractDropDown;
        replaceDropDown = correctiveCallsPanel.replaceDropDown;

        extractionTable = correctiveCallsPanel.extractionTable;
        replacementTable = correctiveCallsPanel.replacementTable;

        requestEditor = correctiveCallsPanel.requestEditor;
        responseEditor = correctiveCallsPanel.responseEditor;
    }
}

