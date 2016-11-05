package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import java.sql.Timestamp;

/**
 * Created by xxlxpto on 28-10-2016.
 */
public class FirewallEvent extends Event {

    public FirewallEvent(Timestamp timestamp, int computerId) {
        super(timestamp, computerId);
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

    public short getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(short sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public short getDestPort() {
        return destPort;
    }

    public void setDestPort(short destPort) {
        this.destPort = destPort;
    }

    private boolean allowed;
    private String protocol;
    private String sourceIp;
    private short sourcePort;
    private String destIp;
    private short destPort;

}
