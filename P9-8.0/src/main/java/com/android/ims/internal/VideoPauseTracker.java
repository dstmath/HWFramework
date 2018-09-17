package com.android.ims.internal;

import android.telecom.Log;
import android.util.ArraySet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class VideoPauseTracker {
    public static final int SOURCE_DATA_ENABLED = 2;
    private static final String SOURCE_DATA_ENABLED_STR = "DATA_ENABLED";
    public static final int SOURCE_INCALL = 1;
    private static final String SOURCE_INCALL_STR = "INCALL";
    private Set<Integer> mPauseRequests = new ArraySet(2);
    private Object mPauseRequestsLock = new Object();

    public boolean shouldPauseVideoFor(int source) {
        synchronized (this.mPauseRequestsLock) {
            boolean wasPaused = isPaused();
            this.mPauseRequests.add(Integer.valueOf(source));
            if (wasPaused) {
                Log.i(this, "shouldPauseVideoFor: source=%s, pendingRequests=%s - already paused", new Object[]{sourceToString(source), sourcesToString(this.mPauseRequests)});
                return false;
            }
            Log.i(this, "shouldPauseVideoFor: source=%s, pendingRequests=%s - should pause", new Object[]{sourceToString(source), sourcesToString(this.mPauseRequests)});
            return true;
        }
    }

    public boolean shouldResumeVideoFor(int source) {
        synchronized (this.mPauseRequestsLock) {
            boolean wasPaused = isPaused();
            this.mPauseRequests.remove(Integer.valueOf(source));
            boolean isPaused = isPaused();
            if (wasPaused && (isPaused ^ 1) != 0) {
                Log.i(this, "shouldResumeVideoFor: source=%s, pendingRequests=%s - should resume", new Object[]{sourceToString(source), sourcesToString(this.mPauseRequests)});
                return true;
            } else if (wasPaused && isPaused) {
                Log.i(this, "shouldResumeVideoFor: source=%s, pendingRequests=%s - stay paused", new Object[]{sourceToString(source), sourcesToString(this.mPauseRequests)});
                return false;
            } else {
                Log.i(this, "shouldResumeVideoFor: source=%s, pendingRequests=%s - not paused", new Object[]{sourceToString(source), sourcesToString(this.mPauseRequests)});
                return false;
            }
        }
    }

    public boolean isPaused() {
        boolean isEmpty;
        synchronized (this.mPauseRequestsLock) {
            isEmpty = this.mPauseRequests.isEmpty() ^ 1;
        }
        return isEmpty;
    }

    public boolean wasVideoPausedFromSource(int source) {
        boolean contains;
        synchronized (this.mPauseRequestsLock) {
            contains = this.mPauseRequests.contains(Integer.valueOf(source));
        }
        return contains;
    }

    public void clearPauseRequests() {
        synchronized (this.mPauseRequestsLock) {
            this.mPauseRequests.clear();
        }
    }

    private String sourceToString(int source) {
        switch (source) {
            case 1:
                return SOURCE_INCALL_STR;
            case 2:
                return SOURCE_DATA_ENABLED_STR;
            default:
                return "unknown";
        }
    }

    private String sourcesToString(Collection<Integer> sources) {
        String str;
        synchronized (this.mPauseRequestsLock) {
            str = (String) sources.stream().map(new -$Lambda$TOyCZeFTwq-Q8yjdlZDJDEiAokw(this)).collect(Collectors.joining(", "));
        }
        return str;
    }

    /* synthetic */ String lambda$-com_android_ims_internal_VideoPauseTracker_7222(Integer source) {
        return sourceToString(source.intValue());
    }
}
