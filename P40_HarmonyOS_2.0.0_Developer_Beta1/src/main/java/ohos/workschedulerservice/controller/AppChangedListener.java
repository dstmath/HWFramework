package ohos.workschedulerservice.controller;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.commonevent.MatchingSkills;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.workschedulerservice.WorkQueueManager;

public final class AppChangedListener extends StateListener {
    private static final long INTERVAL = 100;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, TAG);
    private static final int MAX_SIZE = 20;
    private static final String TAG = "AppChangedListener";
    private static final String UID = "android.intent.extra.UID";
    private String actionCache = "";
    private final Object cacheLock = new Object();
    private AppCommonEventSubscriber commonEventSubscriber;
    private int count = 0;
    private AtomicBoolean initFlag = new AtomicBoolean(false);
    private AtomicBoolean receivedFlag = new AtomicBoolean(false);
    private int uidCache = -1;

    public AppChangedListener(WorkQueueManager workQueueManager) {
        super(workQueueManager);
    }

    public void init() {
        HiLog.info(LOG_LABEL, "try to subscribe common event", new Object[0]);
        if (this.workQueueMgr != null) {
            while (!this.initFlag.get() && this.count <= 20) {
                trySubscribeCommonEvent();
            }
        }
    }

    private void trySubscribeCommonEvent() {
        if (this.commonEventSubscriber == null) {
            this.commonEventSubscriber = new AppCommonEventSubscriber(getInfo());
        }
        try {
            Thread.sleep(INTERVAL);
            CommonEventManager.subscribeCommonEvent(this.commonEventSubscriber);
            this.initFlag.set(true);
            HiLog.info(LOG_LABEL, "subscribe common event success", new Object[0]);
        } catch (IllegalArgumentException | InterruptedException | RemoteException unused) {
            HiLog.info(LOG_LABEL, "subscribeCommonEvent occur exception.", new Object[0]);
        }
        this.count++;
    }

    public void unInit() {
        AppCommonEventSubscriber appCommonEventSubscriber = this.commonEventSubscriber;
        if (appCommonEventSubscriber == null) {
            HiLog.error(LOG_LABEL, "commonEventSubscriber not valid, init first.", new Object[0]);
            return;
        }
        try {
            CommonEventManager.unsubscribeCommonEvent(appCommonEventSubscriber);
            HiLog.info(LOG_LABEL, "uninit subscribe common event", new Object[0]);
            this.initFlag.set(false);
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
    public void updateTrackedTasks(WorkStatus workStatus) {
        HiLog.debug(LOG_LABEL, "updateTrackedTasks.", new Object[0]);
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
        printWriter.println("AppChangedListener latest event:");
        synchronized (this.cacheLock) {
            printWriter.println(str + "uid:" + this.uidCache);
            printWriter.println(str + "action:" + this.actionCache);
        }
    }

    private CommonEventSubscribeInfo getInfo() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent("usual.event.PACKAGE_ADDED");
        matchingSkills.addEvent("usual.event.PACKAGE_REMOVED");
        matchingSkills.addEvent("usual.event.PACKAGE_REPLACED");
        matchingSkills.addEvent("usual.event.PACKAGE_RESTARTED");
        IntentParams intentParams = new IntentParams();
        intentParams.setParam("scheme", new String[]{"package"});
        matchingSkills.setIntentParams(intentParams);
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        commonEventSubscribeInfo.setUserId(-1);
        return commonEventSubscribeInfo;
    }

    /* access modifiers changed from: private */
    public final class AppCommonEventSubscriber extends CommonEventSubscriber {
        AppCommonEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        public void onReceiveEvent(CommonEventData commonEventData) {
            if (commonEventData == null) {
                HiLog.error(AppChangedListener.LOG_LABEL, "AppCommonEventSubscriber onReceiveEvent, event data invalid", new Object[0]);
                return;
            }
            Intent intent = commonEventData.getIntent();
            if (intent == null) {
                HiLog.error(AppChangedListener.LOG_LABEL, "AppCommonEventSubscriber onReceiveEvent, intent invalid", new Object[0]);
                return;
            }
            String action = intent.getAction();
            int intParam = intent.getIntParam(AppChangedListener.UID, -1);
            if (intParam == -1) {
                HiLog.error(AppChangedListener.LOG_LABEL, "uid invalid.", new Object[0]);
                return;
            }
            setCacheData(action, intParam);
            if ("usual.event.PACKAGE_REMOVED".equals(action) || "usual.event.PACKAGE_REPLACED".equals(action)) {
                HiLog.info(AppChangedListener.LOG_LABEL, "%{public}d remove.", Integer.valueOf(intParam));
                AppChangedListener.this.workQueueMgr.onAppStateChanged(intParam, "remove");
                AppChangedListener.this.receivedFlag.set(true);
            } else if ("usual.event.PACKAGE_RESTARTED".equals(action)) {
                HiLog.info(AppChangedListener.LOG_LABEL, "%{public}d force stop.", Integer.valueOf(intParam));
                AppChangedListener.this.workQueueMgr.onAppStateChanged(intParam, "forcestop");
                AppChangedListener.this.receivedFlag.set(true);
            } else if ("usual.event.PACKAGE_ADDED".equals(action)) {
                HiLog.info(AppChangedListener.LOG_LABEL, "%{public}d add.", Integer.valueOf(intParam));
                AppChangedListener.this.workQueueMgr.onAppStateChanged(intParam, "add");
                AppChangedListener.this.receivedFlag.set(true);
            } else {
                HiLog.info(AppChangedListener.LOG_LABEL, "AppCommonEventSubscriber other action.", new Object[0]);
            }
        }

        private void setCacheData(String str, int i) {
            synchronized (AppChangedListener.this.cacheLock) {
                AppChangedListener.this.actionCache = str;
                AppChangedListener.this.uidCache = i;
            }
        }
    }
}
