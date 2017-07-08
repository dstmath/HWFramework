package android.util;

public class HwLogExceptionInner implements LogException {
    public static final int LEVEL_A = 65;
    public static final int LEVEL_B = 66;
    public static final int LEVEL_C = 67;
    public static final int LEVEL_D = 68;
    private static final int LOG_ID_EXCEPTION = 5;
    private static LogException mLogExceptionInner;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.HwLogExceptionInner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.HwLogExceptionInner.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.HwLogExceptionInner.<clinit>():void");
    }

    public static native int println_exception_native(String str, int i, String str2, String str3);

    public static synchronized LogException getInstance() {
        LogException logException;
        synchronized (HwLogExceptionInner.class) {
            if (mLogExceptionInner == null) {
                mLogExceptionInner = new HwLogExceptionInner();
            }
            logException = mLogExceptionInner;
        }
        return logException;
    }

    public int cmd(String tag, String contain) {
        return println_exception_native(tag, 0, "command", contain);
    }

    public int msg(String category, int level, String header, String body) {
        return println_exception_native(category, level, "message", header + '\n' + body);
    }

    public int msg(String category, int level, int mask, String header, String body) {
        return println_exception_native(category, level, "message", "mask=" + mask + ";" + header + '\n' + body);
    }
}
