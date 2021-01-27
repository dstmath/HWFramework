package com.huawei.nb.coordinator.helper.verify;

public class VerifyException extends Exception {
    private int code;
    private String msg;

    public VerifyException(int i, String str) {
        this.code = i;
        this.msg = str;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int i) {
        this.code = i;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String str) {
        this.msg = str;
    }
}
