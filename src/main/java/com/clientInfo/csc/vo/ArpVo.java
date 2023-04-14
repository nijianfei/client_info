package com.clientInfo.csc.vo;

import lombok.Builder;

@Builder
public class ArpVo {
    private String clientIp = "";
    private String clientMac = "";
    private String nodeServerIp = "";

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientMac() {
        return clientMac;
    }

    public void setClientMac(String clientMac) {
        this.clientMac = clientMac;
    }

    public String getNodeServerIp() {
        return nodeServerIp;
    }

    public void setNodeServerIp(String nodeServerIp) {
        this.nodeServerIp = nodeServerIp;
    }
}
