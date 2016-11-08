package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;

import java.sql.Timestamp;

public class PasswordChangesUserEvent extends UserEvent{
    public PasswordChangesUserEvent(Timestamp timestamp, int computerId, int userId, String sid, int changedBy, boolean isSuccess) {
        super(timestamp, computerId, userId, sid);
        this.changedBy = changedBy;
        this.isSuccess = isSuccess;
    }

    private int changedBy;
    private boolean isSuccess;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

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
