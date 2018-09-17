package com.android.internal.telephony;

import android.os.SystemProperties;
import android.util.Log;

public class HuaweiTelephonyConfigs {
    public static final int CHIP_PLATFORM_HISI = 1;
    public static final int CHIP_PLATFORM_QCOM = 2;
    public static final int CHIP_PLATFORM_UNDEFINED = 0;
    public static final String FEATURE_ENABLE_HIPRI = "enableHIPRI";
    public static final String FEATURE_ENABLE_HIPRI_SUB1 = "enableHIPRI_sub1";
    public static final String FEATURE_ENABLE_HIPRI_SUB2 = "enableHIPRI_sub2";
    public static final String FEATURE_ENABLE_MMS = "enableMMS";
    public static final String FEATURE_ENABLE_MMS_SUB1 = "enableMMS_sub1";
    public static final String FEATURE_ENABLE_MMS_SUB2 = "enableMMS_sub2";
    public static final String FEATURE_ENABLE_SUPL = "enableSUPL";
    public static final int OPERATOR_CHINA_CMCC = 1;
    public static final int OPERATOR_CHINA_TELECOM = 2;
    public static final int OPERATOR_CHINA_UNICOM = 3;
    public static final int OPERATOR_UNDEFINED = 0;
    public static final String REASON_VOICE_CALL_ENDED = "2GVoiceCallEnded";
    private static final String TAG = "HuaweiTelephonyConfigs";
    public static final String VALUE_CHIP_PLATFORM_HISI = "HISI";
    public static final String VALUE_CHIP_PLATFORM_QCOM = "QCOM";
    public static final String VALUE_CHIP_PLATFORM_UNDEFINED = "UNDEFINED";
    private static final String VALUE_OPERATOR_CHINA_CMCC = "CMCC";
    private static final String VALUE_OPERATOR_CHINA_TELECOM = "TELECOM";
    private static final String VALUE_OPERATOR_CHINA_UNICOM = "UNICOM";
    private static final String VALUE_OPERATOR_UNDEFINED = "UNDEFINED";
    private static final String definedChipPlatformValue = SystemProperties.get("ro.board.platform", "UNDEFINED");
    private static final String definedOperatorValue = SystemProperties.get("ro.config.operators", "UNDEFINED");

    public static int getDefinedOperator() {
        if (VALUE_OPERATOR_CHINA_CMCC.equalsIgnoreCase(definedOperatorValue)) {
            return 1;
        }
        if (VALUE_OPERATOR_CHINA_TELECOM.equalsIgnoreCase(definedOperatorValue)) {
            return 2;
        }
        if (VALUE_OPERATOR_CHINA_UNICOM.equalsIgnoreCase(definedOperatorValue)) {
            return 3;
        }
        return 0;
    }

    public static boolean isChinaMobile() {
        return VALUE_OPERATOR_CHINA_CMCC.equalsIgnoreCase(definedOperatorValue);
    }

    public static boolean isChinaTelecom() {
        return VALUE_OPERATOR_CHINA_TELECOM.equalsIgnoreCase(definedOperatorValue);
    }

    public static boolean isChinaUnicom() {
        return VALUE_OPERATOR_CHINA_UNICOM.equalsIgnoreCase(definedOperatorValue);
    }

    public static int getDefinedChipPlatform() {
        if (VALUE_CHIP_PLATFORM_HISI.equalsIgnoreCase(definedChipPlatformValue)) {
            return 1;
        }
        if (VALUE_CHIP_PLATFORM_QCOM.equalsIgnoreCase(definedChipPlatformValue)) {
            return 2;
        }
        return 0;
    }

    public static boolean isHisiPlatform() {
        return (definedChipPlatformValue.startsWith("hi363") || definedChipPlatformValue.startsWith("hi6210") || definedChipPlatformValue.startsWith("hi365") || definedChipPlatformValue.startsWith("hi6250") || definedChipPlatformValue.startsWith("hi366")) ? true : definedChipPlatformValue.startsWith("kirin9");
    }

    public static boolean isQcomPlatform() {
        return definedChipPlatformValue.startsWith("msm");
    }

    public static boolean isModemBipEnable() {
        return !isHisiPlatform() ? HwModemCapability.isCapabilitySupport(1) : false;
    }

    public static boolean isPsRestrictedByFdn() {
        boolean FDN_PS_CHECK = SystemProperties.getBoolean("ro.config.fdn.restrict.ds", false);
        boolean FDN_PRELOAD_CACHE = SystemProperties.getBoolean("ro.config.fdn.preload", true);
        Log.d(TAG, "fddn EVENT_GET_AD_DONE FDN_PS_CHECK:" + FDN_PS_CHECK + " ,FDN_PRELOAD_CACHE:" + FDN_PRELOAD_CACHE);
        return FDN_PS_CHECK ? FDN_PRELOAD_CACHE : false;
    }
}
