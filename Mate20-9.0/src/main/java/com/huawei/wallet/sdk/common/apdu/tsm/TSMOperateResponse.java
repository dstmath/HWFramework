package com.huawei.wallet.sdk.common.apdu.tsm;

public class TSMOperateResponse {
    public static final int RETURN_DOWN_VERSION = 10099;
    public static final int RETURN_NETWORK_ERROR = 100010;
    public static final int RETURN_REQUESTPARAM_CONN_UNAVAILABLE = 100005;
    public static final int RETURN_REQUESTPARAM_CPLC_IS_NULL = 100002;
    public static final int RETURN_REQUESTPARAM_FUNCALLID_IS_NULL = 100004;
    public static final int RETURN_REQUESTPARAM_NO_NETWORK = 100006;
    public static final int RETURN_REQUESTPARAM_SERVICEID_IS_NULL = 100003;
    public static final int RETURN_REQUESTPARAM_ST_INVALID = 100007;
    public static final int RETURN_REQUEST_PARAMS_IS_NULL = 100001;
    public static final int RETURN_RESPONSE_PARSE_ERROR = 100012;
    public static final int RETURN_SERVER_ERROR = 100013;
    public static final int RETURN_UNKNOW_ERROR = 100011;
    public static final int TSM_OPERATE_RESULT_SUCCESS = 100000;
    private String apduError;
    private boolean isApplyApdu = false;
    private String msg = "Success";
    private int oriResultCode;
    private int resultCode;

    public String getApduError() {
        return this.apduError;
    }

    public void setApduError(String apduError2) {
        this.apduError = apduError2;
    }

    public TSMOperateResponse() {
    }

    public TSMOperateResponse(int result, String msg2) {
        this.resultCode = result;
        this.msg = msg2;
    }

    public TSMOperateResponse(int result, String msg2, String apduError2) {
        this.resultCode = result;
        this.msg = msg2;
        this.apduError = apduError2;
    }

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

    public String getPrintMsg() {
        return "resultCode : " + this.resultCode + " msg : " + this.msg;
    }

    public int getOriResultCode() {
        return this.oriResultCode;
    }

    public void setOriResultCode(int oriResultCode2) {
        this.oriResultCode = oriResultCode2;
    }

    public boolean isApplyApdu() {
        return this.isApplyApdu;
    }

    public void setApplyApdu(boolean applyApdu) {
        this.isApplyApdu = applyApdu;
    }
}
