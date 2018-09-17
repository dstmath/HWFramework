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
        return new DateTimeFormatterBuilder().appendText(ChronoField.MONTH_OF_YEAR, style).toFormatter(locale).format(this);
    }

    public boolean isSupported(TemporalField field) {
        boolean z = false;
        if (field instanceof ChronoField) {
            if (field == ChronoField.MONTH_OF_YEAR) {
                z = true;
            }
            return z;
        }
        if (field != null) {
            z = field.isSupportedBy(this);
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
        switch (-getjava-time-MonthSwitchesValues()[ordinal()]) {
            case 1:
            case 7:
            case 10:
            case 12:
                return 30;
            case 4:
                return leapYear ? 29 : 28;
            default:
                return 31;
        }
    }

    public int minLength() {
        switch (-getjava-time-MonthSwitchesValues()[ordinal()]) {
            case 1:
            case 7:
            case 10:
            case 12:
                return 30;
            case 4:
                return 28;
            default:
                return 31;
        }
    }

    public int maxLength() {
        switch (-getjava-time-MonthSwitchesValues()[ordinal()]) {
            case 1:
            case 7:
            case 10:
            case 12:
                return 30;
            case 4:
                return 29;
            default:
                return 31;
        }
    }

    public int firstDayOfYear(boolean leapYear) {
        int leap = leapYear ? 1 : 0;
        switch (-getjava-time-MonthSwitchesValues()[ordinal()]) {
            case 1:
                return leap + 91;
            case 2:
                return leap + 213;
            case 4:
                return 32;
            case 5:
                return 1;
            case 6:
                return leap + 182;
            case 7:
                return leap + 152;
            case 8:
                return leap + 60;
            case 9:
                return leap + 121;
            case 10:
                return leap + HttpURLConnection.HTTP_USE_PROXY;
            case 11:
                return leap + 274;
            case 12:
                return leap + 244;
            default:
                return leap + 335;
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
