package com.huawei.wallet.sdk.business.diploma.util;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;

public class DiplomaUtil {
    private static final String COMMANDER = "nfc.upload.cert";
    private static final int GETCOUNTRYCODE_FAIL = 1;
    private static final int GETCOUNTRYCODE_SUCCES = 0;
    private static final int GETFACTORYFLAG_FAIL = 0;
    private static final int GETFACTORYFLAG_SUCCESS = 1;
    private static final int GETROUTERINFO_FAIL = 1;
    private static final int GETROUTERINFO_SUCCESS = 0;
    private static final int INIT_FLAG_FAIL = 0;
    private static final int INIT_FLAG_SUCCESS = 1;
    private static final String QUERY_SIGN_FAILED = "query_sign_failed";
    private static final int SETCOUNTRYCODE_FAIL = 1;
    private static final int SETCOUNTRYCODE_SUCCES = 0;
    private static final int SETFACTORYFLAG_FAIL = 0;
    private static final int SETFACTORYFLAG_SUCCESS = 1;
    private static final int SETFLAG_FAIL = 0;
    private static final int SETFLAG_SUCCESS = 1;
    private static final int SETROUTERINFO_FAIL = 0;
    private static final int SETROUTERINFO_SUCCESS = 1;
    private static final String SIGN_CERT = "RSAWithCert";
    private static final int UPLOADFLAG_FAIL = 0;
    private static final int UPLOADFLAG_SUCCESS = 1;

    public static int initDiploma(Context context) {
        int flag = 0;
        try {
            String deviceId = PhoneDeviceUtil.getDeviceID(context);
            String cplcs = ESEInfoManager.getInstance(context).queryCplcListString();
            if (!TextUtils.isEmpty(cplcs) && !cplcs.startsWith("null") && !cplcs.endsWith("null") && !cplcs.startsWith("|")) {
                if (!cplcs.endsWith("|")) {
                    LogC.d("DiplomaUtil|initDiploma|data:" + data, false);
                    WalletTaManager.getInstance(context).initCertification("nfc.upload.cert|" + deviceId + "|" + SIGN_CERT + "|" + cplcs + "|");
                    flag = 1;
                    return flag;
                }
            }
            return 0;
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("initDiploma failed", false);
        }
    }

    public static String getSignature(Context context, String signatureReq) {
        try {
            return WalletTaManager.getInstance(context).getSignature(signatureReq);
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("queryCertAndSign failed", false);
            return QUERY_SIGN_FAILED;
        }
    }

    public static int getUploadFlag(Context context) {
        try {
            return WalletTaManager.getInstance(context).getCertUploadFlag();
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("getUploadFlag failed", false);
            return 0;
        }
    }

    public static int setUploadFlag(Context context, int certUploadFlag) {
        try {
            WalletTaManager.getInstance(context).setCertUploadFlag(certUploadFlag);
            return 1;
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("setUploadFlag failed", false);
            return 0;
        }
    }

    public static int getResetFactoryFlag(Context context) {
        try {
            return WalletTaManager.getInstance(context).getResetFactoryFlag();
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("getResetFactoryFlag failed", false);
            return 0;
        }
    }

    public static int setResetFactoryFlag(Context context, int resetFactoryFlag) {
        try {
            WalletTaManager.getInstance(context).setResetFactoryFlag(resetFactoryFlag);
            return 1;
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("getResetFactoryFlag failed", false);
            return 0;
        }
    }

    public static int getRouterInfo(Context context) {
        try {
            if (!StringUtil.isEmpty(WalletTaManager.getInstance(context).getRouterInfo(), true)) {
                return 0;
            }
            return 1;
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("getResetFactoryFlag failed", false);
            return 1;
        }
    }

    public static int setRouterInfo(Context context, int routerInfo) {
        try {
            WalletTaManager.getInstance(context).setResetFactoryFlag(routerInfo);
            return 1;
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("setRouterInfo failed", false);
            return 0;
        }
    }

    public static int getCountryCode(Context context) {
        try {
            if (!StringUtil.isEmpty(WalletTaManager.getInstance(context).getCountryCode(), true)) {
                return 0;
            }
            return 1;
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("getCountryCode failed", false);
            return 1;
        }
    }

    public static int setCountryCode(Context context, String countryCode) {
        try {
            WalletTaManager.getInstance(context).setCountryCode(countryCode);
            return 0;
        } catch (WalletTaException.WalletTaSystemErrorException e) {
            LogC.e("setCountryCode failed", false);
            return 1;
        }
    }

    public static void unInitTA(Context context) {
        WalletTaManager.unInitTA();
    }
}
