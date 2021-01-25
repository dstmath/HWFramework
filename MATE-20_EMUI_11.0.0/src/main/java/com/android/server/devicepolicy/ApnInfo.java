package com.android.server.devicepolicy;

import java.util.Map;

public class ApnInfo {
    private String apnId;
    private Map<String, String> apnInfo;

    public ApnInfo(Map<String, String> info, String id) {
        this.apnInfo = info;
        this.apnId = id;
    }

    public String getApnId() {
        return this.apnId;
    }

    public Map<String, String> getApnInfo() {
        return this.apnInfo;
    }
}
