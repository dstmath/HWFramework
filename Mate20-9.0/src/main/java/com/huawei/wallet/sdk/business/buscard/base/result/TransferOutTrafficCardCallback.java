package com.huawei.wallet.sdk.business.buscard.base.result;

public interface TransferOutTrafficCardCallback extends TransferTrafficBaseCallback {
    public static final String EVENT = "transferOut";
    public static final int RETURN_APPLY_ORDER_FAILED = 1720;
    public static final int RETURN_CARD_BALANCE_OVERDRAWN = 1723;
    public static final int RETURN_CARD_NUM_LIMIT = 1724;
    public static final int RETURN_DELETE_SSD_FAILED = 1722;
    public static final int RETURN_SP_TRANSFER_OUT_FAILED = 1721;

    void transferOutCallback(int i);
}
