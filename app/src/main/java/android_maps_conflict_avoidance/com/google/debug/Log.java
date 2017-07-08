package android_maps_conflict_avoidance.com.google.debug;

import java.util.Hashtable;

public class Log {
    private static final String[] LEVEL_NAMES = null;
    private static final Logger logger = null;
    private static final Hashtable timers = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.debug.Log.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.debug.Log.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.debug.Log.<clinit>():void");
    }

    private Log() {
    }

    private static Class logger() {
        String name = "android_maps_conflict_avoidance.com.google.debug.StdoutLogger";
        try {
            if (DebugUtil.isAntPropertyExpanded("android_maps_conflict_avoidance.com.google.debug.StdoutLogger")) {
                return Class.forName("android_maps_conflict_avoidance.com.google.debug.StdoutLogger");
            }
            String sysName = System.getProperty("LOGGER");
            if (sysName != null) {
                return Class.forName(sysName);
            }
            System.err.println("WARNING: Missing logger class - using default logger com.google.debug.StdoutLogger");
            System.err.println("         For Ant: Specify the logger class using the LOGGER property");
            System.err.println("         For Bolide: Specify the logger class using constant injection");
            System.err.println("         For J2SE:  Specify the logger class via the LOGGER system property");
            System.err.println("         See JavaDoc or source of com.google.debug.Log.");
            return Class.forName("android_maps_conflict_avoidance.com.google.debug.StdoutLogger");
        } catch (ClassNotFoundException e) {
            throw new Error("Missing logger class com.google.debug.StdoutLogger");
        }
    }

    public static void logThrowable(Object message, Throwable exception) {
        xlogThrowable(message, exception, null, null, -1);
    }

    public static void xlogThrowable(Object message, Throwable exception, String className, String methodName, int lineNumber) {
        xlogThrowable(message, exception, 5, className, methodName, lineNumber);
    }

    public static void xlogThrowable(Object message, Throwable exception, int logLevel, String className, String methodName, int lineNumber) {
        logger.logThrowable(message, exception, logLevel, className, methodName, lineNumber);
    }
}
