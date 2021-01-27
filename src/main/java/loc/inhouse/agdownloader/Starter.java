package loc.inhouse.agdownloader;

import javafx.application.Application;
import loc.inhouse.cjparser.CJPOption;
import loc.inhouse.cjparser.CJParser;
import loc.inhouse.cjconfigmgr.ConfigManager;

public class Starter {

    public static void main(String... args){
        ConfigManager.init();
        CJParser.addOpts(new CJPOption("help", 'h', "Prints this help message."),
                new CJPOption("headless", 'c', "CLI mode."),
                new CJPOption("user", 'u', "Set user.", 1),
                new CJPOption("password", 'p', "Set password.", 1));
        System.out.println(CJParser.getUsage());

        CJParser.parse(args);

        if(!CJParser.getOpt("headless").enabled())
            Application.launch(Launcher.class);
        else{
            CJPOption user = CJParser.getOpt("user");
            CJPOption pass = CJParser.getOpt("password");
            if(user.enabled() && pass.enabled()){
                System.out.println(user.getArgs()[0]);
            }
        }
        //AGHandler handler = new AGHandler();

        //handler.loadMainUc3mPage();
        //handler.login();
        //handler.loadAGPage();
    }
}
