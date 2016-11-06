package pt.ulisboa.tecnico.csf.wecollect.domain;

public class Computer {

    private int id;
    private String name;
    private String sid;


    public Computer() {
    }

    public Computer(int id, String name, String sid) {
        this.id = id;
        this.name = name;
        this.sid = sid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
