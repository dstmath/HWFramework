package com.huawei.wallet.sdk.business.bankcard.api;

public interface CUPOperationListener {
    public static final int OPERATE_RESULT_FAILED = -99;
    public static final int OPERATE_RESULT_FAILED_DATA_OUT_OF_USE = -1;
    public static final int OPERATE_RESULT_SUCCESS = 0;

    void operateFinished(int i, String str);

    void operateStart(String str);
}
