package pt.ulisboa.tecnico.csf.wecollect.core.event;

import pt.ulisboa.tecnico.csf.wecollect.core.database.DatabaseManager;

import java.sql.Timestamp;

public class AppAccessEvent extends UserEvent{
    public AppAccessEvent(Timestamp timestamp, int computerId, int userId, String sid, String appId) {
        super(timestamp, computerId, userId, sid);
        this.appId = appId;
    }

    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitAppAccessEvent(this);
    }

    @Override
    public String toString() {
        return "AppAccessEvent{" +
                "appId='" + appId + '\'' +
                '}';
    }
}