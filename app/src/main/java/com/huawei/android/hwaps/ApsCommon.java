package com.huawei.android.hwaps;

import android.os.SystemProperties;
import android.util.Log;

public class ApsCommon {
    public static final int INT_DEF = -1;
    private static final int LOG_DEBUG = 2;
    private static final int LOG_INFO = 1;
    private static int mLogLevel;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.ApsCommon.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.ApsCommon.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.ApsCommon.<clinit>():void");
    }

    public static void logI(String tag, String msg) {
        if (isLogEnabled(LOG_INFO)) {
            Log.i(tag, "APS: " + msg);
        }
    }

    public static void logD(String tag, String msg) {
        if (isLogEnabled(LOG_DEBUG)) {
            Log.d(tag, "APS: " + msg);
        }
    }

    private static boolean isLogEnabled(int logLevel) {
        if (INT_DEF == mLogLevel) {
            mLogLevel = SystemProperties.getInt("sys.aps.log", 0);
        }
        if (mLogLevel >= logLevel) {
            return true;
        }
        return false;
    }
}
