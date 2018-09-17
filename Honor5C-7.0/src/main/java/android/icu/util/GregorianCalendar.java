package android.icu.util;

import android.icu.impl.Grego;
import android.icu.text.BreakIterator;
import android.icu.util.ULocale.Category;
import java.util.Date;
import java.util.Locale;
import javax.xml.datatype.DatatypeConstants;
import libcore.icu.RelativeDateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;

public class GregorianCalendar extends Calendar {
    public static final int AD = 1;
    public static final int BC = 0;
    private static final int EPOCH_YEAR = 1970;
    private static final int[][] LIMITS = null;
    private static final int[][] MONTH_COUNT = null;
    private static final long serialVersionUID = 9199388694351062137L;
    private transient int cutoverJulianDay;
    private long gregorianCutover;
    private transient int gregorianCutoverYear;
    protected transient boolean invertGregorian;
    protected transient boolean isGregorian;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.GregorianCalendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.GregorianCalendar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.GregorianCalendar.<clinit>():void");
    }

    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    public GregorianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    public GregorianCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    public GregorianCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    public GregorianCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    public GregorianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        setTimeInMillis(System.currentTimeMillis());
    }

    public GregorianCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        setTimeInMillis(System.currentTimeMillis());
    }

    public GregorianCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(BC, AD);
        set(AD, year);
        set(2, month);
        set(5, date);
    }

    public GregorianCalendar(int year, int month, int date, int hour, int minute) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(BC, AD);
        set(AD, year);
        set(2, month);
        set(5, date);
        set(11, hour);
        set(12, minute);
    }

    public GregorianCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.gregorianCutover = -12219292800000L;
        this.cutoverJulianDay = 2299161;
        this.gregorianCutoverYear = 1582;
        set(BC, AD);
        set(AD, year);
        set(2, month);
        set(5, date);
        set(11, hour);
        set(12, minute);
        set(13, second);
    }

    public void setGregorianChange(Date date) {
        this.gregorianCutover = date.getTime();
        if (this.gregorianCutover <= Grego.MIN_MILLIS) {
            this.cutoverJulianDay = DatatypeConstants.FIELD_UNDEFINED;
            this.gregorianCutoverYear = DatatypeConstants.FIELD_UNDEFINED;
        } else if (this.gregorianCutover >= Grego.MAX_MILLIS) {
            this.cutoverJulianDay = AnnualTimeZoneRule.MAX_YEAR;
            this.gregorianCutoverYear = AnnualTimeZoneRule.MAX_YEAR;
        } else {
            this.cutoverJulianDay = (int) Calendar.floorDivide(this.gregorianCutover, (long) RelativeDateTimeFormatter.DAY_IN_MILLIS);
            GregorianCalendar cal = new GregorianCalendar(getTimeZone());
            cal.setTime(date);
            this.gregorianCutoverYear = cal.get(19);
        }
    }

    public final Date getGregorianChange() {
        return new Date(this.gregorianCutover);
    }

    public boolean isLeapYear(int year) {
        if (year >= this.gregorianCutoverYear) {
            return year % 4 == 0 && (year % 100 != 0 || year % BreakIterator.WORD_KANA_LIMIT == 0);
        } else {
            if (year % 4 != 0) {
                return false;
            }
            return true;
        }
    }

    public boolean isEquivalentTo(Calendar other) {
        if (super.isEquivalentTo(other) && this.gregorianCutover == ((GregorianCalendar) other).gregorianCutover) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return super.hashCode() ^ ((int) this.gregorianCutover);
    }

    public void roll(int field, int amount) {
        switch (field) {
            case XmlPullParser.END_TAG /*3*/:
                int woy = get(3);
                int isoYear = get(17);
                int isoDoy = internalGet(6);
                if (internalGet(2) == 0) {
                    if (woy >= 52) {
                        isoDoy += handleGetYearLength(isoYear);
                    }
                } else if (woy == AD) {
                    isoDoy -= handleGetYearLength(isoYear - 1);
                }
                woy += amount;
                if (woy < AD || woy > 52) {
                    int lastDoy = handleGetYearLength(isoYear);
                    int lastRelDow = (((lastDoy - isoDoy) + internalGet(7)) - getFirstDayOfWeek()) % 7;
                    if (lastRelDow < 0) {
                        lastRelDow += 7;
                    }
                    if (6 - lastRelDow >= getMinimalDaysInFirstWeek()) {
                        lastDoy -= 7;
                    }
                    int lastWoy = weekNumber(lastDoy, lastRelDow + AD);
                    woy = (((woy + lastWoy) - 1) % lastWoy) + AD;
                }
                set(3, woy);
                set(AD, isoYear);
            default:
                super.roll(field, amount);
        }
    }

    public int getActualMinimum(int field) {
        return getMinimum(field);
    }

    public int getActualMaximum(int field) {
        switch (field) {
            case AD /*1*/:
                Calendar cal = (Calendar) clone();
                cal.setLenient(true);
                int era = cal.get(BC);
                Date d = cal.getTime();
                int lowGood = LIMITS[AD][AD];
                int highBad = LIMITS[AD][2] + AD;
                while (lowGood + AD < highBad) {
                    int y = (lowGood + highBad) / 2;
                    cal.set(AD, y);
                    if (cal.get(AD) == y && cal.get(BC) == era) {
                        lowGood = y;
                    } else {
                        highBad = y;
                        cal.setTime(d);
                    }
                }
                return lowGood;
            default:
                return super.getActualMaximum(field);
        }
    }

    boolean inDaylightTime() {
        boolean z = false;
        if (!getTimeZone().useDaylightTime()) {
            return false;
        }
        complete();
        if (internalGet(16) != 0) {
            z = true;
        }
        return z;
    }

    protected int handleGetMonthLength(int extendedYear, int month) {
        int i = AD;
        if (month < 0 || month > 11) {
            int[] rem = new int[AD];
            extendedYear += Calendar.floorDivide(month, 12, rem);
            month = rem[BC];
        }
        int[] iArr = MONTH_COUNT[month];
        if (!isLeapYear(extendedYear)) {
            i = BC;
        }
        return iArr[i];
    }

    protected int handleGetYearLength(int eyear) {
        return isLeapYear(eyear) ? 366 : 365;
    }

    protected void handleComputeFields(int julianDay) {
        int month;
        int dayOfMonth;
        int dayOfYear;
        int eyear;
        if (julianDay >= this.cutoverJulianDay) {
            month = getGregorianMonth();
            dayOfMonth = getGregorianDayOfMonth();
            dayOfYear = getGregorianDayOfYear();
            eyear = getGregorianYear();
        } else {
            long julianEpochDay = (long) (julianDay - 1721424);
            eyear = (int) Calendar.floorDivide((4 * julianEpochDay) + 1464, 1461);
            dayOfYear = (int) (julianEpochDay - ((long) (((eyear - 1) * 365) + Calendar.floorDivide(eyear - 1, 4))));
            boolean isLeap = (eyear & 3) == 0;
            int correction = BC;
            if (dayOfYear >= (isLeap ? 60 : 59)) {
                correction = isLeap ? AD : 2;
            }
            month = (((dayOfYear + correction) * 12) + 6) / 367;
            dayOfMonth = (dayOfYear - MONTH_COUNT[month][isLeap ? 3 : 2]) + AD;
            dayOfYear += AD;
        }
        internalSet(2, month);
        internalSet(5, dayOfMonth);
        internalSet(6, dayOfYear);
        internalSet(19, eyear);
        int era = AD;
        if (eyear < AD) {
            era = BC;
            eyear = 1 - eyear;
        }
        internalSet(BC, era);
        internalSet(AD, eyear);
    }

    protected int handleGetExtendedYear() {
        if (newerField(19, AD) == 19) {
            return internalGet(19, EPOCH_YEAR);
        }
        if (internalGet(BC, AD) == 0) {
            return 1 - internalGet(AD, AD);
        }
        return internalGet(AD, EPOCH_YEAR);
    }

    protected int handleComputeJulianDay(int bestField) {
        boolean z = false;
        this.invertGregorian = false;
        int jd = super.handleComputeJulianDay(bestField);
        boolean z2 = this.isGregorian;
        if (jd >= this.cutoverJulianDay) {
            z = true;
        }
        if (z2 == z) {
            return jd;
        }
        this.invertGregorian = true;
        return super.handleComputeJulianDay(bestField);
    }

    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        boolean z;
        boolean z2 = false;
        if (month < 0 || month > 11) {
            int[] rem = new int[AD];
            eyear += Calendar.floorDivide(month, 12, rem);
            month = rem[BC];
        }
        boolean isLeap = eyear % 4 == 0;
        int y = eyear - 1;
        int julianDay = ((y * 365) + Calendar.floorDivide(y, 4)) + 1721423;
        if (eyear >= this.gregorianCutoverYear) {
            z = true;
        } else {
            z = false;
        }
        this.isGregorian = z;
        if (this.invertGregorian) {
            if (!this.isGregorian) {
                z2 = true;
            }
            this.isGregorian = z2;
        }
        if (this.isGregorian) {
            isLeap = isLeap && (eyear % 100 != 0 || eyear % BreakIterator.WORD_KANA_LIMIT == 0);
            julianDay += (Calendar.floorDivide(y, (int) BreakIterator.WORD_KANA_LIMIT) - Calendar.floorDivide(y, 100)) + 2;
        }
        if (month == 0) {
            return julianDay;
        }
        return julianDay + MONTH_COUNT[month][isLeap ? 3 : 2];
    }

    public String getType() {
        return "gregorian";
    }
}
