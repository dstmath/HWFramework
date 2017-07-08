package com.android.server;

import android.util.Log;
import com.android.server.am.ProcessList;

public final class SmartShrinker {
    private static final boolean DEBUG = false;
    private static volatile boolean ENABLE = false;
    public static final int RECLAIM_ALL_MODE = 2;
    public static final int RECLAIM_ANON_MODE = 1;
    public static final int RECLAIM_INACTIVE_MODE = 4;
    public static final int RECLAIM_SOFT_MODE = 3;
    private static final String TAG = "RMS.SmartShrinker";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.SmartShrinker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.SmartShrinker.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.SmartShrinker.<clinit>():void");
    }

    public static final void init_once(boolean enable) {
        ENABLE = enable;
    }

    public static final void reclaim(int pid, int mode) {
        if (ENABLE) {
            ProcessList.callProcReclaim(pid, mode);
            Log.w(TAG, "SmartShrinker is runing in pid =" + pid + " reclaim mode = " + mode);
        }
    }
}
