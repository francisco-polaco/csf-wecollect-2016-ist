package pt.ulisboa.tecnico.csf.wecollect.service;

import pt.ulisboa.tecnico.csf.wecollect.exception.WECollectException;

public class ProcessEvtxService extends WECollectService {
    private String mFilepath;

    public ProcessEvtxService(String filepath) {
        mFilepath = filepath;
    }

    @Override
    void dispatch() throws WECollectException {
        getManager().process(mFilepath);
    }
}
