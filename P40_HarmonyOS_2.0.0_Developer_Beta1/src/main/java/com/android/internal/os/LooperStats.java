package com.android.internal.os;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.os.CachedDeviceState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

public class LooperStats implements Looper.Observer {
    public static final String DEBUG_ENTRY_PREFIX = "__DEBUG_";
    private static final boolean DISABLED_SCREEN_STATE_TRACKING_VALUE = false;
    private static final int SESSION_POOL_SIZE = 50;
    private boolean mAddDebugEntries = false;
    private CachedDeviceState.TimeInStateStopwatch mBatteryStopwatch;
    private CachedDeviceState.Readonly mDeviceState;
    @GuardedBy({"mLock"})
    private final SparseArray<Entry> mEntries = new SparseArray<>(512);
    private final int mEntriesSizeCap;
    private final Entry mHashCollisionEntry = new Entry("HASH_COLLISION");
    private final Object mLock = new Object();
    private final Entry mOverflowEntry = new Entry("OVERFLOW");
    private int mSamplingInterval;
    private final ConcurrentLinkedQueue<DispatchSession> mSessionPool = new ConcurrentLinkedQueue<>();
    private long mStartCurrentTime = System.currentTimeMillis();
    private long mStartElapsedTime = SystemClock.elapsedRealtime();
    private boolean mTrackScreenInteractive = false;

    public LooperStats(int samplingInterval, int entriesSizeCap) {
        this.mSamplingInterval = samplingInterval;
        this.mEntriesSizeCap = entriesSizeCap;
    }

    public void setDeviceState(CachedDeviceState.Readonly deviceState) {
        CachedDeviceState.TimeInStateStopwatch timeInStateStopwatch = this.mBatteryStopwatch;
        if (timeInStateStopwatch != null) {
            timeInStateStopwatch.close();
        }
        this.mDeviceState = deviceState;
        this.mBatteryStopwatch = deviceState.createTimeOnBatteryStopwatch();
    }

    public void setAddDebugEntries(boolean addDebugEntries) {
        this.mAddDebugEntries = addDebugEntries;
    }

    @Override // android.os.Looper.Observer
    public Object messageDispatchStarting() {
        if (!deviceStateAllowsCollection() || !shouldCollectDetailedData()) {
            return DispatchSession.NOT_SAMPLED;
        }
        DispatchSession session = this.mSessionPool.poll();
        DispatchSession session2 = session == null ? new DispatchSession() : session;
        session2.startTimeMicro = getElapsedRealtimeMicro();
        session2.cpuStartMicro = getThreadTimeMicro();
        session2.systemUptimeMillis = getSystemUptimeMillis();
        return session2;
    }

    @Override // android.os.Looper.Observer
    public void messageDispatched(Object token, Message msg) {
        if (deviceStateAllowsCollection()) {
            DispatchSession session = (DispatchSession) token;
            Entry entry = findEntry(msg, session != DispatchSession.NOT_SAMPLED);
            if (entry != null) {
                synchronized (entry) {
                    entry.messageCount++;
                    if (session != DispatchSession.NOT_SAMPLED) {
                        entry.recordedMessageCount++;
                        long latency = getElapsedRealtimeMicro() - session.startTimeMicro;
                        long cpuUsage = getThreadTimeMicro() - session.cpuStartMicro;
                        entry.totalLatencyMicro += latency;
                        entry.maxLatencyMicro = Math.max(entry.maxLatencyMicro, latency);
                        entry.cpuUsageMicro += cpuUsage;
                        entry.maxCpuUsageMicro = Math.max(entry.maxCpuUsageMicro, cpuUsage);
                        if (msg.getWhen() > 0) {
                            long delay = Math.max(0L, session.systemUptimeMillis - msg.getWhen());
                            entry.delayMillis += delay;
                            entry.maxDelayMillis = Math.max(entry.maxDelayMillis, delay);
                            entry.recordedDelayMessageCount++;
                        }
                    }
                }
            }
            recycleSession(session);
        }
    }

    @Override // android.os.Looper.Observer
    public void dispatchingThrewException(Object token, Message msg, Exception exception) {
        if (deviceStateAllowsCollection()) {
            DispatchSession session = (DispatchSession) token;
            Entry entry = findEntry(msg, session != DispatchSession.NOT_SAMPLED);
            if (entry != null) {
                synchronized (entry) {
                    entry.exceptionCount++;
                }
            }
            recycleSession(session);
        }
    }

    private boolean deviceStateAllowsCollection() {
        CachedDeviceState.Readonly readonly = this.mDeviceState;
        return readonly != null && !readonly.isCharging();
    }

    public List<ExportedEntry> getEntries() {
        ArrayList<ExportedEntry> exportedEntries;
        synchronized (this.mLock) {
            int size = this.mEntries.size();
            exportedEntries = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                Entry entry = this.mEntries.valueAt(i);
                synchronized (entry) {
                    exportedEntries.add(new ExportedEntry(entry));
                }
            }
        }
        maybeAddSpecialEntry(exportedEntries, this.mOverflowEntry);
        maybeAddSpecialEntry(exportedEntries, this.mHashCollisionEntry);
        if (this.mAddDebugEntries && this.mBatteryStopwatch != null) {
            exportedEntries.add(createDebugEntry("start_time_millis", this.mStartElapsedTime));
            exportedEntries.add(createDebugEntry("end_time_millis", SystemClock.elapsedRealtime()));
            exportedEntries.add(createDebugEntry("battery_time_millis", this.mBatteryStopwatch.getMillis()));
            exportedEntries.add(createDebugEntry("sampling_interval", (long) this.mSamplingInterval));
        }
        return exportedEntries;
    }

    private ExportedEntry createDebugEntry(String variableName, long value) {
        Entry entry = new Entry(DEBUG_ENTRY_PREFIX + variableName);
        entry.messageCount = 1;
        entry.recordedMessageCount = 1;
        entry.totalLatencyMicro = value;
        return new ExportedEntry(entry);
    }

    public long getStartTimeMillis() {
        return this.mStartCurrentTime;
    }

    public long getStartElapsedTimeMillis() {
        return this.mStartElapsedTime;
    }

    public long getBatteryTimeMillis() {
        CachedDeviceState.TimeInStateStopwatch timeInStateStopwatch = this.mBatteryStopwatch;
        if (timeInStateStopwatch != null) {
            return timeInStateStopwatch.getMillis();
        }
        return 0;
    }

    private void maybeAddSpecialEntry(List<ExportedEntry> exportedEntries, Entry specialEntry) {
        synchronized (specialEntry) {
            if (specialEntry.messageCount > 0 || specialEntry.exceptionCount > 0) {
                exportedEntries.add(new ExportedEntry(specialEntry));
            }
        }
    }

    public void reset() {
        synchronized (this.mLock) {
            this.mEntries.clear();
        }
        synchronized (this.mHashCollisionEntry) {
            this.mHashCollisionEntry.reset();
        }
        synchronized (this.mOverflowEntry) {
            this.mOverflowEntry.reset();
        }
        this.mStartCurrentTime = System.currentTimeMillis();
        this.mStartElapsedTime = SystemClock.elapsedRealtime();
        CachedDeviceState.TimeInStateStopwatch timeInStateStopwatch = this.mBatteryStopwatch;
        if (timeInStateStopwatch != null) {
            timeInStateStopwatch.reset();
        }
    }

    public void setSamplingInterval(int samplingInterval) {
        this.mSamplingInterval = samplingInterval;
    }

    public void setTrackScreenInteractive(boolean enabled) {
        this.mTrackScreenInteractive = enabled;
    }

    private Entry findEntry(Message msg, boolean allowCreateNew) {
        boolean isInteractive;
        Entry entry;
        if (this.mTrackScreenInteractive) {
            isInteractive = this.mDeviceState.isScreenInteractive();
        } else {
            isInteractive = false;
        }
        int id = Entry.idFor(msg, isInteractive);
        synchronized (this.mLock) {
            entry = this.mEntries.get(id);
            if (entry == null) {
                if (!allowCreateNew) {
                    return null;
                }
                if (this.mEntries.size() >= this.mEntriesSizeCap) {
                    return this.mOverflowEntry;
                }
                entry = new Entry(msg, isInteractive);
                this.mEntries.put(id, entry);
            }
        }
        if (entry.workSourceUid == msg.workSourceUid && entry.handler.getClass() == msg.getTarget().getClass() && entry.handler.getLooper().getThread() == msg.getTarget().getLooper().getThread() && entry.isInteractive == isInteractive) {
            return entry;
        }
        return this.mHashCollisionEntry;
    }

    private void recycleSession(DispatchSession session) {
        if (session != DispatchSession.NOT_SAMPLED && this.mSessionPool.size() < 50) {
            this.mSessionPool.add(session);
        }
    }

    /* access modifiers changed from: protected */
    public long getThreadTimeMicro() {
        return SystemClock.currentThreadTimeMicro();
    }

    /* access modifiers changed from: protected */
    public long getElapsedRealtimeMicro() {
        return SystemClock.elapsedRealtimeNanos() / 1000;
    }

    /* access modifiers changed from: protected */
    public long getSystemUptimeMillis() {
        return SystemClock.uptimeMillis();
    }

    /* access modifiers changed from: protected */
    public boolean shouldCollectDetailedData() {
        return ThreadLocalRandom.current().nextInt() % this.mSamplingInterval == 0;
    }

    /* access modifiers changed from: private */
    public static class DispatchSession {
        static final DispatchSession NOT_SAMPLED = new DispatchSession();
        public long cpuStartMicro;
        public long startTimeMicro;
        public long systemUptimeMillis;

        private DispatchSession() {
        }
    }

    /* access modifiers changed from: private */
    public static class Entry {
        public long cpuUsageMicro;
        public long delayMillis;
        public long exceptionCount;
        public final Handler handler;
        public final boolean isInteractive;
        public long maxCpuUsageMicro;
        public long maxDelayMillis;
        public long maxLatencyMicro;
        public long messageCount;
        public final String messageName;
        public long recordedDelayMessageCount;
        public long recordedMessageCount;
        public long totalLatencyMicro;
        public final int workSourceUid;

        Entry(Message msg, boolean isInteractive2) {
            this.workSourceUid = msg.workSourceUid;
            this.handler = msg.getTarget();
            this.messageName = this.handler.getMessageName(msg);
            this.isInteractive = isInteractive2;
        }

        Entry(String specialEntryName) {
            this.workSourceUid = -1;
            this.messageName = specialEntryName;
            this.handler = null;
            this.isInteractive = false;
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.messageCount = 0;
            this.recordedMessageCount = 0;
            this.exceptionCount = 0;
            this.totalLatencyMicro = 0;
            this.maxLatencyMicro = 0;
            this.cpuUsageMicro = 0;
            this.maxCpuUsageMicro = 0;
            this.delayMillis = 0;
            this.maxDelayMillis = 0;
            this.recordedDelayMessageCount = 0;
        }

        static int idFor(Message msg, boolean isInteractive2) {
            int result = (((((((7 * 31) + msg.workSourceUid) * 31) + msg.getTarget().getLooper().getThread().hashCode()) * 31) + msg.getTarget().getClass().hashCode()) * 31) + (isInteractive2 ? MetricsProto.MetricsEvent.AUTOFILL_SERVICE_DISABLED_APP : MetricsProto.MetricsEvent.ANOMALY_TYPE_UNOPTIMIZED_BT);
            if (msg.getCallback() != null) {
                return (result * 31) + msg.getCallback().getClass().hashCode();
            }
            return (result * 31) + msg.what;
        }
    }

    public static class ExportedEntry {
        public final long cpuUsageMicros;
        public final long delayMillis;
        public final long exceptionCount;
        public final String handlerClassName;
        public final boolean isInteractive;
        public final long maxCpuUsageMicros;
        public final long maxDelayMillis;
        public final long maxLatencyMicros;
        public final long messageCount;
        public final String messageName;
        public final long recordedDelayMessageCount;
        public final long recordedMessageCount;
        public final String threadName;
        public final long totalLatencyMicros;
        public final int workSourceUid;

        ExportedEntry(Entry entry) {
            this.workSourceUid = entry.workSourceUid;
            if (entry.handler != null) {
                this.handlerClassName = entry.handler.getClass().getName();
                this.threadName = entry.handler.getLooper().getThread().getName();
            } else {
                this.handlerClassName = "";
                this.threadName = "";
            }
            this.isInteractive = entry.isInteractive;
            this.messageName = entry.messageName;
            this.messageCount = entry.messageCount;
            this.recordedMessageCount = entry.recordedMessageCount;
            this.exceptionCount = entry.exceptionCount;
            this.totalLatencyMicros = entry.totalLatencyMicro;
            this.maxLatencyMicros = entry.maxLatencyMicro;
            this.cpuUsageMicros = entry.cpuUsageMicro;
            this.maxCpuUsageMicros = entry.maxCpuUsageMicro;
            this.delayMillis = entry.delayMillis;
            this.maxDelayMillis = entry.maxDelayMillis;
            this.recordedDelayMessageCount = entry.recordedDelayMessageCount;
        }
    }
}
