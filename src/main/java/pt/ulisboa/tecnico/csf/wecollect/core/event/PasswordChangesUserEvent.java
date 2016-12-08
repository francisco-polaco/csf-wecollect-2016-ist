package pt.ulisboa.tecnico.csf.wecollect.core.event;

import pt.ulisboa.tecnico.csf.wecollect.core.database.DatabaseManager;

import java.sql.Timestamp;

public class PasswordChangesUserEvent extends UserEvent{
    public PasswordChangesUserEvent(Timestamp timestamp, int computerId, int userId, String sid, int changedBy) {
        super(timestamp, computerId, userId, sid);
        this.changedBy = changedBy;
    }

    private int changedBy;

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitPasswordChangesUserEvent(this);
    }

    @Override
    public String toString() {
        return "PasswordChangesUserEvent{" +
                "changedBy=" + changedBy +
                "} + ; " + super.toString();
    }
}
