package com.android.server.wifi.grs.requestremote.model;

import java.util.List;

public class GrsServerBean {
    private List<String> mGrsBaseUrls;
    private String mGrsQueryEndpoint;
    private int mGrsQueryTimeout;

    public List<String> getGrsBaseUrl() {
        return this.mGrsBaseUrls;
    }

    public void setGrsBaseUrl(List<String> urls) {
        this.mGrsBaseUrls = urls;
    }

    public String getGrsQueryEndpoint() {
        return this.mGrsQueryEndpoint;
    }

    public void setGrsQueryEndpoint(String grsQueryEndpoint) {
        this.mGrsQueryEndpoint = grsQueryEndpoint;
    }

    public int getGrsQueryTimeout() {
        return this.mGrsQueryTimeout;
    }

    public void setGrsQueryTimeout(int grsQueryTimeout) {
        this.mGrsQueryTimeout = grsQueryTimeout;
    }
}
