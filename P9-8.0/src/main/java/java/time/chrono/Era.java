package java.time.chrono;

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

public interface Era extends TemporalAccessor, TemporalAdjuster {
    int getValue();

    boolean isSupported(TemporalField field) {
        boolean z = false;
        if (field instanceof ChronoField) {
            if (field == ChronoField.ERA) {
                z = true;
            }
            return z;
        }
        if (field != null) {
            z = field.isSupportedBy(this);
        }
        return z;
    }

    ValueRange range(TemporalField field) {
        return super.range(field);
    }

    int get(TemporalField field) {
        if (field == ChronoField.ERA) {
            return getValue();
        }
        return super.get(field);
    }

    long getLong(TemporalField field) {
        if (field == ChronoField.ERA) {
            return (long) getValue();
        }
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.precision()) {
            return ChronoUnit.ERAS;
        }
        return super.query(query);
    }

    Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.ERA, (long) getValue());
    }

    String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText(ChronoField.ERA, style).toFormatter(locale).format(this);
    }
}
