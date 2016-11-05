package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import java.sql.Timestamp;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public abstract class UserEvent extends Event {

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
    public void commitToDb() {

    }
}
