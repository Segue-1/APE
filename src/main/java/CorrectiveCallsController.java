public class CorrectiveCallsController {
    private Controller controller;
    
    CorrectiveCallsController(Controller controller) {
        this.controller = controller;
        correctiveCallsPanel();
    }

    private void correctiveCallsPanel() {
        disableSaveCallSequence(); // Disable by default
        onCorrectiveCallsPanel();
    }

    private void onCorrectiveCallsPanel() {
        controller.view.correctiveCallsPanel.disableRadioButton.addActionListener(e -> disableSaveCallSequence());
        controller.view.correctiveCallsPanel.enableRadioButton.addActionListener(e -> enableSaveCallSequence());
    }

    private void disableSaveCallSequence() {
        controller.view.correctiveCallsPanel.saveButton.setEnabled(false);
        controller.view.correctiveCallsPanel.removeButton.setEnabled(false);
    }

    private void enableSaveCallSequence() {
        controller.view.correctiveCallsPanel.saveButton.setEnabled(true);
        controller.view.correctiveCallsPanel.removeButton.setEnabled(true);
    }
}                    

