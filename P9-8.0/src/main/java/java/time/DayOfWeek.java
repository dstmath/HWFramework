package java.time;

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

public enum DayOfWeek implements TemporalAccessor, TemporalAdjuster {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;
    
    private static final DayOfWeek[] ENUMS = null;

    static {
        ENUMS = values();
    }

    public static DayOfWeek of(int dayOfWeek) {
        if (dayOfWeek >= 1 && dayOfWeek <= 7) {
            return ENUMS[dayOfWeek - 1];
        }
        throw new DateTimeException("Invalid value for DayOfWeek: " + dayOfWeek);
    }

    public static DayOfWeek from(TemporalAccessor temporal) {
        if (temporal instanceof DayOfWeek) {
            return (DayOfWeek) temporal;
        }
        try {
            return of(temporal.get(ChronoField.DAY_OF_WEEK));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain DayOfWeek from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText(ChronoField.DAY_OF_WEEK, style).toFormatter(locale).format(this);
    }

    public boolean isSupported(TemporalField field) {
        boolean z = false;
        if (field instanceof ChronoField) {
            if (field == ChronoField.DAY_OF_WEEK) {
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
        if (field == ChronoField.DAY_OF_WEEK) {
            return field.range();
        }
        return super.range(field);
    }

    public int get(TemporalField field) {
        if (field == ChronoField.DAY_OF_WEEK) {
            return getValue();
        }
        return super.get(field);
    }

    public long getLong(TemporalField field) {
        if (field == ChronoField.DAY_OF_WEEK) {
            return (long) getValue();
        }
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    public DayOfWeek plus(long days) {
        return ENUMS[(ordinal() + (((int) (days % 7)) + 7)) % 7];
    }

    public DayOfWeek minus(long days) {
        return plus(-(days % 7));
    }

    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.precision()) {
            return ChronoUnit.DAYS;
        }
        return super.query(query);
    }

    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.DAY_OF_WEEK, (long) getValue());
    }
}
