package com.android.server.security.tsmagent.constant;

import android.os.SystemProperties;

public class ServiceConfig {
    private static final String CARD_COMMERCIAL_URL = "https://nfcws.hicloud.com/Wallet/wallet/gateway.action";
    public static final String CARD_INFO_MANAGE_SERVER_URL = SystemProperties.get(CARD_SERVER_PROP, CARD_COMMERCIAL_URL);
    private static final String CARD_SERVER_PROP = "ro.config.card_server";
    public static final String HUAWEI_TSM_REMOTE_URL = SystemProperties.get(TSM_SERVER_PROP, TSM_COMMERCIAL_URL);
    private static final String TSM_COMMERCIAL_URL = "https://tsm.hicloud.com:9001/TSMAPKP/HwTSMServer/applicationBusiness.action";
    private static final String TSM_SERVER_PROP = "ro.config.tsm_server";
    private static final String WALLET_COMMERCIAL_ID = "260086000000068459";
    public static final String WALLET_MERCHANT_ID = SystemProperties.get(WALLET_MERCHANT_ID_PROP, WALLET_COMMERCIAL_ID);
    private static final String WALLET_MERCHANT_ID_PROP = "ro.config.wallet_merchant_id";
    public static final int WALLET_RSA_KEY_INDEX = -1;
}
