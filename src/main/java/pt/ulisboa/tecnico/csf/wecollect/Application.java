package pt.ulisboa.tecnico.csf.wecollect;


import pt.ulisboa.tecnico.csf.wecollect.core.Manager;
import pt.ulisboa.tecnico.csf.wecollect.core.database.DatabaseManager;
import pt.ulisboa.tecnico.csf.wecollect.exception.ArgumentException;
import pt.ulisboa.tecnico.csf.wecollect.exception.RegistryAlreadyExistsException;

import java.util.*;

public class Application {

    public static void main(String[] args) {

        final Map<String, List<String>> params;
        try {
            params = getParameterMapReady(args);
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        String winDir;
        if(isParameterValid(params, "windir")){
            winDir = params.get("windir").get(0);
        }else {
            System.err.println(new ArgumentException().getMessage());
            return;
        }

        if(isParameterValid(params, "h")){
            DatabaseManager.hostname = params.get("h").get(0);
            if(isParameterValid(params, "u")){
                DatabaseManager.username = params.get("u").get(0);

                if(isParameterValid(params, "p")) DatabaseManager.password = params.get("p").get(0);
                else DatabaseManager.password = new String(System.console().readPassword("Password: "));
            }else {
                System.err.println("When you specify a hostname, you should also specify username and password.");
                return;
            }
        }

        ProcessThread pt = new ProcessThread(winDir);
        pt.start();
        try {
            pt.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Terminate the program
        DatabaseManager.getInstance().disconnect();
        Manager.getInstance().clearTmp();
        System.out.println("Job done!");
        System.exit(0);
    }

    private static Map<String, List<String>> getParameterMapReady(String[] args) throws ArgumentException {
        final Map<String, List<String>> params = new HashMap<>();
        List<String> options = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    throw new ArgumentException(a);
                }

                options = new ArrayList<>();
                params.put(a.substring(1), options);
            } else if (options != null) {
                options.add(a);
            } else {
                throw new ArgumentException(a);
            }
        }
        return params;
    }

    private static boolean isParameterValid(Map<String, List<String>> params, String parameter){
        return params.get(parameter) != null && params.get(parameter).size() == 1;
    }

    public static class ProcessThread extends Thread {
        private String arg;

        ProcessThread(String arg) {
            this.arg = arg;
        }


        @Override
        public void run(){
            try {
                Manager.getInstance().process(arg);
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
                }
            }
        }

    }
}
