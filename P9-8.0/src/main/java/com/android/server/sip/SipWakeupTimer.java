package com.android.server.sip;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.telephony.Rlog;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.Executor;

class SipWakeupTimer extends BroadcastReceiver {
    private static final boolean DBG = false;
    private static final String TAG = "SipWakeupTimer";
    private static final String TRIGGER_TIME = "TriggerTime";
    private AlarmManager mAlarmManager;
    private Context mContext;
    private TreeSet<MyEvent> mEventQueue = new TreeSet(new MyEventComparator());
    private Executor mExecutor;
    private PendingIntent mPendingIntent;

    private static class MyEvent {
        Runnable mCallback;
        long mLastTriggerTime;
        int mMaxPeriod;
        int mPeriod;
        long mTriggerTime;

        MyEvent(int period, Runnable callback, long now) {
            this.mMaxPeriod = period;
            this.mPeriod = period;
            this.mCallback = callback;
            this.mLastTriggerTime = now;
        }

        public String toString() {
            String s = super.toString();
            return s.substring(s.indexOf("@")) + ":" + (this.mPeriod / 1000) + ":" + (this.mMaxPeriod / 1000) + ":" + toString(this.mCallback);
        }

        private String toString(Object o) {
            String s = o.toString();
            int index = s.indexOf("$");
            if (index > 0) {
                return s.substring(index + 1);
            }
            return s;
        }
    }

    private static class MyEventComparator implements Comparator<MyEvent> {
        /* synthetic */ MyEventComparator(MyEventComparator -this0) {
            this();
        }

        private MyEventComparator() {
        }

        public int compare(MyEvent e1, MyEvent e2) {
            if (e1 == e2) {
                return 0;
            }
            int diff = e1.mMaxPeriod - e2.mMaxPeriod;
            if (diff == 0) {
                diff = -1;
            }
            return diff;
        }

        public boolean equals(Object that) {
            return this == that ? true : SipWakeupTimer.DBG;
        }
    }

    public SipWakeupTimer(Context context, Executor executor) {
        this.mContext = context;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        context.registerReceiver(this, new IntentFilter(getAction()));
        this.mExecutor = executor;
    }

    public synchronized void stop() {
        this.mContext.unregisterReceiver(this);
        if (this.mPendingIntent != null) {
            this.mAlarmManager.cancel(this.mPendingIntent);
            this.mPendingIntent = null;
        }
        this.mEventQueue.clear();
        this.mEventQueue = null;
    }

    private boolean stopped() {
        if (this.mEventQueue == null) {
            return true;
        }
        return DBG;
    }

    private void cancelAlarm() {
        this.mAlarmManager.cancel(this.mPendingIntent);
        this.mPendingIntent = null;
    }

    private void recalculatePeriods() {
        if (!this.mEventQueue.isEmpty()) {
            MyEvent firstEvent = (MyEvent) this.mEventQueue.first();
            int minPeriod = firstEvent.mMaxPeriod;
            long minTriggerTime = firstEvent.mTriggerTime;
            for (MyEvent e : this.mEventQueue) {
                e.mPeriod = (e.mMaxPeriod / minPeriod) * minPeriod;
                e.mTriggerTime = ((long) ((((int) ((e.mLastTriggerTime + ((long) e.mMaxPeriod)) - minTriggerTime)) / minPeriod) * minPeriod)) + minTriggerTime;
            }
            TreeSet<MyEvent> newQueue = new TreeSet(this.mEventQueue.comparator());
            newQueue.addAll(this.mEventQueue);
            this.mEventQueue.clear();
            this.mEventQueue = newQueue;
        }
    }

    private void insertEvent(MyEvent event) {
        long now = SystemClock.elapsedRealtime();
        if (this.mEventQueue.isEmpty()) {
            event.mTriggerTime = ((long) event.mPeriod) + now;
            this.mEventQueue.add(event);
            return;
        }
        MyEvent firstEvent = (MyEvent) this.mEventQueue.first();
        int minPeriod = firstEvent.mPeriod;
        if (minPeriod <= event.mMaxPeriod) {
            event.mPeriod = (event.mMaxPeriod / minPeriod) * minPeriod;
            event.mTriggerTime = firstEvent.mTriggerTime + ((long) (((event.mMaxPeriod - ((int) (firstEvent.mTriggerTime - now))) / minPeriod) * minPeriod));
            this.mEventQueue.add(event);
        } else {
            long triggerTime = now + ((long) event.mPeriod);
            if (firstEvent.mTriggerTime < triggerTime) {
                event.mTriggerTime = firstEvent.mTriggerTime;
                event.mLastTriggerTime -= (long) event.mPeriod;
            } else {
                event.mTriggerTime = triggerTime;
            }
            this.mEventQueue.add(event);
            recalculatePeriods();
        }
    }

    public synchronized void set(int period, Runnable callback) {
        if (!stopped()) {
            MyEvent event = new MyEvent(period, callback, SystemClock.elapsedRealtime());
            insertEvent(event);
            if (this.mEventQueue.first() == event) {
                if (this.mEventQueue.size() > 1) {
                    cancelAlarm();
                }
                scheduleNext();
            }
            long triggerTime = event.mTriggerTime;
        }
    }

    /* JADX WARNING: Missing block: B:7:0x0010, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:0x0042, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void cancel(Runnable callback) {
        if (!stopped() && !this.mEventQueue.isEmpty()) {
            MyEvent firstEvent = (MyEvent) this.mEventQueue.first();
            Iterator<MyEvent> iter = this.mEventQueue.iterator();
            while (iter.hasNext()) {
                if (((MyEvent) iter.next()).mCallback == callback) {
                    iter.remove();
                }
            }
            if (this.mEventQueue.isEmpty()) {
                cancelAlarm();
            } else if (this.mEventQueue.first() != firstEvent) {
                cancelAlarm();
                firstEvent = (MyEvent) this.mEventQueue.first();
                firstEvent.mPeriod = firstEvent.mMaxPeriod;
                firstEvent.mTriggerTime = firstEvent.mLastTriggerTime + ((long) firstEvent.mPeriod);
                recalculatePeriods();
                scheduleNext();
            }
        }
    }

    private void scheduleNext() {
        if (!stopped() && !this.mEventQueue.isEmpty()) {
            if (this.mPendingIntent != null) {
                throw new RuntimeException("pendingIntent is not null!");
            }
            MyEvent event = (MyEvent) this.mEventQueue.first();
            Intent intent = new Intent(getAction());
            intent.putExtra(TRIGGER_TIME, event.mTriggerTime);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
            this.mPendingIntent = pendingIntent;
            this.mAlarmManager.set(2, event.mTriggerTime, pendingIntent);
        }
    }

    public synchronized void onReceive(Context context, Intent intent) {
        if (getAction().equals(intent.getAction()) && intent.getExtras().containsKey(TRIGGER_TIME)) {
            this.mPendingIntent = null;
            execute(intent.getLongExtra(TRIGGER_TIME, -1));
        } else {
            log("onReceive: unrecognized intent: " + intent);
        }
    }

    private void printQueue() {
        int count = 0;
        for (MyEvent event : this.mEventQueue) {
            log("     " + event + ": scheduled at " + showTime(event.mTriggerTime) + ": last at " + showTime(event.mLastTriggerTime));
            count++;
            if (count >= 5) {
                break;
            }
        }
        if (this.mEventQueue.size() > count) {
            log("     .....");
        } else if (count == 0) {
            log("     <empty>");
        }
    }

    private void execute(long triggerTime) {
        if (!stopped() && !this.mEventQueue.isEmpty()) {
            for (MyEvent event : this.mEventQueue) {
                if (event.mTriggerTime == triggerTime) {
                    event.mLastTriggerTime = triggerTime;
                    event.mTriggerTime += (long) event.mPeriod;
                    this.mExecutor.execute(event.mCallback);
                }
            }
            scheduleNext();
        }
    }

    private String getAction() {
        return toString();
    }

    private String showTime(long time) {
        int ms = (int) (time % 1000);
        int s = (int) (time / 1000);
        int m = s / 60;
        s %= 60;
        return String.format("%d.%d.%d", new Object[]{Integer.valueOf(m), Integer.valueOf(s), Integer.valueOf(ms)});
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }
}
