package ohos.aafwk.ability;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import ohos.aafwk.ability.FormAdapter;
import ohos.event.notification.NotificationRequest;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.system.Parameters;

/* access modifiers changed from: package-private */
public final class FormTimerManager {
    private static final long ABS_TIME = 5000;
    private static final String ACTION_UPDATEATTIMER = "form_update_at_timer";
    private static final int BASE_TIMER_POOL_SIZE = 1;
    private static final String FOUNDATION_NAME = "com.huawei.harmonyos.foundation";
    private static final Object INSTANCE_LOCK = new Object();
    private static final String KEY_FORM_ID = "formId";
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218108160, "FormTimerManager");
    private static final int MAX_HOUR = 23;
    private static final int MAX_MININUTE = 59;
    private static final long MAX_PERIOD = (MIN_PERIOD * 336);
    private static final long MIN_PERIOD = (((long) (Parameters.getInt("persist.sys.fms.form.update.time", 30) * 60)) * 1000);
    private static final int MIN_TIME = 0;
    private static final long TIME_CONVERSION = 1000;
    private static final int WORK_POOL_SIZE = 4;
    private final Object LOCK = new Object();
    private AlarmManager alarmManager = null;
    private long basePeriod = Long.MAX_VALUE;
    private RunnableScheduledFuture<?> baseTimerTask = null;
    private HashMap<Integer, FormAdapter.RefreshRunnable> tasks = new HashMap<>();
    private ScheduledThreadPoolExecutor timerExecutor = null;
    private TimerReceiver timerReceiver = null;
    private HashMap<Integer, UpdateAtItem> updateAtTasks = new HashMap<>();
    private ExecutorService workThreadPool = null;

    FormTimerManager() {
    }

    /* access modifiers changed from: private */
    public static class Holder {
        private static final FormTimerManager INSTANCE = new FormTimerManager();

        private Holder() {
        }
    }

    static FormTimerManager getInstance() {
        return Holder.INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public boolean addFormTimer(FormAdapter.RefreshRunnable refreshRunnable) {
        if (refreshRunnable == null) {
            HiLog.error(LABEL_LOG, "addFormTimer task is null", new Object[0]);
            return false;
        } else if (refreshRunnable.isUpdateAt) {
            return addUpdateAtTimer(refreshRunnable);
        } else {
            return addIntervalTimer(refreshRunnable);
        }
    }

    /* access modifiers changed from: package-private */
    public void deleteFormTimer(int i) {
        ExecutorService executorService;
        List<FormAdapter.RefreshRunnable> list;
        synchronized (this.LOCK) {
            if (this.updateAtTasks.containsKey(Integer.valueOf(i))) {
                deleteUpdateAtTimerLocked(i);
                return;
            }
            executorService = null;
            if (this.tasks.containsKey(Integer.valueOf(i))) {
                list = deleteIntervalTimerLocked(i);
                if (list != null && !list.isEmpty()) {
                    executorService = getWorkThreadPoolLocked();
                }
            } else {
                list = null;
            }
        }
        if (!(list == null || list.isEmpty() || executorService == null || !FormAdapter.getInstance().isScreenOn())) {
            HiLog.info(LABEL_LOG, "force update when delete interval timer", new Object[0]);
            for (FormAdapter.RefreshRunnable refreshRunnable : list) {
                executorService.execute(refreshRunnable);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onTimeOut() {
        ExecutorService workThreadPoolLocked;
        if (!FormAdapter.getInstance().isScreenOn()) {
            HiLog.debug(LABEL_LOG, "screen off,timer do not refresh", new Object[0]);
            return;
        }
        ArrayList<FormAdapter.RefreshRunnable> arrayList = new ArrayList();
        synchronized (this.LOCK) {
            long nanoSecondsToMills = nanoSecondsToMills(System.nanoTime());
            for (FormAdapter.RefreshRunnable refreshRunnable : this.tasks.values()) {
                if (refreshRunnable.refreshTime == Long.MAX_VALUE || nanoSecondsToMills - refreshRunnable.refreshTime >= refreshRunnable.period || Math.abs((nanoSecondsToMills - refreshRunnable.refreshTime) - refreshRunnable.period) < ABS_TIME) {
                    refreshRunnable.refreshTime = nanoSecondsToMills;
                    arrayList.add(refreshRunnable);
                }
            }
            workThreadPoolLocked = getWorkThreadPoolLocked();
        }
        if (!(workThreadPoolLocked == null || arrayList.isEmpty())) {
            for (FormAdapter.RefreshRunnable refreshRunnable2 : arrayList) {
                workThreadPoolLocked.execute(refreshRunnable2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onUpdateAtTrigger(int i) {
        FormAdapter.RefreshRunnable refreshRunnable;
        ExecutorService executorService;
        HiLog.debug(LABEL_LOG, "onUpdateAtTrigger, formId:%{public}d", new Object[]{Integer.valueOf(i)});
        synchronized (this.LOCK) {
            UpdateAtItem updateAtItem = this.updateAtTasks.get(Integer.valueOf(i));
            refreshRunnable = null;
            if (updateAtItem != null) {
                setAlarmLocked(updateAtItem);
                if (FormAdapter.getInstance().isScreenOn()) {
                    refreshRunnable = updateAtItem.refreshTask;
                    executorService = getWorkThreadPoolLocked();
                }
            }
            executorService = null;
        }
        if (refreshRunnable != null && executorService != null) {
            HiLog.debug(LABEL_LOG, "onUpdateAtTrigger, refresh form formId:%{public}d", new Object[]{Integer.valueOf(i)});
            executorService.execute(refreshRunnable);
        }
    }

    private boolean addIntervalTimer(FormAdapter.RefreshRunnable refreshRunnable) {
        ArrayList<FormAdapter.RefreshRunnable> arrayList;
        ExecutorService executorService;
        if (refreshRunnable.period < MIN_PERIOD || refreshRunnable.period > MAX_PERIOD || refreshRunnable.period % MIN_PERIOD != 0) {
            HiLog.error(LABEL_LOG, "addIntervalTimer invalid param", new Object[0]);
            return false;
        }
        synchronized (this.LOCK) {
            if (this.tasks.containsKey(Integer.valueOf(refreshRunnable.formId))) {
                HiLog.error(LABEL_LOG, "already exist formTimer, formId:%{public}d task", new Object[]{Integer.valueOf(refreshRunnable.formId)});
                return false;
            }
            HiLog.info(LABEL_LOG, "FormAdapter period:%{public}d", new Object[]{Long.valueOf(refreshRunnable.period)});
            arrayList = null;
            if (adjustBaseTimerLocked(refreshRunnable.period)) {
                HiLog.info(LABEL_LOG, "need force update", new Object[0]);
                arrayList = new ArrayList();
                long nanoSecondsToMills = nanoSecondsToMills(System.nanoTime());
                for (FormAdapter.RefreshRunnable refreshRunnable2 : this.tasks.values()) {
                    refreshRunnable2.refreshTime = nanoSecondsToMills;
                    arrayList.add(refreshRunnable2);
                }
                executorService = getWorkThreadPoolLocked();
            } else {
                executorService = null;
            }
            this.tasks.put(Integer.valueOf(refreshRunnable.formId), refreshRunnable);
        }
        if (arrayList != null && !arrayList.isEmpty() && executorService != null && FormAdapter.getInstance().isScreenOn()) {
            HiLog.info(LABEL_LOG, "force update", new Object[0]);
            for (FormAdapter.RefreshRunnable refreshRunnable3 : arrayList) {
                executorService.execute(refreshRunnable3);
            }
        }
        HiLog.info(LABEL_LOG, "addIntervalTimer end", new Object[0]);
        return true;
    }

    private boolean addUpdateAtTimer(FormAdapter.RefreshRunnable refreshRunnable) {
        if (refreshRunnable.hour < 0 || refreshRunnable.hour > 23 || refreshRunnable.min < 0 || refreshRunnable.min > 59) {
            HiLog.error(LABEL_LOG, "addUpdateAtTimer time is invalid", new Object[0]);
            return false;
        }
        synchronized (this.LOCK) {
            if (this.updateAtTasks.containsKey(Integer.valueOf(refreshRunnable.formId))) {
                HiLog.error(LABEL_LOG, "already exist formTimer, formId:%{public}d task", new Object[]{Integer.valueOf(refreshRunnable.formId)});
                return false;
            } else if (getAlarmManagerLocked() == null) {
                HiLog.error(LABEL_LOG, "faied to get alarm manager, can not schedul updateAt timer", new Object[0]);
                return false;
            } else {
                UpdateAtItem createRefreshItem = createRefreshItem(refreshRunnable);
                if (createRefreshItem == null) {
                    HiLog.error(LABEL_LOG, "faied to create update item.", new Object[0]);
                    return false;
                } else if (!initTimerReceiverLocked()) {
                    HiLog.error(LABEL_LOG, "faied to init timer task handler.", new Object[0]);
                    return false;
                } else if (!setAlarmLocked(createRefreshItem)) {
                    HiLog.error(LABEL_LOG, "faied to set alarm.", new Object[0]);
                    return false;
                } else {
                    this.updateAtTasks.put(Integer.valueOf(refreshRunnable.formId), createRefreshItem);
                    HiLog.info(LABEL_LOG, "FormAdapter updateAt timer,time %{public}d:%{public}d", new Object[]{Integer.valueOf(refreshRunnable.hour), Integer.valueOf(refreshRunnable.min)});
                    return true;
                }
            }
        }
    }

    private boolean setAlarmLocked(UpdateAtItem updateAtItem) {
        FormAdapter.RefreshRunnable refreshRunnable = updateAtItem.refreshTask;
        if (refreshRunnable == null || !refreshRunnable.isUpdateAt) {
            return false;
        }
        AlarmManager alarmManagerLocked = getAlarmManagerLocked();
        if (alarmManagerLocked == null) {
            HiLog.error(LABEL_LOG, "faied to get alarm manager, can not schedul updateAt timer", new Object[0]);
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(currentTimeMillis);
        instance.set(11, refreshRunnable.hour);
        instance.set(5, Calendar.getInstance().get(5));
        instance.set(12, refreshRunnable.min);
        instance.set(13, 0);
        instance.set(14, 0);
        if (instance.getTimeInMillis() < currentTimeMillis) {
            instance.add(5, 1);
        }
        alarmManagerLocked.setExact(0, instance.getTimeInMillis(), updateAtItem.pendingItent);
        return true;
    }

    private List<FormAdapter.RefreshRunnable> deleteIntervalTimerLocked(int i) {
        if (this.tasks.get(Integer.valueOf(i)) == null) {
            return null;
        }
        this.tasks.remove(Integer.valueOf(i));
        if (!this.tasks.isEmpty()) {
            return adjustDeletedTimerLocked();
        }
        clearIntervalTimerResourceLocked();
        return null;
    }

    private void deleteUpdateAtTimerLocked(int i) {
        ExecutorService executorService;
        UpdateAtItem updateAtItem = this.updateAtTasks.get(Integer.valueOf(i));
        if (updateAtItem != null) {
            this.updateAtTasks.remove(Integer.valueOf(i));
            AlarmManager alarmManagerLocked = getAlarmManagerLocked();
            if (!(alarmManagerLocked == null || updateAtItem.pendingItent == null)) {
                alarmManagerLocked.cancel(updateAtItem.pendingItent);
            }
            if (this.updateAtTasks.isEmpty() && this.tasks.isEmpty() && (executorService = this.workThreadPool) != null) {
                executorService.shutdownNow();
                this.workThreadPool = null;
            }
        }
    }

    private long findMinPeriodLocked() {
        ArrayList arrayList = new ArrayList();
        for (FormAdapter.RefreshRunnable refreshRunnable : this.tasks.values()) {
            arrayList.add(Long.valueOf(refreshRunnable.period));
        }
        if (arrayList.isEmpty()) {
            return Long.MAX_VALUE;
        }
        long longValue = ((Long) arrayList.get(0)).longValue();
        for (int i = 1; i < arrayList.size(); i++) {
            longValue = getGcd(longValue, ((Long) arrayList.get(i)).longValue());
            if (longValue == MIN_PERIOD) {
                return longValue;
            }
        }
        return longValue;
    }

    private List<FormAdapter.RefreshRunnable> adjustDeletedTimerLocked() {
        long findMinPeriodLocked = findMinPeriodLocked();
        ArrayList arrayList = null;
        if (!(findMinPeriodLocked == this.basePeriod || findMinPeriodLocked == Long.MAX_VALUE || this.baseTimerTask == null)) {
            ScheduledThreadPoolExecutor baseTimerExecutorLocked = getBaseTimerExecutorLocked();
            baseTimerExecutorLocked.remove(this.baseTimerTask);
            this.basePeriod = findMinPeriodLocked;
            TimerRunnable timerRunnable = new TimerRunnable();
            long j = this.basePeriod;
            ScheduledFuture<?> scheduleAtFixedRate = baseTimerExecutorLocked.scheduleAtFixedRate(timerRunnable, j, j, TimeUnit.MILLISECONDS);
            if (scheduleAtFixedRate instanceof RunnableScheduledFuture) {
                this.baseTimerTask = (RunnableScheduledFuture) scheduleAtFixedRate;
            }
            arrayList = new ArrayList();
            long nanoSecondsToMills = nanoSecondsToMills(System.nanoTime());
            for (FormAdapter.RefreshRunnable refreshRunnable : this.tasks.values()) {
                refreshRunnable.refreshTime = nanoSecondsToMills;
                arrayList.add(refreshRunnable);
            }
        }
        return arrayList;
    }

    private long findAdjustPeriodLocked(long j) {
        ArrayList arrayList = new ArrayList();
        for (FormAdapter.RefreshRunnable refreshRunnable : this.tasks.values()) {
            arrayList.add(Long.valueOf(refreshRunnable.period));
        }
        for (int i = 0; i < arrayList.size(); i++) {
            j = getGcd(j, ((Long) arrayList.get(i)).longValue());
            if (j == MIN_PERIOD) {
                return j;
            }
        }
        return j;
    }

    private long getGcd(long j, long j2) {
        if (j < j2) {
            j = j2;
            j2 = j;
        }
        long j3 = j % j2;
        if (j3 == 0) {
            return j2;
        }
        return getGcd(j2, j3);
    }

    private void clearIntervalTimerResourceLocked() {
        ExecutorService executorService;
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = this.timerExecutor;
        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdownNow();
            this.timerExecutor = null;
            this.basePeriod = Long.MAX_VALUE;
        }
        if (this.baseTimerTask != null) {
            this.baseTimerTask = null;
        }
        if (this.updateAtTasks.isEmpty() && (executorService = this.workThreadPool) != null) {
            executorService.shutdownNow();
            this.workThreadPool = null;
        }
    }

    private ScheduledThreadPoolExecutor getBaseTimerExecutorLocked() {
        if (this.timerExecutor == null) {
            this.timerExecutor = new ScheduledThreadPoolExecutor(1);
        }
        return this.timerExecutor;
    }

    private ExecutorService getWorkThreadPoolLocked() {
        if (this.workThreadPool == null) {
            this.workThreadPool = Executors.newFixedThreadPool(4);
        }
        return this.workThreadPool;
    }

    private boolean adjustBaseTimerLocked(long j) {
        ScheduledThreadPoolExecutor baseTimerExecutorLocked = getBaseTimerExecutorLocked();
        RunnableScheduledFuture<?> runnableScheduledFuture = this.baseTimerTask;
        if (runnableScheduledFuture == null) {
            this.basePeriod = j;
            HiLog.info(LABEL_LOG, "FormAdapter period:%{public}d", new Object[]{Long.valueOf(this.basePeriod)});
            TimerRunnable timerRunnable = new TimerRunnable();
            long j2 = this.basePeriod;
            ScheduledFuture<?> scheduleAtFixedRate = baseTimerExecutorLocked.scheduleAtFixedRate(timerRunnable, j2, j2, TimeUnit.MILLISECONDS);
            if (scheduleAtFixedRate instanceof RunnableScheduledFuture) {
                this.baseTimerTask = (RunnableScheduledFuture) scheduleAtFixedRate;
            }
            return false;
        } else if (j % this.basePeriod == 0) {
            return false;
        } else {
            baseTimerExecutorLocked.remove(runnableScheduledFuture);
            this.basePeriod = findAdjustPeriodLocked(j);
            TimerRunnable timerRunnable2 = new TimerRunnable();
            long j3 = this.basePeriod;
            ScheduledFuture<?> scheduleAtFixedRate2 = baseTimerExecutorLocked.scheduleAtFixedRate(timerRunnable2, j3, j3, TimeUnit.MILLISECONDS);
            if (!(scheduleAtFixedRate2 instanceof RunnableScheduledFuture)) {
                return true;
            }
            this.baseTimerTask = (RunnableScheduledFuture) scheduleAtFixedRate2;
            return true;
        }
    }

    private AlarmManager getAlarmManagerLocked() {
        Context context;
        if (this.alarmManager == null && (context = FormAdapter.getInstance().getContext()) != null) {
            Object systemService = context.getSystemService(NotificationRequest.CLASSIFICATION_ALARM);
            if (systemService instanceof AlarmManager) {
                this.alarmManager = (AlarmManager) systemService;
            }
        }
        return this.alarmManager;
    }

    private boolean initTimerReceiverLocked() {
        if (this.timerReceiver != null) {
            return true;
        }
        Context context = FormAdapter.getInstance().getContext();
        if (context == null) {
            return false;
        }
        this.timerReceiver = new TimerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATEATTIMER);
        context.registerReceiver(this.timerReceiver, intentFilter);
        return true;
    }

    private UpdateAtItem createRefreshItem(FormAdapter.RefreshRunnable refreshRunnable) {
        Context context = FormAdapter.getInstance().getContext();
        if (context == null) {
            return null;
        }
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATEATTIMER);
        intent.setPackage(FOUNDATION_NAME);
        intent.putExtra(KEY_FORM_ID, refreshRunnable.formId);
        PendingIntent broadcast = PendingIntent.getBroadcast(context, refreshRunnable.formId, intent, 268435456);
        if (broadcast == null) {
            HiLog.info(LABEL_LOG, "create target IntentAgent failed.", new Object[0]);
            return null;
        }
        UpdateAtItem updateAtItem = new UpdateAtItem();
        updateAtItem.pendingItent = broadcast;
        updateAtItem.refreshTask = refreshRunnable;
        return updateAtItem;
    }

    static long nanoSecondsToMills(long j) {
        return (j / 1000) / 1000;
    }

    /* access modifiers changed from: package-private */
    public static class UpdateAtItem {
        PendingIntent pendingItent;
        FormAdapter.RefreshRunnable refreshTask;

        UpdateAtItem() {
        }
    }

    /* access modifiers changed from: private */
    public static class TimerRunnable implements Runnable {
        private TimerRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            FormTimerManager.getInstance().onTimeOut();
        }
    }

    /* access modifiers changed from: private */
    public static class TimerReceiver extends BroadcastReceiver {
        private TimerReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && FormTimerManager.ACTION_UPDATEATTIMER.equals(intent.getAction())) {
                int intExtra = intent.getIntExtra(FormTimerManager.KEY_FORM_ID, 0);
                if (intExtra <= 0) {
                    HiLog.info(FormTimerManager.LABEL_LOG, "TimerReceiver invalid formId:%{public}d.", new Object[]{Integer.valueOf(intExtra)});
                } else {
                    FormTimerManager.getInstance().onUpdateAtTrigger(intExtra);
                }
            }
        }
    }
}
