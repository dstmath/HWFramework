package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.response;

import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;

public class BaseResponse {
    public static final int RESULT_CODE_DELETE_NO_APPLET = 5002;
    public static final int RESULT_CODE_INVALID_PARAM = 1;
    public static final int RESULT_CODE_INVALID_ST = 4;
    public static final int RESULT_CODE_NO_NETWORK = 2;
    public static final int RESULT_CODE_NO_SERVICE = 3;
    public static final int RESULT_CODE_OTHER_ERROR = -99;
    public static final int RESULT_CODE_SUCCESS = 0;
    public static final String RESULT_DESC_INVALID_PARAM = "client check, invalid param";
    private String apduError;
    private ErrorInfo errorInfo = null;
    protected int originResultCode = -99;
    protected int resultCode = -99;
    protected String resultDesc = null;
    private String srcTranId = null;

    public int getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(int resultCode2) {
        this.resultCode = resultCode2;
    }

    public String getResultDesc() {
        return this.resultDesc;
    }

    public void setResultDesc(String resultDesc2) {
        this.resultDesc = resultDesc2;
    }

    public int getOriginResultCode() {
        return this.originResultCode;
    }

    public void setOriginResultCode(int originResultCode2) {
        this.originResultCode = originResultCode2;
    }

    public ErrorInfo getErrorInfo() {
        return this.errorInfo;
    }

    public void setErrorInfo(ErrorInfo errorInfo2) {
        this.errorInfo = errorInfo2;
    }

    public String getApduError() {
        return this.apduError;
    }

    public void setApduError(String apduError2) {
        this.apduError = apduError2;
    }

    public String getTransactionId() {
        return this.srcTranId;
    }

    public void setTransactionId(String transactionId) {
        this.srcTranId = transactionId;
    }
}
