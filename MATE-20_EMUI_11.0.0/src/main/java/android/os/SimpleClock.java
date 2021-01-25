package android.os;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public abstract class SimpleClock extends Clock {
    private final ZoneId zone;

    @Override // java.time.Clock
    public abstract long millis();

    public SimpleClock(ZoneId zone2) {
        this.zone = zone2;
    }

    @Override // java.time.Clock
    public ZoneId getZone() {
        return this.zone;
    }

    @Override // java.time.Clock
    public Clock withZone(ZoneId zone2) {
        return new SimpleClock(zone2) {
            /* class android.os.SimpleClock.AnonymousClass1 */

            @Override // android.os.SimpleClock, java.time.Clock
            public long millis() {
                return SimpleClock.this.millis();
            }
        };
    }

    @Override // java.time.Clock
    public Instant instant() {
        return Instant.ofEpochMilli(millis());
    }
}
