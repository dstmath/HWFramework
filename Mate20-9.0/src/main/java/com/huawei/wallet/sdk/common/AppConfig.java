package com.huawei.wallet.sdk.common;

import android.content.Context;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;

public class AppConfig {
    public static final String CARD_SERVER_PROTOCAL_VERSION = "1.0";
    public static final String CLIENTVERSION = "2.0.6";
    public static final boolean ISFORROM = true;
    public static final String MERCHANT_ID = "900086000000010204";
    public static final String VERSION = "1.0";
    public static final int WALLET_RSA_KEY_INDEX = -1;

    public static String getImei(Context context) {
        return PhoneDeviceUtil.getDeviceID(context);
    }
}
