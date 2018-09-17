package java.time.format;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.Chronology;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

final class Parsed implements TemporalAccessor {
    Chronology chrono;
    private ChronoLocalDate date;
    Period excessDays = Period.ZERO;
    final Map<TemporalField, Long> fieldValues = new HashMap();
    boolean leapSecond;
    private ResolverStyle resolverStyle;
    private LocalTime time;
    ZoneId zone;

    Parsed() {
    }

    Parsed copy() {
        Parsed cloned = new Parsed();
        cloned.fieldValues.putAll(this.fieldValues);
        cloned.zone = this.zone;
        cloned.chrono = this.chrono;
        cloned.leapSecond = this.leapSecond;
        return cloned;
    }

    public boolean isSupported(TemporalField field) {
        if (this.fieldValues.containsKey(field) || ((this.date != null && this.date.isSupported(field)) || (this.time != null && this.time.isSupported(field)))) {
            return true;
        }
        boolean isSupportedBy = (field == null || (field instanceof ChronoField)) ? false : field.isSupportedBy(this);
        return isSupportedBy;
    }

    public long getLong(TemporalField field) {
        Objects.requireNonNull((Object) field, "field");
        Long value = (Long) this.fieldValues.get(field);
        if (value != null) {
            return value.longValue();
        }
        if (this.date != null && this.date.isSupported(field)) {
            return this.date.getLong(field);
        }
        if (this.time != null && this.time.isSupported(field)) {
            return this.time.getLong(field);
        }
        if (!(field instanceof ChronoField)) {
            return field.getFrom(this);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    public <R> R query(TemporalQuery<R> query) {
        R r = null;
        if (query == TemporalQueries.zoneId()) {
            return this.zone;
        }
        if (query == TemporalQueries.chronology()) {
            return this.chrono;
        }
        if (query == TemporalQueries.localDate()) {
            if (this.date != null) {
                r = LocalDate.from(this.date);
            }
            return r;
        } else if (query == TemporalQueries.localTime()) {
            return this.time;
        } else {
            if (query == TemporalQueries.zone() || query == TemporalQueries.offset()) {
                return query.queryFrom(this);
            }
            if (query == TemporalQueries.precision()) {
                return null;
            }
            return query.queryFrom(this);
        }
    }

    TemporalAccessor resolve(ResolverStyle resolverStyle, Set<TemporalField> resolverFields) {
        if (resolverFields != null) {
            this.fieldValues.keySet().retainAll(resolverFields);
        }
        this.resolverStyle = resolverStyle;
        resolveFields();
        resolveTimeLenient();
        crossCheck();
        resolvePeriod();
        resolveFractional();
        resolveInstant();
        return this;
    }

    private void resolveFields() {
        resolveInstantFields();
        resolveDateFields();
        resolveTimeFields();
        if (this.fieldValues.size() > 0) {
            int changedCount = 0;
            loop0:
            while (changedCount < 50) {
                for (Entry<TemporalField, Long> entry : this.fieldValues.entrySet()) {
                    TemporalField targetField = (TemporalField) entry.getKey();
                    TemporalAccessor resolvedObject = targetField.resolve(this.fieldValues, this, this.resolverStyle);
                    if (resolvedObject != null) {
                        if (resolvedObject instanceof ChronoZonedDateTime) {
                            ChronoZonedDateTime<?> czdt = (ChronoZonedDateTime) resolvedObject;
                            if (this.zone == null) {
                                this.zone = czdt.getZone();
                            } else if (!this.zone.equals(czdt.getZone())) {
                                throw new DateTimeException("ChronoZonedDateTime must use the effective parsed zone: " + this.zone);
                            }
                            resolvedObject = czdt.toLocalDateTime();
                        }
                        if (resolvedObject instanceof ChronoLocalDateTime) {
                            ChronoLocalDateTime<?> cldt = (ChronoLocalDateTime) resolvedObject;
                            updateCheckConflict(cldt.toLocalTime(), Period.ZERO);
                            updateCheckConflict(cldt.toLocalDate());
                            changedCount++;
                        } else if (resolvedObject instanceof ChronoLocalDate) {
                            updateCheckConflict((ChronoLocalDate) resolvedObject);
                            changedCount++;
                        } else if (resolvedObject instanceof LocalTime) {
                            updateCheckConflict((LocalTime) resolvedObject, Period.ZERO);
                            changedCount++;
                        } else {
                            throw new DateTimeException("Method resolve() can only return ChronoZonedDateTime, ChronoLocalDateTime, ChronoLocalDate or LocalTime");
                        }
                    } else if (!this.fieldValues.containsKey(targetField)) {
                        changedCount++;
                    }
                }
                break loop0;
            }
            if (changedCount == 50) {
                throw new DateTimeException("One of the parsed fields has an incorrectly implemented resolve method");
            } else if (changedCount > 0) {
                resolveInstantFields();
                resolveDateFields();
                resolveTimeFields();
            }
        }
    }

    private void updateCheckConflict(TemporalField targetField, TemporalField changeField, Long changeValue) {
        Object old = (Long) this.fieldValues.put(changeField, changeValue);
        if (old != null && old.longValue() != changeValue.longValue()) {
            throw new DateTimeException("Conflict found: " + changeField + " " + old + " differs from " + changeField + " " + changeValue + " while resolving  " + targetField);
        }
    }

    private void resolveInstantFields() {
        if (!this.fieldValues.containsKey(ChronoField.INSTANT_SECONDS)) {
            return;
        }
        if (this.zone != null) {
            resolveInstantFields0(this.zone);
            return;
        }
        Long offsetSecs = (Long) this.fieldValues.get(ChronoField.OFFSET_SECONDS);
        if (offsetSecs != null) {
            resolveInstantFields0(ZoneOffset.ofTotalSeconds(offsetSecs.intValue()));
        }
    }

    private void resolveInstantFields0(ZoneId selectedZone) {
        ChronoZonedDateTime<?> zdt = this.chrono.zonedDateTime(Instant.ofEpochSecond(((Long) this.fieldValues.remove(ChronoField.INSTANT_SECONDS)).longValue()), selectedZone);
        updateCheckConflict(zdt.toLocalDate());
        updateCheckConflict(ChronoField.INSTANT_SECONDS, ChronoField.SECOND_OF_DAY, Long.valueOf((long) zdt.toLocalTime().toSecondOfDay()));
    }

    private void resolveDateFields() {
        updateCheckConflict(this.chrono.resolveDate(this.fieldValues, this.resolverStyle));
    }

    private void updateCheckConflict(ChronoLocalDate cld) {
        if (this.date != null) {
            if (cld != null && !this.date.equals(cld)) {
                throw new DateTimeException("Conflict found: Fields resolved to two different dates: " + this.date + " " + cld);
            }
        } else if (cld == null) {
        } else {
            if (this.chrono.equals(cld.getChronology())) {
                this.date = cld;
                return;
            }
            throw new DateTimeException("ChronoLocalDate must use the effective parsed chronology: " + this.chrono);
        }
    }

    private void resolveTimeFields() {
        long ch;
        TemporalField temporalField;
        TemporalField temporalField2;
        if (this.fieldValues.containsKey(ChronoField.CLOCK_HOUR_OF_DAY)) {
            ch = ((Long) this.fieldValues.remove(ChronoField.CLOCK_HOUR_OF_DAY)).longValue();
            if (this.resolverStyle == ResolverStyle.STRICT || (this.resolverStyle == ResolverStyle.SMART && ch != 0)) {
                ChronoField.CLOCK_HOUR_OF_DAY.checkValidValue(ch);
            }
            temporalField = ChronoField.CLOCK_HOUR_OF_DAY;
            temporalField2 = ChronoField.HOUR_OF_DAY;
            if (ch == 24) {
                ch = 0;
            }
            updateCheckConflict(temporalField, temporalField2, Long.valueOf(ch));
        }
        if (this.fieldValues.containsKey(ChronoField.CLOCK_HOUR_OF_AMPM)) {
            ch = ((Long) this.fieldValues.remove(ChronoField.CLOCK_HOUR_OF_AMPM)).longValue();
            if (this.resolverStyle == ResolverStyle.STRICT || (this.resolverStyle == ResolverStyle.SMART && ch != 0)) {
                ChronoField.CLOCK_HOUR_OF_AMPM.checkValidValue(ch);
            }
            temporalField = ChronoField.CLOCK_HOUR_OF_AMPM;
            temporalField2 = ChronoField.HOUR_OF_AMPM;
            if (ch == 12) {
                ch = 0;
            }
            updateCheckConflict(temporalField, temporalField2, Long.valueOf(ch));
        }
        if (this.fieldValues.containsKey(ChronoField.AMPM_OF_DAY) && this.fieldValues.containsKey(ChronoField.HOUR_OF_AMPM)) {
            long ap = ((Long) this.fieldValues.remove(ChronoField.AMPM_OF_DAY)).longValue();
            long hap = ((Long) this.fieldValues.remove(ChronoField.HOUR_OF_AMPM)).longValue();
            if (this.resolverStyle == ResolverStyle.LENIENT) {
                updateCheckConflict(ChronoField.AMPM_OF_DAY, ChronoField.HOUR_OF_DAY, Long.valueOf(Math.addExact(Math.multiplyExact(ap, 12), hap)));
            } else {
                ChronoField.AMPM_OF_DAY.checkValidValue(ap);
                ChronoField.HOUR_OF_AMPM.checkValidValue(ap);
                updateCheckConflict(ChronoField.AMPM_OF_DAY, ChronoField.HOUR_OF_DAY, Long.valueOf((12 * ap) + hap));
            }
        }
        if (this.fieldValues.containsKey(ChronoField.NANO_OF_DAY)) {
            long nod = ((Long) this.fieldValues.remove(ChronoField.NANO_OF_DAY)).longValue();
            if (this.resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.NANO_OF_DAY.checkValidValue(nod);
            }
            updateCheckConflict(ChronoField.NANO_OF_DAY, ChronoField.HOUR_OF_DAY, Long.valueOf(nod / 3600000000000L));
            updateCheckConflict(ChronoField.NANO_OF_DAY, ChronoField.MINUTE_OF_HOUR, Long.valueOf((nod / 60000000000L) % 60));
            updateCheckConflict(ChronoField.NANO_OF_DAY, ChronoField.SECOND_OF_MINUTE, Long.valueOf((nod / 1000000000) % 60));
            updateCheckConflict(ChronoField.NANO_OF_DAY, ChronoField.NANO_OF_SECOND, Long.valueOf(nod % 1000000000));
        }
        if (this.fieldValues.containsKey(ChronoField.MICRO_OF_DAY)) {
            long cod = ((Long) this.fieldValues.remove(ChronoField.MICRO_OF_DAY)).longValue();
            if (this.resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.MICRO_OF_DAY.checkValidValue(cod);
            }
            updateCheckConflict(ChronoField.MICRO_OF_DAY, ChronoField.SECOND_OF_DAY, Long.valueOf(cod / 1000000));
            updateCheckConflict(ChronoField.MICRO_OF_DAY, ChronoField.MICRO_OF_SECOND, Long.valueOf(cod % 1000000));
        }
        if (this.fieldValues.containsKey(ChronoField.MILLI_OF_DAY)) {
            long lod = ((Long) this.fieldValues.remove(ChronoField.MILLI_OF_DAY)).longValue();
            if (this.resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.MILLI_OF_DAY.checkValidValue(lod);
            }
            updateCheckConflict(ChronoField.MILLI_OF_DAY, ChronoField.SECOND_OF_DAY, Long.valueOf(lod / 1000));
            updateCheckConflict(ChronoField.MILLI_OF_DAY, ChronoField.MILLI_OF_SECOND, Long.valueOf(lod % 1000));
        }
        if (this.fieldValues.containsKey(ChronoField.SECOND_OF_DAY)) {
            long sod = ((Long) this.fieldValues.remove(ChronoField.SECOND_OF_DAY)).longValue();
            if (this.resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.SECOND_OF_DAY.checkValidValue(sod);
            }
            updateCheckConflict(ChronoField.SECOND_OF_DAY, ChronoField.HOUR_OF_DAY, Long.valueOf(sod / 3600));
            updateCheckConflict(ChronoField.SECOND_OF_DAY, ChronoField.MINUTE_OF_HOUR, Long.valueOf((sod / 60) % 60));
            updateCheckConflict(ChronoField.SECOND_OF_DAY, ChronoField.SECOND_OF_MINUTE, Long.valueOf(sod % 60));
        }
        if (this.fieldValues.containsKey(ChronoField.MINUTE_OF_DAY)) {
            long mod = ((Long) this.fieldValues.remove(ChronoField.MINUTE_OF_DAY)).longValue();
            if (this.resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.MINUTE_OF_DAY.checkValidValue(mod);
            }
            updateCheckConflict(ChronoField.MINUTE_OF_DAY, ChronoField.HOUR_OF_DAY, Long.valueOf(mod / 60));
            updateCheckConflict(ChronoField.MINUTE_OF_DAY, ChronoField.MINUTE_OF_HOUR, Long.valueOf(mod % 60));
        }
        if (this.fieldValues.containsKey(ChronoField.NANO_OF_SECOND)) {
            long nos = ((Long) this.fieldValues.get(ChronoField.NANO_OF_SECOND)).longValue();
            if (this.resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.NANO_OF_SECOND.checkValidValue(nos);
            }
            if (this.fieldValues.containsKey(ChronoField.MICRO_OF_SECOND)) {
                long cos = ((Long) this.fieldValues.remove(ChronoField.MICRO_OF_SECOND)).longValue();
                if (this.resolverStyle != ResolverStyle.LENIENT) {
                    ChronoField.MICRO_OF_SECOND.checkValidValue(cos);
                }
                nos = (1000 * cos) + (nos % 1000);
                updateCheckConflict(ChronoField.MICRO_OF_SECOND, ChronoField.NANO_OF_SECOND, Long.valueOf(nos));
            }
            if (this.fieldValues.containsKey(ChronoField.MILLI_OF_SECOND)) {
                long los = ((Long) this.fieldValues.remove(ChronoField.MILLI_OF_SECOND)).longValue();
                if (this.resolverStyle != ResolverStyle.LENIENT) {
                    ChronoField.MILLI_OF_SECOND.checkValidValue(los);
                }
                updateCheckConflict(ChronoField.MILLI_OF_SECOND, ChronoField.NANO_OF_SECOND, Long.valueOf((1000000 * los) + (nos % 1000000)));
            }
        }
        if (this.fieldValues.containsKey(ChronoField.HOUR_OF_DAY) && this.fieldValues.containsKey(ChronoField.MINUTE_OF_HOUR) && this.fieldValues.containsKey(ChronoField.SECOND_OF_MINUTE) && this.fieldValues.containsKey(ChronoField.NANO_OF_SECOND)) {
            resolveTime(((Long) this.fieldValues.remove(ChronoField.HOUR_OF_DAY)).longValue(), ((Long) this.fieldValues.remove(ChronoField.MINUTE_OF_HOUR)).longValue(), ((Long) this.fieldValues.remove(ChronoField.SECOND_OF_MINUTE)).longValue(), ((Long) this.fieldValues.remove(ChronoField.NANO_OF_SECOND)).longValue());
        }
    }

    private void resolveTimeLenient() {
        if (this.time == null) {
            if (this.fieldValues.containsKey(ChronoField.MILLI_OF_SECOND)) {
                long los = ((Long) this.fieldValues.remove(ChronoField.MILLI_OF_SECOND)).longValue();
                if (this.fieldValues.containsKey(ChronoField.MICRO_OF_SECOND)) {
                    long cos = (1000 * los) + (((Long) this.fieldValues.get(ChronoField.MICRO_OF_SECOND)).longValue() % 1000);
                    updateCheckConflict(ChronoField.MILLI_OF_SECOND, ChronoField.MICRO_OF_SECOND, Long.valueOf(cos));
                    this.fieldValues.remove(ChronoField.MICRO_OF_SECOND);
                    this.fieldValues.put(ChronoField.NANO_OF_SECOND, Long.valueOf(1000 * cos));
                } else {
                    this.fieldValues.put(ChronoField.NANO_OF_SECOND, Long.valueOf(1000000 * los));
                }
            } else if (this.fieldValues.containsKey(ChronoField.MICRO_OF_SECOND)) {
                this.fieldValues.put(ChronoField.NANO_OF_SECOND, Long.valueOf(1000 * ((Long) this.fieldValues.remove(ChronoField.MICRO_OF_SECOND)).longValue()));
            }
            Long hod = (Long) this.fieldValues.get(ChronoField.HOUR_OF_DAY);
            if (hod != null) {
                Long moh = (Long) this.fieldValues.get(ChronoField.MINUTE_OF_HOUR);
                Long som = (Long) this.fieldValues.get(ChronoField.SECOND_OF_MINUTE);
                Long nos = (Long) this.fieldValues.get(ChronoField.NANO_OF_SECOND);
                if ((moh != null || (som == null && nos == null)) && (moh == null || som != null || nos == null)) {
                    resolveTime(hod.longValue(), moh != null ? moh.longValue() : 0, som != null ? som.longValue() : 0, nos != null ? nos.longValue() : 0);
                    this.fieldValues.remove(ChronoField.HOUR_OF_DAY);
                    this.fieldValues.remove(ChronoField.MINUTE_OF_HOUR);
                    this.fieldValues.remove(ChronoField.SECOND_OF_MINUTE);
                    this.fieldValues.remove(ChronoField.NANO_OF_SECOND);
                } else {
                    return;
                }
            }
        }
        if (this.resolverStyle != ResolverStyle.LENIENT && this.fieldValues.size() > 0) {
            for (Entry<TemporalField, Long> entry : this.fieldValues.entrySet()) {
                TemporalField field = (TemporalField) entry.getKey();
                if ((field instanceof ChronoField) && field.isTimeBased()) {
                    ((ChronoField) field).checkValidValue(((Long) entry.getValue()).longValue());
                }
            }
        }
    }

    private void resolveTime(long hod, long moh, long som, long nos) {
        if (this.resolverStyle == ResolverStyle.LENIENT) {
            long totalNanos = Math.addExact(Math.addExact(Math.addExact(Math.multiplyExact(hod, 3600000000000L), Math.multiplyExact(moh, 60000000000L)), Math.multiplyExact(som, 1000000000)), nos);
            updateCheckConflict(LocalTime.ofNanoOfDay(Math.floorMod(totalNanos, 86400000000000L)), Period.ofDays((int) Math.floorDiv(totalNanos, 86400000000000L)));
            return;
        }
        int mohVal = ChronoField.MINUTE_OF_HOUR.checkValidIntValue(moh);
        int nosVal = ChronoField.NANO_OF_SECOND.checkValidIntValue(nos);
        if (this.resolverStyle == ResolverStyle.SMART && hod == 24 && mohVal == 0 && som == 0 && nosVal == 0) {
            updateCheckConflict(LocalTime.MIDNIGHT, Period.ofDays(1));
        } else {
            updateCheckConflict(LocalTime.of(ChronoField.HOUR_OF_DAY.checkValidIntValue(hod), mohVal, ChronoField.SECOND_OF_MINUTE.checkValidIntValue(som), nosVal), Period.ZERO);
        }
    }

    private void resolvePeriod() {
        if (this.date != null && this.time != null && !this.excessDays.isZero()) {
            this.date = this.date.plus(this.excessDays);
            this.excessDays = Period.ZERO;
        }
    }

    private void resolveFractional() {
        if (this.time != null) {
            return;
        }
        if (!this.fieldValues.containsKey(ChronoField.INSTANT_SECONDS) && !this.fieldValues.containsKey(ChronoField.SECOND_OF_DAY) && !this.fieldValues.containsKey(ChronoField.SECOND_OF_MINUTE)) {
            return;
        }
        if (this.fieldValues.containsKey(ChronoField.NANO_OF_SECOND)) {
            long nos = ((Long) this.fieldValues.get(ChronoField.NANO_OF_SECOND)).longValue();
            this.fieldValues.put(ChronoField.MICRO_OF_SECOND, Long.valueOf(nos / 1000));
            this.fieldValues.put(ChronoField.MILLI_OF_SECOND, Long.valueOf(nos / 1000000));
            return;
        }
        this.fieldValues.put(ChronoField.NANO_OF_SECOND, Long.valueOf(0));
        this.fieldValues.put(ChronoField.MICRO_OF_SECOND, Long.valueOf(0));
        this.fieldValues.put(ChronoField.MILLI_OF_SECOND, Long.valueOf(0));
    }

    private void resolveInstant() {
        if (this.date != null && this.time != null) {
            if (this.zone != null) {
                this.fieldValues.put(ChronoField.INSTANT_SECONDS, Long.valueOf(this.date.atTime(this.time).atZone(this.zone).getLong(ChronoField.INSTANT_SECONDS)));
                return;
            }
            Long offsetSecs = (Long) this.fieldValues.get(ChronoField.OFFSET_SECONDS);
            if (offsetSecs != null) {
                this.fieldValues.put(ChronoField.INSTANT_SECONDS, Long.valueOf(this.date.atTime(this.time).atZone(ZoneOffset.ofTotalSeconds(offsetSecs.intValue())).getLong(ChronoField.INSTANT_SECONDS)));
            }
        }
    }

    private void updateCheckConflict(LocalTime timeToSet, Period periodToSet) {
        if (this.time == null) {
            this.time = timeToSet;
            this.excessDays = periodToSet;
        } else if (!this.time.equals(timeToSet)) {
            throw new DateTimeException("Conflict found: Fields resolved to different times: " + this.time + " " + timeToSet);
        } else if (this.excessDays.isZero() || periodToSet.isZero() || this.excessDays.equals(periodToSet)) {
            this.excessDays = periodToSet;
        } else {
            throw new DateTimeException("Conflict found: Fields resolved to different excess periods: " + this.excessDays + " " + periodToSet);
        }
    }

    private void crossCheck() {
        if (this.date != null) {
            crossCheck(this.date);
        }
        if (this.time != null) {
            crossCheck(this.time);
            if (this.date != null && this.fieldValues.size() > 0) {
                crossCheck(this.date.atTime(this.time));
            }
        }
    }

    private void crossCheck(TemporalAccessor target) {
        Iterator<Entry<TemporalField, Long>> it = this.fieldValues.entrySet().iterator();
        while (it.hasNext()) {
            Entry<TemporalField, Long> entry = (Entry) it.next();
            Object field = (TemporalField) entry.getKey();
            if (target.isSupported(field)) {
                try {
                    long val1 = target.getLong(field);
                    long val2 = ((Long) entry.getValue()).longValue();
                    if (val1 != val2) {
                        throw new DateTimeException("Conflict found: Field " + field + " " + val1 + " differs from " + field + " " + val2 + " derived from " + target);
                    }
                    it.remove();
                } catch (RuntimeException e) {
                }
            }
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(64);
        buf.append(this.fieldValues).append(',').append(this.chrono);
        if (this.zone != null) {
            buf.append(',').append(this.zone);
        }
        if (!(this.date == null && this.time == null)) {
            buf.append(" resolved to ");
            if (this.date != null) {
                buf.append(this.date);
                if (this.time != null) {
                    buf.append('T').append(this.time);
                }
            } else {
                buf.append(this.time);
            }
        }
        return buf.toString();
    }
}
