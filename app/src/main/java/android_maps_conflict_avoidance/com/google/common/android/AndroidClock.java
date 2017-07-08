package android_maps_conflict_avoidance.com.google.common.android;

import android.os.SystemClock;
import android_maps_conflict_avoidance.com.google.common.Clock;

public class AndroidClock implements Clock {
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public long relativeTimeMillis() {
        return SystemClock.elapsedRealtime();
    }
}
