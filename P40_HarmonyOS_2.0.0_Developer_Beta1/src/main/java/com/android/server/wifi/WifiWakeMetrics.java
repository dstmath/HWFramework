package com.android.server.wifi;

import android.os.SystemClock;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.nano.WifiMetricsProto;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class WifiWakeMetrics {
    @VisibleForTesting
    static final int MAX_RECORDED_SESSIONS = 10;
    @GuardedBy({"mLock"})
    private Session mCurrentSession;
    private int mIgnoredStarts = 0;
    private boolean mIsInSession = false;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private final List<Session> mSessions = new ArrayList();
    private int mTotalSessions = 0;
    private int mTotalWakeups = 0;

    public void recordStartEvent(int numNetworks) {
        synchronized (this.mLock) {
            this.mCurrentSession = new Session(numNetworks, SystemClock.elapsedRealtime());
            this.mIsInSession = true;
        }
    }

    public void recordInitializeEvent(int numScans, int numNetworks) {
        synchronized (this.mLock) {
            if (this.mIsInSession) {
                this.mCurrentSession.recordInitializeEvent(numScans, numNetworks, SystemClock.elapsedRealtime());
            }
        }
    }

    public void recordUnlockEvent(int numScans) {
        synchronized (this.mLock) {
            if (this.mIsInSession) {
                this.mCurrentSession.recordUnlockEvent(numScans, SystemClock.elapsedRealtime());
            }
        }
    }

    public void recordWakeupEvent(int numScans) {
        synchronized (this.mLock) {
            if (this.mIsInSession) {
                this.mCurrentSession.recordWakeupEvent(numScans, SystemClock.elapsedRealtime());
            }
        }
    }

    public void recordResetEvent(int numScans) {
        synchronized (this.mLock) {
            if (this.mIsInSession) {
                this.mCurrentSession.recordResetEvent(numScans, SystemClock.elapsedRealtime());
                if (this.mCurrentSession.hasWakeupTriggered()) {
                    this.mTotalWakeups++;
                }
                this.mTotalSessions++;
                if (this.mSessions.size() < 10) {
                    this.mSessions.add(this.mCurrentSession);
                }
                this.mIsInSession = false;
            }
        }
    }

    public void recordIgnoredStart() {
        this.mIgnoredStarts++;
    }

    public WifiMetricsProto.WifiWakeStats buildProto() {
        WifiMetricsProto.WifiWakeStats proto = new WifiMetricsProto.WifiWakeStats();
        proto.numSessions = this.mTotalSessions;
        proto.numWakeups = this.mTotalWakeups;
        proto.numIgnoredStarts = this.mIgnoredStarts;
        proto.sessions = new WifiMetricsProto.WifiWakeStats.Session[this.mSessions.size()];
        for (int i = 0; i < this.mSessions.size(); i++) {
            proto.sessions[i] = this.mSessions.get(i).buildProto();
        }
        return proto;
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("-------WifiWake metrics-------");
            pw.println("mTotalSessions: " + this.mTotalSessions);
            pw.println("mTotalWakeups: " + this.mTotalWakeups);
            pw.println("mIgnoredStarts: " + this.mIgnoredStarts);
            pw.println("mIsInSession: " + this.mIsInSession);
            pw.println("Stored Sessions: " + this.mSessions.size());
            for (Session session : this.mSessions) {
                session.dump(pw);
            }
            if (this.mCurrentSession != null) {
                pw.println("Current Session: ");
                this.mCurrentSession.dump(pw);
            }
            pw.println("----end of WifiWake metrics----");
        }
    }

    public void clear() {
        synchronized (this.mLock) {
            this.mSessions.clear();
            this.mTotalSessions = 0;
            this.mTotalWakeups = 0;
            this.mIgnoredStarts = 0;
        }
    }

    public static class Session {
        @VisibleForTesting
        Event mInitEvent;
        private int mInitializeNetworks = 0;
        @VisibleForTesting
        Event mResetEvent;
        private final int mStartNetworks;
        private final long mStartTimestamp;
        @VisibleForTesting
        Event mUnlockEvent;
        @VisibleForTesting
        Event mWakeupEvent;

        public Session(int numNetworks, long timestamp) {
            this.mStartNetworks = numNetworks;
            this.mStartTimestamp = timestamp;
        }

        public void recordInitializeEvent(int numScans, int numNetworks, long timestamp) {
            if (this.mInitEvent == null) {
                this.mInitializeNetworks = numNetworks;
                this.mInitEvent = new Event(numScans, timestamp - this.mStartTimestamp);
            }
        }

        public void recordUnlockEvent(int numScans, long timestamp) {
            if (this.mUnlockEvent == null) {
                this.mUnlockEvent = new Event(numScans, timestamp - this.mStartTimestamp);
            }
        }

        public void recordWakeupEvent(int numScans, long timestamp) {
            if (this.mWakeupEvent == null) {
                this.mWakeupEvent = new Event(numScans, timestamp - this.mStartTimestamp);
            }
        }

        public boolean hasWakeupTriggered() {
            return this.mWakeupEvent != null;
        }

        public void recordResetEvent(int numScans, long timestamp) {
            if (this.mResetEvent == null) {
                this.mResetEvent = new Event(numScans, timestamp - this.mStartTimestamp);
            }
        }

        public WifiMetricsProto.WifiWakeStats.Session buildProto() {
            WifiMetricsProto.WifiWakeStats.Session sessionProto = new WifiMetricsProto.WifiWakeStats.Session();
            sessionProto.startTimeMillis = this.mStartTimestamp;
            sessionProto.lockedNetworksAtStart = this.mStartNetworks;
            Event event = this.mInitEvent;
            if (event != null) {
                sessionProto.lockedNetworksAtInitialize = this.mInitializeNetworks;
                sessionProto.initializeEvent = event.buildProto();
            }
            Event event2 = this.mUnlockEvent;
            if (event2 != null) {
                sessionProto.unlockEvent = event2.buildProto();
            }
            Event event3 = this.mWakeupEvent;
            if (event3 != null) {
                sessionProto.wakeupEvent = event3.buildProto();
            }
            Event event4 = this.mResetEvent;
            if (event4 != null) {
                sessionProto.resetEvent = event4.buildProto();
            }
            return sessionProto;
        }

        public void dump(PrintWriter pw) {
            pw.println("WifiWakeMetrics.Session:");
            pw.println("mStartTimestamp: " + this.mStartTimestamp);
            pw.println("mStartNetworks: " + this.mStartNetworks);
            pw.println("mInitializeNetworks: " + this.mInitializeNetworks);
            StringBuilder sb = new StringBuilder();
            sb.append("mInitEvent: ");
            Event event = this.mInitEvent;
            String str = "{}";
            sb.append(event == null ? str : event.toString());
            pw.println(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("mUnlockEvent: ");
            Event event2 = this.mUnlockEvent;
            sb2.append(event2 == null ? str : event2.toString());
            pw.println(sb2.toString());
            StringBuilder sb3 = new StringBuilder();
            sb3.append("mWakeupEvent: ");
            Event event3 = this.mWakeupEvent;
            sb3.append(event3 == null ? str : event3.toString());
            pw.println(sb3.toString());
            StringBuilder sb4 = new StringBuilder();
            sb4.append("mResetEvent: ");
            Event event4 = this.mResetEvent;
            if (event4 != null) {
                str = event4.toString();
            }
            sb4.append(str);
            pw.println(sb4.toString());
        }
    }

    public static class Event {
        public final long mElapsedTime;
        public final int mNumScans;

        public Event(int numScans, long elapsedTime) {
            this.mNumScans = numScans;
            this.mElapsedTime = elapsedTime;
        }

        public WifiMetricsProto.WifiWakeStats.Session.Event buildProto() {
            WifiMetricsProto.WifiWakeStats.Session.Event eventProto = new WifiMetricsProto.WifiWakeStats.Session.Event();
            eventProto.elapsedScans = this.mNumScans;
            eventProto.elapsedTimeMillis = this.mElapsedTime;
            return eventProto;
        }

        public String toString() {
            return "{ mNumScans: " + this.mNumScans + ", elapsedTime: " + this.mElapsedTime + " }";
        }
    }
}
