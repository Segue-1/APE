import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;


public class MyFirstExtension implements BurpExtension{
    @Override
    public void initialize(MontoyaApi api) {

        api.extension().setName("My First Extension");
        api.logging().logToOutput("Mischief!");
    }
}
