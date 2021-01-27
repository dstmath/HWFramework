package com.huawei.nb.coordinator.helper;

public class RequestResult {
    private String mCode;
    private String mDesc;
    private String mMessage;
    private String mUrl;

    public String message() {
        return this.mMessage;
    }

    public void setMessage(String str) {
        this.mMessage = str;
    }

    public String code() {
        return this.mCode;
    }

    public void setCode(String str) {
        this.mCode = str;
    }

    public String desc() {
        return this.mDesc;
    }

    public void setDesc(String str) {
        this.mDesc = str;
    }

    public String url() {
        return this.mUrl;
    }

    public void setUrl(String str) {
        this.mUrl = str;
    }
}
