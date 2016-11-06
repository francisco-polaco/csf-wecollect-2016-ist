package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;

import java.sql.Timestamp;

public class LogoutUserEvent extends LogUserEvent {

    public LogoutUserEvent(Timestamp timestamp, int computerId, String sid, long loginId, short loginType) {
        super(timestamp, computerId, sid, loginId, loginType);
    }

    public LogoutUserEvent(Timestamp timestamp, int computerId, int userId, String sid, long loginId, short loginType) {
        super(timestamp, computerId, userId, sid, loginId, loginType);
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitLogoutEvents(this);
    }

    @Override
    public String toString() {
        return "LogoutUserEvent{} ; " + super.toString();
    }
}
