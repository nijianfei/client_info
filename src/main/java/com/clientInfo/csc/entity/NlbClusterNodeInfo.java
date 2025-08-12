package com.clientInfo.csc.entity;

public class NlbClusterNodeInfo {
    private String name;
    private String state;
    private String interfaceName;
    private String hostId;

    public NlbClusterNodeInfo() {
    }

    public NlbClusterNodeInfo(String name, String state, String interfaceName, String hostId) {
        this.name = name;
        this.state = state;
        this.interfaceName = interfaceName;
        this.hostId = hostId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
}
