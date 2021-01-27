package com.huawei.secure.android.common.util;

public class SecurityCommonException extends Exception {
    private static final long serialVersionUID = 1;
    private String msgDes;
    private String retCd;

    public SecurityCommonException() {
    }

    public SecurityCommonException(Throwable cause) {
        super(cause);
    }

    public SecurityCommonException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityCommonException(String message) {
        super(message);
        this.msgDes = message;
    }

    public SecurityCommonException(String retCd2, String msgDes2) {
        this.retCd = retCd2;
        this.msgDes = msgDes2;
    }

    public String getRetCd() {
        return this.retCd;
    }

    public String getMsgDes() {
        return this.msgDes;
    }
}
