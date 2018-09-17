package com.android.internal.telephony.metrics;

import android.os.SystemClock;
import com.android.internal.telephony.nano.TelephonyProto.TelephonyCallSession.Event;
import java.util.ArrayDeque;
import java.util.Deque;

public class InProgressCallSession {
    private static final int MAX_EVENTS = 300;
    public final Deque<Event> events;
    private boolean mEventsDropped = false;
    private long mLastElapsedTimeMs;
    private int mLastKnownPhoneState;
    public final int phoneId;
    public final long startElapsedTimeMs;
    public final int startSystemTimeMin;

    public boolean isEventsDropped() {
        return this.mEventsDropped;
    }

    public InProgressCallSession(int phoneId) {
        this.phoneId = phoneId;
        this.events = new ArrayDeque();
        this.startSystemTimeMin = TelephonyMetrics.roundSessionStart(System.currentTimeMillis());
        this.startElapsedTimeMs = SystemClock.elapsedRealtime();
        this.mLastElapsedTimeMs = this.startElapsedTimeMs;
    }

    public void addEvent(CallSessionEventBuilder builder) {
        addEvent(SystemClock.elapsedRealtime(), builder);
    }

    public synchronized void addEvent(long timestamp, CallSessionEventBuilder builder) {
        if (this.events.size() >= 300) {
            this.events.removeFirst();
            this.mEventsDropped = true;
        }
        builder.setDelay(TelephonyMetrics.toPrivacyFuzzedTimeInterval(this.mLastElapsedTimeMs, timestamp));
        this.events.add(builder.build());
        this.mLastElapsedTimeMs = timestamp;
    }

    public boolean containsCsCalls() {
        for (Event event : this.events) {
            if (event.type == 10) {
                return true;
            }
        }
        return false;
    }

    public void setLastKnownPhoneState(int state) {
        this.mLastKnownPhoneState = state;
    }

    public boolean isPhoneIdle() {
        return this.mLastKnownPhoneState == 1;
    }
}
