package pt.ulisboa.tecnico.csf.wecollect.core.event;

import pt.ulisboa.tecnico.csf.wecollect.core.database.DatabaseManager;

import java.sql.Timestamp;

public class WifiEvent extends Event {

    public WifiEvent(Timestamp timestamp, int computerId, String ssid, Boolean isConnect) {
        super(timestamp, computerId);
        this.ssid = ssid;
        this.isConnect = isConnect;
    }

    private String ssid;
    private Boolean isConnect;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Boolean getConnect() {
        return isConnect;
    }

    public void setConnect(Boolean connect) {
        isConnect = connect;
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitWifiEvents(this);
    }

    @Override
    public String toString() {
        return "WifiEvent{" +
                "ssid='" + ssid + '\'' +
                ", isConnect=" + isConnect +
                '}';
    }
}