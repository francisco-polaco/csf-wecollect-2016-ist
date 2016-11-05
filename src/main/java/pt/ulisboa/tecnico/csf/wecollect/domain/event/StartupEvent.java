package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;

import java.sql.Timestamp;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class StartupEvent extends Event {

    public StartupEvent(Timestamp timestamp, int computerId) {
        super(timestamp, computerId);
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitStartupEvents(this);
    }
}
