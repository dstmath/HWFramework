package android.icu.impl;

import android.icu.util.VersionInfo;

public final class ICUDebug {
    private static boolean debug;
    private static boolean help;
    public static final boolean isJDK14OrHigher = false;
    public static final VersionInfo javaVersion = null;
    public static final String javaVersionString = null;
    private static String params;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.ICUDebug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.ICUDebug.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.ICUDebug.<clinit>():void");
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
