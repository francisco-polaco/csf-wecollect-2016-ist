package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;

import java.sql.Timestamp;

public class FirewallEvent extends Event {


    public FirewallEvent(Timestamp timestamp, int computerId, boolean allowed, String protocol, String sourceIp, String destIp, int sourcePort, int destPort) {
        super(timestamp, computerId);
        if(sourcePort > 65535 && sourcePort < 0 || destPort > 65535 && destPort < 0)
            throw new IllegalArgumentException();
        this.allowed = allowed;
        this.protocol = protocol;
        this.sourceIp = sourceIp;
        this.sourcePort = sourcePort;
        this.destIp = destIp;
        this.destPort = destPort;
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitFirewallEvents(this);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public int getDestPort() {
        return destPort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    private boolean allowed;
    private String protocol;
    private String sourceIp;
    private int sourcePort;
    private String destIp;
    private int destPort;

    @Override
    public String toString() {
        return "FirewallEvent{" +
                "allowed=" + allowed +
                ", protocol='" + protocol + '\'' +
                ", sourceIp='" + sourceIp + '\'' +
                ", sourcePort=" + sourcePort +
                ", destIp='" + destIp + '\'' +
                ", destPort=" + destPort +
                "} ; " + super.toString();
    }
}
