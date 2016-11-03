package pt.ulisboa.tecnico.csf.wecollect.exception;

/**
 * Created by xxlxpto on 21-10-2016.
 */
public class ImpossibleToCreateWorkingDirException extends WECollectException{


    @Override
    public String getMessage() {
        return "Impossible to create working directory.";
    }
}
