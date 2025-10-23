import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;


public class Controller implements BurpExtension {
    public static MontoyaApi montoyaApi;
    private Model model;
    private View view;

    public SettingsController settingsController;
    public ContextMenusController contextMenusController;
    public ErrorDetectionController errorDetectionController;
    public CorrectiveCallsController correctiveCallsController;


    // This class is automatically instantiated by burp suite. Burp calls initialize first and
    // passes the monotoyaApi object.
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        Controller.montoyaApi = montoyaApi;

        model = new Model();
        view = new View();
        settingsController = new SettingsController(view.settingsPanel, model);
        contextMenusController = new ContextMenusController(view, model, this);
        errorDetectionController = new ErrorDetectionController(view.errorDetectionPanel, model);
        correctiveCallsController = new CorrectiveCallsController(view.correctiveCallsPanel, model);
    }
}