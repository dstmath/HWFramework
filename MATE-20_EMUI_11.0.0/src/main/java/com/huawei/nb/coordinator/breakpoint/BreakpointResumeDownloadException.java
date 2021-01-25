package com.huawei.nb.coordinator.breakpoint;

public class BreakpointResumeDownloadException extends Exception {
    private int errorCode;

    public BreakpointResumeDownloadException(int i, String str) {
        super(str);
        this.errorCode = i;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
