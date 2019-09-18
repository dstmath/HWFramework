package android.icu.impl;

import android.icu.util.VersionInfo;
import java.io.PrintStream;

public final class ICUDebug {
    private static boolean debug = (params != null);
    private static boolean help = (debug && (params.equals("") || params.indexOf("help") != -1));
    public static final boolean isJDK14OrHigher;
    public static final VersionInfo javaVersion = getInstanceLenient(javaVersionString);
    public static final String javaVersionString = System.getProperty("java.version", AndroidHardcodedSystemProperties.JAVA_VERSION);
    private static String params;

    static {
        try {
            params = System.getProperty("ICUDebug");
        } catch (SecurityException e) {
        }
        boolean z = false;
        if (debug) {
            System.out.println("\nICUDebug=" + params);
        }
        if (javaVersion.compareTo(VersionInfo.getInstance("1.4.0")) >= 0) {
            z = true;
        }
        isJDK14OrHigher = z;
    }

    public static VersionInfo getInstanceLenient(String s) {
        int i;
        int[] ver = new int[4];
        int i2 = 0;
        boolean numeric = false;
        int vidx = 0;
        while (true) {
            if (i2 >= s.length()) {
                break;
            }
            i = i2 + 1;
            int i3 = s.charAt(i2);
            if (i3 < 48 || i3 > 57) {
                if (!numeric) {
                    continue;
                } else if (vidx == 3) {
                    break;
                } else {
                    numeric = false;
                    vidx++;
                }
                i2 = i;
            } else {
                if (numeric) {
                    ver[vidx] = (ver[vidx] * 10) + (i3 - 48);
                    if (ver[vidx] > 255) {
                        ver[vidx] = 0;
                        break;
                    }
                } else {
                    numeric = true;
                    ver[vidx] = i3 - 48;
                }
                i2 = i;
            }
        }
        int i4 = i;
        return VersionInfo.getInstance(ver[0], ver[1], ver[2], ver[3]);
    }

    public static boolean enabled() {
        return debug;
    }

    public static boolean enabled(String arg) {
        boolean z = false;
        if (!debug) {
            return false;
        }
        if (params.indexOf(arg) != -1) {
            z = true;
        }
        boolean result = z;
        if (help) {
            PrintStream printStream = System.out;
            printStream.println("\nICUDebug.enabled(" + arg + ") = " + result);
        }
        return result;
    }

    public static String value(String arg) {
        String result = "false";
        if (debug) {
            int index = params.indexOf(arg);
            if (index != -1) {
                int index2 = index + arg.length();
                if (params.length() <= index2 || params.charAt(index2) != '=') {
                    result = "true";
                } else {
                    int index3 = index2 + 1;
                    int limit = params.indexOf(",", index3);
                    result = params.substring(index3, limit == -1 ? params.length() : limit);
                }
            }
            if (help) {
                PrintStream printStream = System.out;
                printStream.println("\nICUDebug.value(" + arg + ") = " + result);
            }
        }
        return result;
    }
}
