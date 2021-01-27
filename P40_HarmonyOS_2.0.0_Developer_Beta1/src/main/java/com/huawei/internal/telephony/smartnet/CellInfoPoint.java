package com.huawei.internal.telephony.smartnet;

public class CellInfoPoint {
    private long arriveTime;
    private int blackType = 0;
    private long cellIdentity;
    private long convertToExceptionDuration;
    private int exceptionCounts;
    private double exceptionProbability;
    private String iccIdHash;
    private int normalCounts;
    private int routeId;
    private int totalCounts;

    public CellInfoPoint(int routeId2, long cellIdentity2, String iccIdHash2) {
        this.routeId = routeId2;
        this.cellIdentity = cellIdentity2;
        this.normalCounts = 0;
        this.exceptionCounts = 0;
        this.totalCounts = 0;
        this.convertToExceptionDuration = 0;
        this.exceptionProbability = 0.0d;
        this.iccIdHash = iccIdHash2;
    }

    public int getNormalCounts() {
        return this.normalCounts;
    }

    public void setNormalCounts(int normalCounts2) {
        this.normalCounts = normalCounts2;
    }

    public int getExceptionCounts() {
        return this.exceptionCounts;
    }

    public void setExceptionCounts(int exceptionCounts2) {
        this.exceptionCounts = exceptionCounts2;
    }

    public int getTotalCounts() {
        return this.totalCounts;
    }

    public void setTotalCounts(int totalCounts2) {
        this.totalCounts = totalCounts2;
    }

    public void increaseNormalCounts() {
        this.normalCounts++;
    }

    public void increaseExceptionCounts() {
        this.exceptionCounts++;
    }

    public void increaseTotalCounts() {
        this.totalCounts++;
    }

    public long getConvertToExceptionDuration() {
        return this.convertToExceptionDuration;
    }

    public void setConvertToExceptionDuration(long duration) {
        this.convertToExceptionDuration = duration;
    }

    public long getArriveTime() {
        return this.arriveTime;
    }

    public void setArriveTime(long time) {
        this.arriveTime = time;
    }

    public int getBlackType() {
        return this.blackType;
    }

    public void setBlackType(int blackType2) {
        this.blackType = blackType2;
    }

    public void updateBlackType(int blackType2) {
        this.blackType |= blackType2;
    }

    public double getExceptionProbability() {
        return this.exceptionProbability;
    }

    public void setExceptionProbability(double exceptionProbability2) {
        this.exceptionProbability = exceptionProbability2;
    }

    public long getCellIdentity() {
        return this.cellIdentity;
    }

    public int getRouteId() {
        return this.routeId;
    }

    public String getIccIdHash() {
        return this.iccIdHash;
    }

    public String toString() {
        return "CellInfoPoint{routeId=" + this.routeId + ", blackType=" + this.blackType + ", iccIdHash='" + this.iccIdHash + ", totalCounts=" + this.totalCounts + ", normalCounts=" + this.normalCounts + ", exceptionCounts=" + this.exceptionCounts + ", convertToExceptionDuration=" + this.convertToExceptionDuration + ", exceptionProbability=" + this.exceptionProbability + "}";
    }
}
