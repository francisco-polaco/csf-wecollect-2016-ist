package pt.ulisboa.tecnico.csf.wecollect;


import pt.ulisboa.tecnico.csf.wecollect.exception.RegistryAlreadyExistsException;
import pt.ulisboa.tecnico.csf.wecollect.exception.WECollectException;
import pt.ulisboa.tecnico.csf.wecollect.service.ProcessEvtxService;

import java.util.Scanner;

/**
 * Created by xxlxpto on 08-10-2016.
 */
public class Application {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Error running WECollect.");
            return;
        }
        ProcessThread pt = new ProcessThread(args[0]);
        pt.start();
        try {
            pt.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class ProcessThread extends Thread {
        private String arg;

        ProcessThread(String arg) {
            this.arg = arg;
        }


        @Override
        public void run(){
            try {
                new ProcessEvtxService(arg).execute();
            }catch (RegistryAlreadyExistsException e){
                System.out.println(e.getMessage());
                System.out.println("Do you want to force analysis? [y/n]");
                Scanner scanner = new Scanner(System.in);
                String line = scanner.nextLine().toLowerCase();
                if(line.equals("y")){
                    System.out.println("Forcing...");
                    ProcessThread pt2 = new ProcessThread(arg);
                    pt2.start();
                    try {
                        pt2.join();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }else{
                    System.out.println("Cya sir!");
                }
            }
        }

    }
}
