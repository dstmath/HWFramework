package com.huawei.iconnect.wearable;

import android.util.Log;

public class HwLog {
    private static final String TAG = "iConnect:Wearablejar";
    private static boolean sHwDetailLog;
    private static boolean sHwInfo;
    private static boolean sHwModuleDebug;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.iconnect.wearable.HwLog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.iconnect.wearable.HwLog.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.iconnect.wearable.HwLog.<clinit>():void");
    }

    static void e(String tag, String msg) {
        Log.e(TAG, tag + ":" + msg);
    }

    static void w(String tag, String msg) {
        Log.w(TAG, tag + ":" + msg);
    }

    static void d(String tag, String msg) {
        if (sHwDetailLog) {
            Log.d(TAG, tag + ":" + msg);
        }
    }
}
