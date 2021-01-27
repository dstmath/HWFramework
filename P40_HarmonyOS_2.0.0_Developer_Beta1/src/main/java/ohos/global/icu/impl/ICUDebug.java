package ohos.global.icu.impl;

import java.io.PrintStream;
import ohos.global.icu.util.VersionInfo;

public final class ICUDebug {
    private static boolean debug = (params != null);
    private static boolean help = (debug && (params.equals("") || params.indexOf("help") != -1));
    public static final boolean isJDK14OrHigher;
    public static final VersionInfo javaVersion = getInstanceLenient(javaVersionString);
    public static final String javaVersionString = System.getProperty("java.version", "0");
    private static String params;

    static {
        try {
            params = System.getProperty("ICUDebug");
        } catch (SecurityException unused) {
        }
        boolean z = true;
        if (debug) {
            System.out.println("\nICUDebug=" + params);
        }
        if (javaVersion.compareTo(VersionInfo.getInstance("1.4.0")) < 0) {
            z = false;
        }
        isJDK14OrHigher = z;
    }

    public static VersionInfo getInstanceLenient(String str) {
        int[] iArr = new int[4];
        int i = 0;
        boolean z = false;
        int i2 = 0;
        while (true) {
            if (i >= str.length()) {
                break;
            }
            int i3 = i + 1;
            char charAt = str.charAt(i);
            if (charAt < '0' || charAt > '9') {
                if (!z) {
                    continue;
                } else if (i2 == 3) {
                    break;
                } else {
                    i2++;
                    z = false;
                }
            } else if (z) {
                iArr[i2] = (iArr[i2] * 10) + (charAt - '0');
                if (iArr[i2] > 255) {
                    iArr[i2] = 0;
                    break;
                }
            } else {
                iArr[i2] = charAt - '0';
                z = true;
            }
            i = i3;
        }
        return VersionInfo.getInstance(iArr[0], iArr[1], iArr[2], iArr[3]);
    }

    public static boolean enabled() {
        return debug;
    }

    public static boolean enabled(String str) {
        boolean z = false;
        if (debug) {
            if (params.indexOf(str) != -1) {
                z = true;
            }
            if (help) {
                PrintStream printStream = System.out;
                printStream.println("\nICUDebug.enabled(" + str + ") = " + z);
            }
        }
        return z;
    }

    public static String value(String str) {
        String str2;
        String str3 = "false";
        if (debug) {
            int indexOf = params.indexOf(str);
            if (indexOf != -1) {
                int length = indexOf + str.length();
                if (params.length() <= length || params.charAt(length) != '=') {
                    str2 = "true";
                } else {
                    int i = length + 1;
                    int indexOf2 = params.indexOf(",", i);
                    String str4 = params;
                    if (indexOf2 == -1) {
                        indexOf2 = str4.length();
                    }
                    str2 = str4.substring(i, indexOf2);
                }
                str3 = str2;
            }
            if (help) {
                PrintStream printStream = System.out;
                printStream.println("\nICUDebug.value(" + str + ") = " + str3);
            }
        }
        return str3;
    }
}
