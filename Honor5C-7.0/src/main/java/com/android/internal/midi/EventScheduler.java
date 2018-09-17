package com.android.internal.midi;

import com.huawei.android.statistical.StatisticalConstant;
import java.util.SortedMap;
import java.util.TreeMap;

public class EventScheduler {
    private static final long NANOS_PER_MILLI = 1000000;
    private boolean mClosed;
    private volatile SortedMap<Long, FastEventQueue> mEventBuffer;
    private FastEventQueue mEventPool;
    private final Object mLock;
    private int mMaxPoolSize;

    private class FastEventQueue {
        volatile long mEventsAdded;
        volatile long mEventsRemoved;
        volatile SchedulableEvent mFirst;
        volatile SchedulableEvent mLast;

        FastEventQueue(SchedulableEvent event) {
            this.mFirst = event;
            this.mLast = this.mFirst;
            this.mEventsAdded = 1;
            this.mEventsRemoved = 0;
        }

        int size() {
            return (int) (this.mEventsAdded - this.mEventsRemoved);
        }

        public SchedulableEvent remove() {
            this.mEventsRemoved++;
            SchedulableEvent event = this.mFirst;
            this.mFirst = event.mNext;
            event.mNext = null;
            return event;
        }

        public void add(SchedulableEvent event) {
            event.mNext = null;
            this.mLast.mNext = event;
            this.mLast = event;
            this.mEventsAdded++;
        }
    }

    public static class SchedulableEvent {
        private volatile SchedulableEvent mNext;
        private long mTimestamp;

        public SchedulableEvent(long timestamp) {
            this.mNext = null;
            this.mTimestamp = timestamp;
        }

        public long getTimestamp() {
            return this.mTimestamp;
        }

        public void setTimestamp(long timestamp) {
            this.mTimestamp = timestamp;
        }
    }

    public EventScheduler() {
        this.mLock = new Object();
        this.mEventPool = null;
        this.mMaxPoolSize = StatisticalConstant.TYPE_WIFI_CONNECT_ACTION;
        this.mEventBuffer = new TreeMap();
    }

    public SchedulableEvent removeEventfromPool() {
        if (this.mEventPool == null || this.mEventPool.size() <= 1) {
            return null;
        }
        return this.mEventPool.remove();
    }

    public void addEventToPool(SchedulableEvent event) {
        if (this.mEventPool == null) {
            this.mEventPool = new FastEventQueue(event);
        } else if (this.mEventPool.size() < this.mMaxPoolSize) {
            this.mEventPool.add(event);
        }
    }

    public void add(SchedulableEvent event) {
        synchronized (this.mLock) {
            FastEventQueue list = (FastEventQueue) this.mEventBuffer.get(Long.valueOf(event.getTimestamp()));
            if (list == null) {
                long lowestTime;
                if (this.mEventBuffer.isEmpty()) {
                    lowestTime = Long.MAX_VALUE;
                } else {
                    lowestTime = ((Long) this.mEventBuffer.firstKey()).longValue();
                }
                this.mEventBuffer.put(Long.valueOf(event.getTimestamp()), new FastEventQueue(event));
                if (event.getTimestamp() < lowestTime) {
                    this.mLock.notify();
                }
            } else {
                list.add(event);
            }
        }
    }

    private SchedulableEvent removeNextEventLocked(long lowestTime) {
        FastEventQueue list = (FastEventQueue) this.mEventBuffer.get(Long.valueOf(lowestTime));
        if (list.size() == 1) {
            this.mEventBuffer.remove(Long.valueOf(lowestTime));
        }
        return list.remove();
    }

    public SchedulableEvent getNextEvent(long time) {
        SchedulableEvent event = null;
        synchronized (this.mLock) {
            if (!this.mEventBuffer.isEmpty()) {
                long lowestTime = ((Long) this.mEventBuffer.firstKey()).longValue();
                if (lowestTime <= time) {
                    event = removeNextEventLocked(lowestTime);
                }
            }
        }
        return event;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SchedulableEvent waitNextEvent() throws InterruptedException {
        SchedulableEvent event = null;
        synchronized (this.mLock) {
            while (true) {
                if (this.mClosed) {
                    break;
                }
                long millisToWait = 2147483647L;
                if (!this.mEventBuffer.isEmpty()) {
                    long now = System.nanoTime();
                    long lowestTime = ((Long) this.mEventBuffer.firstKey()).longValue();
                    if (lowestTime <= now) {
                        break;
                    }
                    millisToWait = 1 + ((lowestTime - now) / NANOS_PER_MILLI);
                    if (millisToWait > 2147483647L) {
                        millisToWait = 2147483647L;
                    }
                }
                this.mLock.wait((long) ((int) millisToWait));
            }
        }
        return event;
    }

    protected void flush() {
        this.mEventBuffer = new TreeMap();
    }

    public void close() {
        synchronized (this.mLock) {
            this.mClosed = true;
            this.mLock.notify();
        }
    }
}
