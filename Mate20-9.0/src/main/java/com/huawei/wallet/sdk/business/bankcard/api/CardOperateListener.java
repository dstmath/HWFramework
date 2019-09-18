package com.huawei.wallet.sdk.business.bankcard.api;

public interface CardOperateListener {
    public static final int OPERATE_RESULT_ENROLMENT_TIMED_OUT = 95;
    public static final int OPERATE_RESULT_FAILED = -99;
    public static final int OPERATE_RESULT_FAILED_DATA_OUT_OF_USE = -1;
    public static final int OPERATE_RESULT_SUCCESS = 0;

    void operateFinished(String str, String str2, int i);

    void operateStart(String str, String str2);
}
