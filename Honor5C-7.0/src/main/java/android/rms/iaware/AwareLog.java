package android.rms.iaware;

import android.util.Log;

public final class AwareLog {
    private static final boolean HWDBG = false;
    private static final boolean HWFLOW = false;
    private static final boolean HWLOGW_E = true;
    private static final boolean HWVERBOSE = false;
    private static final String TAG = "AwareLog";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.rms.iaware.AwareLog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.rms.iaware.AwareLog.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.rms.iaware.AwareLog.<clinit>():void");
    }

    public static void v(String tag, String msg) {
        if (HWVERBOSE) {
            Log.v(TAG, tag + ": " + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (HWDBG) {
            Log.d(TAG, tag + ": " + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (HWFLOW) {
            Log.i(TAG, tag + ": " + msg);
        }
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + ": " + msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + ": " + msg);
    }
}
