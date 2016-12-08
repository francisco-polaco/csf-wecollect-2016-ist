package pt.ulisboa.tecnico.csf.wecollect.core.event;

import pt.ulisboa.tecnico.csf.wecollect.core.database.DatabaseManager;

import java.sql.Timestamp;

public class StartupEvent extends Event {

    public StartupEvent(Timestamp timestamp, int computerId) {
        super(timestamp, computerId);
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitStartupEvents(this);
    }

    @Override
    public String toString() {
        return "StartupEvent{} ; " + super.toString();
    }
}
