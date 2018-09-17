package android.icu.util;

public class SimpleHoliday extends Holiday {
    public static final SimpleHoliday ALL_SAINTS_DAY = null;
    public static final SimpleHoliday ALL_SOULS_DAY = null;
    public static final SimpleHoliday ASSUMPTION = null;
    public static final SimpleHoliday BOXING_DAY = null;
    public static final SimpleHoliday CHRISTMAS = null;
    public static final SimpleHoliday CHRISTMAS_EVE = null;
    public static final SimpleHoliday EPIPHANY = null;
    public static final SimpleHoliday IMMACULATE_CONCEPTION = null;
    public static final SimpleHoliday MAY_DAY = null;
    public static final SimpleHoliday NEW_YEARS_DAY = null;
    public static final SimpleHoliday NEW_YEARS_EVE = null;
    public static final SimpleHoliday ST_STEPHENS_DAY = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.SimpleHoliday.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.SimpleHoliday.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.SimpleHoliday.<clinit>():void");
    }

    public SimpleHoliday(int month, int dayOfMonth, String name) {
        super(name, new SimpleDateRule(month, dayOfMonth));
    }

    public SimpleHoliday(int month, int dayOfMonth, String name, int startYear) {
        super(name, rangeRule(startYear, 0, new SimpleDateRule(month, dayOfMonth)));
    }

    public SimpleHoliday(int month, int dayOfMonth, String name, int startYear, int endYear) {
        super(name, rangeRule(startYear, endYear, new SimpleDateRule(month, dayOfMonth)));
    }

    public SimpleHoliday(int month, int dayOfMonth, int dayOfWeek, String name) {
        boolean z = false;
        int i = dayOfWeek > 0 ? dayOfWeek : -dayOfWeek;
        if (dayOfWeek > 0) {
            z = true;
        }
        super(name, new SimpleDateRule(month, dayOfMonth, i, z));
    }

    public SimpleHoliday(int month, int dayOfMonth, int dayOfWeek, String name, int startYear) {
        boolean z;
        int i = dayOfWeek > 0 ? dayOfWeek : -dayOfWeek;
        if (dayOfWeek > 0) {
            z = true;
        } else {
            z = false;
        }
        super(name, rangeRule(startYear, 0, new SimpleDateRule(month, dayOfMonth, i, z)));
    }

    public SimpleHoliday(int month, int dayOfMonth, int dayOfWeek, String name, int startYear, int endYear) {
        boolean z = false;
        int i = dayOfWeek > 0 ? dayOfWeek : -dayOfWeek;
        if (dayOfWeek > 0) {
            z = true;
        }
        super(name, rangeRule(startYear, endYear, new SimpleDateRule(month, dayOfMonth, i, z)));
    }

    private static DateRule rangeRule(int startYear, int endYear, DateRule rule) {
        if (startYear == 0 && endYear == 0) {
            return rule;
        }
        RangeDateRule rangeRule = new RangeDateRule();
        if (startYear != 0) {
            rangeRule.add(new GregorianCalendar(startYear, 0, 1).getTime(), rule);
        } else {
            rangeRule.add(rule);
        }
        if (endYear != 0) {
            rangeRule.add(new GregorianCalendar(endYear, 11, 31).getTime(), null);
        }
        return rangeRule;
    }
}
