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
    private static final String TAG = "HuaweiTelephonyConfigs";
    public static final String VALUE_CHIP_PLATFORM_HISI = "HISI";
    public static final String VALUE_CHIP_PLATFORM_QCOM = "QCOM";
    public static final String VALUE_CHIP_PLATFORM_UNDEFINED = "UNDEFINED";
    private static final String VALUE_OPERATOR_CHINA_CMCC = "CMCC";
    private static final String VALUE_OPERATOR_CHINA_TELECOM = "TELECOM";
    private static final String VALUE_OPERATOR_CHINA_UNICOM = "UNICOM";
    private static final String VALUE_OPERATOR_UNDEFINED = "UNDEFINED";
    private static final String definedChipPlatformValue = null;
    private static final String definedOperatorValue = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HuaweiTelephonyConfigs.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HuaweiTelephonyConfigs.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HuaweiTelephonyConfigs.<clinit>():void");
    }

    public static int getDefinedOperator() {
        if (VALUE_OPERATOR_CHINA_CMCC.equalsIgnoreCase(definedOperatorValue)) {
            return OPERATOR_CHINA_CMCC;
        }
        if (VALUE_OPERATOR_CHINA_TELECOM.equalsIgnoreCase(definedOperatorValue)) {
            return OPERATOR_CHINA_TELECOM;
        }
        if (VALUE_OPERATOR_CHINA_UNICOM.equalsIgnoreCase(definedOperatorValue)) {
            return OPERATOR_CHINA_UNICOM;
        }
        return OPERATOR_UNDEFINED;
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
            return OPERATOR_CHINA_CMCC;
        }
        if (VALUE_CHIP_PLATFORM_QCOM.equalsIgnoreCase(definedChipPlatformValue)) {
            return OPERATOR_CHINA_TELECOM;
        }
        return OPERATOR_UNDEFINED;
    }

    public static boolean isHisiPlatform() {
        if (definedChipPlatformValue.startsWith("hi363") || definedChipPlatformValue.startsWith("hi6210") || definedChipPlatformValue.startsWith("hi365") || definedChipPlatformValue.startsWith("hi6250")) {
            return true;
        }
        return definedChipPlatformValue.startsWith("hi366");
    }

    public static boolean isQcomPlatform() {
        return definedChipPlatformValue.startsWith("msm");
    }

    public static boolean isModemBipEnable() {
        return !isHisiPlatform() ? HwModemCapability.isCapabilitySupport(OPERATOR_CHINA_CMCC) : false;
    }

    public static boolean isPsRestrictedByFdn() {
        boolean FDN_PS_CHECK = SystemProperties.getBoolean("ro.config.fdn.restrict.ds", false);
        boolean FDN_PRELOAD_CACHE = SystemProperties.getBoolean("ro.config.fdn.preload", true);
        Log.d(TAG, "fddn EVENT_GET_AD_DONE FDN_PS_CHECK:" + FDN_PS_CHECK + " ,FDN_PRELOAD_CACHE:" + FDN_PRELOAD_CACHE);
        return FDN_PS_CHECK ? FDN_PRELOAD_CACHE : false;
    }
}
