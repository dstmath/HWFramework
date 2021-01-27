package ohos.workschedulerservice.controller;

import android.app.AlarmManager;
import android.content.Context;
import android.os.SystemClock;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.workschedulerservice.WorkQueueManager;

public final class TimerStateListener extends StateListener {
    private static final int LOG_DOMAIN = 218109696;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, LOG_DOMAIN, TAG);
    private static final String TAG = "TimerStateListener";
    private static final String TAG_EARLIEST_TIMER = "earliest_timer_listener";
    private static final String TAG_LATEST_TIMER = "latest_timer_listener";
    private static final int TIMER_TYPE_REALTIME = 1;
    private AlarmManager alarmTimer;
    private Context context;
    private final AlarmManager.OnAlarmListener earliestExpiredListener = new AlarmManager.OnAlarmListener() {
        /* class ohos.workschedulerservice.controller.TimerStateListener.AnonymousClass1 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            HiLog.debug(TimerStateListener.LOG_LABEL, "earliestExpiredListener trigger", new Object[0]);
            synchronized (TimerStateListener.this.lock) {
                TimerStateListener.this.updateTrackedWorksEarliestTriggerTime();
            }
        }
    };
    private volatile long lastEarliestTimeSetMillis;
    private final Object lock = new Object();
    private final LinkedList<WorkStatus> trackedTasks = new LinkedList<>();

    public TimerStateListener(WorkQueueManager workQueueManager) {
        super(workQueueManager);
    }

    public void init(Context context2) {
        if (context2 != null && this.workQueueMgr != null) {
            this.context = context2;
            this.lastEarliestTimeSetMillis = Long.MAX_VALUE;
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void dumpStateListenerStatus(PrintWriter printWriter, String str) {
        if (printWriter == null || str == null) {
            HiLog.error(LOG_LABEL, "error dump PrintWriter or prefix input", new Object[0]);
            return;
        }
        printWriter.println();
        printWriter.println("TimerStateListener:");
        synchronized (this.lock) {
            if (this.lastEarliestTimeSetMillis == Long.MAX_VALUE) {
                printWriter.println(str + "none lastEarliestTimeSetMillis info");
            } else {
                printWriter.println(str + "lastEarliestTimeSetMillis:" + formatTime(this.lastEarliestTimeSetMillis));
            }
            printWriter.println(str + "trackedTasks size:" + this.trackedTasks.size());
        }
    }

    private String formatTime(long j) {
        return new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date((System.currentTimeMillis() - SystemClock.elapsedRealtime()) + j));
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStartSignWork(WorkStatus workStatus) {
        if (workStatus == null) {
            HiLog.debug(LOG_LABEL, "try start failed, work invalid.", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            if (workStatus.hasRepeatCondition()) {
                this.trackedTasks.remove(workStatus);
                insertWorkAccordingLatestTime(workStatus);
                updateTrackedWorksEarliestTriggerTime();
            }
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStopSignWork(WorkStatus workStatus) {
        if (workStatus == null) {
            HiLog.debug(LOG_LABEL, "try stop failed, work invalid.", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            this.trackedTasks.remove(workStatus);
            updateTrackedWorksEarliestTriggerTime();
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void updateTrackedTasks(WorkStatus workStatus) {
        HiLog.debug(LOG_LABEL, "updateTrackedTasks.", new Object[0]);
    }

    private void insertWorkAccordingLatestTime(WorkStatus workStatus) {
        boolean z;
        ListIterator<WorkStatus> listIterator = this.trackedTasks.listIterator();
        while (true) {
            if (listIterator.hasNext()) {
                if (listIterator.next().getEarliestRunTime() > workStatus.getEarliestRunTime()) {
                    z = true;
                    break;
                }
            } else {
                z = false;
                break;
            }
        }
        if (z) {
            listIterator.previous();
        }
        listIterator.add(workStatus);
    }

    private void updateEarliestTriggerTimer(long j) {
        if (this.lastEarliestTimeSetMillis != j) {
            useAlarmTimer(j, 1);
            this.lastEarliestTimeSetMillis = j;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTrackedWorksEarliestTriggerTime() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        LinkedList linkedList = new LinkedList();
        Iterator<WorkStatus> it = this.trackedTasks.iterator();
        HiLog.debug(LOG_LABEL, "before check timer: %{public}d", Integer.valueOf(this.trackedTasks.size()));
        while (it.hasNext()) {
            WorkStatus next = it.next();
            if (next.getEarliestRunTime() <= elapsedRealtime && next.changeTimingRepeatSatisfiedCondition(true)) {
                it.remove();
                linkedList.add(next);
                this.workQueueMgr.onDeviceStateChanged(next, 6);
            }
        }
        HiLog.debug(LOG_LABEL, "after check timer: %{public}d", Integer.valueOf(this.trackedTasks.size()));
        Iterator it2 = linkedList.iterator();
        while (it2.hasNext()) {
            WorkStatus workStatus = (WorkStatus) it2.next();
            if (!workStatus.isRepeatOutTimes()) {
                insertWorkAccordingLatestTime(workStatus);
            }
        }
        HiLog.debug(LOG_LABEL, "after add timer: %{public}d", Integer.valueOf(this.trackedTasks.size()));
        linkedList.clear();
        long j = Long.MAX_VALUE;
        Iterator<WorkStatus> it3 = this.trackedTasks.iterator();
        while (true) {
            if (!it3.hasNext()) {
                break;
            }
            WorkStatus next2 = it3.next();
            if (next2.getEarliestRunTime() > elapsedRealtime) {
                HiLog.info(LOG_LABEL, "set nextTriggerTime called", new Object[0]);
                j = next2.getEarliestRunTime();
                break;
            }
        }
        HiLog.debug(LOG_LABEL, "EarliestTimer %{public}d -> %{public}d", Long.valueOf(this.lastEarliestTimeSetMillis), Long.valueOf(j));
        updateEarliestTriggerTimer(j);
    }

    private void useAlarmTimer(long j, int i) {
        if (this.alarmTimer == null) {
            if (this.context.getSystemService("alarm") instanceof AlarmManager) {
                this.alarmTimer = (AlarmManager) this.context.getSystemService("alarm");
            } else {
                return;
            }
        }
        AlarmManager.OnAlarmListener onAlarmListener = this.earliestExpiredListener;
        if (j == Long.MAX_VALUE) {
            this.alarmTimer.cancel(onAlarmListener);
        } else {
            this.alarmTimer.set(3, j, TAG_EARLIEST_TIMER, onAlarmListener, null);
        }
    }
}
