package com.clientInfo.csc.entity;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class PingEntity {
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;

    private String errorMsg;

    public PingEntity() {
    }

    public PingEntity(Date date, String errorMsg) {
        this.date = date;
        this.errorMsg = errorMsg;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
