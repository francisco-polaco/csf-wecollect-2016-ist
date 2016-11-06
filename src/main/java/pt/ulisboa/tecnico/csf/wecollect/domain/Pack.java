package pt.ulisboa.tecnico.csf.wecollect.domain;

import pt.ulisboa.tecnico.csf.wecollect.domain.event.Event;

import java.util.ArrayList;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class Pack {

    private static final int LIMIT_EVENTS_ON_RAM = 500;

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
        if(events.size() == LIMIT_EVENTS_ON_RAM){
            forceCommitToDb();
        }
    }

    public void forceCommitToDb() {
        for (Event e : events) {
            System.out.println(e.toString());
            e.commitToDb();
        }
        events.clear();

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

    public int getUserIdBySid(String sid) throws IllegalStateException{
        int indexOfLastDash = sid.lastIndexOf("-");
        sid = sid.substring(indexOfLastDash + 1);;
        for(User u : users){
            if(u.getUserSid().equals(sid))
                return u.getId();
        }
        throw new IllegalStateException();
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

}
