package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import java.sql.Timestamp;

public abstract class LogUserEvent extends UserEvent {

    public LogUserEvent(Timestamp timestamp, int computerId, int userId, String sid, long loginId, short loginType) {
        super(timestamp, computerId, userId, sid);
        this.loginId = loginId;
        this.loginType = loginType;
    }

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

    public LogUserEvent(Timestamp timestamp, int computerId, String sid, long loginId, short loginType) {
        super(timestamp, computerId, sid);
        this.loginId = loginId;
        this.loginType = loginType;
    }

    private long loginId;
    private short loginType;


    @Override
    public String toString() {
        return "LogUserEvent{" +
                "loginId=" + loginId +
                ", loginType=" + loginType +
                '}' + " ; " + super.toString();
    }
}
