package com.clientInfo.csc.entity;

public class ServerStatusEntity {
    private String deviceId;

    //节点状态(Active ,Standby)
    private String nodeType;
    //外网状态
    private String outNetStatus;
    //内网状态
    private String innerNetStatus;
    //泰康网状态
    private String tkNetStatus;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getOutNetStatus() {
        return outNetStatus;
    }

    public void setOutNetStatus(String outNetStatus) {
        this.outNetStatus = outNetStatus;
    }

    public String getInnerNetStatus() {
        return innerNetStatus;
    }

    public void setInnerNetStatus(String innerNetStatus) {
        this.innerNetStatus = innerNetStatus;
    }

    public String getTkNetStatus() {
        return tkNetStatus;
    }

    public void setTkNetStatus(String tkNetStatus) {
        this.tkNetStatus = tkNetStatus;
    }
}
