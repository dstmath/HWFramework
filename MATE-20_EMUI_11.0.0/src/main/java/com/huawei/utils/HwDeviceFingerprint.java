package com.huawei.utils;

import android.os.Build;
import android.text.TextUtils;
import android.util.Slog;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.storage.StorageManagerExt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwDeviceFingerprint {
    private static final String BRAND = getString("ro.product.brand");
    private static final String DEVICE = getString("ro.product.device");
    private static final String EMUI_VERSION = SystemPropertiesEx.get("ro.build.version.emui", StorageManagerExt.INVALID_KEY_DESC);
    private static final boolean EXPOSE_PRODUCT_HARDWARE_INFO = SystemPropertiesEx.getBoolean("ro.build.hardware_expose", false);
    private static final boolean HIDE_PRODUCT_INFO = isNeedHidden();
    private static final String ID = getString("ro.build.id");
    static final String[] MATCHERS = SystemPropertiesEx.get("ro.build.hide.matchers", StorageManagerExt.INVALID_KEY_DESC).trim().split(";");
    private static final long MSPES_HIDDEN_MASK = 2;
    private static final boolean NO_HOTA = SystemPropertiesEx.getBoolean("ro.build.nohota", false);
    private static final String PRODUCT = getString("ro.product.name");
    private static final String RELEASE = getString("ro.build.version.release");
    static final String[] REPLACEMENTS = SystemPropertiesEx.get("ro.build.hide.replacements", StorageManagerExt.INVALID_KEY_DESC).trim().split(";");
    private static final String TAG = "HwDeviceFingerprint";
    private static final String UNKNOWN = "unknown";

    private HwDeviceFingerprint() {
    }

    public static String deriveFingerprint() {
        String finger = getString("ro.huawei.build.fingerprint");
        if (!TextUtils.isEmpty(finger)) {
            return finger;
        }
        return getString("ro.product.brand") + '/' + getString("ro.product.name") + '/' + getString("ro.product.device") + ':' + getString("ro.build.version.release") + '/' + getString("ro.build.id") + '/' + getString("ro.huawei.build.version.incremental") + ':' + getString("ro.build.type") + '/' + getString("ro.build.tags");
    }

    public static String getString(String property) {
        String str = SystemPropertiesEx.get(property, UNKNOWN);
        if (NO_HOTA && property.equals("ro.huawei.build.display.id")) {
            str = str + "_NoHota";
        }
        if ((!HIDE_PRODUCT_INFO || !EXPOSE_PRODUCT_HARDWARE_INFO || !property.equals("ro.product.model")) && HIDE_PRODUCT_INFO && (property.equals("ro.build.id") || property.equals("ro.huawei.build.host") || property.equals("ro.build.user") || property.equals("ro.product.manufacturer") || property.equals("ro.hardware") || property.equals("ro.huawei.build.display.id") || property.equals("ro.huawei.build.fingerprint") || property.equals("ro.product.board") || property.equals("ro.product.device") || property.equals("ro.product.model") || property.equals("ro.product.name") || property.equals("ro.product.brand") || property.equals("ro.build.version.codename"))) {
            return hideBuildInfo(property, str);
        }
        return str;
    }

    private static String hideBuildInfo(String property, String str) {
        String[] strArr = MATCHERS;
        if (strArr.length == 0 || strArr[0].length() == 0) {
            Pattern pnHwverSp = Pattern.compile("C\\w{2}B\\d{3}SP\\d{2}");
            Pattern pnHwver = Pattern.compile("C\\w{2}B\\d{3}");
            Matcher mhwSp = pnHwverSp.matcher(str);
            Matcher mhw = pnHwver.matcher(str);
            String ver = null;
            if (mhwSp.find()) {
                ver = mhwSp.group();
            } else if (mhw.find()) {
                ver = mhw.group();
            } else {
                Slog.i(TAG, "ver = " + ((String) null));
            }
            if (property.equals("ro.huawei.build.fingerprint")) {
                if (ver == null) {
                    ver = "KRT16M";
                }
                return BRAND + "/" + PRODUCT + "/" + DEVICE + ":" + RELEASE + "/" + ID + "/" + ver + ":user/release-keys";
            } else if (property.equals("ro.build.user")) {
                return "android";
            } else {
                if (property.equals("ro.huawei.build.host")) {
                    return "localhost";
                }
                if (property.equals("ro.build.version.codename")) {
                    return "NOTREL";
                }
                if (ver != null) {
                    return ver;
                }
                Slog.i(TAG, "tar = " + UNKNOWN);
                return UNKNOWN;
            }
        } else {
            String st = str;
            int i = 0;
            while (true) {
                String[] strArr2 = MATCHERS;
                if (i >= strArr2.length) {
                    return st;
                }
                String match = strArr2[i];
                String[] strArr3 = REPLACEMENTS;
                st = Build.replaceIgnoreCase(st, match, i >= strArr3.length ? UNKNOWN : strArr3[i]);
                i++;
            }
        }
    }

    private static boolean isNeedHidden() {
        String mspesStr = SystemPropertiesEx.get("ro.mspes.config", UNKNOWN);
        if (mspesStr != null && !mspesStr.equals(UNKNOWN)) {
            try {
                if ((2 & Long.decode(mspesStr.trim()).longValue()) == 0) {
                    return false;
                }
                return true;
            } catch (NumberFormatException e) {
                Slog.e(TAG, "mspes config decode number fail: " + mspesStr.trim());
            }
        }
        return SystemPropertiesEx.getBoolean("ro.build.hide", false);
    }
}
