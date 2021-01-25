package android.os;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.media.MediaDrm;
import android.media.midi.MidiDeviceInfo;
import android.net.ProxyInfo;
import android.os.IDeviceIdentifiersPolicyService;
import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.telephony.TelephonyProperties;
import dalvik.system.VMRuntime;
import java.util.ArrayList;
import java.util.List;
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
    private static final boolean EXPOSE_PRODUCT_HARDWARE_INFO = SystemProperties.getBoolean("ro.build.hardware_expose", false);
    public static final String FINGERPRINT = deriveFingerprint();
    public static final String FINGERPRINTEX = deriveFingerprintEx();
    public static final String HARDWARE = getString(HIDE_PRODUCT_INFO ? "ro.hardware.alter" : "ro.hardware");
    public static final boolean HIDE_PRODUCT_INFO = isNeedHidden();
    public static final String HOST = getString("ro.build.host");
    public static final String HWFINGERPRINT = (SystemProperties.get("ro.comp.real.hl.product_base_version", "") + "/" + SystemProperties.get("ro.comp.real.hl.product_cust_version", "") + "/" + SystemProperties.get("ro.comp.real.hl.product_preload_version", "") + "/" + SystemProperties.get("ro.comp.hl.product_base_version", "") + "/" + SystemProperties.get("ro.comp.hl.product_cust_version", "") + "/" + SystemProperties.get("ro.comp.hl.product_preload_version", "") + "/" + SystemProperties.get("ro.product.CotaVersion", "") + "/" + SystemProperties.get("ro.patchversion", ""));
    public static final String ID = getString("ro.build.id");
    public static final boolean IS_CONTAINER = SystemProperties.getBoolean("ro.boot.container", false);
    @UnsupportedAppUsage
    public static final boolean IS_DEBUGGABLE;
    public static final boolean IS_EMULATOR = getString("ro.kernel.qemu").equals("1");
    public static final boolean IS_ENG = "eng".equals(TYPE);
    public static final boolean IS_TREBLE_ENABLED = SystemProperties.getBoolean("ro.treble.enabled", false);
    public static final boolean IS_USER = "user".equals(TYPE);
    public static final boolean IS_USERDEBUG = "userdebug".equals(TYPE);
    public static final String MANUFACTURER = getString("ro.product.manufacturer");
    public static final String MODEL = getString("ro.product.model");
    public static final boolean NO_HOTA = SystemProperties.getBoolean("ro.build.nohota", false);
    @SystemApi
    public static final boolean PERMISSIONS_REVIEW_REQUIRED = true;
    public static final String PRODUCT = getString("ro.product.name");
    @Deprecated
    public static final String RADIO = getString(TelephonyProperties.PROPERTY_BASEBAND_VERSION);
    @Deprecated
    public static final String SERIAL = getString("no.such.thing");
    public static final String[] SUPPORTED_32_BIT_ABIS = getStringList("ro.product.cpu.abilist32", SmsManager.REGEX_PREFIX_DELIMITER);
    public static final String[] SUPPORTED_64_BIT_ABIS = getStringList("ro.product.cpu.abilist64", SmsManager.REGEX_PREFIX_DELIMITER);
    public static final String[] SUPPORTED_ABIS = getStringList("ro.product.cpu.abilist", SmsManager.REGEX_PREFIX_DELIMITER);
    private static final String TAG = "Build";
    public static final String TAGS = getString("ro.build.tags");
    public static final long TIME = (getLong("ro.build.date.utc") * 1000);
    public static final String TYPE = getString("ro.build.type");
    public static final String UNKNOWN = "unknown";
    public static final String USER = getString("ro.build.user");
    static final String[] matchers = SystemProperties.get("ro.build.hide.matchers", "").trim().split(";");
    static final String[] replacements = SystemProperties.get("ro.build.hide.replacements", "").trim().split(";");

    public static class VERSION {
        @UnsupportedAppUsage
        public static final String[] ACTIVE_CODENAMES = ("REL".equals(ALL_CODENAMES[0]) ? new String[0] : ALL_CODENAMES);
        private static final String[] ALL_CODENAMES = Build.getStringList("ro.build.version.all_codenames", SmsManager.REGEX_PREFIX_DELIMITER);
        public static final String BASE_OS = SystemProperties.get("ro.build.version.base_os", "");
        public static final String CODENAME = Build.getString("ro.build.version.codename");
        public static final int FIRST_SDK_INT = SystemProperties.getInt("ro.product.first_api_level", 28);
        public static final String INCREMENTAL = Build.getString("ro.build.version.incremental");
        public static final int MIN_SUPPORTED_TARGET_SDK_INT = SystemProperties.getInt("ro.build.version.min_supported_target_sdk", 0);
        @SystemApi
        public static final String PREVIEW_SDK_FINGERPRINT = SystemProperties.get("ro.build.version.preview_sdk_fingerprint", "REL");
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
        public static final int Q = 29;
    }

    static {
        String[] abiList;
        if (VMRuntime.getRuntime().is64Bit()) {
            abiList = SUPPORTED_64_BIT_ABIS;
        } else {
            abiList = SUPPORTED_32_BIT_ABIS;
        }
        CPU_ABI = abiList[0];
        boolean z = true;
        if (abiList.length > 1) {
            CPU_ABI2 = abiList[1];
        } else {
            CPU_ABI2 = "";
        }
        if (SystemProperties.getInt("ro.debuggable", 0) != 1) {
            z = false;
        }
        IS_DEBUGGABLE = z;
    }

    public static String getSerial() {
        IDeviceIdentifiersPolicyService service = IDeviceIdentifiersPolicyService.Stub.asInterface(ServiceManager.getService(Context.DEVICE_IDENTIFIERS_SERVICE));
        try {
            Application application = ActivityThread.currentApplication();
            return service.getSerialForPackage(application != null ? application.getPackageName() : null);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return "unknown";
        }
    }

    public static boolean is64BitAbi(String abi) {
        return VMRuntime.is64BitAbi(abi);
    }

    private static String deriveFingerprint() {
        String finger = getString("ro.build.fingerprint");
        if (!TextUtils.isEmpty(finger)) {
            return finger;
        }
        return getString("ro.product.brand") + '/' + getString("ro.product.name") + '/' + getString("ro.product.device") + ':' + getString("ro.build.version.release") + '/' + getString("ro.build.id") + '/' + getString("ro.build.version.incremental") + ':' + getString("ro.build.type") + '/' + getString("ro.build.tags");
    }

    private static String deriveFingerprintEx() {
        String finger = getString("ro.huawei.build.fingerprint");
        if (!TextUtils.isEmpty(finger)) {
            return finger;
        }
        return getString("ro.product.brand") + '/' + getString("ro.product.name") + '/' + getString("ro.product.device") + ':' + getString("ro.build.version.release") + '/' + getString("ro.build.id") + '/' + getString("ro.huawei.build.version.incremental") + ':' + getString("ro.build.type") + '/' + getString("ro.build.tags");
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
        String vendor2 = SystemProperties.get("ro.vendor.build.fingerprint");
        SystemProperties.get("ro.bootimage.build.fingerprint");
        SystemProperties.get("ro.build.expect.bootloader");
        SystemProperties.get("ro.bootloader");
        SystemProperties.get("ro.build.expect.baseband");
        SystemProperties.get(TelephonyProperties.PROPERTY_BASEBAND_VERSION);
        if (TextUtils.isEmpty(system)) {
            Slog.e(TAG, "Required ro.build.fingerprint is empty!");
            return false;
        } else if (TextUtils.isEmpty(vendor2) || Objects.equals(system, vendor2)) {
            return true;
        } else {
            Slog.e(TAG, "Mismatched fingerprints; system reported " + system + " but vendor reported " + vendor2);
            return false;
        }
    }

    public static class Partition {
        public static final String PARTITION_NAME_SYSTEM = "system";
        private final String mFingerprint;
        private final String mName;
        private final long mTimeMs;

        private Partition(String name, String fingerprint, long timeMs) {
            this.mName = name;
            this.mFingerprint = fingerprint;
            this.mTimeMs = timeMs;
        }

        public String getName() {
            return this.mName;
        }

        public String getFingerprint() {
            return this.mFingerprint;
        }

        public long getBuildTimeMillis() {
            return this.mTimeMs;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Partition)) {
                return false;
            }
            Partition op = (Partition) o;
            if (!this.mName.equals(op.mName) || !this.mFingerprint.equals(op.mFingerprint) || this.mTimeMs != op.mTimeMs) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mName, this.mFingerprint, Long.valueOf(this.mTimeMs));
        }
    }

    public static List<Partition> getFingerprintedPartitions() {
        ArrayList<Partition> partitions = new ArrayList<>();
        String[] names = {"bootimage", "odm", MidiDeviceInfo.PROPERTY_PRODUCT, "product_services", "system", MediaDrm.PROPERTY_VENDOR};
        for (String name : names) {
            String fingerprint = SystemProperties.get("ro." + name + ".build.fingerprint");
            if (!TextUtils.isEmpty(fingerprint)) {
                partitions.add(new Partition(name, fingerprint, getLong("ro." + name + ".build.date.utc") * 1000));
            }
        }
        return partitions;
    }

    public static String getRadioVersion() {
        String propVal = SystemProperties.get(TelephonyProperties.PROPERTY_BASEBAND_VERSION);
        if (TextUtils.isEmpty(propVal)) {
            return null;
        }
        return propVal;
    }

    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public static String getString(String property) {
        String str = SystemProperties.get(property, "unknown");
        if (NO_HOTA && property.equals("ro.build.display.id")) {
            str = str + "_NoHota";
        }
        if ((!HIDE_PRODUCT_INFO || !EXPOSE_PRODUCT_HARDWARE_INFO || !property.equals("ro.product.model")) && HIDE_PRODUCT_INFO && (property.equals("ro.build.id") || property.equals("ro.build.host") || property.equals("ro.build.user") || property.equals("ro.product.manufacturer") || property.equals("ro.hardware") || property.equals("ro.build.display.id") || property.equals("ro.build.fingerprint") || property.equals("ro.huawei.build.fingerprint") || property.equals("ro.product.board") || property.equals("ro.product.device") || property.equals("ro.product.model") || property.equals("ro.product.name") || property.equals("ro.product.brand") || property.equals("ro.build.version.codename"))) {
            return hide_build_info(property, str);
        }
        return str;
    }

    /* access modifiers changed from: private */
    public static String[] getStringList(String property, String separator) {
        String value = SystemProperties.get(property);
        if (value.isEmpty()) {
            return new String[0];
        }
        return value.split(separator);
    }

    @UnsupportedAppUsage
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
            ret.append((CharSequence) src, pos_begin, pos);
            ret.append(replacement);
            pos_begin = pos + target.length();
            pos = lowersrc.indexOf(lowertarget, pos_begin);
        }
        if (pos_begin < lowersrc.length()) {
            ret.append((CharSequence) src, pos_begin, src.length());
        }
        return ret.toString();
    }

    public static final String hide_build_info(String property, String str) {
        String[] strArr = matchers;
        if (strArr.length == 0 || strArr[0].length() == 0) {
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
            if (property.equals("ro.huawei.build.fingerprint")) {
                if (ver == null) {
                    ver = "KRT16M";
                }
                tar = BRAND + "/" + PRODUCT + "/" + DEVICE + SettingsStringUtil.DELIMITER + VERSION.RELEASE + "/" + ID + "/" + ver + ":user/release-keys";
            }
            if (property.equals("ro.build.fingerprint")) {
                if (ver == null) {
                    ver = "KRT16M";
                }
                return BRAND + "/" + PRODUCT + "/" + DEVICE + SettingsStringUtil.DELIMITER + VERSION.RELEASE + "/" + ID + "/" + ver + ":user/release-keys";
            } else if (property.equals("ro.build.user")) {
                return "android";
            } else {
                if (property.equals("ro.build.host")) {
                    return ProxyInfo.LOCAL_HOST;
                }
                if (property.equals("ro.build.version.codename")) {
                    return "NOTREL";
                }
                return ver != null ? ver : tar;
            }
        } else {
            String s = str;
            int i = 0;
            while (true) {
                String[] strArr2 = matchers;
                if (i >= strArr2.length) {
                    return s;
                }
                String match = strArr2[i];
                String[] strArr3 = replacements;
                s = replaceIgnoreCase(s, match, i >= strArr3.length ? "unknown" : strArr3[i]);
                i++;
            }
        }
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
