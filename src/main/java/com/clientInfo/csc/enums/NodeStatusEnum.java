package com.clientInfo.csc.enums;

public enum NodeStatusEnum {
    S1("Started","启动"),
    S2("Stopped","停止"),
    S3("Suspended","挂起"),

    ;
    private String code;
    private String name;

    NodeStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
