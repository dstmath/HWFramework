package com.huawei.wallet.sdk.business.idcard.accesscard.logic.exception;

import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;

public class AccessCardOperatorException extends Exception {
    private int errorCode;
    private String errorHappenStepCode;
    private ErrorInfo errorInfo = null;
    private String eventId;
    private int spErrorCode;

    public AccessCardOperatorException() {
    }

    public AccessCardOperatorException(int errorCode2, int spErrorCode2, String errorHappenStepCode2, String message, String eventId2) {
        super(message);
        this.errorCode = errorCode2;
        this.spErrorCode = spErrorCode2;
        this.errorHappenStepCode = errorHappenStepCode2;
        this.eventId = eventId2;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int errorCode2) {
        this.errorCode = errorCode2;
    }

    public String getMessage() {
        return "oriErrorCd : " + this.spErrorCode + ", " + super.getMessage();
    }

    public ErrorInfo getErrorInfo() {
        return this.errorInfo;
    }

    public void setErrorInfo(ErrorInfo errorInfo2) {
        this.errorInfo = errorInfo2;
    }
}
