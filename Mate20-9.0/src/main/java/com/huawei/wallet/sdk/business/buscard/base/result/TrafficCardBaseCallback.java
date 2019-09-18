package com.huawei.wallet.sdk.business.buscard.base.result;

public interface TrafficCardBaseCallback {
    public static final int RETURN_FAILED_ACTIVATED_FAILED = 28;
    public static final int RETURN_FAILED_CARD_BALANCE_ERROR = 23;
    public static final int RETURN_FAILED_CARD_DISABLED = 21;
    public static final int RETURN_FAILED_CARD_IN_BLACKLIST = 22;
    public static final int RETURN_FAILED_CARD_OVERDRAFT_ERROR = 24;
    public static final int RETURN_FAILED_CONN_UNAVAILABLE = 25;
    public static final int RETURN_FAILED_CPLC_IS_NULL = 13;
    public static final int RETURN_FAILED_INNER_ERROR = 99;
    public static final int RETURN_FAILED_NFC_CLOSED = 12;
    public static final int RETURN_FAILED_NO_NETWORK = 11;
    public static final int RETURN_FAILED_ORDER_COMPLETED = 3005;
    public static final int RETURN_FAILED_ORDER_HAS_OPENED_SUCCESS = 2005;
    public static final int RETURN_FAILED_ORDER_USED = 4005;
    public static final int RETURN_FAILED_PARAM_ERROR = 10;
    public static final int RETURN_FAILED_READ_ESE_FAILED = 20;
    public static final int RETURN_FAILED_RETYR_DELAY = 27;
    public static final int RETURN_FAILED_SSD_DELETE_FAILED = 26;
    public static final int RETURN_FAILED_ST_CHECK_FAILED = 14;
    public static final int RETURN_FAILED_USE_NEW_ORDER = 3006;
    public static final int RETURN_SUCCESS = 0;
    public static final int RETURN_UNSUPPORTED = 1;
}
