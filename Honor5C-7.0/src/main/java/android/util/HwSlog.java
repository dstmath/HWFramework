package android.util;

import android.common.HwFrameworkFactory;

public final class HwSlog {
    public static final int DEBUG_HIERACHY_CODE = 1000;
    public static boolean HWFLOW = false;
    public static boolean HW_DEBUG = false;
    public static boolean HW_DEBUG_STATES = false;
    private static final String TAG = "CoreServices";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.HwSlog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.HwSlog.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.HwSlog.<clinit>():void");
    }

    private HwSlog() {
    }

    public static int v(String tag, String msg) {
        return HwFrameworkFactory.getHwFlogManager().slogv(tag, msg);
    }

    public static int d(String tag, String msg) {
        return HwFrameworkFactory.getHwFlogManager().slogd(tag, msg);
    }

    public static boolean handleLogRequest(String[] args) {
        return HwFrameworkFactory.getHwFlogManager().handleLogRequest(args);
    }
}
