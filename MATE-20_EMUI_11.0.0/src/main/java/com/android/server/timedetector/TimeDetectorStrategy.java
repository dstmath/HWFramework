package com.android.server.timedetector;

import android.app.timedetector.TimeSignal;
import android.content.Intent;
import android.util.TimestampedValue;
import java.io.PrintWriter;

public interface TimeDetectorStrategy {

    public interface Callback {
        void acquireWakeLock();

        long elapsedRealtimeMillis();

        boolean isTimeDetectionEnabled();

        void releaseWakeLock();

        void sendStickyBroadcast(Intent intent);

        void setSystemClock(long j);

        long systemClockMillis();

        int systemClockUpdateThresholdMillis();
    }

    void dump(PrintWriter printWriter, String[] strArr);

    void handleAutoTimeDetectionToggle(boolean z);

    void initialize(Callback callback);

    void suggestTime(TimeSignal timeSignal);

    static long getTimeAt(TimestampedValue<Long> timeValue, long referenceClockMillisNow) {
        return (referenceClockMillisNow - timeValue.getReferenceTimeMillis()) + ((Long) timeValue.getValue()).longValue();
    }
}
