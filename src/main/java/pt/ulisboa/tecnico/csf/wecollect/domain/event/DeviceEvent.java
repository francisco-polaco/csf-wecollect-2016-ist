package pt.ulisboa.tecnico.csf.wecollect.domain.event;

import pt.ulisboa.tecnico.csf.wecollect.domain.database.DatabaseManager;

import java.sql.Timestamp;

public class DeviceEvent extends Event {

    public DeviceEvent(Timestamp timestamp, int computerId, String name, String containerId, int taskCount, int propertyCount, int workTime) {
        super(timestamp, computerId);
        this.name = name;
        this.containerId = containerId;
        this.taskCount = taskCount;
        this.propertyCount = propertyCount;
        this.workTime = workTime;
    }

    private String name;
    private String containerId;
    private int taskCount;
    private int propertyCount;
    private int workTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getPropertyCount() {
        return propertyCount;
    }

    public void setPropertyCount(int propertyCount) {
        this.propertyCount = propertyCount;
    }

    public int getWorkTime() {
        return workTime;
    }

    public void setWorkTime(int workTime) {
        this.workTime = workTime;
    }

    @Override
    public void commitToDb() {
        DatabaseManager.getInstance().commitDeviceEvent(this);
    }

    @Override
    public String toString() {
        return "DeviceEvent{" +
                "name='" + name + '\'' +
                ", containerId='" + containerId + '\'' +
                ", taskCount=" + taskCount +
                ", propertyCount=" + propertyCount +
                ", workTime=" + workTime +
                '}';
    }
}