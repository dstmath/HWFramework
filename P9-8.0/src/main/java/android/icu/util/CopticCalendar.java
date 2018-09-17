package android.icu.util;

import java.util.Date;
import java.util.Locale;

public final class CopticCalendar extends CECalendar {
    public static final int AMSHIR = 5;
    public static final int BABA = 1;
    public static final int BARAMHAT = 6;
    public static final int BARAMOUDA = 7;
    public static final int BASHANS = 8;
    private static final int BCE = 0;
    private static final int CE = 1;
    public static final int EPEP = 10;
    public static final int HATOR = 2;
    private static final int JD_EPOCH_OFFSET = 1824665;
    public static final int KIAHK = 3;
    public static final int MESRA = 11;
    public static final int NASIE = 12;
    public static final int PAONA = 9;
    public static final int TOBA = 4;
    public static final int TOUT = 0;
    private static final long serialVersionUID = 5903818751846742911L;

    public CopticCalendar(TimeZone zone) {
        super(zone);
    }

    public CopticCalendar(Locale aLocale) {
        super(aLocale);
    }

    public CopticCalendar(ULocale locale) {
        super(locale);
    }

    public CopticCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }

    public CopticCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
    }

    public CopticCalendar(int year, int month, int date) {
        super(year, month, date);
    }

    public CopticCalendar(Date date) {
        super(date);
    }

    public CopticCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(year, month, date, hour, minute, second);
    }

    public String getType() {
        return "coptic";
    }

    @Deprecated
    protected int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, 1);
        }
        if (internalGet(0, 1) == 0) {
            return 1 - internalGet(1, 1);
        }
        return internalGet(1, 1);
    }

    @Deprecated
    protected void handleComputeFields(int julianDay) {
        int era;
        int year;
        int[] fields = new int[3];
        CECalendar.jdToCE(julianDay, getJDEpochOffset(), fields);
        if (fields[0] <= 0) {
            era = 0;
            year = 1 - fields[0];
        } else {
            era = 1;
            year = fields[0];
        }
        internalSet(19, fields[0]);
        internalSet(0, era);
        internalSet(1, year);
        internalSet(2, fields[1]);
        internalSet(5, fields[2]);
        internalSet(6, (fields[1] * 30) + fields[2]);
    }

    @Deprecated
    protected int getJDEpochOffset() {
        return JD_EPOCH_OFFSET;
    }

    public static int copticToJD(long year, int month, int date) {
        return CECalendar.ceToJD(year, month, date, JD_EPOCH_OFFSET);
    }
}
