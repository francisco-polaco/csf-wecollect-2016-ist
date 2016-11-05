package pt.ulisboa.tecnico.csf.wecollect.domain;

import pt.ulisboa.tecnico.csf.wecollect.domain.event.Event;

import java.util.ArrayList;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class Pack {
    private Computer computer;

    private ArrayList<User> users = new ArrayList<>();

    public ArrayList<Event> getEvents() {
        return events;
    }

    private ArrayList<Event> events = new ArrayList<>();

    public void addEvent(Event event){
        events.add(event);
    }

    public Computer getComputer() {
        return computer;
    }

    public void setComputer(Computer computer) {
        this.computer = computer;

    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

}
