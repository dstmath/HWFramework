package com.huawei.nb.client;

public class ErrorInfo {
    public static final int INPUT_PARAM_IS_INVALID = 1;
    public static final int NOT_CONNECT_TO_DATA_SERVICE = 2;
    public static final int NO_ERROR = 0;
    public static final int OPERATION_FAILED = 4;
    public static final int RUNTIME_EXCEPTION = 3;
    private final int errorCode;
    private final String errorDescription;

    public ErrorInfo(int errorCode2, String errorInfo) {
        this.errorCode = errorCode2;
        this.errorDescription = errorInfo;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

    public String toString() {
        return "ErrorInfo{errorCode=" + this.errorCode + ", errorDescription='" + this.errorDescription + '\'' + '}';
    }
}
