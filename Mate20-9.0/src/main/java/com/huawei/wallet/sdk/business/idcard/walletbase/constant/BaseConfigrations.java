package com.huawei.wallet.sdk.business.idcard.walletbase.constant;

import com.huawei.wallet.sdk.common.AppConfig;
import com.huawei.wallet.sdk.common.apdu.properties.WalletSystemProperties;

public class BaseConfigrations {
    public static final boolean IS_NEED_SERVICETOKENAUTH = true;
    public static final String SERVICENAME_CFG_AGREEMENTSERVICE = "com.huawei.cloud.agreementservice";
    public static final String SERVICE_NAME_GREEMENT = "GREMENT";
    public static final String SERVICE_NAME_WALLET = "WALLET";
    public static final String SERVICE_NAME_WALLETPASS = "WALLETPASS";
    public static final String TAG_HIANALYTICS_FOR_NO_UDID = "hianalytics_no_udid";

    public static String getWalletMerchantId() {
        return WalletSystemProperties.getInstance().getProperty("WALLET_MERCHANT_ID", AppConfig.MERCHANT_ID);
    }

    public static String getAppName() {
        return WalletSystemProperties.getInstance().getProperty("APP_NAME", "hiwallet");
    }

    public static String getServicenameCfgWalletservices() {
        return WalletSystemProperties.getInstance().getProperty("SERVICENAME_CFG_WALLETSERVICES", "hiwalletServices");
    }
}
