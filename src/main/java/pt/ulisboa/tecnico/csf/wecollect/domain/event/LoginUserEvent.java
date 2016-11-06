package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;

import java.sql.Timestamp;

public class LoginUserEvent extends LogUserEvent {
    public LoginUserEvent(Timestamp timestamp, int computerId, String sid, long loginId, short loginType) {
        super(timestamp, computerId, sid, loginId, loginType);
    }

    public LoginUserEvent(Timestamp timestamp, int computerId, int userId, String sid, long loginId, short loginType) {
        super(timestamp, computerId, userId, sid, loginId, loginType);
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitLoginEvents(this);
    }

    @Override
    public String toString() {
        return "LoginUserEvent{} ; " + super.toString();
    }
}
