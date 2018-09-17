package android.icu.util;

public class TimeZoneTransition {
    private final TimeZoneRule from;
    private final long time;
    private final TimeZoneRule to;

    public TimeZoneTransition(long time, TimeZoneRule from, TimeZoneRule to) {
        this.time = time;
        this.from = from;
        this.to = to;
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
        StringBuilder buf = new StringBuilder();
        buf.append("time=").append(this.time);
        buf.append(", from={").append(this.from).append("}");
        buf.append(", to={").append(this.to).append("}");
        return buf.toString();
    }
}
