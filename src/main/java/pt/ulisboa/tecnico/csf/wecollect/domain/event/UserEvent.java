package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import java.sql.Timestamp;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public abstract class UserEvent extends Event {
    public UserEvent(Timestamp timestamp, int computerId) {
        super(timestamp, computerId);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private int userId;

    @Override
    public void commitToDb() {

    }
}
