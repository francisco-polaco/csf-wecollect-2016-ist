package pt.ulisboa.tecnico.csf.wecollect.core;

import pt.ulisboa.tecnico.csf.wecollect.core.event.Event;

import java.util.ArrayList;

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

    Pack(){
        instance = this;
    }


    public ArrayList<Event> getEvents() {
        return events;
    }

    void addEvent(Event event){
        events.add(event);
        if(events.size() == LIMIT_EVENTS_ON_RAM){
            forceCommitToDb();
        }
    }

    void forceCommitToDb() {
        // Optimze by having a thread that is writing in DB and other that is continuing the program
        for (Event e : events) {
            System.out.println(e.toString());
            e.commitToDb();
        }
        events.clear();

    }

    public Computer getComputer() {
        return computer;
    }

    void setComputer(Computer computer) {
        this.computer = computer;

    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    int getUserIdBySid(String sid) throws IllegalStateException{
        int indexOfLastDash = sid.lastIndexOf("-");
        sid = sid.substring(indexOfLastDash + 1);
        for(User u : users){
            if(u.getUserSid().equals(sid))
                return u.getId();
        }
        throw new IllegalStateException();
    }

    void setUsers(ArrayList<User> users) {
        this.users = users;
    }

}
