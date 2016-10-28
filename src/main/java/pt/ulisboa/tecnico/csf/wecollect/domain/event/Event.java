package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import org.joda.time.DateTime;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public abstract class Event {
    private int id;
    private DateTime timestamp;
    private int computerId;

    public int getComputerId() {
        return computerId;
    }

    public void setComputerId(int computerId) {
        this.computerId = computerId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }
}
