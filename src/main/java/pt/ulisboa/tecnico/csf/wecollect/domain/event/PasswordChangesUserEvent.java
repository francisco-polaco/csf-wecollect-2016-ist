package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import java.sql.Timestamp;

public class PasswordChangesUserEvent extends UserEvent{
    public PasswordChangesUserEvent(Timestamp timestamp, int computerId, String sid) {
        super(timestamp, computerId, sid);
    }

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    private int changedBy;

    @Override
    public void commitToDb() {

    }

    @Override
    public String toString() {
        return "PasswordChangesUserEvent{" +
                "changedBy=" + changedBy +
                "} + ; " + super.toString();
    }
}
