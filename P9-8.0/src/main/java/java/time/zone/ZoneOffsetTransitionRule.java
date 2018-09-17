package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.chrono.IsoChronology;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import sun.util.logging.PlatformLogger;

public final class ZoneOffsetTransitionRule implements Serializable {
    private static final long serialVersionUID = 6889046316657758795L;
    private final byte dom;
    private final DayOfWeek dow;
    private final Month month;
    private final ZoneOffset offsetAfter;
    private final ZoneOffset offsetBefore;
    private final ZoneOffset standardOffset;
    private final LocalTime time;
    private final TimeDefinition timeDefinition;
    private final boolean timeEndOfDay;

    public enum TimeDefinition {
        UTC,
        WALL,
        STANDARD;

        public LocalDateTime createDateTime(LocalDateTime dateTime, ZoneOffset standardOffset, ZoneOffset wallOffset) {
            switch (-getjava-time-zone-ZoneOffsetTransitionRule$TimeDefinitionSwitchesValues()[ordinal()]) {
                case 1:
                    return dateTime.plusSeconds((long) (wallOffset.getTotalSeconds() - standardOffset.getTotalSeconds()));
                case 2:
                    return dateTime.plusSeconds((long) (wallOffset.getTotalSeconds() - ZoneOffset.UTC.getTotalSeconds()));
                default:
                    return dateTime;
            }
        }
    }

    public static ZoneOffsetTransitionRule of(Month month, int dayOfMonthIndicator, DayOfWeek dayOfWeek, LocalTime time, boolean timeEndOfDay, TimeDefinition timeDefnition, ZoneOffset standardOffset, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        Objects.requireNonNull((Object) month, "month");
        Objects.requireNonNull((Object) time, "time");
        Objects.requireNonNull((Object) timeDefnition, "timeDefnition");
        Objects.requireNonNull((Object) standardOffset, "standardOffset");
        Objects.requireNonNull((Object) offsetBefore, "offsetBefore");
        Objects.requireNonNull((Object) offsetAfter, "offsetAfter");
        if (dayOfMonthIndicator < -28 || dayOfMonthIndicator > 31 || dayOfMonthIndicator == 0) {
            throw new IllegalArgumentException("Day of month indicator must be between -28 and 31 inclusive excluding zero");
        } else if (!timeEndOfDay || time.equals(LocalTime.MIDNIGHT)) {
            return new ZoneOffsetTransitionRule(month, dayOfMonthIndicator, dayOfWeek, time, timeEndOfDay, timeDefnition, standardOffset, offsetBefore, offsetAfter);
        } else {
            throw new IllegalArgumentException("Time must be midnight when end of day flag is true");
        }
    }

    ZoneOffsetTransitionRule(Month month, int dayOfMonthIndicator, DayOfWeek dayOfWeek, LocalTime time, boolean timeEndOfDay, TimeDefinition timeDefnition, ZoneOffset standardOffset, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        this.month = month;
        this.dom = (byte) dayOfMonthIndicator;
        this.dow = dayOfWeek;
        this.time = time;
        this.timeEndOfDay = timeEndOfDay;
        this.timeDefinition = timeDefnition;
        this.standardOffset = standardOffset;
        this.offsetBefore = offsetBefore;
        this.offsetAfter = offsetAfter;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 3, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        int timeSecs = this.timeEndOfDay ? 86400 : this.time.toSecondOfDay();
        int stdOffset = this.standardOffset.getTotalSeconds();
        int beforeDiff = this.offsetBefore.getTotalSeconds() - stdOffset;
        int afterDiff = this.offsetAfter.getTotalSeconds() - stdOffset;
        int timeByte = timeSecs % 3600 == 0 ? this.timeEndOfDay ? 24 : this.time.getHour() : 31;
        int stdOffsetByte = stdOffset % PlatformLogger.WARNING == 0 ? (stdOffset / PlatformLogger.WARNING) + 128 : 255;
        int beforeByte = (beforeDiff == 0 || beforeDiff == 1800 || beforeDiff == 3600) ? beforeDiff / 1800 : 3;
        int afterByte = (afterDiff == 0 || afterDiff == 1800 || afterDiff == 3600) ? afterDiff / 1800 : 3;
        out.writeInt((((((((this.month.getValue() << 28) + ((this.dom + 32) << 22)) + ((this.dow == null ? 0 : this.dow.getValue()) << 19)) + (timeByte << 14)) + (this.timeDefinition.ordinal() << 12)) + (stdOffsetByte << 4)) + (beforeByte << 2)) + afterByte);
        if (timeByte == 31) {
            out.writeInt(timeSecs);
        }
        if (stdOffsetByte == 255) {
            out.writeInt(stdOffset);
        }
        if (beforeByte == 3) {
            out.writeInt(this.offsetBefore.getTotalSeconds());
        }
        if (afterByte == 3) {
            out.writeInt(this.offsetAfter.getTotalSeconds());
        }
    }

    static ZoneOffsetTransitionRule readExternal(DataInput in) throws IOException {
        boolean z;
        int data = in.readInt();
        Month month = Month.of(data >>> 28);
        int dom = ((264241152 & data) >>> 22) - 32;
        int dowByte = (3670016 & data) >>> 19;
        DayOfWeek dow = dowByte == 0 ? null : DayOfWeek.of(dowByte);
        int timeByte = (507904 & data) >>> 14;
        TimeDefinition defn = TimeDefinition.values()[(data & 12288) >>> 12];
        int stdByte = (data & 4080) >>> 4;
        int beforeByte = (data & 12) >>> 2;
        int afterByte = data & 3;
        LocalTime time = timeByte == 31 ? LocalTime.ofSecondOfDay((long) in.readInt()) : LocalTime.of(timeByte % 24, 0);
        ZoneOffset std = stdByte == 255 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds((stdByte - 128) * PlatformLogger.WARNING);
        ZoneOffset before = beforeByte == 3 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(std.getTotalSeconds() + (beforeByte * 1800));
        ZoneOffset after = afterByte == 3 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(std.getTotalSeconds() + (afterByte * 1800));
        if (timeByte == 24) {
            z = true;
        } else {
            z = false;
        }
        return of(month, dom, dow, time, z, defn, std, before, after);
    }

    public Month getMonth() {
        return this.month;
    }

    public int getDayOfMonthIndicator() {
        return this.dom;
    }

    public DayOfWeek getDayOfWeek() {
        return this.dow;
    }

    public LocalTime getLocalTime() {
        return this.time;
    }

    public boolean isMidnightEndOfDay() {
        return this.timeEndOfDay;
    }

    public TimeDefinition getTimeDefinition() {
        return this.timeDefinition;
    }

    public ZoneOffset getStandardOffset() {
        return this.standardOffset;
    }

    public ZoneOffset getOffsetBefore() {
        return this.offsetBefore;
    }

    public ZoneOffset getOffsetAfter() {
        return this.offsetAfter;
    }

    public ZoneOffsetTransition createTransition(int year) {
        LocalDate date;
        if (this.dom < (byte) 0) {
            date = LocalDate.of(year, this.month, (this.month.length(IsoChronology.INSTANCE.isLeapYear((long) year)) + 1) + this.dom);
            if (this.dow != null) {
                date = date.with(TemporalAdjusters.previousOrSame(this.dow));
            }
        } else {
            date = LocalDate.of(year, this.month, this.dom);
            if (this.dow != null) {
                date = date.with(TemporalAdjusters.nextOrSame(this.dow));
            }
        }
        if (this.timeEndOfDay) {
            date = date.plusDays(1);
        }
        return new ZoneOffsetTransition(this.timeDefinition.createDateTime(LocalDateTime.of(date, this.time), this.standardOffset, this.offsetBefore), this.offsetBefore, this.offsetAfter);
    }

    public boolean equals(Object otherRule) {
        boolean z = false;
        if (otherRule == this) {
            return true;
        }
        if (!(otherRule instanceof ZoneOffsetTransitionRule)) {
            return false;
        }
        ZoneOffsetTransitionRule other = (ZoneOffsetTransitionRule) otherRule;
        if (this.month == other.month && this.dom == other.dom && this.dow == other.dow && this.timeDefinition == other.timeDefinition && this.time.equals(other.time) && this.timeEndOfDay == other.timeEndOfDay && this.standardOffset.equals(other.standardOffset) && this.offsetBefore.equals(other.offsetBefore)) {
            z = this.offsetAfter.equals(other.offsetAfter);
        }
        return z;
    }

    public int hashCode() {
        return ((this.standardOffset.hashCode() ^ ((((this.dow == null ? 7 : this.dow.ordinal()) << 2) + (((this.dom + 32) << 5) + ((((this.timeEndOfDay ? 1 : 0) + this.time.toSecondOfDay()) << 15) + (this.month.ordinal() << 11)))) + this.timeDefinition.ordinal())) ^ this.offsetBefore.hashCode()) ^ this.offsetAfter.hashCode();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("TransitionRule[").append(this.offsetBefore.compareTo(this.offsetAfter) > 0 ? "Gap " : "Overlap ").append(this.offsetBefore).append(" to ").append(this.offsetAfter).append(", ");
        if (this.dow == null) {
            buf.append(this.month.name()).append(' ').append(this.dom);
        } else if (this.dom == (byte) -1) {
            buf.append(this.dow.name()).append(" on or before last day of ").append(this.month.name());
        } else if (this.dom < (byte) 0) {
            buf.append(this.dow.name()).append(" on or before last day minus ").append((-this.dom) - 1).append(" of ").append(this.month.name());
        } else {
            buf.append(this.dow.name()).append(" on or after ").append(this.month.name()).append(' ').append(this.dom);
        }
        buf.append(" at ").append(this.timeEndOfDay ? "24:00" : this.time.toString()).append(" ").append(this.timeDefinition).append(", standard offset ").append(this.standardOffset).append(']');
        return buf.toString();
    }
}
