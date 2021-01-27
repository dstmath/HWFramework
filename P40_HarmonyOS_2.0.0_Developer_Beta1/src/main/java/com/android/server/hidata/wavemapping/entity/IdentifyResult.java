package com.android.server.hidata.wavemapping.entity;

public class IdentifyResult implements Comparable<IdentifyResult> {
    private int batch;
    private String bssid;
    private int dist = 0;
    private String modelName;
    private int preLabel = 0;
    private String result;
    private String serveMac;
    private String ssid;

    public IdentifyResult() {
    }

    public IdentifyResult(int batch2, int preLabel2, int dist2) {
        this.batch = batch2;
        this.preLabel = preLabel2;
        this.dist = 0;
    }

    public IdentifyResult(int preLabel2) {
        this.preLabel = preLabel2;
    }

    public IdentifyResult(int dist2, int preLabel2) {
        this.dist = dist2;
        this.preLabel = preLabel2;
    }

    public String getSsid() {
        return this.ssid;
    }

    public void setSsid(String ssid2) {
        this.ssid = ssid2;
    }

    public String getServeMac() {
        return this.serveMac;
    }

    public void setServeMac(String serveMac2) {
        this.serveMac = serveMac2;
    }

    public String getBssid() {
        return this.bssid;
    }

    public void setBssid(String bssid2) {
        this.bssid = bssid2;
    }

    public int getBatch() {
        return this.batch;
    }

    public void setBatch(int batch2) {
        this.batch = batch2;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result2) {
        this.result = result2;
    }

    public int getDist() {
        return this.dist;
    }

    public void setDist(int dist2) {
        this.dist = dist2;
    }

    public int getPreLabel() {
        return this.preLabel;
    }

    public void setPreLabel(int preLabel2) {
        this.preLabel = preLabel2;
    }

    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName2) {
        this.modelName = modelName2;
    }

    public int compareTo(IdentifyResult s) {
        int num = new Integer(this.dist).compareTo(new Integer(s.dist));
        if (num == 0) {
            return new Integer(this.preLabel).compareTo(new Integer(s.preLabel));
        }
        return num;
    }
}
