package com.android.server.hidata.wavemapping.entity;

public class ClusterResult {
    private int cluster_num = -1;
    private int mainAp_cluster_num = -1;
    private String place;
    private String result;

    public String getResult() {
        this.result = this.cluster_num + "_" + this.mainAp_cluster_num;
        return this.result;
    }

    public void setResult(String result2) {
        this.result = result2;
    }

    public ClusterResult(String place2) {
        this.place = place2;
    }

    public int getCluster_num() {
        return this.cluster_num;
    }

    public void setCluster_num(int cluster_num2) {
        this.cluster_num = cluster_num2;
    }

    public String getPlace() {
        return this.place;
    }

    public void setPlace(String place2) {
        this.place = place2;
    }

    public int getMainAp_cluster_num() {
        return this.mainAp_cluster_num;
    }

    public void setMainAp_cluster_num(int mainAp_cluster_num2) {
        this.mainAp_cluster_num = mainAp_cluster_num2;
    }

    public String toString() {
        return "ClusterResult{cluster_num=" + this.cluster_num + ", place='" + this.place + '\'' + ", mainAp_cluster_num=" + this.mainAp_cluster_num + '}';
    }
}
