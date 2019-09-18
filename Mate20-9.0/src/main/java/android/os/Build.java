package android.os;

import android.annotation.SystemApi;
import android.os.IDeviceIdentifiersPolicyService;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.Slog;
import dalvik.system.VMRuntime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Build {
    public static final String BOARD = getString("ro.product.board");
    public static final String BOOTLOADER = getString("ro.bootloader");
    public static final String BRAND = getString("ro.product.brand");
    @Deprecated
    public static final String CPU_ABI;
    @Deprecated
    public static final String CPU_ABI2;
    public static final String DEVICE = getString("ro.product.device");
    public static final String DISPLAY = getString("ro.build.display.id");
    public static final String FINGERPRINT = deriveFingerprint();
    public static final String HARDWARE = getString(HIDE_PRODUCT_INFO ? "ro.hardware.alter" : "ro.hardware");
    public static final boolean HIDE_PRODUCT_INFO = isNeedHidden();
    public static final String HOST = getString("ro.build.host");
    public static final String HWFINGERPRINT = (SystemProperties.get("ro.comp.real.hl.product_base_version", "") + "/" + SystemProperties.get("ro.comp.real.hl.product_cust_version", "") + "/" + SystemProperties.get("ro.comp.real.hl.product_preload_version", "") + "/" + SystemProperties.get("ro.comp.hl.product_base_version", "") + "/" + SystemProperties.get("ro.comp.hl.product_cust_version", "") + "/" + SystemProperties.get("ro.comp.hl.product_preload_version", "") + "/" + SystemProperties.get("ro.product.CotaVersion", "") + "/" + SystemProperties.get("ro.patchversion", ""));
    public static final String ID = getString("ro.build.id");
    public static final boolean IS_CONTAINER = SystemProperties.getBoolean("ro.boot.container", false);
    public static final boolean IS_DEBUGGABLE = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    public static final boolean IS_EMULATOR = getString("ro.kernel.qemu").equals("1");
    public static final boolean IS_ENG = "eng".equals(TYPE);
    public static final boolean IS_TREBLE_ENABLED = SystemProperties.getBoolean("ro.treble.enabled", false);
    public static final boolean IS_USER = "user".equals(TYPE);
    public static final boolean IS_USERDEBUG = "userdebug".equals(TYPE);
    public static final String MANUFACTURER = getString("ro.product.manufacturer");
    public static final String MODEL = getString("ro.product.model");
    public static final boolean NO_HOTA = SystemProperties.getBoolean("ro.build.nohota", false);
    @SystemApi
    public static final boolean PERMISSIONS_REVIEW_REQUIRED;
    public static final String PRODUCT = getString("ro.product.name");
    @Deprecated
    public static final String RADIO = getString("gsm.version.baseband");
    @Deprecated
    public static final String SERIAL = getString("no.such.thing");
    public static final String[] SUPPORTED_32_BIT_ABIS = getStringList("ro.product.cpu.abilist32", ",");
    public static final String[] SUPPORTED_64_BIT_ABIS = getStringList("ro.product.cpu.abilist64", ",");
    public static final String[] SUPPORTED_ABIS = getStringList("ro.product.cpu.abilist", ",");
    private static final String TAG = "Build";
    public static final String TAGS = getString("ro.build.tags");
    public static final long TIME = (getLong("ro.build.date.utc") * 1000);
    public static final String TYPE = getString("ro.build.type");
    public static final String UNKNOWN = "unknown";
    public static final String USER = getString("ro.build.user");
    static final String[] matchers = SystemProperties.get("ro.build.hide.matchers", "").trim().split(";");
    static final String[] replacements = SystemProperties.get("ro.build.hide.replacements", "").trim().split(";");

    public static class VERSION {
        public static final String[] ACTIVE_CODENAMES = ("REL".equals(ALL_CODENAMES[0]) ? new String[0] : ALL_CODENAMES);
        private static final String[] ALL_CODENAMES = Build.getStringList("ro.build.version.all_codenames", ",");
        public static final String BASE_OS = SystemProperties.get("ro.build.version.base_os", "");
        public static final String CODENAME = Build.getString("ro.build.version.codename");
        public static final int FIRST_SDK_INT = SystemProperties.getInt("ro.product.first_api_level", 28);
        public static final String INCREMENTAL = Build.getString("ro.build.version.incremental");
        public static final int MIN_SUPPORTED_TARGET_SDK_INT = SystemProperties.getInt("ro.build.version.min_supported_target_sdk", 0);
        public static final int PREVIEW_SDK_INT = SystemProperties.getInt("ro.build.version.preview_sdk", 0);
        public static final String RELEASE = Build.getString("ro.build.version.release");
        public static final int RESOURCES_SDK_INT = (SDK_INT + ACTIVE_CODENAMES.length);
        @Deprecated
        public static final String SDK = Build.getString("ro.build.version.sdk");
        public static final int SDK_INT = SystemProperties.getInt("ro.build.version.sdk", 0);
        public static final String SECURITY_PATCH = SystemProperties.get("ro.build.version.security_patch", "");
    }

    public static class VERSION_CODES {
        public static final int BASE = 1;
        public static final int BASE_1_1 = 2;
        public static final int CUPCAKE = 3;
        public static final int CUR_DEVELOPMENT = 10000;
        public static final int DONUT = 4;
        public static final int ECLAIR = 5;
        public static final int ECLAIR_0_1 = 6;
        public static final int ECLAIR_MR1 = 7;
        public static final int FROYO = 8;
        public static final int GINGERBREAD = 9;
        public static final int GINGERBREAD_MR1 = 10;
        public static final int HONEYCOMB = 11;
        public static final int HONEYCOMB_MR1 = 12;
        public static final int HONEYCOMB_MR2 = 13;
        public static final int ICE_CREAM_SANDWICH = 14;
        public static final int ICE_CREAM_SANDWICH_MR1 = 15;
        public static final int JELLY_BEAN = 16;
        public static final int JELLY_BEAN_MR1 = 17;
        public static final int JELLY_BEAN_MR2 = 18;
        public static final int KITKAT = 19;
        public static final int KITKAT_WATCH = 20;
        public static final int L = 21;
        public static final int LOLLIPOP = 21;
        public static final int LOLLIPOP_MR1 = 22;
        public static final int M = 23;
        public static final int N = 24;
        public static final int N_MR1 = 25;
        public static final int O = 26;
        public static final int O_MR1 = 27;
        public static final int P = 28;
    }

    static {
        String[] abiList;
        boolean z = false;
        if (VMRuntime.getRuntime().is64Bit()) {
            abiList = SUPPORTED_64_BIT_ABIS;
        } else {
            abiList = SUPPORTED_32_BIT_ABIS;
        }
        CPU_ABI = abiList[0];
        if (abiList.length > 1) {
            CPU_ABI2 = abiList[1];
        } else {
            CPU_ABI2 = "";
        }
        if (SystemProperties.getInt("ro.permission_review_required", 0) == 1) {
            z = true;
        }
        PERMISSIONS_REVIEW_REQUIRED = z;
    }

    public static String getSerial() {
        try {
            return IDeviceIdentifiersPolicyService.Stub.asInterface(ServiceManager.getService("device_identifiers")).getSerial();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return "unknown";
        }
    }

    private static String deriveFingerprint() {
        String finger = getString("ro.build.fingerprint");
        if (!TextUtils.isEmpty(finger)) {
            return finger;
        }
        return getString("ro.product.brand") + '/' + getString("ro.product.name") + '/' + getString("ro.product.device") + ':' + getString("ro.build.version.release") + '/' + getString("ro.build.id") + '/' + getString("ro.build.version.incremental") + ':' + getString("ro.build.type") + '/' + getString("ro.build.tags");
    }

    public static void ensureFingerprintProperty() {
        if (TextUtils.isEmpty(SystemProperties.get("ro.build.fingerprint"))) {
            try {
                SystemProperties.set("ro.build.fingerprint", FINGERPRINT);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "Failed to set fingerprint property", e);
            }
        }
    }

    public static boolean isBuildConsistent() {
        if (IS_ENG || IS_TREBLE_ENABLED) {
            return true;
        }
        String system = SystemProperties.get("ro.build.fingerprint");
        String vendor = SystemProperties.get("ro.vendor.build.fingerprint");
        String str = SystemProperties.get("ro.bootimage.build.fingerprint");
        String str2 = SystemProperties.get("ro.build.expect.bootloader");
        String str3 = SystemProperties.get("ro.bootloader");
        String str4 = SystemProperties.get("ro.build.expect.baseband");
        String str5 = SystemProperties.get("gsm.version.baseband");
        if (TextUtils.isEmpty(system)) {
            Slog.e(TAG, "Required ro.build.fingerprint is empty!");
            return false;
        } else if (TextUtils.isEmpty(vendor) || Objects.equals(system, vendor)) {
            return true;
        } else {
            Slog.e(TAG, "Mismatched fingerprints; system reported " + system + " but vendor reported " + vendor);
            return false;
        }
    }

    public static String getRadioVersion() {
        return SystemProperties.get("gsm.version.baseband", null);
    }

    /* access modifiers changed from: private */
    public static String getString(String property) {
        String str = SystemProperties.get(property, "unknown");
        if (NO_HOTA && property.equals("ro.build.display.id")) {
            str = str + "_NoHota";
        }
        if (!HIDE_PRODUCT_INFO || (!property.equals("ro.build.id") && !property.equals("ro.build.host") && !property.equals("ro.build.user") && !property.equals("ro.product.manufacturer") && !property.equals("ro.hardware") && !property.equals("ro.build.display.id") && !property.equals("ro.build.fingerprint") && !property.equals("ro.product.board") && !property.equals("ro.product.device") && !property.equals("ro.product.model") && !property.equals("ro.product.name") && !property.equals("ro.product.brand") && !property.equals("ro.build.version.codename"))) {
            return str;
        }
        return hide_build_info(property, str);
    }

    /* access modifiers changed from: private */
    public static String[] getStringList(String property, String separator) {
        String value = SystemProperties.get(property);
        if (value.isEmpty()) {
            return new String[0];
        }
        return value.split(separator);
    }

    private static long getLong(String property) {
        try {
            return Long.parseLong(SystemProperties.get(property));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static final String replaceIgnoreCase(String src, String target, String replacement) {
        String lowersrc = src.toLowerCase();
        String lowertarget = target.toLowerCase();
        StringBuilder ret = new StringBuilder();
        int pos_begin = 0;
        int pos = lowersrc.indexOf(lowertarget);
        while (pos != -1) {
            ret.append(src, pos_begin, pos);
            ret.append(replacement);
            pos_begin = pos + target.length();
            pos = lowersrc.indexOf(lowertarget, pos_begin);
        }
        if (pos_begin < lowersrc.length()) {
            ret.append(src, pos_begin, src.length());
        }
        return ret.toString();
    }

    public static final String hide_build_info(String property, String str) {
        if (matchers.length != 0) {
            int i = 0;
            if (matchers[0].length() != 0) {
                String s = str;
                while (i < matchers.length) {
                    s = replaceIgnoreCase(s, matchers[i], i >= replacements.length ? "unknown" : replacements[i]);
                    i++;
                }
                return s;
            }
        }
        String tar = "unknown";
        Pattern pnHwverSP = Pattern.compile("C\\w{2}B\\d{3}SP\\d{2}");
        Pattern pnHwver = Pattern.compile("C\\w{2}B\\d{3}");
        Matcher mhwSP = pnHwverSP.matcher(str);
        Matcher mhw = pnHwver.matcher(str);
        String ver = null;
        if (mhwSP.find()) {
            ver = mhwSP.group();
        } else if (mhw.find()) {
            ver = mhw.group();
        }
        if (property.equals("ro.build.fingerprint")) {
            if (ver == null) {
                ver = "KRT16M";
            }
            tar = BRAND + "/" + PRODUCT + "/" + DEVICE + SettingsStringUtil.DELIMITER + VERSION.RELEASE + "/" + ID + "/" + ver + ":user/release-keys";
        } else if (property.equals("ro.build.user")) {
            tar = "android";
        } else if (property.equals("ro.build.host")) {
            tar = "localhost";
        } else if (property.equals("ro.build.version.codename")) {
            tar = "NOTREL";
        } else if (ver != null) {
            tar = ver;
        }
        return tar;
    }

    private static boolean isNeedHidden() {
        String mspesStr = SystemProperties.get("ro.mspes.config", "unknown");
        if (mspesStr != null && !mspesStr.equals("unknown")) {
            try {
                if ((2 & Long.decode(mspesStr.trim()).longValue()) == 0) {
                    return false;
                }
                return true;
            } catch (NumberFormatException e) {
                Slog.e(TAG, "mspes config decode number fail: " + mspesStr.trim());
            }
        }
        return SystemProperties.getBoolean("ro.build.hide", false);
    }
}
