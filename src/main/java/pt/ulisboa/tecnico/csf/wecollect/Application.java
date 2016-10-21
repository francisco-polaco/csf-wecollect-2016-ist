package pt.ulisboa.tecnico.csf.wecollect;


import pt.ulisboa.tecnico.csf.wecollect.service.ProcessEvtxService;

/**
 * Created by xxlxpto on 08-10-2016.
 */
public class Application {

    public static void main(String[] args){
        System.out.println("Hello CSF!");
        new ProcessEvtxService(null).execute();
    }
}
