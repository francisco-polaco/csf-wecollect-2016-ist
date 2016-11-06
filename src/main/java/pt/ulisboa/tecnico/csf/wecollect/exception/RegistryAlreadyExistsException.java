package pt.ulisboa.tecnico.csf.wecollect.exception;

public class RegistryAlreadyExistsException extends WECollectException{

    private String computerName;
    private String sid;

    public RegistryAlreadyExistsException(String compName, String sid){
        computerName = compName;
        this.sid = sid;
    }
    @Override
    public String getMessage() {
        return "Computer with name " + computerName + " and sid " + sid + " already exists in the DB.";
    }
}
