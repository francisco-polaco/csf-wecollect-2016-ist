package pt.ulisboa.tecnico.csf.wecollect.domain;

import pt.ulisboa.tecnico.csf.wecollect.domain.event.Event;

import java.util.ArrayList;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class Pack {
    public void setComputer(Computer computer) {
        this.computer = computer;
    }

    private Computer computer;

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Event> events = new ArrayList<>();

    public void addUser(User u){
        users.add(u);
    }

    public void addEvent(Event event){
        events.add(event);
    }

}
