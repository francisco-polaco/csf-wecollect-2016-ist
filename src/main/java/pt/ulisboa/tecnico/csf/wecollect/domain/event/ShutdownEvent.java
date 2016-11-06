package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;

import java.sql.Timestamp;

public class ShutdownEvent extends Event {

    public ShutdownEvent(Timestamp timestamp, int computerId) {
        super(timestamp, computerId);
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitShutdownEvents(this);
    }

    @Override
    public String toString() {
        return "ShutdownEvent{} ; " + super.toString();
    }
}
