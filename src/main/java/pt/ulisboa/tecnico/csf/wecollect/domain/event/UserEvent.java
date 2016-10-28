package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import org.joda.time.DateTime;

import javax.swing.border.EmptyBorder;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public abstract class UserEvent extends Event {
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private int userId;
}
