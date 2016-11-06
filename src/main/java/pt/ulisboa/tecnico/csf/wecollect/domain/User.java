package pt.ulisboa.tecnico.csf.wecollect.domain;

import java.sql.Timestamp;

public class User {

    private int id;
    private String userSid;
    private String username;
    private User createdBy;
    private String createdBySid;
    private String createdByName;
    private Timestamp createdOn;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public User(String userSid, String username, String createdBySid, String createdByName, Timestamp timestamp) {
        this.userSid = userSid;
        this.username = username;
        this.createdBySid = createdBySid;
        this.createdByName = createdByName;
        createdOn = timestamp;
    }
    public User(String userSid, String username, User createdBy, Timestamp timestamp) {
        this.userSid = userSid;
        this.username = username;
        this.createdBy = createdBy;
        createdOn = timestamp;
    }
    @Override
    public String toString() {
        if(createdBy == null)
            return "User{" +
                    "id=" + id +
                    ", userSid='" + userSid + '\'' +
                    ", username='" + username + '\'' +
                    ", createdBySid='" + createdBySid + '\'' +
                    ", createdByName='" + createdByName + '\'' +
                    '}';
        else
            return "User{" +
                    "id=" + id +
                    ", userSid='" + userSid + '\'' +
                    ", username='" + username + '\'' +
                    ", " + createdBy.toString() +
                    '}';
    }

    public String getCreatedBySid(){
        if(createdBy == null)   return createdBySid;
        else return createdBy.getUserSid();
    }

    public Integer getCreatedById(){
        if(createdBy != null) return createdBy.getId();
        else return null; // vai ser giro
    }

    public String getUsername() {
        return username;
    }


    public String getUserSid() {
        return userSid;
    }
}
