package com.huawei.android.bastet;

public class BastetException extends Exception {
    private String msgDescription;

    public BastetException() {
    }

    public BastetException(String message) {
        super(message);
        this.msgDescription = message;
    }

    public String getMsgDes() {
        return this.msgDescription;
    }
}
