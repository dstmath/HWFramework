package android.icu.util;

import java.util.Date;
import java.util.Locale;

public class TaiwanCalendar extends GregorianCalendar {
    public static final int BEFORE_MINGUO = 0;
    private static final int GREGORIAN_EPOCH = 1970;
    public static final int MINGUO = 1;
    private static final int Taiwan_ERA_START = 1911;
    private static final long serialVersionUID = 2583005278132380631L;

    public TaiwanCalendar(TimeZone zone) {
        super(zone);
    }

    public TaiwanCalendar(Locale aLocale) {
        super(aLocale);
    }

    public TaiwanCalendar(ULocale locale) {
        super(locale);
    }

    public TaiwanCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }

    public TaiwanCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
    }

    public TaiwanCalendar(Date date) {
        this();
        setTime(date);
    }

    public TaiwanCalendar(int year, int month, int date) {
        super(year, month, date);
    }

    public TaiwanCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(year, month, date, hour, minute, second);
    }

    protected int handleGetExtendedYear() {
        if (newerField(19, 1) == 19 && newerField(19, 0) == 19) {
            return internalGet(19, GREGORIAN_EPOCH);
        }
        if (internalGet(0, 1) == 1) {
            return internalGet(1, 1) + Taiwan_ERA_START;
        }
        return (1 - internalGet(1, 1)) + Taiwan_ERA_START;
    }

    protected void handleComputeFields(int julianDay) {
        super.handleComputeFields(julianDay);
        int y = internalGet(19) - 1911;
        if (y > 0) {
            internalSet(0, 1);
            internalSet(1, y);
            return;
        }
        internalSet(0, 0);
        internalSet(1, 1 - y);
    }

    protected int handleGetLimit(int field, int limitType) {
        if (field == 0) {
            return (limitType == 0 || limitType == 1) ? 0 : 1;
        } else {
            return super.handleGetLimit(field, limitType);
        }
    }

    public String getType() {
        return "roc";
    }
}
