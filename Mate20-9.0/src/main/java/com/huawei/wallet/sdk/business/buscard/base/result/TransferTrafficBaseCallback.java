package com.huawei.wallet.sdk.business.buscard.base.result;

public interface TransferTrafficBaseCallback extends TrafficCardBaseCallback {
    public static final int RETURN_CARD_QUERY_BALANCE_FAILED = 1702;
    public static final int RETURN_CARD_TRANSFER_STATUS_ERROR = 1706;
    public static final int RETURN_QUERY_CPLC_FAILED = 1704;
    public static final int RETURN_QUERY_TRANSFER_STATUS_FAILED = 1701;
    public static final int RETURN_REPORT_EVENT_FAILED = 1703;
    public static final int RETURN_THIS_ACCOUNT_HAS_NOT_VALID_TRANSFER_EVENT = 1705;
}
