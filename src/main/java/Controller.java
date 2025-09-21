import burp.api.montoya.MontoyaApi;

public class Controller {
    private MontoyaApi montoyaApi;
    private Model model;
    private View view;

    public Controller(MontoyaApi montoyaApi){
        this.montoyaApi = montoyaApi;
        model = new Model(montoyaApi);
        view = new View(montoyaApi);
    }
}
