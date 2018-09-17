package android.icu.util;

import java.util.Date;
import java.util.Locale;

public class BuddhistCalendar extends GregorianCalendar {
    public static final int BE = 0;
    private static final int BUDDHIST_ERA_START = -543;
    private static final int GREGORIAN_EPOCH = 1970;
    private static final long serialVersionUID = 2583005278132380631L;

    public BuddhistCalendar(TimeZone zone) {
        super(zone);
    }

    public BuddhistCalendar(Locale aLocale) {
        super(aLocale);
    }

    public BuddhistCalendar(ULocale locale) {
        super(locale);
    }

    public BuddhistCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }

    public BuddhistCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
    }

    public BuddhistCalendar(Date date) {
        this();
        setTime(date);
    }

    public BuddhistCalendar(int year, int month, int date) {
        super(year, month, date);
    }

    public BuddhistCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(year, month, date, hour, minute, second);
    }

    protected int handleGetExtendedYear() {
        if (newerField(19, 1) == 19) {
            return internalGet(19, GREGORIAN_EPOCH);
        }
        return internalGet(1, 2513) + BUDDHIST_ERA_START;
    }

    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        return super.handleComputeMonthStart(eyear, month, useMonth);
    }

    protected void handleComputeFields(int julianDay) {
        super.handleComputeFields(julianDay);
        int y = internalGet(19) + 543;
        internalSet(0, 0);
        internalSet(1, y);
    }

    protected int handleGetLimit(int field, int limitType) {
        if (field == 0) {
            return 0;
        }
        return super.handleGetLimit(field, limitType);
    }

    public String getType() {
        return "buddhist";
    }
}
