package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ZoneOffsetTransition implements Comparable<ZoneOffsetTransition>, Serializable {
    private static final long serialVersionUID = -6946044323557704546L;
    private final ZoneOffset offsetAfter;
    private final ZoneOffset offsetBefore;
    private final LocalDateTime transition;

    public static ZoneOffsetTransition of(LocalDateTime transition, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        Objects.requireNonNull((Object) transition, "transition");
        Objects.requireNonNull((Object) offsetBefore, "offsetBefore");
        Objects.requireNonNull((Object) offsetAfter, "offsetAfter");
        if (offsetBefore.equals(offsetAfter)) {
            throw new IllegalArgumentException("Offsets must not be equal");
        } else if (transition.getNano() == 0) {
            return new ZoneOffsetTransition(transition, offsetBefore, offsetAfter);
        } else {
            throw new IllegalArgumentException("Nano-of-second must be zero");
        }
    }

    ZoneOffsetTransition(LocalDateTime transition, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        this.transition = transition;
        this.offsetBefore = offsetBefore;
        this.offsetAfter = offsetAfter;
    }

    ZoneOffsetTransition(long epochSecond, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        this.transition = LocalDateTime.ofEpochSecond(epochSecond, 0, offsetBefore);
        this.offsetBefore = offsetBefore;
        this.offsetAfter = offsetAfter;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 2, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        Ser.writeEpochSec(toEpochSecond(), out);
        Ser.writeOffset(this.offsetBefore, out);
        Ser.writeOffset(this.offsetAfter, out);
    }

    static ZoneOffsetTransition readExternal(DataInput in) throws IOException {
        long epochSecond = Ser.readEpochSec(in);
        ZoneOffset before = Ser.readOffset(in);
        ZoneOffset after = Ser.readOffset(in);
        if (!before.equals(after)) {
            return new ZoneOffsetTransition(epochSecond, before, after);
        }
        throw new IllegalArgumentException("Offsets must not be equal");
    }

    public Instant getInstant() {
        return this.transition.toInstant(this.offsetBefore);
    }

    public long toEpochSecond() {
        return this.transition.toEpochSecond(this.offsetBefore);
    }

    public LocalDateTime getDateTimeBefore() {
        return this.transition;
    }

    public LocalDateTime getDateTimeAfter() {
        return this.transition.plusSeconds((long) getDurationSeconds());
    }

    public ZoneOffset getOffsetBefore() {
        return this.offsetBefore;
    }

    public ZoneOffset getOffsetAfter() {
        return this.offsetAfter;
    }

    public Duration getDuration() {
        return Duration.ofSeconds((long) getDurationSeconds());
    }

    private int getDurationSeconds() {
        return getOffsetAfter().getTotalSeconds() - getOffsetBefore().getTotalSeconds();
    }

    public boolean isGap() {
        return getOffsetAfter().getTotalSeconds() > getOffsetBefore().getTotalSeconds();
    }

    public boolean isOverlap() {
        return getOffsetAfter().getTotalSeconds() < getOffsetBefore().getTotalSeconds();
    }

    public boolean isValidOffset(ZoneOffset offset) {
        if (isGap()) {
            return false;
        }
        return !getOffsetBefore().equals(offset) ? getOffsetAfter().equals(offset) : true;
    }

    List<ZoneOffset> getValidOffsets() {
        if (isGap()) {
            return Collections.emptyList();
        }
        return Arrays.asList(getOffsetBefore(), getOffsetAfter());
    }

    public int compareTo(ZoneOffsetTransition transition) {
        return getInstant().compareTo(transition.getInstant());
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (other == this) {
            return true;
        }
        if (!(other instanceof ZoneOffsetTransition)) {
            return false;
        }
        ZoneOffsetTransition d = (ZoneOffsetTransition) other;
        if (this.transition.equals(d.transition) && this.offsetBefore.equals(d.offsetBefore)) {
            z = this.offsetAfter.equals(d.offsetAfter);
        }
        return z;
    }

    public int hashCode() {
        return (this.transition.hashCode() ^ this.offsetBefore.hashCode()) ^ Integer.rotateLeft(this.offsetAfter.hashCode(), 16);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Transition[").append(isGap() ? "Gap" : "Overlap").append(" at ").append(this.transition).append(this.offsetBefore).append(" to ").append(this.offsetAfter).append(']');
        return buf.toString();
    }
}
