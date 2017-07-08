package android.icu.impl.duration;

public final class TimeUnit {
    public static final TimeUnit DAY = null;
    public static final TimeUnit HOUR = null;
    public static final TimeUnit MILLISECOND = null;
    public static final TimeUnit MINUTE = null;
    public static final TimeUnit MONTH = null;
    public static final TimeUnit SECOND = null;
    public static final TimeUnit WEEK = null;
    public static final TimeUnit YEAR = null;
    static final long[] approxDurations = null;
    static final TimeUnit[] units = null;
    final String name;
    final byte ordinal;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.TimeUnit.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.TimeUnit.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.TimeUnit.<clinit>():void");
    }

    private TimeUnit(String name, int ordinal) {
        this.name = name;
        this.ordinal = (byte) ordinal;
    }

    public String toString() {
        return this.name;
    }

    public TimeUnit larger() {
        return this.ordinal == null ? null : units[this.ordinal - 1];
    }

    public TimeUnit smaller() {
        return this.ordinal == units.length + -1 ? null : units[this.ordinal + 1];
    }

    public int ordinal() {
        return this.ordinal;
    }
}
