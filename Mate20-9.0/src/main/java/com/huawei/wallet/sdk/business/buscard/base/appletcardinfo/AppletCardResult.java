package com.huawei.wallet.sdk.business.buscard.base.appletcardinfo;

public class AppletCardResult<T> {
    public static final int RESULT_FAILED_AID_NOT_EXISTS = 3;
    public static final int RESULT_FAILED_APDU_EXCUTED_FAILED = 6;
    public static final int RESULT_FAILED_BANK_CARD_NUM_ABNORMAL = 301;
    public static final int RESULT_FAILED_CONFIG_JSON_DATA_NOT_FOUND = 5;
    public static final int RESULT_FAILED_INNER_EXCEPTION = 999;
    public static final int RESULT_FAILED_NOT_EXPECTED_SW = 9;
    public static final int RESULT_FAILED_PARAM_CONFIG_ERROR = 2;
    public static final int RESULT_FAILED_PARAM_ILLEGAL = 1;
    public static final int RESULT_FAILED_PARSE_JSON_ERROR = 4;
    public static final int RESULT_FAILED_TLV_DATA_FORMAT_ILLEGAL = 7;
    public static final int RESULT_FAILED_TLV_STATUS_WORD_WRONG = 8;
    public static final int RESULT_FAILED_TRAFFIC_CARD_INFO_AMOUNT_ABNORMAL = 203;
    public static final int RESULT_FAILED_TRAFFIC_CARD_INFO_DATE_FORMAT_ERROR = 206;
    public static final int RESULT_FAILED_TRAFFIC_CARD_INFO_ENABLE_DATE_ABNORMAL = 204;
    public static final int RESULT_FAILED_TRAFFIC_CARD_INFO_OUT_OF_EXPIRE_DATE = 205;
    public static final int RESULT_FAILED_TRAFFIC_CARD_INFO_OVERDRAFT_AMOUNT_ABNORMAL = 202;
    public static final int RESULT_FAILED_TRAFFIC_CARD_INFO_PIN_LOCKED = 208;
    public static final int RESULT_FAILED_TRAFFIC_CARD_INFO_STATUS_ABNORMAL = 201;
    public static final int RESULT_FAILED_TRAFFIC_CARD_INFO_VERIFY_PIN_FAILED = 207;
    public static final int RESULT_SUCCESS = 0;
    private T data;
    private String msg = "Success";
    private int resultCode = 0;

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

    public String getPrintMsg() {
        return " code : " + this.resultCode + " msg : " + this.msg;
    }
}
