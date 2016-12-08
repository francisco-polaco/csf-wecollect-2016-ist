package pt.ulisboa.tecnico.csf.wecollect.exception;

public class ArgumentException extends Exception{


    private String argument;

    public ArgumentException(String argument) {
        this.argument = argument;
    }

    public ArgumentException() {

    }

    @Override
    public String getMessage() {
        if(argument == null)
            return "An unknown argument was passed.\n" +
                    "The correct usage is: java wecollect -windir <windows directory> [-h hostname] [-u username] [-p password]";
        else
            return "There is a problem in argument: " + argument +
                    "\nThe correct usage is: java wecollect -windir <windows directory> [-h hostname] [-u username] [-p password]";
    }
}
