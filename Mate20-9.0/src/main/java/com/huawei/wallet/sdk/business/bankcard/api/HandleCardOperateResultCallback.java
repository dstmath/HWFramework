package com.huawei.wallet.sdk.business.bankcard.api;

public interface HandleCardOperateResultCallback {
    public static final int OPERATE_RESULT_FAILED = -99;
    public static final int OPERATE_RESULT_SUCCESS = 0;
    public static final int PERSONLIZED_RESULT_SUCCESS = 100;

    void operateResultCallback(int i);
}
