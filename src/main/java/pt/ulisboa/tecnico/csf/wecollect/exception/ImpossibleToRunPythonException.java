package pt.ulisboa.tecnico.csf.wecollect.exception;

public class ImpossibleToRunPythonException extends WECollectException{

    private String mMessage;

    public ImpossibleToRunPythonException(String msg){
        mMessage = msg;
    }
    @Override
    public String getMessage() {
        return "Python script was unable to run. More info: " + mMessage;
    }
}
