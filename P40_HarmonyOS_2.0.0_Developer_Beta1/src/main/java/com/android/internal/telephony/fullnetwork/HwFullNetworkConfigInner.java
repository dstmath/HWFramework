package com.android.internal.telephony.fullnetwork;

import android.telephony.HwTelephonyManager;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyfullnetwork.BuildConfig;

public class HwFullNetworkConfigInner {
    public static final String CMCC_UNBIND_DATE = SystemPropertiesEx.get("ro.hwpp.cmcc_unbind_date", BuildConfig.FLAVOR);
    public static final int DEFAULT_NETWORK_MODE = SystemPropertiesEx.getInt("ro.telephony.default_network", -1);
    public static final boolean IS_4G_SWITCH_SUPPORTED = SystemPropertiesEx.getBoolean("persist.sys.dualcards", false);
    public static final boolean IS_AIS_4G_DSDX_ENABLE = (HuaweiTelephonyConfigs.isMTKPlatform() && "ais".equalsIgnoreCase(SystemPropertiesEx.get("ro.hwpp.dualsim_swap_solution", BuildConfig.FLAVOR)));
    public static final boolean IS_CARD2_CDMA_SUPPORTED = SystemPropertiesEx.getBoolean("ro.hwpp.card2_cdma_support", false);
    public static final boolean IS_CHINA_TELECOM = HwFullNetworkConfig.IS_CHINA_TELECOM;
    public static boolean IS_CMCC_4GSWITCH_DISABLE = "cmcc".equalsIgnoreCase(SystemPropertiesEx.get("ro.hwpp.dualsim_swap_solution", BuildConfig.FLAVOR));
    public static boolean IS_CMCC_4G_DSDX_ENABLE = SystemPropertiesEx.getBoolean("ro.hwpp.cmcc_4G_dsdx_enable", false);
    public static final boolean IS_CMCC_CU_DSDX_ENABLE = SystemPropertiesEx.getBoolean("ro.hwpp.cmcc_cu_dsdx_enable", false);
    public static final boolean IS_CMCC_UNBIND = SystemPropertiesEx.getBoolean("ro.hwpp.cmcc_unbind", false);
    public static final boolean IS_CT_4GSWITCH_DISABLE = HwFullNetworkConfig.IS_CT_4GSWITCH_DISABLE;
    public static final boolean IS_DUAL_4G_SUPPORTED = HwModemCapability.isCapabilitySupport(21);
    public static final boolean IS_FAST_SWITCH_SIMSLOT = HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT;
    public static final boolean IS_FULL_NETWORK_SUPPORTED = SystemPropertiesEx.getBoolean("ro.config.full_network_support", false);
    public static final boolean IS_HISI_DSDX = HwFullNetworkConfig.IS_HISI_DSDX;
    public static final boolean IS_MTN_4G_DSDX_ENABLE = (HuaweiTelephonyConfigs.isMTKPlatform() && "mtn".equalsIgnoreCase(SystemPropertiesEx.get("ro.hwpp.dualsim_swap_solution", BuildConfig.FLAVOR)));
    public static final boolean IS_NET_WORK_MODE_HIDE = SystemPropertiesEx.getBoolean("ro.config.networkmode_hide_dyn", false);
    public static final boolean IS_QCOM_DUAL_LTE_STACK = HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK;
    public static final boolean IS_QCRIL_CROSS_MAPPING = HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING;
    public static final boolean IS_SINGLE_CARD_TRAY = SystemPropertiesEx.getBoolean("persist.radio.single_card_tray", true);
    public static final boolean IS_SMART_4G_DSDX_ENABLE = (HuaweiTelephonyConfigs.isMTKPlatform() && "smart".equalsIgnoreCase(SystemPropertiesEx.get("ro.hwpp.dualsim_swap_solution", BuildConfig.FLAVOR)));
    public static final boolean IS_VICE_WCDMA = SystemPropertiesEx.getBoolean("ro.config.support_wcdma_modem1", false);
    private static final String LOG_TAG = "HwFullNetworkConfigInner";
    public static final String PROPERTY_COMMRIL_MODE = "persist.radio.commril_mode";
    public static final boolean USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK;

    static {
        boolean z = true;
        if (!"normal".equals(SystemPropertiesEx.get("ro.runmode", "normal")) || !SystemPropertiesEx.getBoolean("ro.config.use_user_preference_default_slot", true)) {
            z = false;
        }
        USE_USER_PREFERENCE_DEFAULT_SLOT_IN_QC_AND_MTK = z;
    }

    public static boolean isCMCCDsdxEnable() {
        return IS_CMCC_4G_DSDX_ENABLE;
    }

    public static void setCMCCDsdxEnable(boolean isEnable) {
        logd("setCMCC4GDsdsEnable: isEnable = " + isEnable);
        IS_CMCC_4G_DSDX_ENABLE = isEnable;
    }

    public static boolean isCMCCDsdxDisable() {
        return IS_CMCC_4G_DSDX_ENABLE && IS_CMCC_4GSWITCH_DISABLE;
    }

    private static void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    public static boolean isCustomVersion() {
        return IS_AIS_4G_DSDX_ENABLE || IS_SMART_4G_DSDX_ENABLE || IS_MTN_4G_DSDX_ENABLE;
    }

    public static boolean isSupportFastSetNetworkMode() {
        return HuaweiTelephonyConfigs.isMTKPlatform() && HwTelephonyManager.getDefault().isNrSupported() && HwFullNetworkConstantsInner.SIM_NUM == 2;
    }
}
