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

    public void addUser(User u){
        users.add(u);
    }

    public void addEvent(Event event){
        events.add(event);
    }

}
