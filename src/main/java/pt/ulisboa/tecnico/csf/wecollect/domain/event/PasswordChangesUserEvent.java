package pt.ulisboa.tecnico.csf.wecollect.domain.event;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class PasswordChangesUserEvent {
    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }

    private int changedBy;
}
