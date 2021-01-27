package ohos.workschedulerservice.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import ohos.aafwk.content.Intent;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.commonevent.MatchingSkills;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.workschedulerservice.WorkQueueManager;

public final class DeviceIdleListener extends StateListener {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, TAG);
    private static final String START_DREAM = "android.intent.action.DREAMING_STARTED";
    private static final String STOP_DREAM = "android.intent.action.DREAMING_STOPPED";
    private static final String TAG = "DeviceIdleListener";
    private IdleCommonEventSubscriber commonEventSubscriber;
    private AtomicBoolean initFlag = new AtomicBoolean(false);
    private AtomicBoolean isIdle = new AtomicBoolean(false);
    private final Object lock = new Object();
    private final ArrayList<WorkStatus> trackedTasks = new ArrayList<>();

    public DeviceIdleListener(WorkQueueManager workQueueManager) {
        super(workQueueManager);
    }

    public void init() {
        HiLog.info(LOG_LABEL, "try to subscribe common event", new Object[0]);
        if (this.workQueueMgr != null) {
            trySubscribeCommonEvent();
        }
    }

    private void trySubscribeCommonEvent() {
        if (this.commonEventSubscriber == null) {
            this.commonEventSubscriber = new IdleCommonEventSubscriber(getInfo());
        }
        try {
            CommonEventManager.subscribeCommonEvent(this.commonEventSubscriber);
            this.initFlag.set(true);
            HiLog.info(LOG_LABEL, "subscribe common event success", new Object[0]);
        } catch (IllegalArgumentException | RemoteException unused) {
            HiLog.info(LOG_LABEL, "subscribeCommonEvent occur exception, please check it.", new Object[0]);
        }
    }

    public void unInit() {
        IdleCommonEventSubscriber idleCommonEventSubscriber = this.commonEventSubscriber;
        if (idleCommonEventSubscriber == null) {
            HiLog.error(LOG_LABEL, "commonEventSubscriber not valid, no need uninitialize.", new Object[0]);
            return;
        }
        try {
            CommonEventManager.unsubscribeCommonEvent(idleCommonEventSubscriber);
            HiLog.info(LOG_LABEL, "uninit subscribe common event", new Object[0]);
            this.initFlag.set(false);
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "unsubscribe common event occur exception.", new Object[0]);
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStartSignWork(WorkStatus workStatus) {
        HiLog.debug(LOG_LABEL, "try start sign work.", new Object[0]);
        if (workStatus == null) {
            HiLog.debug(LOG_LABEL, "try start failed, work invalid.", new Object[0]);
        } else if (workStatus.hasDeviceIdleCondition()) {
            synchronized (this.lock) {
                this.trackedTasks.add(workStatus);
            }
            workStatus.changeIdleCondition(this.isIdle.get());
            if (!this.initFlag.get()) {
                init();
            }
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStopSignWork(WorkStatus workStatus) {
        boolean isEmpty;
        HiLog.debug(LOG_LABEL, "try stop sign work.", new Object[0]);
        if (workStatus == null) {
            HiLog.debug(LOG_LABEL, "try stop failed, work invalid.", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            this.trackedTasks.remove(workStatus);
            isEmpty = this.trackedTasks.isEmpty();
        }
        if (isEmpty && this.initFlag.get()) {
            unInit();
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void updateTrackedTasks(WorkStatus workStatus) {
        synchronized (this.lock) {
            updateTasks(this.trackedTasks, workStatus);
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void dumpStateListenerStatus(PrintWriter printWriter, String str) {
        if (printWriter == null || str == null) {
            HiLog.error(LOG_LABEL, "error dump PrintWriter or prefix input", new Object[0]);
            return;
        }
        printWriter.println();
        printWriter.println("DeviceIdleListener event:");
        printWriter.println(str + "isIdle:" + this.isIdle.get());
    }

    private CommonEventSubscribeInfo getInfo() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent("usual.event.SCREEN_ON");
        matchingSkills.addEvent("usual.event.SCREEN_OFF");
        matchingSkills.addEvent(START_DREAM);
        matchingSkills.addEvent(STOP_DREAM);
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        commonEventSubscribeInfo.setUserId(-1);
        return commonEventSubscribeInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDeviceIdleChanged(boolean z) {
        HiLog.debug(LOG_LABEL, "onDeviceIdleChanged, state is idle : %{public}b", Boolean.valueOf(z));
        this.workQueueMgr.updateDeviceIdleState(z);
        synchronized (this.lock) {
            Iterator<WorkStatus> it = this.trackedTasks.iterator();
            while (it.hasNext()) {
                WorkStatus next = it.next();
                if (next.changeIdleCondition(z)) {
                    this.workQueueMgr.onDeviceStateChanged(next, 7);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class IdleCommonEventSubscriber extends CommonEventSubscriber {
        IdleCommonEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        public void onReceiveEvent(CommonEventData commonEventData) {
            if (commonEventData == null) {
                HiLog.error(DeviceIdleListener.LOG_LABEL, "IdleCommonEventSubscriber onReceiveEvent, event data invalid", new Object[0]);
                return;
            }
            Intent intent = commonEventData.getIntent();
            if (intent == null) {
                HiLog.error(DeviceIdleListener.LOG_LABEL, "IdleCommonEventSubscriber onReceiveEvent, intent invalid", new Object[0]);
                return;
            }
            HiLog.info(DeviceIdleListener.LOG_LABEL, "enter on receive.", new Object[0]);
            String action = intent.getAction();
            if ("usual.event.SCREEN_ON".equals(action) || DeviceIdleListener.STOP_DREAM.equals(action)) {
                HiLog.info(DeviceIdleListener.LOG_LABEL, "receive screen on action.", new Object[0]);
                DeviceIdleListener.this.isIdle.set(false);
                DeviceIdleListener.this.onDeviceIdleChanged(false);
            } else if ("usual.event.SCREEN_OFF".equals(action) || DeviceIdleListener.START_DREAM.equals(action)) {
                DeviceIdleListener.this.isIdle.set(true);
                DeviceIdleListener.this.onDeviceIdleChanged(true);
            } else {
                HiLog.info(DeviceIdleListener.LOG_LABEL, "IdleCommonEventSubscriber other action.", new Object[0]);
            }
        }
    }
}
