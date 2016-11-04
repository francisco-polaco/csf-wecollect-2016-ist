package pt.ulisboa.tecnico.csf.wecollect.domain;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class User {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;

    private String userSid;
    private String username;
    private User createdBy;
    private String createdBySid;
    private String createdByName;

    public User(String userSid, String username, String createdBySid, String createdByName) {
        this.userSid = userSid;
        this.username = username;
        this.createdBySid = createdBySid;
        this.createdByName = createdByName;
    }
    public User(String userSid, String username, User createdBy) {
        this.userSid = userSid;
        this.username = username;
        this.createdBy = createdBy;
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

    public int getCreatedById(){
        if(createdBy != null) return createdBy.getId();
        else return -1; // vai ser giro
    }

    public String getUsername() {
        return username;
    }


    public String getUserSid() {
        return userSid;
    }
}
