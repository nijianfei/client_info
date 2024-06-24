package com.clientInfo.csc.entity;

import java.util.Objects;

public class ServerStatusEntity {
    private String deviceId;

    //节点状态(Active ,Standby)
    private String nodeType;
    //集群外网状态
    private String vOutNetStatus;
    //集群内网状态
    private String vInnerNetStatus;
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

    //此节点集群是否处于工作状态
    public String getClusterStatus() {
        if (!Objects.equals(vInnerNetStatus, vOutNetStatus)) {
            return "2";//"集群状态不一致"
        } else {
            if (Objects.equals(nodeType, "Active")) {
                if ("0".equals(vInnerNetStatus)) {
                    return "1";//"集群状态正常"
                }else{
                    return "4";//"集群主节点不可用"
                }
            }else{
                if ("0".equals(vInnerNetStatus)) {
                    return "3";//"集群启用备节点"
                }else{
                    return "1";//"集群状态正常"
                }
            }
        }
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getvOutNetStatus() {
        return vOutNetStatus;
    }

    public void setvOutNetStatus(String vOutNetStatus) {
        this.vOutNetStatus = vOutNetStatus;
    }

    public String getvInnerNetStatus() {
        return vInnerNetStatus;
    }

    public void setvInnerNetStatus(String vInnerNetStatus) {
        this.vInnerNetStatus = vInnerNetStatus;
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
