package android.os;

import android.net.ProxyInfo;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.Slog;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Build {
    public static final String BOARD = null;
    public static final String BOOTLOADER = null;
    public static final String BRAND = null;
    @Deprecated
    public static final String CPU_ABI = null;
    @Deprecated
    public static final String CPU_ABI2 = null;
    public static final String DEVICE = null;
    public static final String DISPLAY = null;
    public static final String FINGERPRINT = null;
    public static final String HARDWARE = null;
    public static final boolean HIDE_PRODUCT_INFO = false;
    public static final String HOST = null;
    public static final String ID = null;
    public static final boolean IS_DEBUGGABLE = false;
    public static final boolean IS_EMULATOR = false;
    public static final String MANUFACTURER = null;
    public static final String MODEL = null;
    public static final boolean NO_HOTA = false;
    public static final boolean PERMISSIONS_REVIEW_REQUIRED = false;
    public static final String PRODUCT = null;
    @Deprecated
    public static final String RADIO = null;
    public static final String SERIAL = null;
    public static final String[] SUPPORTED_32_BIT_ABIS = null;
    public static final String[] SUPPORTED_64_BIT_ABIS = null;
    public static final String[] SUPPORTED_ABIS = null;
    private static final String TAG = "Build";
    public static final String TAGS = null;
    public static final long TIME = 0;
    public static final String TYPE = null;
    public static final String UNKNOWN = "unknown";
    public static final String USER = null;
    static final String[] matchers = null;
    static final String[] replacements = null;

    public static class VERSION {
        public static final String[] ACTIVE_CODENAMES = null;
        private static final String[] ALL_CODENAMES = null;
        public static final String BASE_OS = null;
        public static final String CODENAME = null;
        public static final String INCREMENTAL = null;
        public static final int PREVIEW_SDK_INT = 0;
        public static final String RELEASE = null;
        public static final int RESOURCES_SDK_INT = 0;
        @Deprecated
        public static final String SDK = null;
        public static final int SDK_INT = 0;
        public static final String SECURITY_PATCH = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Build.VERSION.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Build.VERSION.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.Build.VERSION.<clinit>():void");
        }
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

        public VERSION_CODES() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Build.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Build.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.Build.<clinit>():void");
    }

    public Build() {
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
        if ("eng".equals(TYPE)) {
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
            return PERMISSIONS_REVIEW_REQUIRED;
        } else if (TextUtils.isEmpty(vendor) || Objects.equals(system, vendor)) {
            return true;
        } else {
            Slog.e(TAG, "Mismatched fingerprints; system reported " + system + " but vendor reported " + vendor);
            return PERMISSIONS_REVIEW_REQUIRED;
        }
    }

    public static String getRadioVersion() {
        return SystemProperties.get("gsm.version.baseband", null);
    }

    private static String getString(String property) {
        String str = SystemProperties.get(property, UNKNOWN);
        if (NO_HOTA && property.equals("ro.build.display.id")) {
            str = str + "_NoHota";
        }
        if (HIDE_PRODUCT_INFO && (property.equals("ro.build.id") || property.equals("ro.build.host") || property.equals("ro.build.user") || property.equals("ro.product.manufacturer") || property.equals("ro.hardware") || property.equals("ro.build.display.id") || property.equals("ro.build.fingerprint") || property.equals("ro.product.board") || property.equals("ro.product.device") || property.equals("ro.product.model") || property.equals("ro.product.name") || property.equals("ro.product.brand"))) {
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
            String tar = UNKNOWN;
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
                tar = ZenModeConfig.SYSTEM_AUTHORITY;
            } else if (property.equals("ro.build.host")) {
                tar = ProxyInfo.LOCAL_HOST;
            } else if (ver != null) {
                tar = ver;
            }
            return tar;
        }
        String s = str;
        int i = 0;
        while (i < matchers.length) {
            s = s.replaceAll("(?i)" + matchers[i], i >= replacements.length ? UNKNOWN : replacements[i]);
            i++;
        }
        return s;
    }
}
