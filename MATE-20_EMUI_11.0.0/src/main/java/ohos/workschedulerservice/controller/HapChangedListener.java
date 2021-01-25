package ohos.workschedulerservice.controller;

import java.io.PrintWriter;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentFilter;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.workschedulerservice.WorkQueueManager;

public final class HapChangedListener extends StateListener {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "HapChangedListener");
    private String action = "";
    private HapCommonEventSubscriber commonEventSubscriber;
    private final Object lock = new Object();
    private int uid = -1;

    public HapChangedListener(WorkQueueManager workQueueManager) {
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
            this.commonEventSubscriber = new HapCommonEventSubscriber(getInfo());
        }
        try {
            CommonEventManager.subscribeCommonEvent(this.commonEventSubscriber);
            HiLog.info(LOG_LABEL, "subscribe common event success", new Object[0]);
        } catch (IllegalArgumentException | RemoteException unused) {
            HiLog.info(LOG_LABEL, "subscribeCommonEvent occur exception.", new Object[0]);
        }
    }

    public void unInit() {
        HapCommonEventSubscriber hapCommonEventSubscriber = this.commonEventSubscriber;
        if (hapCommonEventSubscriber == null) {
            HiLog.error(LOG_LABEL, "commonEventSubscriber not valid, init first.", new Object[0]);
            return;
        }
        try {
            CommonEventManager.unsubscribeCommonEvent(hapCommonEventSubscriber);
            HiLog.info(LOG_LABEL, "uninit subscribe common event", new Object[0]);
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "unsubscribeCommonEvent occur exception.", new Object[0]);
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStartSignWork(WorkStatus workStatus) {
        HiLog.debug(LOG_LABEL, "start sign work.", new Object[0]);
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStopSignWork(WorkStatus workStatus) {
        HiLog.debug(LOG_LABEL, "stop sign work.", new Object[0]);
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void dumpStateListenerStatus(PrintWriter printWriter, String str) {
        if (printWriter == null || str == null) {
            HiLog.error(LOG_LABEL, "error dump PrintWriter or prefix input", new Object[0]);
            return;
        }
        printWriter.println();
        printWriter.println("HapChangedListener latest event:");
        synchronized (this.lock) {
            printWriter.println(str + "uid:" + this.uid);
            printWriter.println(str + "action:" + this.action);
        }
    }

    private CommonEventSubscribeInfo getInfo() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("common.event.ABILITY_ADDED");
        intentFilter.addAction("common.event.ABILITY_REMOVED");
        intentFilter.addAction("common.event.ABILITY_UPDATED");
        return new CommonEventSubscribeInfo(intentFilter);
    }

    /* access modifiers changed from: private */
    public final class HapCommonEventSubscriber extends CommonEventSubscriber {
        HapCommonEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        public void onReceiveEvent(CommonEventData commonEventData) {
            if (commonEventData == null) {
                HiLog.error(HapChangedListener.LOG_LABEL, "HapCommonEventSubscriber onReceiveEvent, event data invalid", new Object[0]);
                return;
            }
            synchronized (HapChangedListener.this.lock) {
                Intent intent = commonEventData.getIntent();
                if (intent == null) {
                    HiLog.error(HapChangedListener.LOG_LABEL, "HapCommonEventSubscriber onReceiveEvent, intent invalid", new Object[0]);
                    return;
                }
                HapChangedListener.this.action = intent.getAction();
                HapChangedListener.this.uid = intent.getIntParam("uid", -1);
                if (HapChangedListener.this.uid == -1) {
                    HiLog.error(HapChangedListener.LOG_LABEL, "uid invalid.", new Object[0]);
                    return;
                }
                if ("common.event.ABILITY_ADDED".equals(HapChangedListener.this.action)) {
                    HiLog.info(HapChangedListener.LOG_LABEL, "%{public}d add.", Integer.valueOf(HapChangedListener.this.uid));
                    HapChangedListener.this.workQueueMgr.onHapStateChanged(HapChangedListener.this.uid, "add");
                } else if ("common.event.ABILITY_REMOVED".equals(HapChangedListener.this.action)) {
                    HiLog.info(HapChangedListener.LOG_LABEL, "%{public}d remove.", Integer.valueOf(HapChangedListener.this.uid));
                    HapChangedListener.this.workQueueMgr.onHapStateChanged(HapChangedListener.this.uid, "remove");
                } else if ("common.event.ABILITY_UPDATED".equals(HapChangedListener.this.action)) {
                    HiLog.info(HapChangedListener.LOG_LABEL, "%{public}d update - remove.", Integer.valueOf(HapChangedListener.this.uid));
                    HapChangedListener.this.workQueueMgr.onHapStateChanged(HapChangedListener.this.uid, "remove");
                    HiLog.info(HapChangedListener.LOG_LABEL, "%{public}d update - add", Integer.valueOf(HapChangedListener.this.uid));
                    HapChangedListener.this.workQueueMgr.onHapStateChanged(HapChangedListener.this.uid, "add");
                } else {
                    HiLog.info(HapChangedListener.LOG_LABEL, "HapCommonEventSubscriber other action.", new Object[0]);
                }
            }
        }
    }
}
