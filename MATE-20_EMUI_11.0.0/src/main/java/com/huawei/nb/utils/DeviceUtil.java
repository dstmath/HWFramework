package com.huawei.nb.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import com.huawei.android.os.BuildEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.nb.utils.logger.DSLog;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class DeviceUtil {
    private static final String BOOT_MODE_KEY = "ro.huawei.odmf.mode";
    private static final String BOOT_MODE_VALUE_BASIC = "basic";
    private static final String BOOT_MODE_VALUE_NORMAL = "normal";
    public static final String CHINA = "China";
    public static final String DEVICE_ATTESTATION_MANAGER = "com.huawei.attestation.HwAttestationManager";
    private static final String DEVICE_INFO_COMMON = "common";
    private static final int DEVICE_TOKEN_KEY_NO_BASE = 2;
    private static final String DOMESTIC_BETA_USER = "3";
    public static final String OVERSEA = "Oversea";
    private static final String SEARCH_SWITCH_KEY = "ro.config.hw_globalSearch";
    private static final String TAG = "DeviceUtil";
    private static String sDeviceToken;

    public static boolean isScreenOn(Context context) {
        if (context == null) {
            return false;
        }
        return ((PowerManager) context.getSystemService("power")).isInteractive();
    }

    public static String getSerialNumber() {
        try {
            if (Build.VERSION.SDK_INT < 26) {
                return Build.SERIAL;
            }
            return Build.getSerial();
        } catch (SecurityException unused) {
            DSLog.e("DeviceUtil VerifyViaHWMember Exception : getSerial without READ_PHONE_STATE permission", new Object[0]);
            return "";
        }
    }

    public static String getVersionName(Context context) {
        PackageInfo packageInfo;
        return (context == null || (packageInfo = getPackageInfo(context)) == null) ? "" : packageInfo.versionName;
    }

    public static int getVersionCode(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo != null) {
            return packageInfo.versionCode;
        }
        return 0;
    }

    private static PackageInfo getPackageInfo(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                return packageManager.getPackageInfo(context.getPackageName(), 0);
            }
            return null;
        } catch (PackageManager.NameNotFoundException | RuntimeException e) {
            DSLog.e("DeviceUtil getPackageInfo fail!" + e.getMessage(), new Object[0]);
            return null;
        }
    }

    public static String getEMMCID() {
        int intFiled = getIntFiled(DEVICE_ATTESTATION_MANAGER, "DEVICE_ID_TYPE_EMMC", -1);
        if (intFiled == -1) {
            DSLog.e("DeviceUtil getAttestationSignature failed: deviceIdTypeEmmc == -1", new Object[0]);
            return "";
        }
        Object obj = null;
        try {
            Class<?> cls = Class.forName(DEVICE_ATTESTATION_MANAGER);
            if (cls == null) {
                return "";
            }
            obj = cls.getDeclaredMethod("getDeviceID", Integer.TYPE).invoke(cls.newInstance(), Integer.valueOf(intFiled));
            if (obj != null) {
                return new String((byte[]) obj, StandardCharsets.UTF_8);
            }
            DSLog.e("DeviceUtil emmcID is empty!", new Object[0]);
            return "";
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            DSLog.e("DeviceUtil getEMMCID failed: ", new Object[0]);
        }
    }

    public static int getIntFiled(String str, String str2, int i) {
        Class<?> cls;
        try {
            cls = Class.forName(str);
        } catch (ClassNotFoundException unused) {
            DSLog.e("DeviceUtil getIntFiled failed: ClassNotFoundException", new Object[0]);
            cls = null;
        }
        if (cls != null) {
            try {
                return cls.getField(str2).getInt(null);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException unused2) {
                DSLog.e("DeviceUtil getIntFiled failed: ", new Object[0]);
            }
        }
        return i;
    }

    public static int getCurrentUserId() {
        try {
            return ((Integer) UserHandle.class.getDeclaredMethod("getCallingUserId", new Class[0]).invoke(null, new Object[0])).intValue();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            DSLog.e("DeviceUtil getCurrentUId Exception :", new Object[0]);
            return -1;
        }
    }

    public static boolean isDomesticBetaUser() {
        return "3".equals(SystemPropertiesEx.get("ro.logsystem.usertype"));
    }

    public static String getChipset() {
        String str = SystemPropertiesEx.get("ro.hardware");
        if (TextUtils.isEmpty(str)) {
            return DEVICE_INFO_COMMON;
        }
        return ("kirin720".equals(str) || "kirin810".equals(str)) ? "orlando" : str;
    }

    public static String getDistrict() {
        return "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", "")) ? CHINA : OVERSEA;
    }

    public static String getRegion() {
        return SystemPropertiesEx.get("ro.product.locale.region", "");
    }

    public static String getProductFamily() {
        if ("tablet".equalsIgnoreCase(SystemPropertiesEx.get("ro.build.characteristics"))) {
            return "tablet";
        }
        return "phone";
    }

    public static String getEmuiFamily() {
        return isLiteDevice() ? "lite" : "full";
    }

    public static boolean isLiteDevice() {
        return SystemPropertiesEx.getBoolean("ro.build.hw_emui_lite.enable", false);
    }

    public static String getProduct() {
        String str = SystemPropertiesEx.get("ro.build.product");
        return TextUtils.isEmpty(str) ? DEVICE_INFO_COMMON : str;
    }

    public static String getChipsetVendor() {
        String str = SystemPropertiesEx.get("ro.hardware");
        if (TextUtils.isEmpty(str)) {
            return DEVICE_INFO_COMMON;
        }
        String lowerCase = str.toLowerCase(Locale.ENGLISH);
        if (lowerCase.contains("kirin") || lowerCase.contains("hisi")) {
            return "Hisi";
        }
        if (lowerCase.contains("msm") || lowerCase.contains("qsd") || lowerCase.contains("apq")) {
            return "Qcom";
        }
        return DEVICE_INFO_COMMON;
    }

    public static String getModel() {
        String str = SystemPropertiesEx.get("ro.product.name");
        if (TextUtils.isEmpty(str)) {
            return DEVICE_INFO_COMMON;
        }
        String[] split = str.split("-");
        return split.length >= 2 ? split[1] : DEVICE_INFO_COMMON;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0065: APUT  
      (r3v1 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Boolean : 0x0061: INVOKE  (r7v2 java.lang.Boolean) = (r7v1 boolean) type: STATIC call: java.lang.Boolean.valueOf(boolean):java.lang.Boolean)
     */
    public static Boolean isUserKnown(Context context, int i) {
        boolean z = true;
        if (context == null || i < 0) {
            Object[] objArr = new Object[2];
            objArr[0] = Boolean.valueOf(context == null);
            objArr[1] = Integer.valueOf(i);
            DSLog.e("DeviceUtil isUserKnown err param, context null[%s] or dirUid invalid [%s]", objArr);
            return null;
        }
        try {
            if (UserManager.class.getDeclaredMethod("getUserInfo", Integer.TYPE).invoke((UserManager) context.getSystemService("user"), Integer.valueOf(i)) == null) {
                z = false;
            }
            return Boolean.valueOf(z);
        } catch (IllegalAccessException | NoSuchMethodException | RuntimeException | InvocationTargetException e) {
            DSLog.e("DeviceUtil isUserKnown failed with exception: " + e.getClass().getName(), new Object[0]);
            return null;
        }
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getProductModel() {
        return Build.MODEL;
    }

    public static String getProductBoard() {
        return SystemPropertiesEx.get("ro.product.board");
    }

    public static boolean isBasicMode() {
        String str = SystemPropertiesEx.get(BOOT_MODE_KEY, BOOT_MODE_VALUE_NORMAL);
        boolean z = SystemPropertiesEx.getBoolean(SEARCH_SWITCH_KEY, true);
        if (BOOT_MODE_VALUE_BASIC.equalsIgnoreCase(str)) {
            if (!z) {
                return true;
            }
            DSLog.e("NaturalBase Basic Mode Can Not Provide Search Service", new Object[0]);
        }
        return false;
    }

    public static String getUdid() {
        return BuildEx.getUDID();
    }
}
