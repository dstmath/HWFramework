package com.huawei.wallet.sdk.business.buscard.model;

import com.huawei.wallet.sdk.business.buscard.base.result.TrafficCardBaseCallback;

public interface CheckTransferOutConditionCallback extends TrafficCardBaseCallback {
    public static final int RESULT_FAILED_TRAFFIC_CARD_RECORDS_READ_FAILED = 1780;
    public static final int RESULT_HAS_UNFINISHED_ORDER = 1781;
    public static final int RETURN_CARD_ACCOUT_CONFRIM_FAILED = 1785;
    public static final int RETURN_CARD_BALANCE_OVERDRAWN = 1783;
    public static final int RETURN_CARD_QUERY_BALANCE_FAILED = 1782;
    public static final int RETURN_CARD_VERIFY_SHIFT_FAILED = 1784;

    void checkTransferOutConditionCallback(int i);
}
