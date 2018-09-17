package android.icu.util;

import android.icu.impl.ICUData;
import android.icu.text.DateFormat;
import java.util.concurrent.ConcurrentHashMap;

public final class VersionInfo implements Comparable<VersionInfo> {
    @Deprecated
    public static final VersionInfo ICU_DATA_VERSION = getInstance(58, 2, 0, 0);
    @Deprecated
    public static final String ICU_DATA_VERSION_PATH = "58b";
    public static final VersionInfo ICU_VERSION = getInstance(58, 2, 0, 0);
    private static final String INVALID_VERSION_NUMBER_ = "Invalid version number: Version number may be negative or greater than 255";
    private static final int LAST_BYTE_MASK_ = 255;
    private static final ConcurrentHashMap<Integer, VersionInfo> MAP_ = new ConcurrentHashMap();
    private static volatile String TZDATA_VERSION = null;
    public static final VersionInfo UCOL_BUILDER_VERSION = getInstance(9);
    public static final VersionInfo UCOL_RUNTIME_VERSION = getInstance(9);
    @Deprecated
    public static final VersionInfo UCOL_TAILORINGS_VERSION = getInstance(1);
    public static final VersionInfo UNICODE_1_0 = getInstance(1, 0, 0, 0);
    public static final VersionInfo UNICODE_1_0_1 = getInstance(1, 0, 1, 0);
    public static final VersionInfo UNICODE_1_1_0 = getInstance(1, 1, 0, 0);
    public static final VersionInfo UNICODE_1_1_5 = getInstance(1, 1, 5, 0);
    public static final VersionInfo UNICODE_2_0 = getInstance(2, 0, 0, 0);
    public static final VersionInfo UNICODE_2_1_2 = getInstance(2, 1, 2, 0);
    public static final VersionInfo UNICODE_2_1_5 = getInstance(2, 1, 5, 0);
    public static final VersionInfo UNICODE_2_1_8 = getInstance(2, 1, 8, 0);
    public static final VersionInfo UNICODE_2_1_9 = getInstance(2, 1, 9, 0);
    public static final VersionInfo UNICODE_3_0 = getInstance(3, 0, 0, 0);
    public static final VersionInfo UNICODE_3_0_1 = getInstance(3, 0, 1, 0);
    public static final VersionInfo UNICODE_3_1_0 = getInstance(3, 1, 0, 0);
    public static final VersionInfo UNICODE_3_1_1 = getInstance(3, 1, 1, 0);
    public static final VersionInfo UNICODE_3_2 = getInstance(3, 2, 0, 0);
    public static final VersionInfo UNICODE_4_0 = getInstance(4, 0, 0, 0);
    public static final VersionInfo UNICODE_4_0_1 = getInstance(4, 0, 1, 0);
    public static final VersionInfo UNICODE_4_1 = getInstance(4, 1, 0, 0);
    public static final VersionInfo UNICODE_5_0 = getInstance(5, 0, 0, 0);
    public static final VersionInfo UNICODE_5_1 = getInstance(5, 1, 0, 0);
    public static final VersionInfo UNICODE_5_2 = getInstance(5, 2, 0, 0);
    public static final VersionInfo UNICODE_6_0 = getInstance(6, 0, 0, 0);
    public static final VersionInfo UNICODE_6_1 = getInstance(6, 1, 0, 0);
    public static final VersionInfo UNICODE_6_2 = getInstance(6, 2, 0, 0);
    public static final VersionInfo UNICODE_6_3 = getInstance(6, 3, 0, 0);
    public static final VersionInfo UNICODE_7_0 = getInstance(7, 0, 0, 0);
    public static final VersionInfo UNICODE_8_0 = getInstance(8, 0, 0, 0);
    public static final VersionInfo UNICODE_9_0 = getInstance(9, 0, 0, 0);
    private static final VersionInfo UNICODE_VERSION = UNICODE_9_0;
    private static volatile VersionInfo javaVersion;
    private int m_version_;

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
                if (c < 0 || c > 9) {
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
            if (array[i] < 0 || array[i] > 255) {
                throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
            }
            i++;
        }
        return getInstance(array[0], array[1], array[2], array[3]);
    }

    public static VersionInfo getInstance(int major, int minor, int milli, int micro) {
        if (major < 0 || major > 255 || minor < 0 || minor > 255 || milli < 0 || milli > 255 || micro < 0 || micro > 255) {
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
                    char[] chars = System.getProperty("java.version").toCharArray();
                    int r = 0;
                    int w = 0;
                    int count = 0;
                    boolean numeric = false;
                    while (true) {
                        int w2 = w;
                        int r2 = r;
                        if (r2 >= chars.length) {
                            w = w2;
                            r = r2;
                            break;
                        }
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
                    }
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
        return (this.m_version_ >> 24) & 255;
    }

    public int getMinor() {
        return (this.m_version_ >> 16) & 255;
    }

    public int getMilli() {
        return (this.m_version_ >> 8) & 255;
    }

    public int getMicro() {
        return this.m_version_ & 255;
    }

    public boolean equals(Object other) {
        return other == this;
    }

    public int hashCode() {
        return this.m_version_;
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
                icuApiVer = "" + major + "." + minor + DateFormat.NUM_MONTH + ICU_VERSION.getMilli();
            } else {
                icuApiVer = ICU_VERSION.getVersionString(2, 2);
            }
        } else if (ICU_VERSION.getMinor() == 0) {
            icuApiVer = "" + ICU_VERSION.getMajor() + DateFormat.NUM_MONTH + ICU_VERSION.getMilli();
        } else {
            icuApiVer = ICU_VERSION.getVersionString(2, 2);
        }
        System.out.println("International Components for Unicode for Java " + icuApiVer);
        System.out.println("");
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
                    TZDATA_VERSION = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "zoneinfo64").getString("TZVersion");
                }
            }
        }
        return TZDATA_VERSION;
    }
}
