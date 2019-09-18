package com.huawei.wallet.sdk.business.bankcard.api;

import android.graphics.Bitmap;

public interface CardInfoManagerApi {
    public static final int CHECK_TYPE_ACCESS_CARD_NUM = 4;
    public static final int CHECK_TYPE_BANK_CARD_NUM = 2;
    public static final int CHECK_TYPE_INIT_DATA = 0;
    public static final int CHECK_TYPE_TOTAL_CARD_NUM = 1;
    public static final int CHECK_TYPE_TRAFFIC_CARD_NUM = 3;
    public static final String DIC_NAME_DELETE_CARD_TIPS = "DeleteCardTips";
    public static final String NOTICE_TYPE_ISSUE_CARD = "2";
    public static final String NOTICE_TYPE_RECHARGE = "1";
    public static final int SETTING_DEFAULT_CARD_TYPE_AUTO = 1;
    public static final int SETTING_DEFAULT_CARD_TYPE_AUTO_CHOOSE = 3;
    public static final int SETTING_DEFAULT_CARD_TYPE_USER = 2;
    public static final int SPECIAL_TRAFFIC_BUSINESS_NONE_CODE = 0;
    public static final int SPECIAL_TRAFFIC_BUSINESS_OPENCARD_CODE = 1;
    public static final int SPECIAL_TRAFFIC_BUSINESS_RECHARGE_CODE = 2;
    public static final int UNSUPPORTED_MODE = -1;

    Bitmap getCardIcon(String str, int i);

    void refreshAllCardList();

    void refreshCardList(String str);

    void syncRFConfFiles(boolean z);
}
