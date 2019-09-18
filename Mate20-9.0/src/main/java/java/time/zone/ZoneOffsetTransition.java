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

    public static ZoneOffsetTransition of(LocalDateTime transition2, ZoneOffset offsetBefore2, ZoneOffset offsetAfter2) {
        Objects.requireNonNull(transition2, "transition");
        Objects.requireNonNull(offsetBefore2, "offsetBefore");
        Objects.requireNonNull(offsetAfter2, "offsetAfter");
        if (offsetBefore2.equals(offsetAfter2)) {
            throw new IllegalArgumentException("Offsets must not be equal");
        } else if (transition2.getNano() == 0) {
            return new ZoneOffsetTransition(transition2, offsetBefore2, offsetAfter2);
        } else {
            throw new IllegalArgumentException("Nano-of-second must be zero");
        }
    }

    ZoneOffsetTransition(LocalDateTime transition2, ZoneOffset offsetBefore2, ZoneOffset offsetAfter2) {
        this.transition = transition2;
        this.offsetBefore = offsetBefore2;
        this.offsetAfter = offsetAfter2;
    }

    ZoneOffsetTransition(long epochSecond, ZoneOffset offsetBefore2, ZoneOffset offsetAfter2) {
        this.transition = LocalDateTime.ofEpochSecond(epochSecond, 0, offsetBefore2);
        this.offsetBefore = offsetBefore2;
        this.offsetAfter = offsetAfter2;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser((byte) 2, this);
    }

    /* access modifiers changed from: package-private */
    public void writeExternal(DataOutput out) throws IOException {
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
        return getOffsetBefore().equals(offset) || getOffsetAfter().equals(offset);
    }

    /* access modifiers changed from: package-private */
    public List<ZoneOffset> getValidOffsets() {
        if (isGap()) {
            return Collections.emptyList();
        }
        return Arrays.asList(getOffsetBefore(), getOffsetAfter());
    }

    public int compareTo(ZoneOffsetTransition transition2) {
        return getInstant().compareTo(transition2.getInstant());
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == this) {
            return true;
        }
        if (!(other instanceof ZoneOffsetTransition)) {
            return false;
        }
        ZoneOffsetTransition d = (ZoneOffsetTransition) other;
        if (!this.transition.equals(d.transition) || !this.offsetBefore.equals(d.offsetBefore) || !this.offsetAfter.equals(d.offsetAfter)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (this.transition.hashCode() ^ this.offsetBefore.hashCode()) ^ Integer.rotateLeft(this.offsetAfter.hashCode(), 16);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Transition[");
        buf.append(isGap() ? "Gap" : "Overlap");
        buf.append(" at ");
        buf.append((Object) this.transition);
        buf.append((Object) this.offsetBefore);
        buf.append(" to ");
        buf.append((Object) this.offsetAfter);
        buf.append(']');
        return buf.toString();
    }
}
