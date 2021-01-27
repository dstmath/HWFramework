package ohos.workschedulerservice;

import android.content.Context;
import com.huawei.ohos.workscheduleradapter.WorkSchedulerCommon;
import com.huawei.ohos.workscheduleradapter.WorkschedulerAdapter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import ohos.aafwk.content.Intent;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.workscheduler.WorkInfo;
import ohos.workschedulerservice.controller.AppChangedListener;
import ohos.workschedulerservice.controller.BatteryStateListener;
import ohos.workschedulerservice.controller.CommonEventListener;
import ohos.workschedulerservice.controller.CommonEventStatus;
import ohos.workschedulerservice.controller.DeviceIdleListener;
import ohos.workschedulerservice.controller.HapChangedListener;
import ohos.workschedulerservice.controller.NetworkStateListener;
import ohos.workschedulerservice.controller.StateListener;
import ohos.workschedulerservice.controller.StorageStateListener;
import ohos.workschedulerservice.controller.TimerStateListener;
import ohos.workschedulerservice.controller.UserChangedListener;
import ohos.workschedulerservice.controller.WorkStatus;

public class WorkQueueManager implements IDeviceStateMonitor {
    private static final int DUMPER_UID_INDEX = 1;
    private static final int DUMPER_WORKID_INDEX = 2;
    private static final int EVENT_APP_CHANGED = 4;
    private static final int EVENT_CES_CHANGED = 5;
    private static final int EVENT_CHECK_STATE = 1;
    private static final int EVENT_DELAY_EXECUTE = 7;
    private static final int EVENT_IDLE_WAIT = 8;
    private static final int EVENT_PERSIST_WORK = 2;
    private static final int EVENT_READ_WORK = 6;
    private static final int EVENT_REMOVE_WORK = 3;
    private static final int EVENT_USER_CHANGED = 9;
    private static final int ILLEGAL_INDEX = -1;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkQueueManager");
    private static final long LOW_RAM = 600;
    private static final int LOW_SIZE = 1;
    private static final int MAX_CHANGED_HAPS_SIZE = 100;
    private static final int MAX_DUMPER_PARAM = 4;
    private static final long MODERATE_RAM = 1024;
    private static final int MODERATE_SIZE = 3;
    private static final long NORMAL_RAM = 2048;
    private static final int NORMAL_SIZE = 5;
    private static final int UID_MAX_WORK_SZIE = 50;
    private final AppChangedListener appChangedListener;
    private final BatteryStateListener batteryStateListener;
    private final ArrayList<String> changedHap = new ArrayList<>();
    private final CommonEventConnection commonEventConnection;
    private final CommonEventListener commonEventListener;
    private final CommonEventRegister commonEventRegist;
    private final Context context;
    private boolean currentIdleState;
    private int currentUserId;
    private final DeviceIdleListener deviceIdleListener;
    private final Object eventLock = new Object();
    private final HapChangedListener hapChangedListener;
    private final Object lock = new Object();
    private final NetworkStateListener networkStateListener;
    private final ArrayList<WorkStatus> pendingWorks = new ArrayList<>();
    private final List<StateListener> stateListeners;
    private final StorageStateListener storageStateListener;
    private final WorkStore store;
    private final TimerStateListener timerStateListener;
    private final UserChangedListener userChangedListener;
    private WorkMgrEventHandler workMgrEventHandler;
    private final WorkQueue workQueue;

    private long calculateDelayTime(int i) {
        if (i == 0) {
            return 0;
        }
        return i == 1 ? WorkStatus.WORKING_DELAY_TIME : i == 2 ? WorkStatus.FREQUENT_DELAY_TIME : i == 3 ? WorkStatus.RARE_DELAY_TIME : WorkStatus.NEVER_DELAY_TIME;
    }

    private int updateMaxWorks(long j) {
        if (j < LOW_RAM) {
            return 0;
        }
        if (j <= 1024) {
            return 1;
        }
        return j < 2048 ? 3 : 5;
    }

    public WorkQueueManager(Context context2) {
        this.context = context2;
        this.stateListeners = new ArrayList();
        this.currentUserId = -1;
        this.currentIdleState = false;
        this.store = new WorkStore(this.context);
        this.workQueue = new WorkQueue();
        this.commonEventConnection = new CommonEventConnection(this.context);
        this.appChangedListener = new AppChangedListener(this);
        this.hapChangedListener = new HapChangedListener(this);
        this.storageStateListener = new StorageStateListener(this);
        this.userChangedListener = new UserChangedListener(this);
        this.batteryStateListener = new BatteryStateListener(this);
        this.networkStateListener = new NetworkStateListener(this);
        this.timerStateListener = new TimerStateListener(this);
        this.deviceIdleListener = new DeviceIdleListener(this);
        this.commonEventListener = new CommonEventListener(this);
        this.commonEventRegist = new CommonEventRegister(this.context, this);
    }

    public boolean init() {
        EventRunner create = EventRunner.create();
        if (create == null) {
            return false;
        }
        this.workMgrEventHandler = new WorkMgrEventHandler(create);
        this.currentUserId = WorkschedulerAdapter.getInitUserId();
        WorkConnectionManager.getInstance().init(this.context, this, create);
        if (!this.commonEventRegist.init() || !this.commonEventConnection.init()) {
            return false;
        }
        initListener();
        this.workMgrEventHandler.sendEvent(InnerEvent.get(6, this));
        return true;
    }

    private void initListener() {
        this.appChangedListener.init();
        this.hapChangedListener.init();
        this.storageStateListener.init();
        this.userChangedListener.init();
        this.batteryStateListener.init();
        this.networkStateListener.init();
        this.timerStateListener.init(this.context);
        this.deviceIdleListener.init();
        this.stateListeners.add(this.appChangedListener);
        this.stateListeners.add(this.storageStateListener);
        this.stateListeners.add(this.userChangedListener);
        this.stateListeners.add(this.batteryStateListener);
        this.stateListeners.add(this.networkStateListener);
        this.stateListeners.add(this.timerStateListener);
        this.stateListeners.add(this.deviceIdleListener);
    }

    @Override // ohos.workschedulerservice.IDeviceStateMonitor
    public void onDeviceStateChanged(WorkStatus workStatus, long j) {
        this.workMgrEventHandler.sendEvent(InnerEvent.get(1, j, workStatus));
    }

    @Override // ohos.workschedulerservice.IDeviceStateMonitor
    public void onAppStateChanged(int i, String str) {
        addToChangedHap(i, str);
        this.workMgrEventHandler.sendEvent(InnerEvent.get(4, new AppChangedInfo(i, str)));
    }

    @Override // ohos.workschedulerservice.IDeviceStateMonitor
    public void onHapStateChanged(int i, String str) {
        if (this.commonEventRegist == null) {
            HiLog.error(LOG_LABEL, "commonEventRegist is null, hap state change failed", new Object[0]);
            return;
        }
        HiLog.info(LOG_LABEL, "onHapStateChanged, %{public}d : %{public}s", Integer.valueOf(i), str);
        this.commonEventRegist.updateHapStatus(i, str);
    }

    @Override // ohos.workschedulerservice.IDeviceStateMonitor
    public void onCommonEventChanged(Intent intent, ArrayList<CommonEventStatus> arrayList) {
        if (arrayList == null) {
            HiLog.error(LOG_LABEL, "onCommonEventChanged, statusList is null, start fail!", new Object[0]);
            return;
        }
        HiLog.debug(LOG_LABEL, "onCommonEventChanged %{public}s!", intent.getAction());
        Iterator<CommonEventStatus> it = arrayList.iterator();
        while (it.hasNext()) {
            CommonEventStatus next = it.next();
            Intent intent2 = new Intent(intent);
            intent2.setElement(next.getElementName());
            next.setIntent(intent2);
            this.workMgrEventHandler.sendEvent(InnerEvent.get(5, next));
        }
    }

    @Override // ohos.workschedulerservice.IDeviceStateMonitor
    public void onUserStateChanged(int i) {
        HiLog.debug(LOG_LABEL, "userid changed.", new Object[0]);
        this.workMgrEventHandler.sendEvent(InnerEvent.get(9, 0, Integer.valueOf(i)));
    }

    @Override // ohos.workschedulerservice.IDeviceStateMonitor
    public void onRunWorkNow(WorkStatus workStatus) {
        if (workStatus != null) {
            WorkConnectionManager.getInstance().onWorkStartEvent(workStatus);
        }
    }

    public boolean tryStartSignWork(WorkInfo workInfo, int i, int i2) {
        HiLog.info(LOG_LABEL, "tryStartSignWork begin!", new Object[0]);
        if (workInfo == null) {
            return false;
        }
        synchronized (this.lock) {
            WorkStatus generateWorkStatus = WorkStatus.generateWorkStatus(workInfo, i, i2);
            WorkStatus workStatusById = this.workQueue.getWorkStatusById(i, generateWorkStatus.getWorkId());
            if (workStatusById != null) {
                stopWorkByWork(workStatusById, true);
            }
            if (!this.workQueue.add(generateWorkStatus, i)) {
                return false;
            }
            generateWorkStatus.markCurrentState(2);
            for (StateListener stateListener : this.stateListeners) {
                stateListener.tryStartSignWork(generateWorkStatus);
                stateListener.updateTrackedTasks(generateWorkStatus);
            }
            if (workInfo.isRequestPersisted()) {
                this.workMgrEventHandler.sendEvent(InnerEvent.get(2, generateWorkStatus));
            }
            HiLog.info(LOG_LABEL, "tryStartSignWork end!", new Object[0]);
            return true;
        }
    }

    public boolean tryStopSignWork(WorkInfo workInfo, int i, int i2, boolean z) {
        boolean stopWorkByWork;
        HiLog.info(LOG_LABEL, "tryStopSignWork begin!", new Object[0]);
        if (workInfo == null) {
            return false;
        }
        synchronized (this.lock) {
            WorkStatus workStatusById = this.workQueue.getWorkStatusById(i, workInfo.getCurrentWorkID());
            stopWorkByWork = stopWorkByWork(workStatusById, z);
            if (z && stopWorkByWork) {
                if (workStatusById.hasPersistCondition()) {
                    this.workMgrEventHandler.sendEvent(InnerEvent.get(3, workStatusById));
                }
                this.workQueue.remove(i, workStatusById);
                if (this.workQueue.isEmpty(i)) {
                    this.workQueue.remove(i);
                }
                workStatusById.markCurrentState(5);
            }
            HiLog.info(LOG_LABEL, "tryStopSignWork end! uid:%{public}d has %{public}d works!", Integer.valueOf(i), Integer.valueOf(this.workQueue.getSize(i)));
        }
        return stopWorkByWork;
    }

    public WorkInfo tryGetWorkStatus(int i, int i2) {
        HiLog.info(LOG_LABEL, "tryGetWorkStatus begin!", new Object[0]);
        synchronized (this.lock) {
            WorkStatus workStatusById = this.workQueue.getWorkStatusById(i, i2);
            if (workStatusById == null) {
                HiLog.error(LOG_LABEL, "work is not exist!!!", new Object[0]);
                return null;
            }
            HiLog.info(LOG_LABEL, "tryGetWorkStatus end!", new Object[0]);
            return workStatusById.getWork();
        }
    }

    public WorkStatus getWorkStatus(int i, int i2) {
        WorkStatus workStatusById;
        synchronized (this.lock) {
            workStatusById = this.workQueue.getWorkStatusById(i, i2);
        }
        return workStatusById;
    }

    public List<WorkInfo> tryGetAllWorkStatus(int i) {
        HiLog.info(LOG_LABEL, "tryGetAllWorksStatus begin", new Object[0]);
        ArrayList arrayList = new ArrayList();
        synchronized (this.lock) {
            ArrayList<WorkStatus> arrayList2 = this.workQueue.get(i);
            if (arrayList2 == null) {
                return arrayList;
            }
            arrayList2.forEach(new Consumer(arrayList) {
                /* class ohos.workschedulerservice.$$Lambda$WorkQueueManager$fHPWEmJelYEn0q19hTRLjpMgNSo */
                private final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    WorkQueueManager.lambda$tryGetAllWorkStatus$0(this.f$0, (WorkStatus) obj);
                }
            });
            return arrayList;
        }
    }

    static /* synthetic */ void lambda$tryGetAllWorkStatus$0(List list, WorkStatus workStatus) {
        if (workStatus != null) {
            HiLog.info(LOG_LABEL, "tryGetAllWorksStatus begin, workId is %{public}d", Integer.valueOf(workStatus.getWorkId()));
            list.add(workStatus.getWork());
        }
    }

    public void updateDeviceIdleState(boolean z) {
        synchronized (this.lock) {
            this.currentIdleState = z;
        }
    }

    public boolean tryRegistCommonEvent(String str, CommonEventStatus commonEventStatus) {
        synchronized (this.eventLock) {
            HiLog.debug(LOG_LABEL, "tryRegistCommonEvent begin regist event!", new Object[0]);
            this.commonEventListener.registCommonEvent(commonEventStatus);
        }
        return true;
    }

    public boolean tryUnRegistCommonEvent(String str, CommonEventStatus commonEventStatus) {
        synchronized (this.eventLock) {
            HiLog.debug(LOG_LABEL, "tryUnRegistCommonEvent remove event!", new Object[0]);
            this.commonEventListener.unRegistCommonEvent(commonEventStatus);
        }
        return true;
    }

    public void tryStartCommonEvent() {
        synchronized (this.eventLock) {
            this.commonEventListener.updateCommonEventListener();
        }
    }

    private boolean stopWorkByWork(WorkStatus workStatus, boolean z) {
        if (workStatus == null) {
            HiLog.error(LOG_LABEL, "stopWorkByWork: work is null, no need stop!", new Object[0]);
            return false;
        } else if (workStatus.getCurrentState() >= 3) {
            return true;
        } else {
            if (z) {
                for (StateListener stateListener : this.stateListeners) {
                    stateListener.tryStopSignWork(workStatus);
                }
            }
            if (workStatus.getCurrentState() == 0 || workStatus.getCurrentState() == 1) {
                removeWorkInPendingWorks(workStatus);
            }
            return true;
        }
    }

    private void addToChangedHap(int i, String str) {
        String bundleName = getBundleName(i);
        synchronized (this.lock) {
            if (this.changedHap.size() >= 100) {
                this.changedHap.remove(0);
            }
            this.changedHap.add(i + " " + bundleName + " : " + str);
        }
        HiLog.info(LOG_LABEL, "onAppStateChanged, reason:%{public}s, bundle:%{public}s-%{public}d.", str, bundleName, Integer.valueOf(i));
    }

    private String getBundleName(int i) {
        List bundleNameFormUid = WorkSchedulerCommon.getBundleNameFormUid(i);
        return (bundleNameFormUid == null || bundleNameFormUid.isEmpty()) ? "invlid name" : (String) bundleNameFormUid.get(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserChanged(InnerEvent innerEvent) {
        if (innerEvent.object instanceof Integer) {
            int intValue = ((Integer) innerEvent.object).intValue();
            synchronized (this.lock) {
                this.currentUserId = intValue;
                HiLog.debug(LOG_LABEL, "onUserChanged, currentUserId : %{private}d", Integer.valueOf(this.currentUserId));
                for (WorkStatus workStatus : getWorksOfNotCurrentUserId()) {
                    stopWorkByWork(workStatus, true);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onIdleWait(InnerEvent innerEvent) {
        if (innerEvent.object instanceof WorkStatus) {
            WorkStatus workStatus = (WorkStatus) innerEvent.object;
            addWorkInPendingWorks(workStatus, 0);
            HiLog.debug(LOG_LABEL, "idle for work : %{public}d", Integer.valueOf(workStatus.getWorkId()));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDelayExecute(InnerEvent innerEvent) {
        if (innerEvent.object instanceof WorkStatus) {
            WorkStatus workStatus = (WorkStatus) innerEvent.object;
            addWorkInPendingWorks(workStatus, 0);
            HiLog.debug(LOG_LABEL, "delay work : %{public}d", Integer.valueOf(workStatus.getWorkId()));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onReadWork(InnerEvent innerEvent) {
        if (innerEvent.object instanceof WorkQueueManager) {
            this.store.readWorkStatusAsync((WorkQueueManager) innerEvent.object);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRemoveWork(InnerEvent innerEvent) {
        if (innerEvent.object instanceof WorkStatus) {
            WorkStatus workStatus = (WorkStatus) innerEvent.object;
            if (workStatus.hasPersistCondition()) {
                this.store.removeWorkStatusAsync(workStatus);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onWriteWork(InnerEvent innerEvent) {
        if (innerEvent.object instanceof WorkStatus) {
            this.store.writeWorkStatusAsync((WorkStatus) innerEvent.object);
            HiLog.debug(LOG_LABEL, "EVENT_PERSIST_WORK", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCheckWork(InnerEvent innerEvent) {
        if (innerEvent.object instanceof WorkStatus) {
            WorkStatus workStatus = (WorkStatus) innerEvent.object;
            WorkStatus workStatusById = this.workQueue.getWorkStatusById(workStatus.getUid(), workStatus.getWorkId());
            HiLog.debug(LOG_LABEL, "listener %{public}d change, check work is ready to run!", Long.valueOf(innerEvent.param));
            checkWorkState(workStatusById, innerEvent.param);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAppChanged(InnerEvent innerEvent) {
        if (innerEvent.object instanceof AppChangedInfo) {
            AppChangedInfo appChangedInfo = (AppChangedInfo) innerEvent.object;
            String reason = appChangedInfo.getReason();
            int uid = appChangedInfo.getUid();
            if ("remove".equals(reason)) {
                onHapRemove(uid);
            } else if ("forcestop".equals(reason)) {
                onHapForceStop(uid);
            } else {
                HiLog.debug(LOG_LABEL, "onAppStateChanged, add Hap here no need operation for Work", new Object[0]);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCesChanged(InnerEvent innerEvent) {
        if (innerEvent.object instanceof CommonEventStatus) {
            CommonEventStatus commonEventStatus = (CommonEventStatus) innerEvent.object;
            CommonEventConnection commonEventConnection2 = this.commonEventConnection;
            if (commonEventConnection2 != null) {
                commonEventConnection2.onCommonEventTriggered(commonEventStatus);
            }
        }
    }

    private void onHapRemove(int i) {
        synchronized (this.lock) {
            Iterator<WorkStatus> it = this.workQueue.get(i).iterator();
            while (it.hasNext()) {
                WorkStatus next = it.next();
                HiLog.info(LOG_LABEL, "onHapRemove uid : %{public}d remove, workid: %{public}d!", Integer.valueOf(i), Integer.valueOf(next.getWorkId()));
                stopWorkByWork(next, true);
                if (next.hasPersistCondition()) {
                    HiLog.info(LOG_LABEL, "onHapRemove, persist uid : %{public}d remove, workid: %{public}d!", Integer.valueOf(i), Integer.valueOf(next.getWorkId()));
                    this.store.removeWorkStatusAsync(next);
                }
            }
            this.workQueue.remove(i);
            HiLog.info(LOG_LABEL, "After %{public}d remove,total work size: %{public}d!", Integer.valueOf(i), Integer.valueOf(this.workQueue.getSize()));
        }
    }

    private void onHapForceStop(int i) {
        synchronized (this.lock) {
            for (WorkStatus workStatus : getWorksFromPendingWorksByUid(i)) {
                stopWorkByWork(workStatus, false);
            }
        }
    }

    private List<WorkStatus> getWorksFromPendingWorksByUid(int i) {
        ArrayList arrayList = new ArrayList();
        Iterator<WorkStatus> it = this.pendingWorks.iterator();
        while (it.hasNext()) {
            WorkStatus next = it.next();
            if (next.getUid() == i) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    private List<WorkStatus> getWorksOfNotCurrentUserId() {
        ArrayList arrayList = new ArrayList();
        Iterator<WorkStatus> it = this.pendingWorks.iterator();
        while (it.hasNext()) {
            WorkStatus next = it.next();
            if (next.getUserId() != this.currentUserId) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    private void checkWorkState(WorkStatus workStatus, long j) {
        if (workStatus != null) {
            HiLog.debug(LOG_LABEL, "Work State is %{public}d, Checking next state now!", Integer.valueOf(workStatus.getCurrentState()));
            if (workStatus.getCurrentState() == 2) {
                HiLog.debug(LOG_LABEL, "Work getUserId is %{private}d, currentUserId: %{private}d!", Integer.valueOf(workStatus.getUserId()), Integer.valueOf(this.currentUserId));
                if (workStatus.isReady() && workStatus.getUserId() == this.currentUserId) {
                    addWorkInPendingWorks(workStatus, j);
                }
            } else if (workStatus.getCurrentState() == 0) {
                if (!workStatus.isReady()) {
                    workStatus.markCurrentState(4);
                    removeWorkInPendingWorks(workStatus);
                }
            } else if (workStatus.getCurrentState() != 1) {
                HiLog.info(LOG_LABEL, "work is canncel, no need check!", new Object[0]);
            } else if (!workStatus.isReady()) {
                WorkConnectionManager.getInstance().onWorkStopEvent(workStatus);
                removeWorkInPendingWorks(workStatus);
            }
        }
    }

    private void addWorkInPendingWorks(WorkStatus workStatus, long j) {
        if (workStatus != null) {
            if (!workStatus.isRunOutTimes()) {
                synchronized (this.lock) {
                    if (!workStatus.isDelay()) {
                        if (workStatus.isWait() || !workStatus.hasDeviceIdleCondition()) {
                            long updateDelayTime = updateDelayTime(workStatus);
                            if (updateDelayTime > 0) {
                                workStatus.setDelay(true);
                                this.workMgrEventHandler.sendEvent(InnerEvent.get(7, workStatus), updateDelayTime);
                                HiLog.debug(LOG_LABEL, "delay %{public}d for execute", Long.valueOf(updateDelayTime));
                                return;
                            }
                        } else {
                            int idleWaitTime = workStatus.getIdleWaitTime();
                            workStatus.setWait(true);
                            this.workMgrEventHandler.sendEvent(InnerEvent.get(8, workStatus), (long) idleWaitTime);
                            return;
                        }
                    }
                    int i = 0;
                    while (true) {
                        if (i >= this.pendingWorks.size()) {
                            break;
                        } else if (workStatus.getPriority() > this.pendingWorks.get(i).getPriority()) {
                            break;
                        } else {
                            i++;
                        }
                    }
                    this.pendingWorks.add(i, workStatus);
                    workStatus.markCurrentState(0);
                    for (StateListener stateListener : this.stateListeners) {
                        stateListener.updateTrackedTasks(workStatus);
                    }
                    tryExecuteAllPendingWorks();
                }
            } else if (j == 6 && !workStatus.onlyHasRepeatCondition()) {
                HiLog.debug(LOG_LABEL, "update runtime in addWorkInPendingWorks!", new Object[0]);
                workStatus.updateRunTimes(0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeWorkInPendingWorks(WorkStatus workStatus) {
        if (workStatus != null) {
            synchronized (this.lock) {
                this.pendingWorks.remove(workStatus);
                if (workStatus.getCurrentState() == 1) {
                    WorkConnectionManager.getInstance().cleanConnection(workStatus);
                    tryExecuteAllPendingWorks();
                }
                workStatus.markCurrentState(2);
                for (StateListener stateListener : this.stateListeners) {
                    stateListener.updateTrackedTasks(workStatus);
                }
                workStatus.clearSatisfiedStatus();
            }
        }
    }

    private void tryExecuteAllPendingWorks() {
        updateMaxConnections();
        if (WorkConnectionManager.getInstance().hasConnections()) {
            ArrayList arrayList = new ArrayList();
            Iterator<WorkStatus> it = this.pendingWorks.iterator();
            while (it.hasNext()) {
                WorkStatus next = it.next();
                if (!(next.getCurrentState() == 1 || next.getCurrentState() == 3)) {
                    updateMaxConnections();
                    String bundleName = next.getBundleName();
                    if (isForbidden(bundleName) || isHibernate(bundleName) || !checkReadyAgain(next)) {
                        HiLog.debug(LOG_LABEL, "%{public}s is forbidden or hibernate or not ready again", bundleName);
                        arrayList.add(next);
                    } else {
                        WorkConnectionManager.getInstance().onWorkStartEvent(next);
                        if (!WorkConnectionManager.getInstance().hasConnections()) {
                            break;
                        }
                    }
                }
            }
            Iterator it2 = arrayList.iterator();
            while (it2.hasNext()) {
                removeWorkInPendingWorks((WorkStatus) it2.next());
            }
            arrayList.clear();
        }
    }

    private void updateMaxConnections() {
        Context context2 = this.context;
        if (context2 == null) {
            HiLog.error(LOG_LABEL, "setMaxConnections failed, context is null.", new Object[0]);
            return;
        }
        int updateMaxWorks = updateMaxWorks(WorkschedulerAdapter.getFreeMem(context2));
        WorkConnectionManager.getInstance().updateMaxConnections(updateMaxWorks);
        HiLog.debug(LOG_LABEL, "max connection size is %{public}d.", Integer.valueOf(updateMaxWorks));
    }

    private boolean isForbidden(String str) {
        return WorkschedulerAdapter.getAppForbidden().contains(str);
    }

    private boolean isHibernate(String str) {
        return WorkschedulerAdapter.getHibernateApps(this.context).contains(str);
    }

    private boolean checkReadyAgain(WorkStatus workStatus) {
        WorkStatus workStatusById = this.workQueue.getWorkStatusById(workStatus.getUid(), workStatus.getWorkId());
        if (workStatusById != null && workStatusById.getUserId() == this.currentUserId && workStatusById.isReady()) {
            return true;
        }
        return false;
    }

    private long updateDelayTime(WorkStatus workStatus) {
        if (this.currentIdleState) {
            return 0;
        }
        int standbyLevel = WorkschedulerAdapter.getStandbyLevel(this.context, workStatus.getBundleName());
        workStatus.updateActiveLevel(standbyLevel);
        HiLog.debug(LOG_LABEL, "WorkId: %{public}d, activeLevel: %{public}d", Integer.valueOf(workStatus.getWorkId()), Integer.valueOf(workStatus.getActiveLevel()));
        if (workStatus.hasRepeatCondition()) {
            return (workStatus.getWork().getRepeatCycleTime() * ((long) standbyLevel)) / 4;
        }
        return calculateDelayTime(standbyLevel);
    }

    /* access modifiers changed from: private */
    public class WorkMgrEventHandler extends EventHandler {
        private WorkMgrEventHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            WorkQueueManager.super.processEvent(innerEvent);
            if (innerEvent == null) {
                HiLog.error(WorkQueueManager.LOG_LABEL, "process event failed, event is null.", new Object[0]);
                return;
            }
            switch (innerEvent.eventId) {
                case 1:
                    WorkQueueManager.this.onCheckWork(innerEvent);
                    return;
                case 2:
                    WorkQueueManager.this.onWriteWork(innerEvent);
                    return;
                case 3:
                    WorkQueueManager.this.onRemoveWork(innerEvent);
                    return;
                case 4:
                    WorkQueueManager.this.onAppChanged(innerEvent);
                    return;
                case 5:
                    WorkQueueManager.this.onCesChanged(innerEvent);
                    return;
                case 6:
                    WorkQueueManager.this.onReadWork(innerEvent);
                    return;
                case 7:
                    WorkQueueManager.this.onDelayExecute(innerEvent);
                    return;
                case 8:
                    WorkQueueManager.this.onIdleWait(innerEvent);
                    return;
                case 9:
                    WorkQueueManager.this.onUserChanged(innerEvent);
                    return;
                default:
                    HiLog.debug(WorkQueueManager.LOG_LABEL, "processEvent default", new Object[0]);
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class AppChangedInfo {
        private String reason;
        private int uid;

        AppChangedInfo(int i, String str) {
            this.uid = i;
            this.reason = str;
        }

        /* access modifiers changed from: package-private */
        public int getUid() {
            return this.uid;
        }

        /* access modifiers changed from: package-private */
        public String getReason() {
            return this.reason;
        }
    }

    /* access modifiers changed from: private */
    public final class WorkQueue {
        private final HashMap<Integer, ArrayList<WorkStatus>> worksMap = new HashMap<>();

        public WorkQueue() {
        }

        public boolean add(WorkStatus workStatus, int i) {
            if (workStatus == null) {
                return false;
            }
            synchronized (WorkQueueManager.this.lock) {
                ArrayList<WorkStatus> orDefault = this.worksMap.getOrDefault(Integer.valueOf(i), new ArrayList<>());
                WorkStatus workStatusById = getWorkStatusById(i, workStatus.getWorkId());
                if (workStatusById != null || orDefault.size() < WorkQueueManager.UID_MAX_WORK_SZIE) {
                    orDefault.remove(workStatusById);
                    orDefault.add(workStatus);
                    this.worksMap.put(Integer.valueOf(i), orDefault);
                    HiLog.info(WorkQueueManager.LOG_LABEL, "total %{public}d hap, uid %{public}d has %{public}d works!", Integer.valueOf(this.worksMap.size()), Integer.valueOf(i), Integer.valueOf(orDefault.size()));
                    return true;
                }
                HiLog.info(WorkQueueManager.LOG_LABEL, "uid's list too large", new Object[0]);
                return false;
            }
        }

        public WorkStatus getWorkStatusById(int i, int i2) {
            synchronized (WorkQueueManager.this.lock) {
                ArrayList<WorkStatus> arrayList = this.worksMap.get(Integer.valueOf(i));
                if (arrayList != null) {
                    HiLog.debug(WorkQueueManager.LOG_LABEL, "uid:%{public}d has %{public}d works!", Integer.valueOf(i), Integer.valueOf(arrayList.size()));
                    Iterator<WorkStatus> it = arrayList.iterator();
                    while (it.hasNext()) {
                        WorkStatus next = it.next();
                        if (next.getWorkId() == i2) {
                            return next;
                        }
                    }
                }
                return null;
            }
        }

        public ArrayList<WorkStatus> get() {
            ArrayList<WorkStatus> arrayList;
            synchronized (WorkQueueManager.this.lock) {
                arrayList = new ArrayList<>();
                for (ArrayList<WorkStatus> arrayList2 : this.worksMap.values()) {
                    Iterator<WorkStatus> it = arrayList2.iterator();
                    while (it.hasNext()) {
                        arrayList.add(it.next());
                    }
                }
            }
            return arrayList;
        }

        public ArrayList<WorkStatus> get(int i) {
            ArrayList<WorkStatus> orDefault;
            synchronized (WorkQueueManager.this.lock) {
                orDefault = this.worksMap.getOrDefault(Integer.valueOf(i), new ArrayList<>());
            }
            return orDefault;
        }

        public void remove(int i) {
            synchronized (WorkQueueManager.this.lock) {
                this.worksMap.remove(Integer.valueOf(i));
            }
        }

        public void remove(int i, WorkStatus workStatus) {
            synchronized (WorkQueueManager.this.lock) {
                ArrayList<WorkStatus> arrayList = this.worksMap.get(Integer.valueOf(i));
                if (arrayList != null) {
                    arrayList.remove(workStatus);
                }
            }
        }

        public int getSize() {
            int size;
            synchronized (WorkQueueManager.this.lock) {
                size = this.worksMap.size();
            }
            return size;
        }

        public int getSize(int i) {
            int size;
            synchronized (WorkQueueManager.this.lock) {
                size = this.worksMap.getOrDefault(Integer.valueOf(i), new ArrayList<>()).size();
            }
            return size;
        }

        public boolean isEmpty() {
            boolean isEmpty;
            synchronized (WorkQueueManager.this.lock) {
                isEmpty = this.worksMap.isEmpty();
            }
            return isEmpty;
        }

        public boolean isEmpty(int i) {
            boolean isEmpty;
            synchronized (WorkQueueManager.this.lock) {
                isEmpty = this.worksMap.getOrDefault(Integer.valueOf(i), new ArrayList<>()).isEmpty();
            }
            return isEmpty;
        }
    }

    public void dump(PrintWriter printWriter, String[] strArr) {
        if (printWriter == null) {
            HiLog.error(LOG_LABEL, "error dump PrintWriter input", new Object[0]);
        } else if (strArr.length == 0) {
            printWriter.println();
            printWriter.println("WorksMap dump info:");
            dumpWorksMap(printWriter, "  ");
            printWriter.println();
            printWriter.println("PendingWorks dump info:");
            dumpPendingWorks(printWriter, "  ");
            printWriter.println();
            printWriter.println("StateListeners dump info:");
            dumpStateListeners(printWriter, "  ");
            WorkConnectionManager.getInstance().dumpConnectionsStatus(printWriter, "  ");
        } else {
            dumpEx(printWriter, strArr);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0048, code lost:
        if (r2.equals("help") != false) goto L_0x0056;
     */
    private void dumpEx(PrintWriter printWriter, String[] strArr) {
        if (strArr.length > 4) {
            printWriter.println("please check the dumper parameters");
            return;
        }
        boolean z = false;
        String str = strArr[0];
        switch (str.hashCode()) {
            case -1995695841:
                if (str.equals("currentUserId")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 3198785:
                break;
            case 3556498:
                if (str.equals("test")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 492274702:
                if (str.equals("HibernateHaps")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1503566841:
                if (str.equals("forbidden")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            case 1528964461:
                if (str.equals("forcestop")) {
                    z = true;
                    break;
                }
                z = true;
                break;
            default:
                z = true;
                break;
        }
        if (!z) {
            dumpForHelp(printWriter);
        } else if (z) {
            dumpHibernateHaps(printWriter);
        } else if (z) {
            dumpFobidden(printWriter);
        } else if (z) {
            dumpForceStop(printWriter);
        } else if (z) {
            printWriter.println("currentUserId:" + this.currentUserId);
        } else if (!z) {
            printWriter.println("default:");
        } else {
            dumpForTest(printWriter, strArr);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void dumpForTest(PrintWriter printWriter, String[] strArr) {
        char c;
        String str = strArr[1];
        switch (str.hashCode()) {
            case -1889908102:
                if (str.equals("updateMaxConnections")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1069309922:
                if (str.equals("activeLevel")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 112670:
                if (str.equals("ram")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 94341443:
                if (str.equals("commoneventlistener")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 790306090:
                if (str.equals("clearhap")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            updateMaxConnections();
            printWriter.println("updateMaxConnections successfully.");
        } else if (c != 1) {
            if (c == 2) {
                dumpRam(printWriter);
            } else if (c == 3) {
                dumpClearHap(printWriter);
            } else if (c != 4) {
                printWriter.println("default:");
            } else {
                this.commonEventListener.dumpStateListenerStatus(printWriter);
            }
        } else if (strArr.length >= 3) {
            printWriter.println(WorkschedulerAdapter.getStandbyLevel(this.context, strArr[2]));
        }
    }

    private void dumpWorksMap(PrintWriter printWriter, String str) {
        synchronized (this.lock) {
            if (this.workQueue.isEmpty()) {
                printWriter.print(str);
                printWriter.println("<none work info in worksMap>");
                return;
            }
            Iterator<WorkStatus> it = this.workQueue.get().iterator();
            while (it.hasNext()) {
                dumpWorkStatus(printWriter, it.next(), str);
            }
        }
    }

    private void dumpPendingWorks(PrintWriter printWriter, String str) {
        synchronized (this.lock) {
            if (this.pendingWorks.isEmpty()) {
                printWriter.print(str);
                printWriter.println("<none work info in pendingWorks>");
                return;
            }
            Iterator<WorkStatus> it = this.pendingWorks.iterator();
            while (it.hasNext()) {
                dumpWorkStatus(printWriter, it.next(), str);
            }
        }
    }

    private void dumpWorkStatus(PrintWriter printWriter, WorkStatus workStatus, String str) {
        printWriter.print(str);
        printWriter.println("Uid:" + workStatus.getUid() + ", BundleName:" + workStatus.getBundleName() + ", WorkId:" + workStatus.getWorkId() + ", CurrentState:" + workStatus.getCurrentState() + ", SatisfiedStatus:" + Integer.toBinaryString(workStatus.getSatisfiedStatus()) + ", triggerConditions:" + Integer.toBinaryString(workStatus.getRequestStatus()) + ", EarliestRunTime:" + workStatus.getEarliestRunTime() + ", Priority:" + workStatus.getPriority());
    }

    private void dumpStateListeners(PrintWriter printWriter, String str) {
        synchronized (this.lock) {
            if (this.stateListeners.isEmpty()) {
                printWriter.print(str);
                printWriter.println("<none stateListeners>");
                return;
            }
            for (StateListener stateListener : this.stateListeners) {
                stateListener.dumpStateListenerStatus(printWriter, str);
            }
        }
    }

    private void dumpForHelp(PrintWriter printWriter) {
        if (printWriter != null && this.context != null) {
            printWriter.println("this is the help for workscheduler dumper");
            printWriter.println("you can dumper for worksMap, pendingWorks and Listeners info without parameter.");
            printWriter.println("you can dumper current device ram with parameter 'ram'.");
            printWriter.println("you can dumper current hap with parameter 'forbidden' which hap is forbidden to run in background by user.");
            printWriter.println("you can dumper current hap with parameter 'forcestop' which hap has been force-stop.");
            printWriter.println("you can clear the list which contains the forcestop haps with 'clearhap'.");
        }
    }

    private void dumpHibernateHaps(PrintWriter printWriter) {
        if (this.context == null) {
            HiLog.error(LOG_LABEL, "dumpHibernateHaps failed, context is null.", new Object[0]);
            return;
        }
        printWriter.println("dumpHibernateHaps:");
        for (String str : WorkschedulerAdapter.getHibernateApps(this.context)) {
            printWriter.println(str);
        }
    }

    private void dumpFobidden(PrintWriter printWriter) {
        List<String> appForbidden = WorkschedulerAdapter.getAppForbidden();
        printWriter.println("dumpFobidden:");
        for (String str : appForbidden) {
            printWriter.println(str);
        }
    }

    private void dumpForceStop(PrintWriter printWriter) {
        printWriter.println("dumpForceStop:");
        synchronized (this.lock) {
            Iterator<String> it = this.changedHap.iterator();
            while (it.hasNext()) {
                printWriter.println(it.next());
            }
        }
    }

    private void dumpClearHap(PrintWriter printWriter) {
        synchronized (this.lock) {
            this.changedHap.clear();
            printWriter.println("clear changed app");
        }
    }

    private void dumpRam(PrintWriter printWriter) {
        if (this.context == null) {
            HiLog.error(LOG_LABEL, "dumpRam failed, context is null.", new Object[0]);
            return;
        }
        printWriter.println("dumpRam:");
        printWriter.println(String.valueOf(WorkschedulerAdapter.getFreeMem(this.context)));
    }
}
