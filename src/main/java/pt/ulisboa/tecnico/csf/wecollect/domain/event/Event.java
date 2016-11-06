package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import java.sql.Timestamp;

public abstract class Event {
    private int id;
    private Timestamp timestamp;
    private int computerId;

    public Event(Timestamp timestamp, int computerId) {
        this.timestamp = timestamp;
        this.computerId = computerId;
    }

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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public abstract void commitToDb();

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", computerId=" + computerId +
                '}';
    }
}
