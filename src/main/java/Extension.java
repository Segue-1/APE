import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class Extension implements BurpExtension {
    // Entry point for Burp Suite.
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        Controller controller = new Controller(montoyaApi);
    }
}
