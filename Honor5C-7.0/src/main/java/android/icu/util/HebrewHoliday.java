package android.icu.util;

public class HebrewHoliday extends Holiday {
    public static HebrewHoliday ESTHER;
    public static HebrewHoliday GEDALIAH;
    public static HebrewHoliday HANUKKAH;
    public static HebrewHoliday HOSHANAH_RABBAH;
    public static HebrewHoliday LAG_BOMER;
    public static HebrewHoliday PASSOVER;
    public static HebrewHoliday PESACH_SHEINI;
    public static HebrewHoliday PURIM;
    public static HebrewHoliday ROSH_HASHANAH;
    public static HebrewHoliday SELIHOT;
    public static HebrewHoliday SHAVUOT;
    public static HebrewHoliday SHEMINI_ATZERET;
    public static HebrewHoliday SHUSHAN_PURIM;
    public static HebrewHoliday SIMCHAT_TORAH;
    public static HebrewHoliday SUKKOT;
    public static HebrewHoliday TAMMUZ_17;
    public static HebrewHoliday TEVET_10;
    public static HebrewHoliday TISHA_BAV;
    public static HebrewHoliday TU_BSHEVAT;
    public static HebrewHoliday YOM_HAATZMAUT;
    public static HebrewHoliday YOM_HASHOAH;
    public static HebrewHoliday YOM_HAZIKARON;
    public static HebrewHoliday YOM_KIPPUR;
    public static HebrewHoliday YOM_YERUSHALAYIM;
    private static final HebrewCalendar gCalendar = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.HebrewHoliday.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.HebrewHoliday.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.HebrewHoliday.<clinit>():void");
    }

    public HebrewHoliday(int month, int date, String name) {
        this(month, date, 1, name);
    }

    public HebrewHoliday(int month, int date, int length, String name) {
        super(name, new SimpleDateRule(month, date, gCalendar));
    }
}
