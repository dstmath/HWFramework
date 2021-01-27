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

public final class StorageStateListener extends StateListener {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, TAG);
    private static final String TAG = "StorageStateListener";
    private StorageCommonEventSubscriber commonEventSubscriber;
    private AtomicBoolean initFlag = new AtomicBoolean(false);
    private AtomicBoolean isLowStorage = new AtomicBoolean(false);
    private final Object lock = new Object();
    private AtomicBoolean receivedFlag = new AtomicBoolean(false);
    private final ArrayList<WorkStatus> trackedTasks = new ArrayList<>();

    public StorageStateListener(WorkQueueManager workQueueManager) {
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
            this.commonEventSubscriber = new StorageCommonEventSubscriber(getInfo());
        }
        try {
            CommonEventManager.subscribeCommonEvent(this.commonEventSubscriber);
            this.initFlag.set(true);
            HiLog.info(LOG_LABEL, "subscribe common event success", new Object[0]);
        } catch (IllegalArgumentException | RemoteException unused) {
            HiLog.info(LOG_LABEL, "subscribeCommonEvent occur exception.", new Object[0]);
        }
    }

    public void unInit() {
        StorageCommonEventSubscriber storageCommonEventSubscriber = this.commonEventSubscriber;
        if (storageCommonEventSubscriber == null) {
            HiLog.error(LOG_LABEL, "commonEventSubscriber not valid, init first.", new Object[0]);
            return;
        }
        try {
            CommonEventManager.unsubscribeCommonEvent(storageCommonEventSubscriber);
            HiLog.info(LOG_LABEL, "uninit subscribe common event", new Object[0]);
            this.initFlag.set(false);
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "unsubscribeCommonEvent occur exception.", new Object[0]);
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStartSignWork(WorkStatus workStatus) {
        HiLog.debug(LOG_LABEL, "start sign work.", new Object[0]);
        if (workStatus == null) {
            HiLog.debug(LOG_LABEL, "try start failed, work invalid.", new Object[0]);
        } else if (workStatus.hasStorageCondition()) {
            synchronized (this.lock) {
                this.trackedTasks.add(workStatus);
            }
            workStatus.changeStorageSatisfiedCondition(this.isLowStorage.get());
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

    public int getTaskSize() {
        int size;
        synchronized (this.lock) {
            size = this.trackedTasks.size();
        }
        return size;
    }

    public boolean getFlag() {
        return this.receivedFlag.get();
    }

    public void setFlag(boolean z) {
        this.receivedFlag.set(z);
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void dumpStateListenerStatus(PrintWriter printWriter, String str) {
        if (printWriter == null || str == null) {
            HiLog.error(LOG_LABEL, "error dump PrintWriter or prefix input", new Object[0]);
            return;
        }
        printWriter.println();
        printWriter.println("StorageStateListener latest status:");
        printWriter.println(str + "StorageState is low:" + this.isLowStorage.get());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onStorageStatusChanged(boolean z) {
        HiLog.debug(LOG_LABEL, "StorageStateListener onStorageStatusChanged, state is low : %{public}b", Boolean.valueOf(z));
        synchronized (this.lock) {
            Iterator<WorkStatus> it = this.trackedTasks.iterator();
            while (it.hasNext()) {
                WorkStatus next = it.next();
                if (next.changeStorageSatisfiedCondition(z)) {
                    this.workQueueMgr.onDeviceStateChanged(next, 1);
                }
            }
        }
    }

    private CommonEventSubscribeInfo getInfo() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent("usual.event.DEVICE_STORAGE_LOW");
        matchingSkills.addEvent("usual.event.DEVICE_STORAGE_OK");
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        commonEventSubscribeInfo.setUserId(-1);
        return commonEventSubscribeInfo;
    }

    /* access modifiers changed from: private */
    public final class StorageCommonEventSubscriber extends CommonEventSubscriber {
        StorageCommonEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        public void onReceiveEvent(CommonEventData commonEventData) {
            if (commonEventData == null) {
                HiLog.error(StorageStateListener.LOG_LABEL, "onReceiveEvent commonEventData is null.", new Object[0]);
                return;
            }
            Intent intent = commonEventData.getIntent();
            if (intent == null) {
                HiLog.error(StorageStateListener.LOG_LABEL, "onReceiveEvent is null.", new Object[0]);
                return;
            }
            String action = intent.getAction();
            if ("usual.event.DEVICE_STORAGE_LOW".equals(action)) {
                StorageStateListener.this.isLowStorage.set(true);
                StorageStateListener.this.onStorageStatusChanged(true);
                StorageStateListener.this.receivedFlag.set(true);
            } else if ("usual.event.DEVICE_STORAGE_OK".equals(action)) {
                StorageStateListener.this.isLowStorage.set(false);
                StorageStateListener.this.onStorageStatusChanged(false);
                StorageStateListener.this.receivedFlag.set(true);
            } else {
                HiLog.debug(StorageStateListener.LOG_LABEL, "StorageStateListener onStorageStatusChanged else.", new Object[0]);
            }
        }
    }
}
