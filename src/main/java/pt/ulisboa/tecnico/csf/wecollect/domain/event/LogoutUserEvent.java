package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import java.sql.Timestamp;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class LogoutUserEvent extends LogUserEvent {

    public LogoutUserEvent(Timestamp timestamp, int computerId, String sid, long loginId, short loginType) {
        super(timestamp, computerId, sid, loginId, loginType);
    }
}
