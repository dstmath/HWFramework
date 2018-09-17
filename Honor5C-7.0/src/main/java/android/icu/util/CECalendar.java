package android.icu.util;

import android.icu.util.ULocale.Category;
import java.util.Date;
import java.util.Locale;

abstract class CECalendar extends Calendar {
    private static final int[][] LIMITS = null;
    private static final long serialVersionUID = -999547623066414271L;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.CECalendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.CECalendar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.CECalendar.<clinit>():void");
    }

    protected abstract int getJDEpochOffset();

    protected CECalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    protected CECalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    protected CECalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    protected CECalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    protected CECalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    protected CECalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    protected CECalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(year, month, date);
    }

    protected CECalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        setTime(date);
    }

    protected CECalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        set(year, month, date, hour, minute, second);
    }

    protected int handleComputeMonthStart(int eyear, int emonth, boolean useMonth) {
        return ceToJD((long) eyear, emonth, 0, getJDEpochOffset());
    }

    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    protected int handleGetMonthLength(int extendedYear, int month) {
        if ((month + 1) % 13 != 0) {
            return 30;
        }
        return ((extendedYear % 4) / 3) + 5;
    }

    public static int ceToJD(long year, int month, int day, int jdEpochOffset) {
        if (month >= 0) {
            year += (long) (month / 13);
            month %= 13;
        } else {
            month++;
            year += (long) ((month / 13) - 1);
            month = (month % 13) + 12;
        }
        return (int) (((((((long) jdEpochOffset) + (365 * year)) + Calendar.floorDivide(year, 4)) + ((long) (month * 30))) + ((long) day)) - 1);
    }

    public static void jdToCE(int julianDay, int jdEpochOffset, int[] fields) {
        int[] r4 = new int[]{(Calendar.floorDivide(julianDay - jdEpochOffset, 1461, r4) * 4) + ((r4[0] / 365) - (r4[0] / 1460))};
        int doy = r4[0] == 1460 ? 365 : r4[0] % 365;
        fields[1] = doy / 30;
        fields[2] = (doy % 30) + 1;
    }
}
