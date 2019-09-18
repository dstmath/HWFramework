package android.util;

import android.os.Build;
import android.os.SystemClock;
import android.os.Trace;
import java.util.ArrayDeque;
import java.util.Deque;

public class TimingsTraceLog {
    private static final boolean DEBUG_BOOT_TIME = (!Build.IS_USER);
    private final Deque<Pair<String, Long>> mStartTimes;
    private final String mTag;
    private long mThreadId;
    private long mTraceTag;

    public TimingsTraceLog(String tag, long traceTag) {
        this.mStartTimes = DEBUG_BOOT_TIME ? new ArrayDeque() : null;
        this.mTag = tag;
        this.mTraceTag = traceTag;
        this.mThreadId = Thread.currentThread().getId();
    }

    public void traceBegin(String name) {
        assertSameThread();
        Trace.traceBegin(this.mTraceTag, name);
        if (DEBUG_BOOT_TIME) {
            this.mStartTimes.push(Pair.create(name, Long.valueOf(SystemClock.elapsedRealtime())));
        }
    }

    public void traceEnd() {
        assertSameThread();
        Trace.traceEnd(this.mTraceTag);
        if (DEBUG_BOOT_TIME) {
            if (this.mStartTimes.peek() == null) {
                Slog.w(this.mTag, "traceEnd called more times than traceBegin");
                return;
            }
            Pair<String, Long> event = this.mStartTimes.pop();
            logDuration((String) event.first, SystemClock.elapsedRealtime() - ((Long) event.second).longValue());
        }
    }

    private void assertSameThread() {
        Thread currentThread = Thread.currentThread();
        if (currentThread.getId() != this.mThreadId) {
            throw new IllegalStateException("Instance of TimingsTraceLog can only be called from the thread it was created on (tid: " + this.mThreadId + "), but was from " + currentThread.getName() + " (tid: " + currentThread.getId() + ")");
        }
    }

    public void logDuration(String name, long timeMs) {
        String str = this.mTag;
        Slog.d(str, name + " took to complete: " + timeMs + "ms");
    }
}
