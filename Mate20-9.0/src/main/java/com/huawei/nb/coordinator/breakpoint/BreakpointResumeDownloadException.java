package com.huawei.nb.coordinator.breakpoint;

public class BreakpointResumeDownloadException extends Exception {
    private int errorCode;

    public BreakpointResumeDownloadException(int errorCode2, String errMsg) {
        super(errMsg);
        this.errorCode = errorCode2;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
