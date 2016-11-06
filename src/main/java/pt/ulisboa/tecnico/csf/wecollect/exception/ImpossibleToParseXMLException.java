package pt.ulisboa.tecnico.csf.wecollect.exception;

public class ImpossibleToParseXMLException extends WECollectException{

    private String mMessage;

    public ImpossibleToParseXMLException(String msg){
        mMessage = msg;
    }
    @Override
    public String getMessage() {
        return "Impossible to parse XML. More info: " + mMessage;
    }
}
