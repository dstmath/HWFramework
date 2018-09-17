package android.icu.impl;

import android.icu.util.VersionInfo;

public final class ICUDebug {
    private static boolean debug;
    private static boolean help;
    public static final boolean isJDK14OrHigher;
    public static final VersionInfo javaVersion = getInstanceLenient(javaVersionString);
    public static final String javaVersionString = System.getProperty("java.version", AndroidHardcodedSystemProperties.JAVA_VERSION);
    private static String params;

    static {
        boolean z;
        boolean z2 = true;
        try {
            params = System.getProperty("ICUDebug");
        } catch (SecurityException e) {
        }
        if (params != null) {
            z = true;
        } else {
            z = false;
        }
        debug = z;
        if (!debug || (!params.equals("") && params.indexOf("help") == -1)) {
            z = false;
        } else {
            z = true;
        }
        help = z;
        if (debug) {
            System.out.println("\nICUDebug=" + params);
        }
        if (javaVersion.compareTo(VersionInfo.getInstance("1.4.0")) < 0) {
            z2 = false;
        }
        isJDK14OrHigher = z2;
    }

    public static VersionInfo getInstanceLenient(String s) {
        int[] ver = new int[4];
        boolean numeric = false;
        int i = 0;
        int vidx = 0;
        while (i < s.length()) {
            int i2 = i + 1;
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                if (!numeric) {
                    continue;
                } else if (vidx == 3) {
                    i = i2;
                    break;
                } else {
                    numeric = false;
                    vidx++;
                }
            } else if (numeric) {
                ver[vidx] = (ver[vidx] * 10) + (c - 48);
                if (ver[vidx] > 255) {
                    ver[vidx] = 0;
                    i = i2;
                    break;
                }
            } else {
                numeric = true;
                ver[vidx] = c - 48;
            }
            i = i2;
        }
        return VersionInfo.getInstance(ver[0], ver[1], ver[2], ver[3]);
    }

    public static boolean enabled() {
        return debug;
    }

    public static boolean enabled(String arg) {
        if (!debug) {
            return false;
        }
        boolean result = params.indexOf(arg) != -1;
        if (help) {
            System.out.println("\nICUDebug.enabled(" + arg + ") = " + result);
        }
        return result;
    }

    public static String value(String arg) {
        String result = "false";
        if (debug) {
            int index = params.indexOf(arg);
            if (index != -1) {
                index += arg.length();
                if (params.length() <= index || params.charAt(index) != '=') {
                    result = "true";
                } else {
                    index++;
                    int limit = params.indexOf(",", index);
                    String str = params;
                    if (limit == -1) {
                        limit = params.length();
                    }
                    result = str.substring(index, limit);
                }
            }
            if (help) {
                System.out.println("\nICUDebug.value(" + arg + ") = " + result);
            }
        }
        return result;
    }
}
