package com.huawei.wallet.sdk.common.apdu.request;

import java.util.Map;
import java.util.Set;

public class QueryCardProductInfoRequest extends CardServerBaseRequest {
    private static final String DEFAULT_CLIENT = "nfc";
    private static final String DEFAULT_VERSION = "201607V1_9";
    private String client = DEFAULT_CLIENT;
    private Set<Map<String, String>> filters;
    private long timeStamp;
    private String version = DEFAULT_VERSION;

    public String getVersion() {
        return this.version;
    }

    public String getClient() {
        return this.client;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public Set<Map<String, String>> getFilters() {
        return this.filters;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public void setClient(String client2) {
        this.client = client2;
    }

    public void setTimeStamp(long timeStamp2) {
        this.timeStamp = timeStamp2;
    }

    public void setFilters(Set<Map<String, String>> filters2) {
        this.filters = filters2;
    }
}
