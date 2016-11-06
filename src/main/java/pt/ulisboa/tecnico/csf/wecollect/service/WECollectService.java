package pt.ulisboa.tecnico.csf.wecollect.service;

import pt.ulisboa.tecnico.csf.wecollect.domain.Manager;
import pt.ulisboa.tecnico.csf.wecollect.exception.WECollectException;

public abstract class WECollectService {

    public final void execute() throws WECollectException {
        dispatch();
    }

    static Manager getManager() {
        return Manager.getInstance();
    }

    abstract void dispatch() throws WECollectException;

}
