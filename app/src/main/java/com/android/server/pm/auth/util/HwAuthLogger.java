package com.android.server.pm.auth.util;

import android.util.Slog;

public class HwAuthLogger {
    private static final boolean DEBUG_LOGD = false;
    private static final boolean DEBUG_LOGE = true;
    private static final boolean DEBUG_LOGI = false;
    private static final boolean DEBUG_LOGW = true;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.auth.util.HwAuthLogger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.auth.util.HwAuthLogger.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.auth.util.HwAuthLogger.<clinit>():void");
    }

    public static void v(String tag, String msg) {
    }

    public static void d(String tag, String msg) {
    }

    public static void i(String tag, String msg) {
        if (DEBUG_LOGI) {
            Slog.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        Slog.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        Slog.e(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
    }

    public static void d(String tag, String msg, Throwable tr) {
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG_LOGI) {
            Slog.i(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        Slog.w(tag, msg, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Slog.e(tag, msg, tr);
    }

    public static boolean getHWDEBUG() {
        return DEBUG_LOGI;
    }

    public static boolean getHWFLOW() {
        return DEBUG_LOGI;
    }
}
