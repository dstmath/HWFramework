package android.icu.util;

import android.icu.impl.CalendarUtil;
import android.icu.util.ULocale.Category;
import java.util.Date;
import java.util.Locale;

public final class EthiopicCalendar extends CECalendar {
    private static final int AMETE_ALEM = 0;
    private static final int AMETE_ALEM_ERA = 1;
    private static final int AMETE_MIHRET = 1;
    private static final int AMETE_MIHRET_DELTA = 5500;
    private static final int AMETE_MIHRET_ERA = 0;
    public static final int GENBOT = 8;
    public static final int HAMLE = 10;
    public static final int HEDAR = 2;
    private static final int JD_EPOCH_OFFSET_AMETE_MIHRET = 1723856;
    public static final int MEGABIT = 6;
    public static final int MESKEREM = 0;
    public static final int MIAZIA = 7;
    public static final int NEHASSE = 11;
    public static final int PAGUMEN = 12;
    public static final int SENE = 9;
    public static final int TAHSAS = 3;
    public static final int TEKEMT = 1;
    public static final int TER = 4;
    public static final int YEKATIT = 5;
    private static final long serialVersionUID = -2438495771339315608L;
    private int eraType;

    public EthiopicCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    public EthiopicCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    public EthiopicCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    public EthiopicCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    public EthiopicCalendar(TimeZone zone, Locale aLocale) {
        this(zone, ULocale.forLocale(aLocale));
    }

    public EthiopicCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        this.eraType = 0;
        setCalcTypeForLocale(locale);
    }

    public EthiopicCalendar(int year, int month, int date) {
        super(year, month, date);
        this.eraType = 0;
    }

    public EthiopicCalendar(Date date) {
        super(date);
        this.eraType = 0;
    }

    public EthiopicCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(year, month, date, hour, minute, second);
        this.eraType = 0;
    }

    public String getType() {
        if (isAmeteAlemEra()) {
            return "ethiopic-amete-alem";
        }
        return "ethiopic";
    }

    public void setAmeteAlemEra(boolean onOff) {
        this.eraType = onOff ? 1 : 0;
    }

    public boolean isAmeteAlemEra() {
        return this.eraType == 1;
    }

    @Deprecated
    protected int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, 1);
        }
        if (isAmeteAlemEra()) {
            return internalGet(1, 5501) - 5500;
        }
        if (internalGet(0, 1) == 1) {
            return internalGet(1, 1);
        }
        return internalGet(1, 1) - 5500;
    }

    @Deprecated
    protected void handleComputeFields(int julianDay) {
        int era;
        int year;
        int[] fields = new int[3];
        CECalendar.jdToCE(julianDay, getJDEpochOffset(), fields);
        if (isAmeteAlemEra()) {
            era = 0;
            year = fields[0] + AMETE_MIHRET_DELTA;
        } else if (fields[0] > 0) {
            era = 1;
            year = fields[0];
        } else {
            era = 0;
            year = fields[0] + AMETE_MIHRET_DELTA;
        }
        internalSet(19, fields[0]);
        internalSet(0, era);
        internalSet(1, year);
        internalSet(2, fields[1]);
        internalSet(5, fields[2]);
        internalSet(6, (fields[1] * 30) + fields[2]);
    }

    @Deprecated
    protected int handleGetLimit(int field, int limitType) {
        if (isAmeteAlemEra() && field == 0) {
            return 0;
        }
        return super.handleGetLimit(field, limitType);
    }

    @Deprecated
    protected int getJDEpochOffset() {
        return JD_EPOCH_OFFSET_AMETE_MIHRET;
    }

    public static int EthiopicToJD(long year, int month, int date) {
        return CECalendar.ceToJD(year, month, date, JD_EPOCH_OFFSET_AMETE_MIHRET);
    }

    private void setCalcTypeForLocale(ULocale locale) {
        if ("ethiopic-amete-alem".equals(CalendarUtil.getCalendarType(locale))) {
            setAmeteAlemEra(true);
        } else {
            setAmeteAlemEra(false);
        }
    }
}
