import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;


public class Model implements BurpExtension{
    @Override
    public void initialize(MontoyaApi m_api) {

        m_api.extension().setName("APE");
        m_api.logging().logToOutput("Mischief!");
    }
}
