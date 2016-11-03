package pt.ulisboa.tecnico.csf.wecollect.exception;

/**
 * Created by xxlxpto on 21-10-2016.
 */
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
