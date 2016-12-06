package pt.ulisboa.tecnico.csf.wecollect.service;

import pt.ulisboa.tecnico.csf.wecollect.exception.WECollectException;

public class ProcessEvtxService extends WECollectService {
    private String windowsDirPath;

    public ProcessEvtxService(String windowsDirPath) {
        this.windowsDirPath = windowsDirPath;
    }

    @Override
    void dispatch() throws WECollectException {
        getManager().process(windowsDirPath);
    }
}
