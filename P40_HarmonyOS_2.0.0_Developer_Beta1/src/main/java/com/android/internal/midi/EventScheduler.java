package com.android.internal.midi;

import java.util.SortedMap;
import java.util.TreeMap;

public class EventScheduler {
    private static final long NANOS_PER_MILLI = 1000000;
    private boolean mClosed;
    private volatile SortedMap<Long, FastEventQueue> mEventBuffer = new TreeMap();
    private FastEventQueue mEventPool = null;
    private final Object mLock = new Object();
    private int mMaxPoolSize = 200;

    /* access modifiers changed from: private */
    public class FastEventQueue {
        volatile long mEventsAdded = 1;
        volatile long mEventsRemoved = 0;
        volatile SchedulableEvent mFirst;
        volatile SchedulableEvent mLast = this.mFirst;

        FastEventQueue(SchedulableEvent event) {
            this.mFirst = event;
        }

        /* access modifiers changed from: package-private */
        public int size() {
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
        private volatile SchedulableEvent mNext = null;
        private long mTimestamp;

        public SchedulableEvent(long timestamp) {
            this.mTimestamp = timestamp;
        }

        public long getTimestamp() {
            return this.mTimestamp;
        }

        public void setTimestamp(long timestamp) {
            this.mTimestamp = timestamp;
        }
    }

    public SchedulableEvent removeEventfromPool() {
        FastEventQueue fastEventQueue = this.mEventPool;
        if (fastEventQueue == null || fastEventQueue.size() <= 1) {
            return null;
        }
        return this.mEventPool.remove();
    }

    public void addEventToPool(SchedulableEvent event) {
        FastEventQueue fastEventQueue = this.mEventPool;
        if (fastEventQueue == null) {
            this.mEventPool = new FastEventQueue(event);
        } else if (fastEventQueue.size() < this.mMaxPoolSize) {
            this.mEventPool.add(event);
        }
    }

    public void add(SchedulableEvent event) {
        long lowestTime;
        synchronized (this.mLock) {
            FastEventQueue list = this.mEventBuffer.get(Long.valueOf(event.getTimestamp()));
            if (list == null) {
                if (this.mEventBuffer.isEmpty()) {
                    lowestTime = Long.MAX_VALUE;
                } else {
                    lowestTime = this.mEventBuffer.firstKey().longValue();
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
        FastEventQueue list = this.mEventBuffer.get(Long.valueOf(lowestTime));
        if (list.size() == 1) {
            this.mEventBuffer.remove(Long.valueOf(lowestTime));
        }
        return list.remove();
    }

    public SchedulableEvent getNextEvent(long time) {
        SchedulableEvent event = null;
        synchronized (this.mLock) {
            if (!this.mEventBuffer.isEmpty()) {
                long lowestTime = this.mEventBuffer.firstKey().longValue();
                if (lowestTime <= time) {
                    event = removeNextEventLocked(lowestTime);
                }
            }
        }
        return event;
    }

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
                    long lowestTime = this.mEventBuffer.firstKey().longValue();
                    if (lowestTime <= now) {
                        event = removeNextEventLocked(lowestTime);
                        break;
                    }
                    millisToWait = ((lowestTime - now) / 1000000) + 1;
                    if (millisToWait > 2147483647L) {
                        millisToWait = 2147483647L;
                    }
                }
                this.mLock.wait((long) ((int) millisToWait));
            }
        }
        return event;
    }

    /* access modifiers changed from: protected */
    public void flush() {
        this.mEventBuffer = new TreeMap();
    }

    public void close() {
        synchronized (this.mLock) {
            this.mClosed = true;
            this.mLock.notify();
        }
    }
}
