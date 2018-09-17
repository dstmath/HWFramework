package com.huawei.systemmanager.rainbow.comm.request.exception;

public class ResponseException extends Exception {
    private static final long serialVersionUID = 1;
    private int returnCode;
    private String returnDesc;

    public int getReturnCode() {
        return this.returnCode;
    }

    public String getReturnDesc() {
        return this.returnDesc;
    }

    public String toString() {
        return "ResException [returnCode=" + this.returnCode + ", returnDesc=" + this.returnDesc + "]";
    }

    public ResponseException(int returnCode, String returnDesc) {
        this.returnCode = returnCode;
        this.returnDesc = returnDesc;
    }
}
