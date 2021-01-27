package com.android.server;

import android.app.AlarmManager;
import android.app.IAlarmListener;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.WorkSource;
import android.util.ArrayMap;
import com.android.server.AlarmManagerService;
import com.huawei.android.app.IAlarmListenerEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AlarmManagerServiceExt {
    protected static final boolean DEBUG_BATCH = false;
    private static final int INVALID_DATA = -1;
    public static final int PHASE_BOOT_COMPLETED = 1000;
    private AlarmManagerServiceBridge mBridge = null;

    public AlarmManagerServiceExt(Context context) {
        this.mBridge = new AlarmManagerServiceBridge(context);
        this.mBridge.setAlarmManagerServiceExt(this);
    }

    public AlarmManagerService getAlarmManagerService() {
        return this.mBridge;
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
    }

    /* access modifiers changed from: protected */
    public void adjustAlarmLocked(AlarmEx alarmEx) {
    }

    /* access modifiers changed from: protected */
    public long checkHasHwRTCAlarmLock(String packageName) {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void adjustHwRTCAlarmLock(boolean isDeskClock, boolean isBoot, int typeState) {
    }

    /* access modifiers changed from: protected */
    public void printHwWakeupBoot(PrintWriter pw) {
    }

    /* access modifiers changed from: protected */
    public void hwRemoveRtcAlarm(AlarmEx alarmEx, boolean isCancel) {
    }

    /* access modifiers changed from: protected */
    public void hwSetRtcAlarm(AlarmEx alarmEx) {
    }

    /* access modifiers changed from: protected */
    public void hwAddFirstFlagForRtcAlarm(AlarmEx alarmEx, Intent backgroundIntent) {
    }

    /* access modifiers changed from: protected */
    public void setHwRTCAlarmLock() {
    }

    /* access modifiers changed from: protected */
    public void removeDeskClockFromFWK(PendingIntent operation) {
    }

    /* access modifiers changed from: protected */
    public void hwRecordFirstTime() {
    }

    /* access modifiers changed from: protected */
    public void hwRecordTimeChangeRTC(long nowRTC, long nowELAPSED, long lastTimeChangeClockTime, long expectedClockTime) {
    }

    /* access modifiers changed from: protected */
    public boolean isContainsAppUidInWorksource(WorkSource worksource, String packageName) {
        return false;
    }

    public void removePackageAlarm(String pkg, List<String> list, int targetUid) {
    }

    /* access modifiers changed from: protected */
    public void modifyAlarmIfOverload(AlarmEx alarmEx) {
    }

    /* access modifiers changed from: protected */
    public void reportWakeupAlarms(ArrayList<AlarmEx> arrayList) {
    }

    /* access modifiers changed from: protected */
    public boolean isAwareAlarmManagerEnabled() {
        return false;
    }

    /* access modifiers changed from: protected */
    public int getWakeUpNumImpl(int uid, String pkg) {
        return 0;
    }

    public void setAlarmExemption(List<String> list, int type) {
    }

    /* access modifiers changed from: protected */
    public void publishBinderService(String name, IBinder service) {
        this.mBridge.publishBinderService(name, service);
    }

    /* access modifiers changed from: protected */
    public Object getLock() {
        return this.mBridge.mLock;
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mBridge.getContext();
    }

    /* access modifiers changed from: protected */
    public void setIsCancelRemoveAction(boolean isCancelRemoveAction) {
        this.mBridge.mIsCancelRemoveAction = isCancelRemoveAction;
    }

    /* access modifiers changed from: protected */
    public void restorePendingWhileIdleAlarmsLocked() {
        this.mBridge.restorePendingWhileIdleAlarmsLocked();
    }

    /* access modifiers changed from: protected */
    public void rescheduleKernelAlarmsLocked() {
        this.mBridge.rescheduleKernelAlarmsLocked();
    }

    /* access modifiers changed from: protected */
    public void updateNextAlarmClockLocked() {
        this.mBridge.updateNextAlarmClockLocked();
    }

    /* access modifiers changed from: protected */
    public void hwSetClockRTC(long seconds, long nanoseconds) {
        this.mBridge.mInjector.hwSetClockRTC(seconds, nanoseconds);
    }

    /* access modifiers changed from: protected */
    public void rebatchAllAlarmsLocked(boolean isDoValidate) {
        this.mBridge.rebatchAllAlarmsLocked(isDoValidate);
    }

    /* access modifiers changed from: protected */
    public void removeLocked(PendingIntent operation, IAlarmListenerEx directReceiverEx) {
        IAlarmListener directReceiver = null;
        if (directReceiverEx != null) {
            directReceiver = directReceiverEx.getIAlarmListener();
        }
        this.mBridge.removeLocked(operation, directReceiver);
    }

    /* access modifiers changed from: protected */
    public void setImpl(int type, long triggerAtTime, long windowLength, long interval, PendingIntent operation, IAlarmListenerEx directReceiverEx, String listenerTag, int flags, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        IAlarmListener directReceiver = null;
        if (directReceiverEx != null) {
            directReceiver = directReceiverEx.getIAlarmListener();
        }
        this.mBridge.setImpl(type, triggerAtTime, windowLength, interval, operation, directReceiver, listenerTag, flags, workSource, alarmClock, callingUid, callingPackage);
    }

    /* access modifiers changed from: protected */
    public void reAddAlarmLocked(AlarmEx alarmEx, long nowElapsed, boolean isDoValidate) {
        this.mBridge.reAddAlarmLocked(alarmEx.getAlarm(), nowElapsed, isDoValidate);
    }

    /* access modifiers changed from: protected */
    public int getAlarmBatchesSize() {
        return this.mBridge.mAlarmBatches.size();
    }

    /* access modifiers changed from: protected */
    public BatchEx getAlarmBatches(int index) {
        if (this.mBridge.mAlarmBatches.size() <= index) {
            return new BatchEx();
        }
        BatchEx batchEx = new BatchEx();
        batchEx.setBatch((AlarmManagerService.Batch) this.mBridge.mAlarmBatches.get(index));
        return batchEx;
    }

    /* access modifiers changed from: protected */
    public void removeAlarmBatches(int index) {
        this.mBridge.mAlarmBatches.remove(index);
    }

    /* access modifiers changed from: protected */
    public int getPendingNonWakeupAlarmsSize() {
        return this.mBridge.mPendingNonWakeupAlarms.size();
    }

    /* access modifiers changed from: protected */
    public AlarmEx getPendingNonWakeupAlarms(int index) {
        if (this.mBridge.mPendingNonWakeupAlarms.size() <= index) {
            return new AlarmEx();
        }
        AlarmEx alarmEx = new AlarmEx();
        alarmEx.setAlarm((AlarmManagerService.Alarm) this.mBridge.mPendingNonWakeupAlarms.get(index));
        return alarmEx;
    }

    /* access modifiers changed from: protected */
    public void removePendingNonWakeupAlarms(int index) {
        this.mBridge.mPendingNonWakeupAlarms.remove(index);
    }

    /* access modifiers changed from: protected */
    public int getPendingWhileIdleAlarmsSize() {
        return this.mBridge.mPendingWhileIdleAlarms.size();
    }

    /* access modifiers changed from: protected */
    public AlarmEx getPendingWhileIdleAlarms(int index) {
        if (this.mBridge.mPendingWhileIdleAlarms.size() <= index) {
            return new AlarmEx();
        }
        AlarmEx alarmEx = new AlarmEx();
        alarmEx.setAlarm((AlarmManagerService.Alarm) this.mBridge.mPendingWhileIdleAlarms.get(index));
        return alarmEx;
    }

    /* access modifiers changed from: protected */
    public void removePendingWhileIdleAlarms(int index) {
        this.mBridge.mPendingWhileIdleAlarms.remove(index);
    }

    /* access modifiers changed from: protected */
    public void decrementAlarmCount(int uid, int decrement) {
        this.mBridge.decrementAlarmCount(uid, decrement);
    }

    /* access modifiers changed from: protected */
    public AlarmEx getPendingIdleUntil() {
        AlarmManagerService.Alarm alarm = this.mBridge.mPendingIdleUntil;
        if (alarm == null) {
            return null;
        }
        AlarmEx alarmEx = new AlarmEx();
        alarmEx.setAlarm(alarm);
        return alarmEx;
    }

    /* access modifiers changed from: protected */
    public void setNextAlarmClockMayChange(boolean isChange) {
        this.mBridge.mNextAlarmClockMayChange = isChange;
    }

    /* access modifiers changed from: protected */
    public void postHandler(Runnable r) {
        if (this.mBridge.mHandler != null) {
            this.mBridge.mHandler.post(r);
        }
    }

    /* access modifiers changed from: protected */
    public int getWakeUpNumImplEx(int uid, String pkg) {
        ArrayMap<String, AlarmManagerService.BroadcastStats> uidStats = (ArrayMap) this.mBridge.mBroadcastStats.get(uid);
        if (uidStats == null) {
            uidStats = new ArrayMap<>();
            this.mBridge.mBroadcastStats.put(uid, uidStats);
        }
        AlarmManagerService.BroadcastStats bs = uidStats.get(pkg);
        if (bs == null) {
            bs = new AlarmManagerService.BroadcastStats(uid, pkg);
            uidStats.put(pkg, bs);
        }
        return bs.numWakeup;
    }

    public static long maxTriggerTime(long now, long triggerAtTime, long interval) {
        return AlarmManagerService.maxTriggerTime(now, triggerAtTime, interval);
    }

    /* access modifiers changed from: protected */
    public void rebatchPkgAlarmsLocked(boolean isByPkgs, List<String> list, List<String> list2) {
    }

    public static class AlarmEx {
        private AlarmManagerService.Alarm mAlarm = null;

        public void setAlarm(AlarmManagerService.Alarm alarm) {
            this.mAlarm = alarm;
        }

        public AlarmManagerService.Alarm getAlarm() {
            return this.mAlarm;
        }

        /* access modifiers changed from: protected */
        public boolean isAlarmEquals(AlarmEx alarmEx) {
            return alarmEx != null ? this.mAlarm == alarmEx.mAlarm : this.mAlarm == null;
        }

        public static String makeTag(PendingIntent pi, String tag, int type) {
            return AlarmManagerService.Alarm.makeTag(pi, tag, type);
        }

        public int getType() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return -1;
            }
            return alarm.type;
        }

        public int getUid() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return -1;
            }
            return alarm.uid;
        }

        public String getPkgName() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return null;
            }
            return alarm.packageName;
        }

        public String getProcName() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return null;
            }
            return alarm.procName;
        }

        public boolean getWakeup() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return false;
            }
            return alarm.wakeup;
        }

        public String getStatsTag() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return null;
            }
            return alarm.statsTag;
        }

        public long getWhenElapsed() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return 0;
            }
            return alarm.whenElapsed;
        }

        public long getWindowLength() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return 0;
            }
            return alarm.windowLength;
        }

        public long getRepeatInterval() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return 0;
            }
            return alarm.repeatInterval;
        }

        public long getMaxWhenElapsed() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return 0;
            }
            return alarm.maxWhenElapsed;
        }

        public void setWhenElapsed(long whenElapsed) {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm != null) {
                alarm.whenElapsed = whenElapsed;
            }
        }

        public void setMaxWhenElapsed(long maxWhenElapsed) {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm != null) {
                alarm.maxWhenElapsed = maxWhenElapsed;
            }
        }

        public void setWakeup(boolean isWakeup) {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm != null) {
                alarm.wakeup = isWakeup;
            }
        }

        public int getCreatorUid() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return -1;
            }
            return alarm.creatorUid;
        }

        public PendingIntent getOperation() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return null;
            }
            return alarm.operation;
        }

        public String getListenerTag() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return null;
            }
            return alarm.listenerTag;
        }

        public long getWhen() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return 0;
            }
            return alarm.when;
        }

        public AlarmManager.AlarmClockInfo getAlarmClock() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return null;
            }
            return alarm.alarmClock;
        }

        public int getFlags() {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return -1;
            }
            return alarm.flags;
        }

        public boolean matches(String packageName) {
            AlarmManagerService.Alarm alarm = this.mAlarm;
            if (alarm == null) {
                return false;
            }
            return alarm.matches(packageName);
        }
    }

    public static class BatchEx {
        public ArrayList<AlarmEx> alarmsEx = new ArrayList<>();
        private AlarmManagerService.Batch mBatch = null;

        public void setBatch(AlarmManagerService.Batch batch) {
            this.mBatch = batch;
            Iterator it = this.mBatch.alarms.iterator();
            while (it.hasNext()) {
                AlarmEx alarmEx = new AlarmEx();
                alarmEx.setAlarm((AlarmManagerService.Alarm) it.next());
                this.alarmsEx.add(alarmEx);
            }
        }

        public AlarmManagerService.Batch getBatch() {
            return this.mBatch;
        }

        public int size() {
            AlarmManagerService.Batch batch = this.mBatch;
            if (batch == null) {
                return 0;
            }
            return batch.size();
        }

        public AlarmEx get(int index) {
            AlarmManagerService.Batch batch = this.mBatch;
            if (batch == null) {
                return null;
            }
            AlarmManagerService.Alarm alarm = batch.get(index);
            AlarmEx alarmEx = new AlarmEx();
            alarmEx.setAlarm(alarm);
            return alarmEx;
        }

        public void setStart(long start) {
            this.mBatch.start = start;
        }

        public void setEnd(long end) {
            this.mBatch.end = end;
        }

        public void setFlags(int flags) {
            this.mBatch.flags = flags;
        }
    }
}
