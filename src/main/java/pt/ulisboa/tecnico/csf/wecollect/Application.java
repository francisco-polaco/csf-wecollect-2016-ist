package pt.ulisboa.tecnico.csf.wecollect;


import pt.ulisboa.tecnico.csf.wecollect.exception.WECollectException;
import pt.ulisboa.tecnico.csf.wecollect.service.ProcessEvtxService;

/**
 * Created by xxlxpto on 08-10-2016.
 */
public class Application {

    public static void main(String[] args){

        if(args.length != 1){
            System.err.println("Error running WECollect.");
            return;
        }
        try {
            new ProcessEvtxService(args[0]).execute();
        }catch (WECollectException e){
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}
