package dalvik.system;

public final class CloseGuard {
    private static volatile boolean ENABLED;
    private static final CloseGuard NOOP = null;
    private static volatile Reporter REPORTER;
    private Throwable allocationSite;

    public interface Reporter {
        void report(String str, Throwable th);
    }

    private static final class DefaultReporter implements Reporter {
        private DefaultReporter() {
        }

        public void report(String message, Throwable allocationSite) {
            System.logW(message, allocationSite);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: dalvik.system.CloseGuard.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: dalvik.system.CloseGuard.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: dalvik.system.CloseGuard.<clinit>():void");
    }

    public static CloseGuard get() {
        if (ENABLED) {
            return new CloseGuard();
        }
        return NOOP;
    }

    public static void setEnabled(boolean enabled) {
        ENABLED = enabled;
    }

    public static void setReporter(Reporter reporter) {
        if (reporter == null) {
            throw new NullPointerException("reporter == null");
        }
        REPORTER = reporter;
    }

    public static Reporter getReporter() {
        return REPORTER;
    }

    private CloseGuard() {
    }

    public void open(String closer) {
        if (closer == null) {
            throw new NullPointerException("closer == null");
        } else if (this != NOOP && ENABLED) {
            this.allocationSite = new Throwable("Explicit termination method '" + closer + "' not called");
        }
    }

    public void close() {
        this.allocationSite = null;
    }

    public void warnIfOpen() {
        if (this.allocationSite != null && ENABLED) {
            REPORTER.report("A resource was acquired at attached stack trace but never released. See java.io.Closeable for information on avoiding resource leaks.", this.allocationSite);
        }
    }
}
