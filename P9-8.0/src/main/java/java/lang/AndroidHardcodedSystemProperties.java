package java.lang;

public final class AndroidHardcodedSystemProperties {
    public static final String JAVA_VERSION = "0";
    static final String[][] STATIC_PROPERTIES;

    static {
        r0 = new String[32][];
        r0[0] = new String[]{"java.class.version", "50.0"};
        r0[1] = new String[]{"java.version", JAVA_VERSION};
        r0[2] = new String[]{"java.compiler", ""};
        r0[3] = new String[]{"java.ext.dirs", ""};
        r0[4] = new String[]{"java.specification.name", "Dalvik Core Library"};
        r0[5] = new String[]{"java.specification.vendor", "The Android Project"};
        r0[6] = new String[]{"java.specification.version", "0.9"};
        r0[7] = new String[]{"java.vendor", "The Android Project"};
        r0[8] = new String[]{"java.vendor.url", "http://www.android.com/"};
        r0[9] = new String[]{"java.vm.name", "Dalvik"};
        r0[10] = new String[]{"java.vm.specification.name", "Dalvik Virtual Machine Specification"};
        r0[11] = new String[]{"java.vm.specification.vendor", "The Android Project"};
        r0[12] = new String[]{"java.vm.specification.version", "0.9"};
        r0[13] = new String[]{"java.vm.vendor", "The Android Project"};
        r0[14] = new String[]{"java.vm.vendor.url", "http://www.android.com/"};
        r0[15] = new String[]{"java.net.preferIPv6Addresses", "false"};
        r0[16] = new String[]{"file.encoding", "UTF-8"};
        r0[17] = new String[]{"file.separator", "/"};
        r0[18] = new String[]{"line.separator", "\n"};
        r0[19] = new String[]{"path.separator", ":"};
        r0[20] = new String[]{"ICUDebug", null};
        r0[21] = new String[]{"android.icu.text.DecimalFormat.SkipExtendedSeparatorParsing", null};
        r0[22] = new String[]{"android.icu.text.MessagePattern.ApostropheMode", null};
        r0[23] = new String[]{"sun.io.useCanonCaches", null};
        r0[24] = new String[]{"sun.io.useCanonPrefixCache", null};
        r0[25] = new String[]{"http.keepAlive", null};
        r0[26] = new String[]{"http.keepAliveDuration", null};
        r0[27] = new String[]{"http.maxConnections", null};
        r0[28] = new String[]{"os.name", "Linux"};
        r0[29] = new String[]{"javax.net.debug", null};
        r0[30] = new String[]{"com.sun.security.preserveOldDCEncoding", null};
        r0[31] = new String[]{"java.util.logging.manager", null};
        STATIC_PROPERTIES = r0;
    }
}
