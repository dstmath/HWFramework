package com.huawei.wallet.sdk.business.buscard.model;

import com.huawei.wallet.sdk.business.buscard.base.result.TrafficCardBaseCallback;

public interface ApplyPayOrderCallback extends TrafficCardBaseCallback {
    public static final int RETURN_FAILED_APPLY_ORDER_INNER_ERROR = 1099;
    public static final int RETURN_FAILED_APPLY_ORDER_PROMOTION_IS_OVER_ERROR = 10001;
    public static final int RETURN_FAILED_BALANCE_IS_MINUS = 1003;
    public static final int RETURN_FAILED_BALANCE_REACH_LIMIT = 1001;
    public static final int RETURN_FAILED_CARDINFO_AFTER_EXPIRE_DATE_ERROR = 1021;
    public static final int RETURN_FAILED_CARDINFO_BALANCE_ERROR = 1026;
    public static final int RETURN_FAILED_CARDINFO_BEFORE_ENABLE_DATE_ERROR = 1020;
    public static final int RETURN_FAILED_CARDINFO_DATE_ERROR = 1022;
    public static final int RETURN_FAILED_CARDINFO_OVERDRAFT_AMOUNT_MINUS = 1027;
    public static final int RETURN_FAILED_CARDINFO_PIN_LOCKED = 1024;
    public static final int RETURN_FAILED_CARDINFO_PIN_VERIFY_FAILED = 1023;
    public static final int RETURN_FAILED_CARDINFO_READ_FAILED = 1039;
    public static final int RETURN_FAILED_CARDINFO_STATUS_DISABLE = 1025;
    public static final int RETURN_FAILED_CARD_RECHARGE_AMOUNT_ILLEGAL_ERROR = 1007;
    public static final int RETURN_FAILED_CITYCODE_ILLEGAL = 1004;
    public static final int RETURN_FAILED_COST_REFRESH = 1207;
    public static final int RETURN_FAILED_FLOW_CONTROL = 6;
    public static final int RETURN_FAILED_NO_ENOUGH_CARD_RESOURCE = 1005;
    public static final int RETURN_FAILED_OPERATE_FREQUENT = 1126;
    public static final int RETURN_FAILED_OVER_DAILY_RECHARGE_COUNT_LIMIT = 1029;
    public static final int RETURN_FAILED_OVER_DAILY_RECHARGE_LIMIT = 1028;
    public static final int RETURN_FAILED_SP_SERVICE_STOPPED = 1006;
    public static final int RETURN_FAILED_UNFINISHED_ORDERS_EXIST = 1002;
    public static final int RETURN_SUCCESS_APPLY_ORDER_CLOUD_TRANSFER_IN = 1101;
    public static final int RETURN_SUCCESS_APPLY_ORDER_CLOUD_TRANSFER_IN_RESTORE = 1102;
    public static final int RETURN_SUCCESS_APPLY_ORDER_CLOUD_TRANSFER_OUT = 1001;
    public static final int RETURN_SUCCESS_APPLY_ORDER_CLOUD_TRANSFER_OUT_BACKUP = 1002;
    public static final int RETURN_SUCCESS_APPLY_ORDER_CLOUD_TRANSFER_OUT_REMOVE = 1003;

    void applyPayOrderCallback(int i, TrafficOrder trafficOrder);
}
