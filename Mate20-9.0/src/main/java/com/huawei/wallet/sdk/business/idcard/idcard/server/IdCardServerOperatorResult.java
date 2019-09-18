package com.huawei.wallet.sdk.business.idcard.idcard.server;

import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;

public class IdCardServerOperatorResult {
    public static final int RESULT_CODE_SUCCESS = 0;
    public static final int RESULT_CODE_UNKNOWN_ERROR = 99;
    public static final String RESULT_MSG_UNKNOWN_ERROR = "unknown error";
    private ErrorInfo errorCodeInfo;
    private int resultCode = 99;
    private String resultMsg = RESULT_MSG_UNKNOWN_ERROR;

    public int getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(int resultCode2) {
        this.resultCode = resultCode2;
    }

    public String getResultMsg() {
        return this.resultMsg;
    }

    public void setResultMsg(String resultMsg2) {
        this.resultMsg = resultMsg2;
    }

    public void setInnerError() {
        this.resultCode = 99;
        this.resultMsg = RESULT_MSG_UNKNOWN_ERROR;
    }

    public ErrorInfo getErrorCodeInfo() {
        return this.errorCodeInfo;
    }

    public void setErrorCodeInfo(ErrorInfo errorCodeInfo2) {
        this.errorCodeInfo = errorCodeInfo2;
    }
}
