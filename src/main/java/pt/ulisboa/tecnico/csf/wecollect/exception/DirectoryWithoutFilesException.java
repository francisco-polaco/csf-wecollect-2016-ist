package pt.ulisboa.tecnico.csf.wecollect.exception;


public class DirectoryWithoutFilesException extends WECollectException{

    private String mMessage;

    public DirectoryWithoutFilesException(String msg){
        mMessage = msg;
    }
    @Override
    public String getMessage() {
        return "Directory " + mMessage + " does not have files.";
    }
}
