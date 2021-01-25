package com.huawei.securitycenter;

public class PermissionDenyException extends Exception {
    private static final long serialVersionUID = -4095789727389893679L;
    private String mMessage;

    public void setMessage(String message) {
        this.mMessage = message;
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return this.mMessage;
    }
}
