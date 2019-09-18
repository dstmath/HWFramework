package com.android.server.security.tsmagent.constant;

import android.os.SystemProperties;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.regex.Pattern;

public class ServiceConfig {
    private static final String CARD_COMMERCIAL_URL_CN = "https://vcardmgt-drcn.wallet.hicloud.com/WiseCloudVirtualCardMgmtService/app/gateway";
    private static final String CARD_COMMERCIAL_URL_OVERSEAS = "https://vcardmgt-dre.wallet.hicloud.com/WiseCloudVirtualCardMgmtService/app/gateway";
    private static String CARD_INFO_MANAGE_SERVER_URL = getDefaultCardUrl();
    private static final String CARD_SERVER_PROP = "ro.config.card_server";
    private static String HUAWEI_TSM_REMOTE_URL = getDefaultTsmUrl();
    private static final Boolean IS_CHINA_AREA = Boolean.valueOf("CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, "")));
    private static final String TSM_COMMERCIAL_URL_CN = "https://tsm.hicloud.com:9001/TSMAPKP/HwTSMServer/applicationBusiness.action";
    private static final String TSM_COMMERCIAL_URL_OVERSEAS = "https://tsm-dre.wallet.hicloud.com/TSMAPKP/HwTSMServer/applicationBusiness.action";
    private static final String TSM_SERVER_PROP = "ro.config.tsm_server";
    private static final String WALLET_COMMERCIAL_ID = "260086000000068459";
    private static String WALLET_MERCHANT_ID = SystemProperties.get(WALLET_MERCHANT_ID_PROP, WALLET_COMMERCIAL_ID);
    private static final String WALLET_MERCHANT_ID_PROP = "ro.config.wallet_merchant_id";
    public static final int WALLET_RSA_KEY_INDEX = -1;
    private static Pattern pattern = Pattern.compile(regEx);
    private static String regEx = "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\\\/])+$";

    private static String getDefaultCardUrl() {
        return SystemProperties.get(CARD_SERVER_PROP, IS_CHINA_AREA.booleanValue() ? CARD_COMMERCIAL_URL_CN : CARD_COMMERCIAL_URL_OVERSEAS);
    }

    private static String getDefaultTsmUrl() {
        return SystemProperties.get(TSM_SERVER_PROP, IS_CHINA_AREA.booleanValue() ? TSM_COMMERCIAL_URL_CN : TSM_COMMERCIAL_URL_OVERSEAS);
    }

    public static void setTsmUrl(String url) {
        if (url == null || !isUrl(url)) {
            HUAWEI_TSM_REMOTE_URL = getDefaultTsmUrl();
        } else {
            HUAWEI_TSM_REMOTE_URL = url;
        }
    }

    public static void setCardUrl(String url) {
        if (url == null || !isUrl(url)) {
            CARD_INFO_MANAGE_SERVER_URL = getDefaultCardUrl();
        } else {
            CARD_INFO_MANAGE_SERVER_URL = url;
        }
    }

    public static void setWalletId(String id) {
        if (id != null) {
            WALLET_MERCHANT_ID = id;
        } else {
            WALLET_MERCHANT_ID = SystemProperties.get(WALLET_MERCHANT_ID_PROP, WALLET_COMMERCIAL_ID);
        }
    }

    public static String getCardUrl() {
        return CARD_INFO_MANAGE_SERVER_URL;
    }

    public static String getTsmUrl() {
        return HUAWEI_TSM_REMOTE_URL;
    }

    public static String getWalletId() {
        return WALLET_MERCHANT_ID;
    }

    private static boolean isUrl(String pInput) {
        if (pInput == null) {
            return false;
        }
        return pattern.matcher(pInput).matches();
    }
}
