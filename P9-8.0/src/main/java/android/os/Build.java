package android.os;

import android.content.Context;
import android.net.ProxyInfo;
import android.os.IDeviceIdentifiersPolicyService.Stub;
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
    public static final String HARDWARE;
    public static final boolean HIDE_PRODUCT_INFO = SystemProperties.getBoolean("ro.build.hide", false);
    public static final String HOST = getString("ro.build.host");
    public static final String ID = getString("ro.build.id");
    public static final boolean IS_CONTAINER = SystemProperties.getBoolean("ro.boot.container", false);
    public static final boolean IS_DEBUGGABLE = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    public static final boolean IS_EMULATOR = getString("ro.kernel.qemu").equals("1");
    public static final boolean IS_ENG = "eng".equals(TYPE);
    public static final boolean IS_TREBLE_ENABLED = SystemProperties.getBoolean("ro.treble.enabled", false);
    public static final boolean IS_USER = Context.USER_SERVICE.equals(TYPE);
    public static final boolean IS_USERDEBUG = "userdebug".equals(TYPE);
    public static final String MANUFACTURER = getString("ro.product.manufacturer");
    public static final String MODEL = getString("ro.product.model");
    public static final boolean NO_HOTA = SystemProperties.getBoolean("ro.build.nohota", false);
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
    static final String[] matchers = SystemProperties.get("ro.build.hide.matchers", ProxyInfo.LOCAL_EXCL_LIST).trim().split(";");
    static final String[] replacements = SystemProperties.get("ro.build.hide.replacements", ProxyInfo.LOCAL_EXCL_LIST).trim().split(";");

    public static class VERSION {
        public static final String[] ACTIVE_CODENAMES = ("REL".equals(ALL_CODENAMES[0]) ? new String[0] : ALL_CODENAMES);
        private static final String[] ALL_CODENAMES = Build.getStringList("ro.build.version.all_codenames", ",");
        public static final String BASE_OS = SystemProperties.get("ro.build.version.base_os", ProxyInfo.LOCAL_EXCL_LIST);
        public static final String CODENAME = Build.getString("ro.build.version.codename");
        public static final String INCREMENTAL = Build.getString("ro.build.version.incremental");
        public static final int PREVIEW_SDK_INT = SystemProperties.getInt("ro.build.version.preview_sdk", 0);
        public static final String RELEASE = Build.getString("ro.build.version.release");
        public static final int RESOURCES_SDK_INT = (SDK_INT + ACTIVE_CODENAMES.length);
        @Deprecated
        public static final String SDK = Build.getString("ro.build.version.sdk");
        public static final int SDK_INT = SystemProperties.getInt("ro.build.version.sdk", 0);
        public static final String SECURITY_PATCH = SystemProperties.get("ro.build.version.security_patch", ProxyInfo.LOCAL_EXCL_LIST);
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
    }

    static {
        String string;
        String[] abiList;
        boolean z = true;
        if (HIDE_PRODUCT_INFO) {
            string = getString("ro.hardware.alter");
        } else {
            string = getString("ro.hardware");
        }
        HARDWARE = string;
        if (VMRuntime.getRuntime().is64Bit()) {
            abiList = SUPPORTED_64_BIT_ABIS;
        } else {
            abiList = SUPPORTED_32_BIT_ABIS;
        }
        CPU_ABI = abiList[0];
        if (abiList.length > 1) {
            CPU_ABI2 = abiList[1];
        } else {
            CPU_ABI2 = ProxyInfo.LOCAL_EXCL_LIST;
        }
        if (SystemProperties.getInt("ro.permission_review_required", 0) != 1) {
            z = false;
        }
        PERMISSIONS_REVIEW_REQUIRED = z;
    }

    public static String getSerial() {
        try {
            return Stub.asInterface(ServiceManager.getService(Context.DEVICE_IDENTIFIERS_SERVICE)).getSerial();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return "unknown";
        }
    }

    private static String deriveFingerprint() {
        String finger = getString("ro.build.fingerprint");
        if (TextUtils.isEmpty(finger)) {
            return getString("ro.product.brand") + '/' + getString("ro.product.name") + '/' + getString("ro.product.device") + ':' + getString("ro.build.version.release") + '/' + getString("ro.build.id") + '/' + getString("ro.build.version.incremental") + ':' + getString("ro.build.type") + '/' + getString("ro.build.tags");
        }
        return finger;
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
        String bootimage = SystemProperties.get("ro.bootimage.build.fingerprint");
        String requiredBootloader = SystemProperties.get("ro.build.expect.bootloader");
        String currentBootloader = SystemProperties.get("ro.bootloader");
        String requiredRadio = SystemProperties.get("ro.build.expect.baseband");
        String currentRadio = SystemProperties.get("gsm.version.baseband");
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

    private static String getString(String property) {
        String str = SystemProperties.get(property, "unknown");
        if (NO_HOTA && property.equals("ro.build.display.id")) {
            str = str + "_NoHota";
        }
        if (HIDE_PRODUCT_INFO && (property.equals("ro.build.id") || property.equals("ro.build.host") || property.equals("ro.build.user") || property.equals("ro.product.manufacturer") || property.equals("ro.hardware") || property.equals("ro.build.display.id") || property.equals("ro.build.fingerprint") || property.equals("ro.product.board") || property.equals("ro.product.device") || property.equals("ro.product.model") || property.equals("ro.product.name") || property.equals("ro.product.brand") || property.equals("ro.build.version.codename"))) {
            return hide_build_info(property, str);
        }
        return str;
    }

    private static String[] getStringList(String property, String separator) {
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

    public static final String hide_build_info(String property, String str) {
        if (matchers.length == 0 || matchers[0].length() == 0) {
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
                tar = BRAND + "/" + PRODUCT + "/" + DEVICE + ":" + VERSION.RELEASE + "/" + ID + "/" + ver + ":user/release-keys";
            } else if (property.equals("ro.build.user")) {
                tar = "android";
            } else if (property.equals("ro.build.host")) {
                tar = ProxyInfo.LOCAL_HOST;
            } else if (property.equals("ro.build.version.codename")) {
                tar = "NOTREL";
            } else if (ver != null) {
                tar = ver;
            }
            return tar;
        }
        String s = str;
        int i = 0;
        while (i < matchers.length) {
            s = s.replaceAll("(?i)" + matchers[i], i >= replacements.length ? "unknown" : replacements[i]);
            i++;
        }
        return s;
    }
}
