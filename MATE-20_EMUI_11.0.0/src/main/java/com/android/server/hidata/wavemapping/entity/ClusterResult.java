package com.android.server.hidata.wavemapping.entity;

public class ClusterResult {
    private int clusterNum = -1;
    private int mainApClusterNum = -1;
    private String place;
    private String result;

    public ClusterResult(String place2) {
        this.place = place2;
    }

    public String getResult() {
        this.result = this.clusterNum + "_" + this.mainApClusterNum;
        return this.result;
    }

    public void setResult(String result2) {
        this.result = result2;
    }

    public int getClusterNum() {
        return this.clusterNum;
    }

    public void setClusterNum(int clusterNum2) {
        this.clusterNum = clusterNum2;
    }

    public String getPlace() {
        return this.place;
    }

    public void setPlace(String place2) {
        this.place = place2;
    }

    public int getMainApClusterNum() {
        return this.mainApClusterNum;
    }

    public void setMainApClusterNum(int mainApClusterNum2) {
        this.mainApClusterNum = mainApClusterNum2;
    }

    public String toString() {
        return "ClusterResult{clusterNum=" + this.clusterNum + ", place='" + this.place + "', mainApClusterNum=" + this.mainApClusterNum + '}';
    }
}
