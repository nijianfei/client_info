package com.clientInfo.csc.entity;

import java.util.LinkedList;

public class PingTaskEntity {
    private String ip;
    private int minTime = 0;
    private int maxTime = 0;

    private int totalCount = 0;

    private LinkedList<PingEntity> errorList = new LinkedList<>();

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getMinTime() {
        return minTime;
    }

    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getErrorCount() {
        return errorList.size();
    }

    public int getSuccessCount() {
        return totalCount - errorList.size();
    }
    public void totalCountAdd(){
        this.totalCount++;
    }
    public LinkedList<PingEntity> getErrorList() {
        return errorList;
    }

    public void changeTime(String pingInfo){
        String timeMs = pingInfo.split(" ")[4].substring(3).replace("ms", "");
        int tMs = Integer.parseInt(timeMs);
        if(this.maxTime < tMs){
            this.maxTime = tMs;
        }
        if(this.minTime > tMs){
            this.minTime = tMs;
        }
    }
    public void addEntity(PingEntity entity){
        errorList.add(entity);
    }
}
