package android.util;

import android.os.Build;
import android.os.SystemClock;
import android.os.Trace;
import java.util.ArrayDeque;
import java.util.Deque;

public class BootTimingsTraceLog {
    private static final boolean DEBUG_BOOT_TIME = ("user".equals(Build.TYPE) ^ 1);
    private final Deque<Pair<String, Long>> mStartTimes;
    private final String mTag;
    private long mTraceTag;

    public BootTimingsTraceLog(String tag, long traceTag) {
        this.mStartTimes = DEBUG_BOOT_TIME ? new ArrayDeque() : null;
        this.mTag = tag;
        this.mTraceTag = traceTag;
    }

    public void traceBegin(String name) {
        Trace.traceBegin(this.mTraceTag, name);
        if (DEBUG_BOOT_TIME) {
            this.mStartTimes.push(Pair.create(name, Long.valueOf(SystemClock.elapsedRealtime())));
        }
    }

    public void traceEnd() {
        Trace.traceEnd(this.mTraceTag);
        if (!DEBUG_BOOT_TIME) {
            return;
        }
        if (this.mStartTimes.peek() == null) {
            Slog.w(this.mTag, "traceEnd called more times than traceBegin");
            return;
        }
        Pair<String, Long> event = (Pair) this.mStartTimes.pop();
        Slog.d(this.mTag, ((String) event.first) + " took to complete: " + (SystemClock.elapsedRealtime() - ((Long) event.second).longValue()) + "ms");
    }
}
