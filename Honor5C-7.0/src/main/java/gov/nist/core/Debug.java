package gov.nist.core;

public class Debug {
    public static boolean debug;
    public static boolean parserDebug;
    static StackLogger stackLogger;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: gov.nist.core.Debug.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: gov.nist.core.Debug.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.core.Debug.<clinit>():void");
    }

    public static void setStackLogger(StackLogger stackLogger) {
        stackLogger = stackLogger;
    }

    public static void println(String s) {
        if ((parserDebug || debug) && stackLogger != null) {
            stackLogger.logDebug(s + Separators.RETURN);
        }
    }

    public static void printStackTrace(Exception ex) {
        if ((parserDebug || debug) && stackLogger != null) {
            stackLogger.logError("Stack Trace", ex);
        }
    }

    public static void logError(String message, Exception ex) {
        if ((parserDebug || debug) && stackLogger != null) {
            stackLogger.logError(message, ex);
        }
    }
}
