package com.huawei.nb.coordinator.helper.verify;

public class VerifyException extends Exception {
    private int code;
    private String msg;

    public VerifyException(int code2, String msg2) {
        this.code = code2;
        this.msg = msg2;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code2) {
        this.code = code2;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg2) {
        this.msg = msg2;
    }
}
