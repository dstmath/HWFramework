package com.leisen.wallet.sdk.business;

public class BaseResponse<T extends Business> {
    private T business;
    private String version;

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public T getBusiness() {
        return this.business;
    }

    public void setBusiness(T business) {
        this.business = business;
    }
}
