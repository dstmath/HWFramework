package com.android.server.security.trustcircle.utils;

import android.util.Log;

public class LogHelper {
    private static final boolean HWDBG = false;
    private static final boolean HWERROR = true;
    private static final boolean HWINFO = false;
    public static final String SEPARATOR = " - ";
    public static final String TAG = "TrustCircleService";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.utils.LogHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.utils.LogHelper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.utils.LogHelper.<clinit>():void");
    }

    public static void v(String tag, String msg) {
        if (HWDBG) {
            Log.v(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void v(String tag, String... msg) {
        if (HWDBG) {
            Log.v(TAG, tag + SEPARATOR + appendString(msg));
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (HWDBG) {
            Log.v(TAG, tag + SEPARATOR + msg, tr);
        }
    }

    public static void v(String tag, Throwable tr, String... msg) {
        if (HWDBG) {
            Log.v(TAG, tag + SEPARATOR + appendString(msg), tr);
        }
    }

    public static void d(String tag, String msg) {
        if (HWDBG) {
            Log.d(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void d(String tag, String... msg) {
        if (HWDBG) {
            Log.d(TAG, tag + SEPARATOR + appendString(msg));
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (HWDBG) {
            Log.d(TAG, tag + SEPARATOR + msg, tr);
        }
    }

    public static void d(String tag, Throwable tr, String... msg) {
        if (HWDBG) {
            Log.d(TAG, tag + SEPARATOR + appendString(msg), tr);
        }
    }

    public static void i(String tag, String msg) {
        if (HWINFO) {
            Log.i(TAG, tag + SEPARATOR + msg);
        }
    }

    public static void i(String tag, String... msg) {
        if (HWINFO) {
            Log.i(TAG, tag + SEPARATOR + appendString(msg));
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (HWINFO) {
            Log.i(TAG, tag + SEPARATOR + msg, tr);
        }
    }

    public static void i(String tag, Throwable tr, String... msg) {
        if (HWINFO) {
            Log.i(TAG, tag + SEPARATOR + appendString(msg), tr);
        }
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + SEPARATOR + msg);
    }

    public static void w(String tag, String... msg) {
        w(tag, appendString(msg));
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(TAG, tag + SEPARATOR + msg, tr);
    }

    public static void w(String tag, Throwable tr) {
        Log.w(TAG, tag + SEPARATOR + tr);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + SEPARATOR + msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(TAG, tag + SEPARATOR + msg, tr);
    }

    private static String appendString(String... msg) {
        StringBuilder builder = new StringBuilder();
        for (String s : msg) {
            builder.append(s);
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}
