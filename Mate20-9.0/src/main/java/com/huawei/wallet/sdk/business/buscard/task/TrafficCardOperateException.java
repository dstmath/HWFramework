package com.huawei.wallet.sdk.business.buscard.task;

import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;

public class TrafficCardOperateException extends Exception {
    private int errorCode;
    private String errorHappenStepCode;
    private ErrorInfo errorInfo = null;
    private String eventId;
    private boolean isSpaceLimit;
    private int newErrorCode = 0;
    private int spErrorCode;

    public boolean isSpaceLimit() {
        return this.isSpaceLimit;
    }

    public void setSpaceLimit(boolean spaceLimit) {
        this.isSpaceLimit = spaceLimit;
    }

    public TrafficCardOperateException() {
    }

    public TrafficCardOperateException(int errorCode2, int spErrorCode2, String errorHappenStepCode2, String message, String eventId2) {
        super(message);
        this.errorCode = errorCode2;
        this.spErrorCode = spErrorCode2;
        this.errorHappenStepCode = errorHappenStepCode2;
        this.eventId = eventId2;
    }

    public TrafficCardOperateException(int errorCode2, int spErrorCode2, String errorHappenStepCode2, String message) {
        super(message);
        this.errorCode = errorCode2;
        this.spErrorCode = spErrorCode2;
        this.errorHappenStepCode = errorHappenStepCode2;
        this.eventId = this.eventId;
    }

    public TrafficCardOperateException(int errorCode2, int spErrorCode2, int newErrorCode2, String errorHappenStepCode2, String message, String eventId2) {
        super(message);
        this.errorCode = errorCode2;
        this.spErrorCode = spErrorCode2;
        this.errorHappenStepCode = errorHappenStepCode2;
        this.eventId = eventId2;
        this.newErrorCode = newErrorCode2;
    }

    public TrafficCardOperateException(int errorCode2, int spErrorCode2, String errorHappenStepCode2, String message, String eventId2, ErrorInfo errorInfo2) {
        super(message);
        this.errorCode = errorCode2;
        this.spErrorCode = spErrorCode2;
        this.errorHappenStepCode = errorHappenStepCode2;
        this.eventId = eventId2;
        this.errorInfo = errorInfo2;
    }

    public TrafficCardOperateException(int errorCode2, int spErrorCode2, int newErrorCode2, String errorHappenStepCode2, String message, String eventId2, ErrorInfo errorInfo2) {
        super(message);
        this.errorCode = errorCode2;
        this.spErrorCode = spErrorCode2;
        this.errorHappenStepCode = errorHappenStepCode2;
        this.eventId = eventId2;
        this.errorInfo = errorInfo2;
        this.newErrorCode = newErrorCode2;
    }

    public TrafficCardOperateException(int errorCode2, int spErrorCode2, String errorHappenStepCode2, String message, String eventId2, boolean isSpaceLimit2) {
        super(message);
        this.errorCode = errorCode2;
        this.spErrorCode = spErrorCode2;
        this.errorHappenStepCode = errorHappenStepCode2;
        this.eventId = eventId2;
        this.isSpaceLimit = isSpaceLimit2;
    }

    public TrafficCardOperateException(int errorCode2, int spErrorCode2, int newErrorCode2, String errorHappenStepCode2, String message, String eventId2, boolean isSpaceLimit2) {
        super(message);
        this.errorCode = errorCode2;
        this.spErrorCode = spErrorCode2;
        this.errorHappenStepCode = errorHappenStepCode2;
        this.eventId = eventId2;
        this.isSpaceLimit = isSpaceLimit2;
        this.newErrorCode = newErrorCode2;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode2) {
        this.errorCode = errorCode2;
    }

    public int getSpErrorCode() {
        return this.spErrorCode;
    }

    public void setSpErrorCode(int spErrorCode2) {
        this.spErrorCode = spErrorCode2;
    }

    public String getErrorHappenStepCode() {
        return this.errorHappenStepCode;
    }

    public void setErrorHappenStepCode(String errorHappenStepCode2) {
        this.errorHappenStepCode = errorHappenStepCode2;
    }

    public String getMessage() {
        return "oriErrorCd : " + this.spErrorCode + ", " + super.getMessage();
    }

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId2) {
        this.eventId = eventId2;
    }

    public ErrorInfo getErrorInfo() {
        return this.errorInfo;
    }

    public void setErrorInfo(ErrorInfo errorInfo2) {
        this.errorInfo = errorInfo2;
    }

    public int getNewErrorCode() {
        return this.newErrorCode;
    }

    public void setNewErrorCode(int newErrorCode2) {
        this.newErrorCode = newErrorCode2;
    }
}
