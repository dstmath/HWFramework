package android_maps_conflict_avoidance.com.google.common;

public class GenericClock implements Clock {
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public long relativeTimeMillis() {
        return System.currentTimeMillis();
    }
}
