package pt.ulisboa.tecnico.csf.wecollect.domain;

import pt.ulisboa.tecnico.csf.wecollect.domain.event.Event;

import java.util.ArrayList;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class Pack {

    private Computer computer;

    private ArrayList<User> users = new ArrayList<>();

    private ArrayList<Event> events = new ArrayList<>();


    private static Pack instance;

    public static Pack getInstance(){
        if(instance == null) throw new IllegalStateException("Pack should have been instantiated.");
        return instance;
    }

    public Pack(){
        instance = this;
    }


    public ArrayList<Event> getEvents() {
        return events;
    }

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
