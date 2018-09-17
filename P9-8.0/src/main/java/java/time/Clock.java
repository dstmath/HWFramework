package java.time;

import java.io.Serializable;
import java.util.Objects;

public abstract class Clock {

    static final class FixedClock extends Clock implements Serializable {
        private static final long serialVersionUID = 7430389292664866958L;
        private final Instant instant;
        private final ZoneId zone;

        FixedClock(Instant fixedInstant, ZoneId zone) {
            this.instant = fixedInstant;
            this.zone = zone;
        }

        public ZoneId getZone() {
            return this.zone;
        }

        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.zone)) {
                return this;
            }
            return new FixedClock(this.instant, zone);
        }

        public long millis() {
            return this.instant.toEpochMilli();
        }

        public Instant instant() {
            return this.instant;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof FixedClock)) {
                return false;
            }
            FixedClock other = (FixedClock) obj;
            if (this.instant.equals(other.instant)) {
                z = this.zone.equals(other.zone);
            }
            return z;
        }

        public int hashCode() {
            return this.instant.hashCode() ^ this.zone.hashCode();
        }

        public String toString() {
            return "FixedClock[" + this.instant + "," + this.zone + "]";
        }
    }

    static final class OffsetClock extends Clock implements Serializable {
        private static final long serialVersionUID = 2007484719125426256L;
        private final Clock baseClock;
        private final Duration offset;

        OffsetClock(Clock baseClock, Duration offset) {
            this.baseClock = baseClock;
            this.offset = offset;
        }

        public ZoneId getZone() {
            return this.baseClock.getZone();
        }

        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.baseClock.getZone())) {
                return this;
            }
            return new OffsetClock(this.baseClock.withZone(zone), this.offset);
        }

        public long millis() {
            return Math.addExact(this.baseClock.millis(), this.offset.toMillis());
        }

        public Instant instant() {
            return this.baseClock.instant().plus(this.offset);
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof OffsetClock)) {
                return false;
            }
            OffsetClock other = (OffsetClock) obj;
            if (this.baseClock.equals(other.baseClock)) {
                z = this.offset.equals(other.offset);
            }
            return z;
        }

        public int hashCode() {
            return this.baseClock.hashCode() ^ this.offset.hashCode();
        }

        public String toString() {
            return "OffsetClock[" + this.baseClock + "," + this.offset + "]";
        }
    }

    static final class SystemClock extends Clock implements Serializable {
        private static final long serialVersionUID = 6740630888130243051L;
        private final ZoneId zone;

        SystemClock(ZoneId zone) {
            this.zone = zone;
        }

        public ZoneId getZone() {
            return this.zone;
        }

        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.zone)) {
                return this;
            }
            return new SystemClock(zone);
        }

        public long millis() {
            return System.currentTimeMillis();
        }

        public Instant instant() {
            return Instant.ofEpochMilli(millis());
        }

        public boolean equals(Object obj) {
            if (obj instanceof SystemClock) {
                return this.zone.equals(((SystemClock) obj).zone);
            }
            return false;
        }

        public int hashCode() {
            return this.zone.hashCode() + 1;
        }

        public String toString() {
            return "SystemClock[" + this.zone + "]";
        }
    }

    static final class TickClock extends Clock implements Serializable {
        private static final long serialVersionUID = 6504659149906368850L;
        private final Clock baseClock;
        private final long tickNanos;

        TickClock(Clock baseClock, long tickNanos) {
            this.baseClock = baseClock;
            this.tickNanos = tickNanos;
        }

        public ZoneId getZone() {
            return this.baseClock.getZone();
        }

        public Clock withZone(ZoneId zone) {
            if (zone.equals(this.baseClock.getZone())) {
                return this;
            }
            return new TickClock(this.baseClock.withZone(zone), this.tickNanos);
        }

        public long millis() {
            long millis = this.baseClock.millis();
            return millis - Math.floorMod(millis, this.tickNanos / 1000000);
        }

        public Instant instant() {
            if (this.tickNanos % 1000000 == 0) {
                long millis = this.baseClock.millis();
                return Instant.ofEpochMilli(millis - Math.floorMod(millis, this.tickNanos / 1000000));
            }
            Instant instant = this.baseClock.instant();
            return instant.minusNanos(Math.floorMod((long) instant.getNano(), this.tickNanos));
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof TickClock)) {
                return false;
            }
            TickClock other = (TickClock) obj;
            if (this.baseClock.equals(other.baseClock) && this.tickNanos == other.tickNanos) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.baseClock.hashCode() ^ ((int) (this.tickNanos ^ (this.tickNanos >>> 32)));
        }

        public String toString() {
            return "TickClock[" + this.baseClock + "," + Duration.ofNanos(this.tickNanos) + "]";
        }
    }

    public abstract ZoneId getZone();

    public abstract Instant instant();

    public abstract Clock withZone(ZoneId zoneId);

    public static Clock systemUTC() {
        return new SystemClock(ZoneOffset.UTC);
    }

    public static Clock systemDefaultZone() {
        return new SystemClock(ZoneId.systemDefault());
    }

    public static Clock system(ZoneId zone) {
        Objects.requireNonNull((Object) zone, "zone");
        return new SystemClock(zone);
    }

    public static Clock tickSeconds(ZoneId zone) {
        return new TickClock(system(zone), 1000000000);
    }

    public static Clock tickMinutes(ZoneId zone) {
        return new TickClock(system(zone), 60000000000L);
    }

    public static Clock tick(Clock baseClock, Duration tickDuration) {
        Objects.requireNonNull((Object) baseClock, "baseClock");
        Objects.requireNonNull((Object) tickDuration, "tickDuration");
        if (tickDuration.isNegative()) {
            throw new IllegalArgumentException("Tick duration must not be negative");
        }
        long tickNanos = tickDuration.toNanos();
        if (tickNanos % 1000000 != 0 && 1000000000 % tickNanos != 0) {
            throw new IllegalArgumentException("Invalid tick duration");
        } else if (tickNanos <= 1) {
            return baseClock;
        } else {
            return new TickClock(baseClock, tickNanos);
        }
    }

    public static Clock fixed(Instant fixedInstant, ZoneId zone) {
        Objects.requireNonNull((Object) fixedInstant, "fixedInstant");
        Objects.requireNonNull((Object) zone, "zone");
        return new FixedClock(fixedInstant, zone);
    }

    public static Clock offset(Clock baseClock, Duration offsetDuration) {
        Objects.requireNonNull((Object) baseClock, "baseClock");
        Objects.requireNonNull((Object) offsetDuration, "offsetDuration");
        if (offsetDuration.equals(Duration.ZERO)) {
            return baseClock;
        }
        return new OffsetClock(baseClock, offsetDuration);
    }

    protected Clock() {
    }

    public long millis() {
        return instant().toEpochMilli();
    }

    public boolean equals(Object obj) {
        return super.lambda$-java_util_function_Predicate_4628(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }
}
