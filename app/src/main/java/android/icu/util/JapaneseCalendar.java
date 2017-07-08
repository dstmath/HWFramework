package android.icu.util;

import java.util.Date;
import java.util.Locale;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public class JapaneseCalendar extends GregorianCalendar {
    public static final int CURRENT_ERA = 0;
    private static final int[] ERAS = null;
    private static final int GREGORIAN_EPOCH = 1970;
    public static final int HEISEI = 0;
    public static final int MEIJI = 0;
    public static final int SHOWA = 0;
    public static final int TAISHO = 0;
    private static final long serialVersionUID = -2977189902603704691L;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.JapaneseCalendar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.JapaneseCalendar.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.JapaneseCalendar.<clinit>():void");
    }

    public JapaneseCalendar(TimeZone zone) {
        super(zone);
    }

    public JapaneseCalendar(Locale aLocale) {
        super(aLocale);
    }

    public JapaneseCalendar(ULocale locale) {
        super(locale);
    }

    public JapaneseCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }

    public JapaneseCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
    }

    public JapaneseCalendar(Date date) {
        this();
        setTime(date);
    }

    public JapaneseCalendar(int era, int year, int month, int date) {
        super(year, month, date);
        set(TAISHO, era);
    }

    public JapaneseCalendar(int year, int month, int date) {
        super(year, month, date);
        set(TAISHO, CURRENT_ERA);
    }

    public JapaneseCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(year, month, date, hour, minute, second);
        set(TAISHO, CURRENT_ERA);
    }

    protected int handleGetExtendedYear() {
        if (newerField(19, 1) == 19 && newerField(19, TAISHO) == 19) {
            return internalGet(19, GREGORIAN_EPOCH);
        }
        return (internalGet(1, 1) + ERAS[internalGet(TAISHO, CURRENT_ERA) * 3]) - 1;
    }

    protected int getDefaultMonthInYear(int extendedYear) {
        int era = internalGet(TAISHO, CURRENT_ERA);
        if (extendedYear == ERAS[era * 3]) {
            return ERAS[(era * 3) + 1] - 1;
        }
        return super.getDefaultMonthInYear(extendedYear);
    }

    protected int getDefaultDayInMonth(int extendedYear, int month) {
        int era = internalGet(TAISHO, CURRENT_ERA);
        if (extendedYear == ERAS[era * 3] && month == ERAS[(era * 3) + 1] - 1) {
            return ERAS[(era * 3) + 2];
        }
        return super.getDefaultDayInMonth(extendedYear, month);
    }

    protected void handleComputeFields(int julianDay) {
        super.handleComputeFields(julianDay);
        int year = internalGet(19);
        int low = TAISHO;
        if (year > ERAS[ERAS.length - 3]) {
            low = CURRENT_ERA;
        } else {
            int high = ERAS.length / 3;
            while (low < high - 1) {
                int i = (low + high) / 2;
                int diff = year - ERAS[i * 3];
                if (diff == 0) {
                    diff = internalGet(2) - (ERAS[(i * 3) + 1] - 1);
                    if (diff == 0) {
                        diff = internalGet(5) - ERAS[(i * 3) + 2];
                    }
                }
                if (diff >= 0) {
                    low = i;
                } else {
                    high = i;
                }
            }
        }
        internalSet(TAISHO, low);
        internalSet(1, (year - ERAS[low * 3]) + 1);
    }

    protected int handleGetLimit(int field, int limitType) {
        switch (field) {
            case TAISHO /*0*/:
                if (limitType == 0 || limitType == 1) {
                    return TAISHO;
                }
                return CURRENT_ERA;
            case NodeFilter.SHOW_ELEMENT /*1*/:
                switch (limitType) {
                    case TAISHO /*0*/:
                    case NodeFilter.SHOW_ELEMENT /*1*/:
                        return 1;
                    case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                        return 1;
                    case XmlPullParser.END_TAG /*3*/:
                        return super.handleGetLimit(field, 3) - ERAS[CURRENT_ERA * 3];
                    default:
                        break;
                }
        }
        return super.handleGetLimit(field, limitType);
    }

    public String getType() {
        return "japanese";
    }

    @Deprecated
    public boolean haveDefaultCentury() {
        return false;
    }

    public int getActualMaximum(int field) {
        if (field != 1) {
            return super.getActualMaximum(field);
        }
        int era = get(TAISHO);
        if (era == CURRENT_ERA) {
            return handleGetLimit(1, 3);
        }
        int nextEraYear = ERAS[(era + 1) * 3];
        int nextEraMonth = ERAS[((era + 1) * 3) + 1];
        int nextEraDate = ERAS[((era + 1) * 3) + 2];
        int maxYear = (nextEraYear - ERAS[era * 3]) + 1;
        if (nextEraMonth == 1 && nextEraDate == 1) {
            maxYear--;
        }
        return maxYear;
    }
}
