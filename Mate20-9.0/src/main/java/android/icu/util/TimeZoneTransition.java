package android.icu.util;

public class TimeZoneTransition {
    private final TimeZoneRule from;
    private final long time;
    private final TimeZoneRule to;

    public TimeZoneTransition(long time2, TimeZoneRule from2, TimeZoneRule to2) {
        this.time = time2;
        this.from = from2;
        this.to = to2;
    }

    public long getTime() {
        return this.time;
    }

    public TimeZoneRule getTo() {
        return this.to;
    }

    public TimeZoneRule getFrom() {
        return this.from;
    }

    public String toString() {
        return ("time=" + this.time) + (", from={" + this.from + "}") + (", to={" + this.to + "}");
    }
}
