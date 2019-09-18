package com.huawei.wallet.sdk.common.apdu.base;

import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;

public interface BaseCallback {
    public static final int GROUP_IS_NULL = 8;
    public static final int RESULT_FAILED = -1;
    public static final int RESULT_FAILED_DELETE_PASS = -401;
    public static final int RESULT_FAILED_NO_NETWORK = -2;
    public static final int RESULT_FAILED_PARAM_ERROR = -3;
    public static final int RESULT_FAILED_PERSONALIZE_ERROR = -5;
    public static final int RESULT_FAILED_TA_ERROR = -4;
    public static final int RESULT_SUCCESS = 0;
    public static final int WHITE_CARD_FAIL = 9;

    void onFail(int i, ErrorInfo errorInfo);

    void onSuccess(int i);
}
