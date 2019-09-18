package com.huawei.wallet.sdk.common.apdu.tsm.requester.response;

public class TSMParamRequestTaskResult<T> {
    public static final int TSM_OPERATE_RESULT_DOWN_VERSION = 10099;
    public static final int TSM_OPERATE_RESULT_FAILED_CONN_UNAVAILABLE = -2;
    public static final int TSM_OPERATE_RESULT_FAILED_CPLC_ERRO = -3;
    public static final int TSM_OPERATE_RESULT_FAILED_NO_NETWORK = -1;
    public static final int TSM_OPERATE_RESULT_FAILED_ST_INVALID = -4;
    public static final int TSM_OPERATE_RESULT_FAILED_UNKNOWN_ERROR = -99;
    public static final int TSM_OPERATE_RESULT_SA_CPLC_ERROR = 12001;
    public static final int TSM_OPERATE_RESULT_SUCCESS = 0;
    private T data;
    private String msg;
    private int oriResultCode;
    private int resultCode;

    public int getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(int resultCode2) {
        this.resultCode = resultCode2;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg2) {
        this.msg = msg2;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data2) {
        this.data = data2;
    }

    public int getOriResultCode() {
        return this.oriResultCode;
    }

    public void setOriResultCode(int oriResultCode2) {
        this.oriResultCode = oriResultCode2;
    }
}
