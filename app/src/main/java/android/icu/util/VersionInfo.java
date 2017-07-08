package android.icu.util;

import android.icu.impl.ICUResourceBundle;
import android.icu.text.DateFormat;
import java.util.concurrent.ConcurrentHashMap;
import org.xmlpull.v1.XmlPullParser;

public final class VersionInfo implements Comparable<VersionInfo> {
    @Deprecated
    public static final VersionInfo ICU_DATA_VERSION = null;
    @Deprecated
    public static final String ICU_DATA_VERSION_PATH = "56b";
    public static final VersionInfo ICU_VERSION = null;
    private static final String INVALID_VERSION_NUMBER_ = "Invalid version number: Version number may be negative or greater than 255";
    private static final int LAST_BYTE_MASK_ = 255;
    private static final ConcurrentHashMap<Integer, VersionInfo> MAP_ = null;
    private static volatile String TZDATA_VERSION;
    public static final VersionInfo UCOL_BUILDER_VERSION = null;
    public static final VersionInfo UCOL_RUNTIME_VERSION = null;
    @Deprecated
    public static final VersionInfo UCOL_TAILORINGS_VERSION = null;
    public static final VersionInfo UNICODE_1_0 = null;
    public static final VersionInfo UNICODE_1_0_1 = null;
    public static final VersionInfo UNICODE_1_1_0 = null;
    public static final VersionInfo UNICODE_1_1_5 = null;
    public static final VersionInfo UNICODE_2_0 = null;
    public static final VersionInfo UNICODE_2_1_2 = null;
    public static final VersionInfo UNICODE_2_1_5 = null;
    public static final VersionInfo UNICODE_2_1_8 = null;
    public static final VersionInfo UNICODE_2_1_9 = null;
    public static final VersionInfo UNICODE_3_0 = null;
    public static final VersionInfo UNICODE_3_0_1 = null;
    public static final VersionInfo UNICODE_3_1_0 = null;
    public static final VersionInfo UNICODE_3_1_1 = null;
    public static final VersionInfo UNICODE_3_2 = null;
    public static final VersionInfo UNICODE_4_0 = null;
    public static final VersionInfo UNICODE_4_0_1 = null;
    public static final VersionInfo UNICODE_4_1 = null;
    public static final VersionInfo UNICODE_5_0 = null;
    public static final VersionInfo UNICODE_5_1 = null;
    public static final VersionInfo UNICODE_5_2 = null;
    public static final VersionInfo UNICODE_6_0 = null;
    public static final VersionInfo UNICODE_6_1 = null;
    public static final VersionInfo UNICODE_6_2 = null;
    public static final VersionInfo UNICODE_6_3 = null;
    public static final VersionInfo UNICODE_7_0 = null;
    public static final VersionInfo UNICODE_8_0 = null;
    private static final VersionInfo UNICODE_VERSION = null;
    private static volatile VersionInfo javaVersion;
    private int m_version_;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.VersionInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.VersionInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.VersionInfo.<clinit>():void");
    }

    public static VersionInfo getInstance(String version) {
        int length = version.length();
        int[] array = new int[]{0, 0, 0, 0};
        int count = 0;
        int index = 0;
        while (count < 4 && index < length) {
            char c = version.charAt(index);
            if (c == '.') {
                count++;
            } else {
                c = (char) (c - 48);
                if (c < '\u0000' || c > '\t') {
                    throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
                }
                array[count] = array[count] * 10;
                array[count] = array[count] + c;
            }
            index++;
        }
        if (index != length) {
            throw new IllegalArgumentException("Invalid version number: String '" + version + "' exceeds version format");
        }
        int i = 0;
        while (i < 4) {
            if (array[i] < 0 || array[i] > LAST_BYTE_MASK_) {
                throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
            }
            i++;
        }
        return getInstance(array[0], array[1], array[2], array[3]);
    }

    public static VersionInfo getInstance(int major, int minor, int milli, int micro) {
        if (major < 0 || major > LAST_BYTE_MASK_ || minor < 0 || minor > LAST_BYTE_MASK_ || milli < 0 || milli > LAST_BYTE_MASK_ || micro < 0 || micro > LAST_BYTE_MASK_) {
            throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
        }
        int version = getInt(major, minor, milli, micro);
        Integer key = Integer.valueOf(version);
        VersionInfo result = (VersionInfo) MAP_.get(key);
        if (result != null) {
            return result;
        }
        result = new VersionInfo(version);
        VersionInfo tmpvi = (VersionInfo) MAP_.putIfAbsent(key, result);
        if (tmpvi != null) {
            return tmpvi;
        }
        return result;
    }

    public static VersionInfo getInstance(int major, int minor, int milli) {
        return getInstance(major, minor, milli, 0);
    }

    public static VersionInfo getInstance(int major, int minor) {
        return getInstance(major, minor, 0, 0);
    }

    public static VersionInfo getInstance(int major) {
        return getInstance(major, 0, 0, 0);
    }

    @Deprecated
    public static VersionInfo javaVersion() {
        if (javaVersion == null) {
            synchronized (VersionInfo.class) {
                if (javaVersion == null) {
                    int r;
                    int w;
                    char[] chars = System.getProperty("java.version").toCharArray();
                    int count = 0;
                    boolean numeric = false;
                    int w2 = 0;
                    int r2 = 0;
                    while (r2 < chars.length) {
                        r = r2 + 1;
                        char c = chars[r2];
                        if (c >= '0' && c <= '9') {
                            numeric = true;
                            w = w2 + 1;
                            chars[w2] = c;
                        } else if (!numeric) {
                            w = w2;
                        } else if (count == 3) {
                            w = w2;
                            break;
                        } else {
                            numeric = false;
                            w = w2 + 1;
                            chars[w2] = '.';
                            count++;
                        }
                        w2 = w;
                        r2 = r;
                    }
                    w = w2;
                    r = r2;
                    while (w > 0 && chars[w - 1] == '.') {
                        w--;
                    }
                    javaVersion = getInstance(new String(chars, 0, w));
                }
            }
        }
        return javaVersion;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(7);
        result.append(getMajor());
        result.append('.');
        result.append(getMinor());
        result.append('.');
        result.append(getMilli());
        result.append('.');
        result.append(getMicro());
        return result.toString();
    }

    public int getMajor() {
        return (this.m_version_ >> 24) & LAST_BYTE_MASK_;
    }

    public int getMinor() {
        return (this.m_version_ >> 16) & LAST_BYTE_MASK_;
    }

    public int getMilli() {
        return (this.m_version_ >> 8) & LAST_BYTE_MASK_;
    }

    public int getMicro() {
        return this.m_version_ & LAST_BYTE_MASK_;
    }

    public boolean equals(Object other) {
        return other == this;
    }

    public int compareTo(VersionInfo other) {
        return this.m_version_ - other.m_version_;
    }

    private VersionInfo(int compactversion) {
        this.m_version_ = compactversion;
    }

    private static int getInt(int major, int minor, int milli, int micro) {
        return (((major << 24) | (minor << 16)) | (milli << 8)) | micro;
    }

    public static void main(String[] args) {
        String icuApiVer;
        if (ICU_VERSION.getMajor() <= 4) {
            if (ICU_VERSION.getMinor() % 2 != 0) {
                int major = ICU_VERSION.getMajor();
                int minor = ICU_VERSION.getMinor() + 1;
                if (minor >= 10) {
                    minor -= 10;
                    major++;
                }
                icuApiVer = XmlPullParser.NO_NAMESPACE + major + "." + minor + DateFormat.NUM_MONTH + ICU_VERSION.getMilli();
            } else {
                icuApiVer = ICU_VERSION.getVersionString(2, 2);
            }
        } else if (ICU_VERSION.getMinor() == 0) {
            icuApiVer = XmlPullParser.NO_NAMESPACE + ICU_VERSION.getMajor() + DateFormat.NUM_MONTH + ICU_VERSION.getMilli();
        } else {
            icuApiVer = ICU_VERSION.getVersionString(2, 2);
        }
        System.out.println("International Components for Unicode for Java " + icuApiVer);
        System.out.println(XmlPullParser.NO_NAMESPACE);
        System.out.println("Implementation Version: " + ICU_VERSION.getVersionString(2, 4));
        System.out.println("Unicode Data Version:   " + UNICODE_VERSION.getVersionString(2, 4));
        System.out.println("CLDR Data Version:      " + LocaleData.getCLDRVersion().getVersionString(2, 4));
        System.out.println("Time Zone Data Version: " + getTZDataVersion());
    }

    @Deprecated
    public String getVersionString(int minDigits, int maxDigits) {
        if (minDigits < 1 || maxDigits < 1 || minDigits > 4 || maxDigits > 4 || minDigits > maxDigits) {
            throw new IllegalArgumentException("Invalid min/maxDigits range");
        }
        int[] digits = new int[]{getMajor(), getMinor(), getMilli(), getMicro()};
        int numDigits = maxDigits;
        while (numDigits > minDigits && digits[numDigits - 1] == 0) {
            numDigits--;
        }
        StringBuilder verStr = new StringBuilder(7);
        verStr.append(digits[0]);
        for (int i = 1; i < numDigits; i++) {
            verStr.append(".");
            verStr.append(digits[i]);
        }
        return verStr.toString();
    }

    static String getTZDataVersion() {
        if (TZDATA_VERSION == null) {
            synchronized (VersionInfo.class) {
                if (TZDATA_VERSION == null) {
                    TZDATA_VERSION = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "zoneinfo64").getString("TZVersion");
                }
            }
        }
        return TZDATA_VERSION;
    }
}
