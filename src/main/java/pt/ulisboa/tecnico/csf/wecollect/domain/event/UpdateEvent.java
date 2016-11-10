package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;

import java.sql.Timestamp;

public class UpdateEvent extends Event {

    public UpdateEvent(Timestamp timestamp, int computerId, String updateTitle) {
        super(timestamp, computerId);
        this.updateTitle = updateTitle;
    }

    private String updateTitle;

    public String getUpdateTitle() {
        return updateTitle;
    }

    public void setUpdateTitle(String updateTitle) {
        this.updateTitle = updateTitle;
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitUpdateEvents(this);
    }

    @Override
    public String toString() {
        return "StartupEvent{} ; " + super.toString();
    }
}