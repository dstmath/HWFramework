package java.time.temporal;

import java.time.DateTimeException;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.format.ResolverStyle;
import java.util.Map;

public final class JulianFields {
    public static final TemporalField JULIAN_DAY = Field.JULIAN_DAY;
    private static final long JULIAN_DAY_OFFSET = 2440588;
    public static final TemporalField MODIFIED_JULIAN_DAY = Field.MODIFIED_JULIAN_DAY;
    public static final TemporalField RATA_DIE = Field.RATA_DIE;

    private enum Field implements TemporalField {
        JULIAN_DAY("JulianDay", ChronoUnit.DAYS, ChronoUnit.FOREVER, JulianFields.JULIAN_DAY_OFFSET),
        MODIFIED_JULIAN_DAY("ModifiedJulianDay", ChronoUnit.DAYS, ChronoUnit.FOREVER, 40587),
        RATA_DIE("RataDie", ChronoUnit.DAYS, ChronoUnit.FOREVER, 719163);
        
        private static final long serialVersionUID = -7501623920830201812L;
        private final transient TemporalUnit baseUnit;
        private final transient String name;
        private final transient long offset;
        private final transient ValueRange range;
        private final transient TemporalUnit rangeUnit;

        private Field(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit, long offset) {
            this.name = name;
            this.baseUnit = baseUnit;
            this.rangeUnit = rangeUnit;
            this.range = ValueRange.of(-365243219162L + offset, 365241780471L + offset);
            this.offset = offset;
        }

        public TemporalUnit getBaseUnit() {
            return this.baseUnit;
        }

        public TemporalUnit getRangeUnit() {
            return this.rangeUnit;
        }

        public boolean isDateBased() {
            return true;
        }

        public boolean isTimeBased() {
            return false;
        }

        public ValueRange range() {
            return this.range;
        }

        public boolean isSupportedBy(TemporalAccessor temporal) {
            return temporal.isSupported(ChronoField.EPOCH_DAY);
        }

        public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
            if (isSupportedBy(temporal)) {
                return range();
            }
            throw new DateTimeException("Unsupported field: " + this);
        }

        public long getFrom(TemporalAccessor temporal) {
            return temporal.getLong(ChronoField.EPOCH_DAY) + this.offset;
        }

        public <R extends Temporal> R adjustInto(R temporal, long newValue) {
            if (range().isValidValue(newValue)) {
                return temporal.with(ChronoField.EPOCH_DAY, Math.subtractExact(newValue, this.offset));
            }
            throw new DateTimeException("Invalid value: " + this.name + " " + newValue);
        }

        public ChronoLocalDate resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
            long value = ((Long) fieldValues.remove(this)).longValue();
            Chronology chrono = Chronology.from(partialTemporal);
            if (resolverStyle == ResolverStyle.LENIENT) {
                return chrono.dateEpochDay(Math.subtractExact(value, this.offset));
            }
            range().checkValidValue(value, this);
            return chrono.dateEpochDay(value - this.offset);
        }

        public String toString() {
            return this.name;
        }
    }

    private JulianFields() {
        throw new AssertionError((Object) "Not instantiable");
    }
}
