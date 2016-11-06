package pt.ulisboa.tecnico.csf.wecollect.exception;

public class ImpossibleToCreateWorkingDirException extends WECollectException{


    @Override
    public String getMessage() {
        return "Impossible to create working directory.";
    }
}
