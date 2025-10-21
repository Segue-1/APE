import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;


public class Controller implements BurpExtension {
    public static MontoyaApi montoyaApi;
    public Model model;
    public View view;

    // This class is automatically instantiated by burp suite. Burp calls initialize first and
    // passes the monotoyaApi object.
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        Controller.montoyaApi = montoyaApi;

        model = new Model();
        view = new View();
        new SettingsController(this);
        new ContextMenusController(this);
        new ErrorDetectionController(this);
        new CorrectiveCallsController(this);
    }
}