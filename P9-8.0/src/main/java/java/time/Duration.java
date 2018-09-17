package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.util.locale.LanguageTag;

public final class Duration implements TemporalAmount, Comparable<Duration>, Serializable {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoUnitSwitchesValues = null;
    private static final BigInteger BI_NANOS_PER_SECOND = BigInteger.valueOf(1000000000);
    private static final Pattern PATTERN = Pattern.compile("([-+]?)P(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?", 2);
    public static final Duration ZERO = new Duration(0, 0);
    private static final long serialVersionUID = 3078945930695997490L;
    private final int nanos;
    private final long seconds;

    private static class DurationUnits {
        static final List<TemporalUnit> UNITS = Collections.unmodifiableList(Arrays.asList(ChronoUnit.SECONDS, ChronoUnit.NANOS));

        private DurationUnits() {
        }
    }

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoUnitSwitchesValues() {
        if (-java-time-temporal-ChronoUnitSwitchesValues != null) {
            return -java-time-temporal-ChronoUnitSwitchesValues;
        }
        int[] iArr = new int[ChronoUnit.values().length];
        try {
            iArr[ChronoUnit.CENTURIES.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoUnit.DAYS.ordinal()] = 6;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoUnit.DECADES.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoUnit.ERAS.ordinal()] = 8;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoUnit.FOREVER.ordinal()] = 9;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoUnit.HALF_DAYS.ordinal()] = 10;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoUnit.HOURS.ordinal()] = 11;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoUnit.MICROS.ordinal()] = 1;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoUnit.MILLENNIA.ordinal()] = 12;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoUnit.MILLIS.ordinal()] = 2;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoUnit.MINUTES.ordinal()] = 13;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoUnit.MONTHS.ordinal()] = 14;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoUnit.NANOS.ordinal()] = 3;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoUnit.SECONDS.ordinal()] = 4;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoUnit.WEEKS.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoUnit.YEARS.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        -java-time-temporal-ChronoUnitSwitchesValues = iArr;
        return iArr;
    }

    public static Duration ofDays(long days) {
        return create(Math.multiplyExact(days, 86400), 0);
    }

    public static Duration ofHours(long hours) {
        return create(Math.multiplyExact(hours, 3600), 0);
    }

    public static Duration ofMinutes(long minutes) {
        return create(Math.multiplyExact(minutes, 60), 0);
    }

    public static Duration ofSeconds(long seconds) {
        return create(seconds, 0);
    }

    public static Duration ofSeconds(long seconds, long nanoAdjustment) {
        return create(Math.addExact(seconds, Math.floorDiv(nanoAdjustment, 1000000000)), (int) Math.floorMod(nanoAdjustment, 1000000000));
    }

    public static Duration ofMillis(long millis) {
        long secs = millis / 1000;
        int mos = (int) (millis % 1000);
        if (mos < 0) {
            mos += 1000;
            secs--;
        }
        return create(secs, 1000000 * mos);
    }

    public static Duration ofNanos(long nanos) {
        long secs = nanos / 1000000000;
        int nos = (int) (nanos % 1000000000);
        if (nos < 0) {
            nos = (int) (((long) nos) + 1000000000);
            secs--;
        }
        return create(secs, nos);
    }

    public static Duration of(long amount, TemporalUnit unit) {
        return ZERO.plus(amount, unit);
    }

    public static Duration from(TemporalAmount amount) {
        Objects.requireNonNull((Object) amount, "amount");
        Duration duration = ZERO;
        for (TemporalUnit unit : amount.getUnits()) {
            duration = duration.plus(amount.get(unit), unit);
        }
        return duration;
    }

    public static Duration parse(CharSequence text) {
        Objects.requireNonNull((Object) text, "text");
        Matcher matcher = PATTERN.matcher(text);
        if (matcher.matches() && !"T".equals(matcher.group(3))) {
            boolean negate = LanguageTag.SEP.equals(matcher.group(1));
            String dayMatch = matcher.group(2);
            String hourMatch = matcher.group(4);
            String minuteMatch = matcher.group(5);
            String secondMatch = matcher.group(6);
            String fractionMatch = matcher.group(7);
            if (!(dayMatch == null && hourMatch == null && minuteMatch == null && secondMatch == null)) {
                long daysAsSecs = parseNumber(text, dayMatch, 86400, "days");
                long hoursAsSecs = parseNumber(text, hourMatch, 3600, "hours");
                long minsAsSecs = parseNumber(text, minuteMatch, 60, "minutes");
                long seconds = parseNumber(text, secondMatch, 1, "seconds");
                try {
                    return create(negate, daysAsSecs, hoursAsSecs, minsAsSecs, seconds, parseFraction(text, fractionMatch, seconds < 0 ? -1 : 1));
                } catch (ArithmeticException ex) {
                    throw ((DateTimeParseException) new DateTimeParseException("Text cannot be parsed to a Duration: overflow", text, 0).initCause(ex));
                }
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a Duration", text, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x000f A:{Splitter: B:3:0x0005, ExcHandler: java.lang.NumberFormatException (r0_0 'ex' java.lang.RuntimeException)} */
    /* JADX WARNING: Missing block: B:6:0x000f, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:8:0x0030, code:
            throw ((java.time.format.DateTimeParseException) new java.time.format.DateTimeParseException("Text cannot be parsed to a Duration: " + r9, r6, 0).initCause(r0));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static long parseNumber(CharSequence text, String parsed, int multiplier, String errorText) {
        if (parsed == null) {
            return 0;
        }
        try {
            return Math.multiplyExact(Long.parseLong(parsed), (long) multiplier);
        } catch (RuntimeException ex) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x002b A:{Splitter: B:5:0x000a, ExcHandler: java.lang.NumberFormatException (r0_0 'ex' java.lang.RuntimeException)} */
    /* JADX WARNING: Missing block: B:9:0x002b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x003a, code:
            throw ((java.time.format.DateTimeParseException) new java.time.format.DateTimeParseException("Text cannot be parsed to a Duration: fraction", r5, 0).initCause(r0));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int parseFraction(CharSequence text, String parsed, int negate) {
        if (parsed == null || parsed.length() == 0) {
            return 0;
        }
        try {
            return Integer.parseInt((parsed + "000000000").substring(0, 9)) * negate;
        } catch (RuntimeException ex) {
        }
    }

    private static Duration create(boolean negate, long daysAsSecs, long hoursAsSecs, long minsAsSecs, long secs, int nanos) {
        long seconds = Math.addExact(daysAsSecs, Math.addExact(hoursAsSecs, Math.addExact(minsAsSecs, secs)));
        if (negate) {
            return ofSeconds(seconds, (long) nanos).negated();
        }
        return ofSeconds(seconds, (long) nanos);
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x000f A:{Splitter: B:1:0x0004, ExcHandler: java.time.DateTimeException (e java.time.DateTimeException)} */
    /* JADX WARNING: Missing block: B:5:0x0010, code:
            r4 = r14.until(r15, java.time.temporal.ChronoUnit.SECONDS);
     */
    /* JADX WARNING: Missing block: B:8:0x0021, code:
            r2 = r15.getLong(java.time.temporal.ChronoField.NANO_OF_SECOND) - r14.getLong(java.time.temporal.ChronoField.NANO_OF_SECOND);
     */
    /* JADX WARNING: Missing block: B:9:0x0026, code:
            if (r4 <= 0) goto L_0x0032;
     */
    /* JADX WARNING: Missing block: B:12:0x002c, code:
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:19:0x003a, code:
            r4 = r4 - 1;
     */
    /* JADX WARNING: Missing block: B:21:0x003d, code:
            r2 = 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Duration between(Temporal startInclusive, Temporal endExclusive) {
        try {
            return ofNanos(startInclusive.until(endExclusive, ChronoUnit.NANOS));
        } catch (DateTimeException e) {
        }
        return ofSeconds(secs, nanos);
    }

    private static Duration create(long seconds, int nanoAdjustment) {
        if ((((long) nanoAdjustment) | seconds) == 0) {
            return ZERO;
        }
        return new Duration(seconds, nanoAdjustment);
    }

    private Duration(long seconds, int nanos) {
        this.seconds = seconds;
        this.nanos = nanos;
    }

    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.SECONDS) {
            return this.seconds;
        }
        if (unit == ChronoUnit.NANOS) {
            return (long) this.nanos;
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    public List<TemporalUnit> getUnits() {
        return DurationUnits.UNITS;
    }

    public boolean isZero() {
        return (this.seconds | ((long) this.nanos)) == 0;
    }

    public boolean isNegative() {
        return this.seconds < 0;
    }

    public long getSeconds() {
        return this.seconds;
    }

    public int getNano() {
        return this.nanos;
    }

    public Duration withSeconds(long seconds) {
        return create(seconds, this.nanos);
    }

    public Duration withNanos(int nanoOfSecond) {
        ChronoField.NANO_OF_SECOND.checkValidIntValue((long) nanoOfSecond);
        return create(this.seconds, nanoOfSecond);
    }

    public Duration plus(Duration duration) {
        return plus(duration.getSeconds(), (long) duration.getNano());
    }

    public Duration plus(long amountToAdd, TemporalUnit unit) {
        Objects.requireNonNull((Object) unit, "unit");
        if (unit == ChronoUnit.DAYS) {
            return plus(Math.multiplyExact(amountToAdd, 86400), 0);
        }
        if (unit.isDurationEstimated()) {
            throw new UnsupportedTemporalTypeException("Unit must not have an estimated duration");
        } else if (amountToAdd == 0) {
            return this;
        } else {
            if (unit instanceof ChronoUnit) {
                switch (-getjava-time-temporal-ChronoUnitSwitchesValues()[((ChronoUnit) unit).ordinal()]) {
                    case 1:
                        return plusSeconds((amountToAdd / 1000000000) * 1000).plusNanos((amountToAdd % 1000000000) * 1000);
                    case 2:
                        return plusMillis(amountToAdd);
                    case 3:
                        return plusNanos(amountToAdd);
                    case 4:
                        return plusSeconds(amountToAdd);
                    default:
                        return plusSeconds(Math.multiplyExact(unit.getDuration().seconds, amountToAdd));
                }
            }
            Duration duration = unit.getDuration().multipliedBy(amountToAdd);
            return plusSeconds(duration.getSeconds()).plusNanos((long) duration.getNano());
        }
    }

    public Duration plusDays(long daysToAdd) {
        return plus(Math.multiplyExact(daysToAdd, 86400), 0);
    }

    public Duration plusHours(long hoursToAdd) {
        return plus(Math.multiplyExact(hoursToAdd, 3600), 0);
    }

    public Duration plusMinutes(long minutesToAdd) {
        return plus(Math.multiplyExact(minutesToAdd, 60), 0);
    }

    public Duration plusSeconds(long secondsToAdd) {
        return plus(secondsToAdd, 0);
    }

    public Duration plusMillis(long millisToAdd) {
        return plus(millisToAdd / 1000, (millisToAdd % 1000) * 1000000);
    }

    public Duration plusNanos(long nanosToAdd) {
        return plus(0, nanosToAdd);
    }

    private Duration plus(long secondsToAdd, long nanosToAdd) {
        if ((secondsToAdd | nanosToAdd) == 0) {
            return this;
        }
        return ofSeconds(Math.addExact(Math.addExact(this.seconds, secondsToAdd), nanosToAdd / 1000000000), ((long) this.nanos) + (nanosToAdd % 1000000000));
    }

    public Duration minus(Duration duration) {
        long secsToSubtract = duration.getSeconds();
        int nanosToSubtract = duration.getNano();
        if (secsToSubtract == Long.MIN_VALUE) {
            return plus((long) Long.MAX_VALUE, (long) (-nanosToSubtract)).plus(1, 0);
        }
        return plus(-secsToSubtract, (long) (-nanosToSubtract));
    }

    public Duration minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? plus((long) Long.MAX_VALUE, unit).plus(1, unit) : plus(-amountToSubtract, unit);
    }

    public Duration minusDays(long daysToSubtract) {
        return daysToSubtract == Long.MIN_VALUE ? plusDays(Long.MAX_VALUE).plusDays(1) : plusDays(-daysToSubtract);
    }

    public Duration minusHours(long hoursToSubtract) {
        return hoursToSubtract == Long.MIN_VALUE ? plusHours(Long.MAX_VALUE).plusHours(1) : plusHours(-hoursToSubtract);
    }

    public Duration minusMinutes(long minutesToSubtract) {
        return minutesToSubtract == Long.MIN_VALUE ? plusMinutes(Long.MAX_VALUE).plusMinutes(1) : plusMinutes(-minutesToSubtract);
    }

    public Duration minusSeconds(long secondsToSubtract) {
        return secondsToSubtract == Long.MIN_VALUE ? plusSeconds(Long.MAX_VALUE).plusSeconds(1) : plusSeconds(-secondsToSubtract);
    }

    public Duration minusMillis(long millisToSubtract) {
        return millisToSubtract == Long.MIN_VALUE ? plusMillis(Long.MAX_VALUE).plusMillis(1) : plusMillis(-millisToSubtract);
    }

    public Duration minusNanos(long nanosToSubtract) {
        return nanosToSubtract == Long.MIN_VALUE ? plusNanos(Long.MAX_VALUE).plusNanos(1) : plusNanos(-nanosToSubtract);
    }

    public Duration multipliedBy(long multiplicand) {
        if (multiplicand == 0) {
            return ZERO;
        }
        if (multiplicand == 1) {
            return this;
        }
        return create(toSeconds().multiply(BigDecimal.valueOf(multiplicand)));
    }

    public Duration dividedBy(long divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        } else if (divisor == 1) {
            return this;
        } else {
            return create(toSeconds().divide(BigDecimal.valueOf(divisor), RoundingMode.DOWN));
        }
    }

    private BigDecimal toSeconds() {
        return BigDecimal.valueOf(this.seconds).add(BigDecimal.valueOf((long) this.nanos, 9));
    }

    private static Duration create(BigDecimal seconds) {
        Object nanos = seconds.movePointRight(9).toBigIntegerExact();
        BigInteger[] divRem = nanos.divideAndRemainder(BI_NANOS_PER_SECOND);
        if (divRem[0].bitLength() <= 63) {
            return ofSeconds(divRem[0].longValue(), (long) divRem[1].intValue());
        }
        throw new ArithmeticException("Exceeds capacity of Duration: " + nanos);
    }

    public Duration negated() {
        return multipliedBy(-1);
    }

    public Duration abs() {
        return isNegative() ? negated() : this;
    }

    public Temporal addTo(Temporal temporal) {
        if (this.seconds != 0) {
            temporal = temporal.plus(this.seconds, ChronoUnit.SECONDS);
        }
        if (this.nanos != 0) {
            return temporal.plus((long) this.nanos, ChronoUnit.NANOS);
        }
        return temporal;
    }

    public Temporal subtractFrom(Temporal temporal) {
        if (this.seconds != 0) {
            temporal = temporal.minus(this.seconds, ChronoUnit.SECONDS);
        }
        if (this.nanos != 0) {
            return temporal.minus((long) this.nanos, ChronoUnit.NANOS);
        }
        return temporal;
    }

    public long toDays() {
        return this.seconds / 86400;
    }

    public long toHours() {
        return this.seconds / 3600;
    }

    public long toMinutes() {
        return this.seconds / 60;
    }

    public long toMillis() {
        return Math.addExact(Math.multiplyExact(this.seconds, 1000), (long) (this.nanos / 1000000));
    }

    public long toNanos() {
        return Math.addExact(Math.multiplyExact(this.seconds, 1000000000), (long) this.nanos);
    }

    public int compareTo(Duration otherDuration) {
        int cmp = Long.compare(this.seconds, otherDuration.seconds);
        if (cmp != 0) {
            return cmp;
        }
        return this.nanos - otherDuration.nanos;
    }

    public boolean equals(Object otherDuration) {
        boolean z = true;
        if (this == otherDuration) {
            return true;
        }
        if (!(otherDuration instanceof Duration)) {
            return false;
        }
        Duration other = (Duration) otherDuration;
        if (this.seconds != other.seconds) {
            z = false;
        } else if (this.nanos != other.nanos) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((int) (this.seconds ^ (this.seconds >>> 32))) + (this.nanos * 51);
    }

    public String toString() {
        if (this == ZERO) {
            return "PT0S";
        }
        long hours = this.seconds / 3600;
        int minutes = (int) ((this.seconds % 3600) / 60);
        int secs = (int) (this.seconds % 60);
        StringBuilder buf = new StringBuilder(24);
        buf.append("PT");
        if (hours != 0) {
            buf.append(hours).append('H');
        }
        if (minutes != 0) {
            buf.append(minutes).append('M');
        }
        if (secs == 0 && this.nanos == 0 && buf.length() > 2) {
            return buf.toString();
        }
        if (secs >= 0 || this.nanos <= 0) {
            buf.append(secs);
        } else if (secs == -1) {
            buf.append("-0");
        } else {
            buf.append(secs + 1);
        }
        if (this.nanos > 0) {
            int pos = buf.length();
            if (secs < 0) {
                buf.append(2000000000 - ((long) this.nanos));
            } else {
                buf.append(((long) this.nanos) + 1000000000);
            }
            while (buf.charAt(buf.length() - 1) == '0') {
                buf.setLength(buf.length() - 1);
            }
            buf.setCharAt(pos, '.');
        }
        buf.append('S');
        return buf.toString();
    }

    private Object writeReplace() {
        return new Ser((byte) 1, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeLong(this.seconds);
        out.writeInt(this.nanos);
    }

    static Duration readExternal(DataInput in) throws IOException {
        return ofSeconds(in.readLong(), (long) in.readInt());
    }
}
