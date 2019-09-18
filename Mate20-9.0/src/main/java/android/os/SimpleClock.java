package android.os;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public abstract class SimpleClock extends Clock {
    private final ZoneId zone;

    public abstract long millis();

    public SimpleClock(ZoneId zone2) {
        this.zone = zone2;
    }

    public ZoneId getZone() {
        return this.zone;
    }

    public Clock withZone(ZoneId zone2) {
        return new SimpleClock(zone2) {
            public long millis() {
                return SimpleClock.this.millis();
            }
        };
    }

    public Instant instant() {
        return Instant.ofEpochMilli(millis());
    }
}
