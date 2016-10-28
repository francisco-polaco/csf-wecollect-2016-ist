package pt.ulisboa.tecnico.csf.wecollect.domain.event;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class LogUserEvent extends UserEvent {
    public long getLoginId() {
        return loginId;
    }

    public void setLoginId(long loginId) {
        this.loginId = loginId;
    }

    public short getLoginType() {
        return loginType;
    }

    public void setLoginType(short loginType) {
        this.loginType = loginType;
    }

    private long loginId;
    private short loginType;


}
