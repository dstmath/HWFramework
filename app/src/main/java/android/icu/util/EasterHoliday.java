package android.icu.util;

public class EasterHoliday extends Holiday {
    public static final EasterHoliday ASCENSION = null;
    public static final EasterHoliday ASH_WEDNESDAY = null;
    public static final EasterHoliday CORPUS_CHRISTI = null;
    public static final EasterHoliday EASTER_MONDAY = null;
    public static final EasterHoliday EASTER_SUNDAY = null;
    public static final EasterHoliday GOOD_FRIDAY = null;
    public static final EasterHoliday MAUNDY_THURSDAY = null;
    public static final EasterHoliday PALM_SUNDAY = null;
    public static final EasterHoliday PENTECOST = null;
    public static final EasterHoliday SHROVE_TUESDAY = null;
    public static final EasterHoliday WHIT_MONDAY = null;
    public static final EasterHoliday WHIT_SUNDAY = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.EasterHoliday.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.EasterHoliday.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.EasterHoliday.<clinit>():void");
    }

    public EasterHoliday(String name) {
        super(name, new EasterRule(0, false));
    }

    public EasterHoliday(int daysAfter, String name) {
        super(name, new EasterRule(daysAfter, false));
    }

    public EasterHoliday(int daysAfter, boolean orthodox, String name) {
        super(name, new EasterRule(daysAfter, orthodox));
    }
}
