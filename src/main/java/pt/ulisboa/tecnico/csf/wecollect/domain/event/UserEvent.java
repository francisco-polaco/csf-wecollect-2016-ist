package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import java.sql.Timestamp;

public abstract class UserEvent extends Event {

    public UserEvent(Timestamp timestamp, int computerId, int userId, String sid) {
        super(timestamp, computerId);
        this.userId = userId;
        this.sid = sid;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private int userId;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public UserEvent(Timestamp timestamp, int computerId, String sid) {
        super(timestamp, computerId);
        this.sid = sid;
    }

    private String sid;


    @Override
    public String toString() {
        return "UserEvent{" +
                "userId=" + userId +
                ", sid='" + sid + '\'' +
                "} ; " + super.toString();
    }
}
