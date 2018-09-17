package com.huawei.device.connectivitychrlog;

import android.util.Log;

public class ChrLog {
    public static final boolean HWDBG = false;
    public static final boolean HWFLOW = false;
    public static final boolean HWLOGW_E = true;
    private static final String TAG = null;
    private static String TAG_PREFIX;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.device.connectivitychrlog.ChrLog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.device.connectivitychrlog.ChrLog.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.device.connectivitychrlog.ChrLog.<clinit>():void");
    }

    public static void chrLogD(String tag, String values) {
        if (HWDBG) {
            Log.d(TAG_PREFIX + tag, values);
        }
    }

    public static void chrLogE(String tag, String values) {
        Log.e(TAG_PREFIX + tag, values);
    }

    public static void chrLogI(String tag, String values) {
        if (HWFLOW) {
            Log.i(TAG_PREFIX + tag, values);
        }
    }

    public static void chrLogW(String tag, String values) {
        Log.w(TAG_PREFIX + tag, values);
    }
}
