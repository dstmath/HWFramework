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

/* access modifiers changed from: package-private */
public class SipWakeupTimer extends BroadcastReceiver {
    private static final boolean DBG = false;
    private static final String TAG = "SipWakeupTimer";
    private static final String TRIGGER_TIME = "TriggerTime";
    private AlarmManager mAlarmManager;
    private Context mContext;
    private TreeSet<MyEvent> mEventQueue = new TreeSet<>(new MyEventComparator());
    private Executor mExecutor;
    private PendingIntent mPendingIntent;

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
            MyEvent firstEvent = this.mEventQueue.first();
            int minPeriod = firstEvent.mMaxPeriod;
            long minTriggerTime = firstEvent.mTriggerTime;
            Iterator<MyEvent> it = this.mEventQueue.iterator();
            while (it.hasNext()) {
                MyEvent e = it.next();
                e.mPeriod = (e.mMaxPeriod / minPeriod) * minPeriod;
                e.mTriggerTime = ((long) ((((int) ((e.mLastTriggerTime + ((long) e.mMaxPeriod)) - minTriggerTime)) / minPeriod) * minPeriod)) + minTriggerTime;
            }
            TreeSet<MyEvent> newQueue = new TreeSet<>(this.mEventQueue.comparator());
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
        MyEvent firstEvent = this.mEventQueue.first();
        int minPeriod = firstEvent.mPeriod;
        if (minPeriod <= event.mMaxPeriod) {
            event.mPeriod = (event.mMaxPeriod / minPeriod) * minPeriod;
            event.mTriggerTime = firstEvent.mTriggerTime + ((long) (((event.mMaxPeriod - ((int) (firstEvent.mTriggerTime - now))) / minPeriod) * minPeriod));
            this.mEventQueue.add(event);
            return;
        }
        long triggerTime = ((long) event.mPeriod) + now;
        if (firstEvent.mTriggerTime < triggerTime) {
            event.mTriggerTime = firstEvent.mTriggerTime;
            event.mLastTriggerTime -= (long) event.mPeriod;
        } else {
            event.mTriggerTime = triggerTime;
        }
        this.mEventQueue.add(event);
        recalculatePeriods();
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
            long j = event.mTriggerTime;
        }
    }

    public synchronized void cancel(Runnable callback) {
        if (!stopped()) {
            if (!this.mEventQueue.isEmpty()) {
                MyEvent firstEvent = this.mEventQueue.first();
                Iterator<MyEvent> iter = this.mEventQueue.iterator();
                while (iter.hasNext()) {
                    if (iter.next().mCallback == callback) {
                        iter.remove();
                    }
                }
                if (this.mEventQueue.isEmpty()) {
                    cancelAlarm();
                } else if (this.mEventQueue.first() != firstEvent) {
                    cancelAlarm();
                    MyEvent firstEvent2 = this.mEventQueue.first();
                    firstEvent2.mPeriod = firstEvent2.mMaxPeriod;
                    firstEvent2.mTriggerTime = firstEvent2.mLastTriggerTime + ((long) firstEvent2.mPeriod);
                    recalculatePeriods();
                    scheduleNext();
                }
            }
        }
    }

    private void scheduleNext() {
        if (!stopped() && !this.mEventQueue.isEmpty()) {
            if (this.mPendingIntent == null) {
                MyEvent event = this.mEventQueue.first();
                Intent intent = new Intent(getAction());
                intent.putExtra(TRIGGER_TIME, event.mTriggerTime);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
                this.mPendingIntent = pendingIntent;
                this.mAlarmManager.set(2, event.mTriggerTime, pendingIntent);
                return;
            }
            throw new RuntimeException("pendingIntent is not null!");
        }
    }

    public synchronized void onReceive(Context context, Intent intent) {
        if (!getAction().equals(intent.getAction()) || intent.getExtras() == null || !intent.getExtras().containsKey(TRIGGER_TIME)) {
            log("onReceive: unrecognized intent: " + intent);
        } else {
            this.mPendingIntent = null;
            execute(intent.getLongExtra(TRIGGER_TIME, -1));
        }
    }

    private void printQueue() {
        int count = 0;
        Iterator<MyEvent> it = this.mEventQueue.iterator();
        while (it.hasNext()) {
            MyEvent event = it.next();
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
            Iterator<MyEvent> it = this.mEventQueue.iterator();
            while (it.hasNext()) {
                MyEvent event = it.next();
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
        int s = (int) (time / 1000);
        return String.format("%d.%d.%d", Integer.valueOf(s / 60), Integer.valueOf(s % 60), Integer.valueOf((int) (time % 1000)));
    }

    /* access modifiers changed from: private */
    public static class MyEvent {
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
            String s2 = s.substring(s.indexOf("@"));
            return s2 + ":" + (this.mPeriod / 1000) + ":" + (this.mMaxPeriod / 1000) + ":" + toString(this.mCallback);
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
        private MyEventComparator() {
        }

        public int compare(MyEvent e1, MyEvent e2) {
            if (e1 == e2) {
                return 0;
            }
            int diff = e1.mMaxPeriod - e2.mMaxPeriod;
            if (diff == 0) {
                return -1;
            }
            return diff;
        }

        public boolean equals(Object that) {
            if (this == that) {
                return true;
            }
            return SipWakeupTimer.DBG;
        }
    }

    private void log(String s) {
        Rlog.d(TAG, s);
    }
}
