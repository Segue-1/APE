public class CorrectiveCallsController {
    private CorrectiveCallsPanel correctiveCallsPanel;
    
    CorrectiveCallsController(CorrectiveCallsPanel correctiveCallsPanel, Model model) {
        this.correctiveCallsPanel = correctiveCallsPanel;
        saveCallSequence();
    }

    private void saveCallSequence() {
        disableSaveCallSequence(); // Disable by default
        onRadioButton();
    }

    private void onRadioButton() {
        correctiveCallsPanel.disableRadioButton.addActionListener(e -> disableSaveCallSequence());
        correctiveCallsPanel.enableRadioButton.addActionListener(e -> enableSaveCallSequence());
    }

    private void disableSaveCallSequence() {
        correctiveCallsPanel.saveButton.setEnabled(false);
        correctiveCallsPanel.removeButton.setEnabled(false);
    }

    private void enableSaveCallSequence() {
        correctiveCallsPanel.saveButton.setEnabled(true);
        correctiveCallsPanel.removeButton.setEnabled(true);
    }
}                    

