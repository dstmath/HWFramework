package com.leisen.wallet.sdk.business;

import com.leisen.wallet.sdk.business.Business;

public class BaseResponse<T extends Business> {
    private T business;
    private String version;

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public T getBusiness() {
        return this.business;
    }

    public void setBusiness(T business2) {
        this.business = business2;
    }
}
