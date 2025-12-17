import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;


public class Controller implements BurpExtension {
    // TODO don't need to pass around montoyaAPI since it is static
    public static MontoyaApi montoyaApi;
    private Model model;
    private View view;

    public SettingsController settingsController;
    public ContextMenusController contextMenusController;
    public ErrorDetectionController errorDetectionController;
    public CorrectiveCallsController correctiveCallsController;
    public TriggerConditionController triggerConditionController;


    // This class is automatically instantiated by burp suite. Burp calls initialize first and
    // passes the monotoyaApi object.
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        Controller.montoyaApi = montoyaApi;
        Controller.montoyaApi.logging().logToOutput("Welcome to the API Processing Engine (APE)");
        DebugLog.init(montoyaApi);

        view = new View();
        model = new Model();

        settingsController = new SettingsController(view.settingsPanel, model);
        contextMenusController = new ContextMenusController(view, model, this);
        errorDetectionController = new ErrorDetectionController(view.errorDetectionPanel, model);
        correctiveCallsController = new CorrectiveCallsController(view.correctiveCallsPanel, model);
        triggerConditionController = new TriggerConditionController(view.triggerConditionPanel, model);
    }
}