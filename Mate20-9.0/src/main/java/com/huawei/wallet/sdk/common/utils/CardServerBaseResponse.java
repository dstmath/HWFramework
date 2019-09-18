package com.huawei.wallet.sdk.common.utils;

public class CardServerBaseResponse {
    public static final int RESPONSE_CODE_CONNECTION_FAILED = -2;
    public static final int RESPONSE_CODE_INTERNAL_ERROR = 3;
    public static final int RESPONSE_CODE_NO_ACCESS_AUTHORITY = 4;
    public static final int RESPONSE_CODE_NO_NETWORK_FAILED = -1;
    public static final int RESPONSE_CODE_OPERATION_FAILED = 5;
    public static final int RESPONSE_CODE_OTHER_ERRORS = -99;
    public static final int RESPONSE_CODE_PARAMS_ERROR = 1;
    public static final int RESPONSE_CODE_SIGNATURE_ERROR = 2;
    public static final int RESPONSE_CODE_SUCCESS = 0;
    public int returnCode = -99;

    public int getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(int returnCode2) {
        this.returnCode = returnCode2;
    }
}
