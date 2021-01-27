package com.huawei.networkit.grs.requestremote.model;

import java.util.List;

public class GrsServerBean {
    private List<String> grsBaseUrl;
    private String grsQueryEndpoint;
    private int grsQueryTimeout;

    public List<String> getGrsBaseUrl() {
        return this.grsBaseUrl;
    }

    public void setGrsBaseUrl(List<String> grsBaseUrl2) {
        this.grsBaseUrl = grsBaseUrl2;
    }

    public String getGrsQueryEndpoint() {
        return this.grsQueryEndpoint;
    }

    public void setGrsQueryEndpoint(String grsQueryEndpoint2) {
        this.grsQueryEndpoint = grsQueryEndpoint2;
    }

    public int getGrsQueryTimeout() {
        return this.grsQueryTimeout;
    }

    public void setGrsQueryTimeout(int grsQueryTimeout2) {
        this.grsQueryTimeout = grsQueryTimeout2;
    }
}
