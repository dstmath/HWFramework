package com.huawei.wallet.sdk.business.buscard.api;

import com.huawei.wallet.sdk.business.buscard.base.result.TransferOutTrafficCardCallback;
import com.huawei.wallet.sdk.business.buscard.base.result.UninstallTrafficCardCallback;
import com.huawei.wallet.sdk.business.buscard.model.ApplyOrderInfo;

public interface CardOperateLogicApi {
    public static final int HANDLE_ISSUE_FAILED_ORDERS = 0;
    public static final int HANDLE_OPENCARD_AND_RECHARGE_FAILED_ORDERS = 2;
    public static final int HANDLE_RECHARGE_FAILED_ORDERS = 1;
    public static final int ORDER_TYPE_CLOUD_CARD_IN = 11;
    public static final int ORDER_TYPE_CLOUD_CARD_OUT = 10;
    public static final int ORDER_TYPE_ISSUECARD = 1;
    public static final int ORDER_TYPE_ISSUECARD_RECHARGE = 3;
    public static final int ORDER_TYPE_RECHARGE = 2;
    public static final int ORDER_TYPE_REFUND = 7;
    public static final int ORDER_TYPE_SHIFT_CARD_IN = 5;
    public static final int ORDER_TYPE_SHIFT_CARD_OUT = 4;
    public static final int ORDER_TYPE_SHIFT_CARD_RECHARGE = 6;
    public static final int PAY_TYPE_HMS = 1;
    public static final int PAY_TYPE_HUAWEIPAY_UNION = 3;
    public static final int PAY_TYPE_IAP = 4;
    public static final int PAY_TYPE_WX = 2;
    public static final int PRIOR_QUERY_DONE_RECORDS = 0;
    public static final int PRIOR_QUERY_RECORDS_ONLY = 2;
    public static final int PRIOR_QUERY_UNDONE_RECORDS = 1;
    public static final int PRIOR_QUERY_UNDONE_RECORDS_ONLY = 3;
    public static final int READ_CARDINFO_BALANCE_BIT = 2;
    public static final int READ_CARDINFO_C8_FILE_STATUS = 16;
    public static final int READ_CARDINFO_IN_OUT_STATION_STATUS = 32;
    public static final int READ_CARDINFO_LOGIC_NUM = 128;
    public static final int READ_CARDINFO_NUM_BIT = 1;
    public static final int READ_CARDINFO_RIDE_TIMES = 64;
    public static final int READ_CARDINFO_TRANSCATION_RECORDS = 8;
    public static final int READ_CARDINFO_VALIDITY_DATE_BIT = 4;

    void applyPayOrder(String str, ApplyOrderInfo applyOrderInfo);

    void cloudTransferOut(String str, String str2, TransferOutTrafficCardCallback transferOutTrafficCardCallback);

    void initEseInfo();

    void queryOrder(String str, int i);

    void uninstallTrafficCard(String str, UninstallTrafficCardCallback uninstallTrafficCardCallback, boolean z, String str2, String str3, String str4, String str5, String str6);
}
