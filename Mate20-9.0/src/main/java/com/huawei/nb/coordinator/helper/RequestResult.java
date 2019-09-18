package com.huawei.nb.coordinator.helper;

public class RequestResult {
    private String mCode;
    private String mDesc;
    private String mMessage;
    private String mUrl;

    public String message() {
        return this.mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public String code() {
        return this.mCode;
    }

    public void setCode(String code) {
        this.mCode = code;
    }

    public String desc() {
        return this.mDesc;
    }

    public void setDesc(String desc) {
        this.mDesc = desc;
    }

    public String url() {
        return this.mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }
}
