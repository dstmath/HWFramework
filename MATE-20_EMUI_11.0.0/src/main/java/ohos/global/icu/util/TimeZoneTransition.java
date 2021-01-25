package ohos.global.icu.util;

public class TimeZoneTransition {
    private final TimeZoneRule from;
    private final long time;
    private final TimeZoneRule to;

    public TimeZoneTransition(long j, TimeZoneRule timeZoneRule, TimeZoneRule timeZoneRule2) {
        this.time = j;
        this.from = timeZoneRule;
        this.to = timeZoneRule2;
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
        StringBuilder sb = new StringBuilder();
        sb.append("time=" + this.time);
        sb.append(", from={" + this.from + "}");
        sb.append(", to={" + this.to + "}");
        return sb.toString();
    }
}
