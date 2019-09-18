package com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant;

import com.huawei.wallet.sdk.business.idcard.walletbase.constant.BaseConfigrations;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.oversea.OverSeasManager;
import com.huawei.wallet.sdk.common.apdu.BaseCommonContext;
import com.huawei.wallet.sdk.common.apdu.properties.WalletSystemProperties;

public class ServiceConfig {
    public static String getCardInfoManageServerUrl() {
        return OverSeasManager.getInstance(BaseCommonContext.getInstance().getApplicationContext()).getGrsUrlSync("WALLET");
    }

    public static String getAccessCardManageServerUrl() {
        return OverSeasManager.getInstance(BaseCommonContext.getInstance().getApplicationContext()).getGrsUrlSync("WALLETPASS");
    }

    public static String getWalletMerchantId() {
        return BaseConfigrations.getWalletMerchantId();
    }

    public static String getUnionMode() {
        return WalletSystemProperties.getInstance().getProperty("UNION_PAY_SERVER_MODE", "00");
    }
}
