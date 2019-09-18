package java.time;

import java.net.HttpURLConnection;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Locale;

public enum Month implements TemporalAccessor, TemporalAdjuster {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;
    
    private static final Month[] ENUMS = null;

    static {
        ENUMS = values();
    }

    public static Month of(int month) {
        if (month >= 1 && month <= 12) {
            return ENUMS[month - 1];
        }
        throw new DateTimeException("Invalid value for MonthOfYear: " + month);
    }

    public static Month from(TemporalAccessor temporal) {
        if (temporal instanceof Month) {
            return (Month) temporal;
        }
        try {
            if (!IsoChronology.INSTANCE.equals(Chronology.from(temporal))) {
                temporal = LocalDate.from(temporal);
            }
            return of(temporal.get(ChronoField.MONTH_OF_YEAR));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain Month from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText((TemporalField) ChronoField.MONTH_OF_YEAR, style).toFormatter(locale).format(this);
    }

    public boolean isSupported(TemporalField field) {
        boolean z = false;
        if (field instanceof ChronoField) {
            if (field == ChronoField.MONTH_OF_YEAR) {
                z = true;
            }
            return z;
        }
        if (field != null && field.isSupportedBy(this)) {
            z = true;
        }
        return z;
    }

    public ValueRange range(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return field.range();
        }
        return super.range(field);
    }

    public int get(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return getValue();
        }
        return super.get(field);
    }

    public long getLong(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return (long) getValue();
        }
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    public Month plus(long months) {
        return ENUMS[(ordinal() + (((int) (months % 12)) + 12)) % 12];
    }

    public Month minus(long months) {
        return plus(-(months % 12));
    }

    public int length(boolean leapYear) {
        switch (this) {
            case FEBRUARY:
                return leapYear ? 29 : 28;
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    public int minLength() {
        switch (this) {
            case FEBRUARY:
                return 28;
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    public int maxLength() {
        switch (this) {
            case FEBRUARY:
                return 29;
            case APRIL:
            case JUNE:
            case SEPTEMBER:
            case NOVEMBER:
                return 30;
            default:
                return 31;
        }
    }

    public int firstDayOfYear(boolean leapYear) {
        int leap = leapYear;
        switch (this) {
            case FEBRUARY:
                return 32;
            case APRIL:
                return true + ((int) leap);
            case JUNE:
                return 152 + leap;
            case SEPTEMBER:
                return 244 + leap;
            case NOVEMBER:
                return HttpURLConnection.HTTP_USE_PROXY + leap;
            case JANUARY:
                return 1;
            case MARCH:
                return 60 + leap;
            case MAY:
                return 121 + leap;
            case JULY:
                return 182 + leap;
            case AUGUST:
                return 213 + leap;
            case OCTOBER:
                return 274 + leap;
            default:
                return 335 + leap;
        }
    }

    public Month firstMonthOfQuarter() {
        return ENUMS[(ordinal() / 3) * 3];
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return IsoChronology.INSTANCE;
        }
        if (query == TemporalQueries.precision()) {
            return ChronoUnit.MONTHS;
        }
        return super.query(query);
    }

    public Temporal adjustInto(Temporal temporal) {
        if (Chronology.from(temporal).equals(IsoChronology.INSTANCE)) {
            return temporal.with(ChronoField.MONTH_OF_YEAR, (long) getValue());
        }
        throw new DateTimeException("Adjustment only supported on ISO date-time");
    }
}
