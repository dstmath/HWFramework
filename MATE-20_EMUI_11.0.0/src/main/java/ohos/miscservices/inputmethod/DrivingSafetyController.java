package ohos.miscservices.inputmethod;

import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentFilter;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.ivicommon.drivingsafety.DrivingSafetyClient;
import ohos.ivicommon.drivingsafety.model.ControlItemEnum;
import ohos.rpc.RemoteException;

/* access modifiers changed from: package-private */
public class DrivingSafetyController {
    private static final int DRIVE_EVENT_PRIORITY = 100;
    private static final String DRIVE_MODE_INTENT = "com.action.ivi.drvmod_change";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "DrivingSafetyController");
    private IDrivingSafety mDrivingSafety;
    private DriveEventSubscriber mEventSubscriber;
    private boolean mIsRegistered = false;

    public DrivingSafetyController() {
        initDriveEventSubscribe();
        this.mDrivingSafety = new DrivingSafetyImpl();
    }

    private void initDriveEventSubscribe() {
        HiLog.info(TAG, "init drive event begin.", new Object[0]);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("common.event.DRIVE_MODE");
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(intentFilter);
        commonEventSubscribeInfo.setPriority(100);
        this.mEventSubscriber = new DriveEventSubscriber(commonEventSubscribeInfo);
    }

    public void registerDriveEventListener() {
        HiLog.info(TAG, "registerDriveEventListener begin.", new Object[0]);
        try {
            if (this.mIsRegistered) {
                HiLog.info(TAG, "already registered.", new Object[0]);
                return;
            }
            CommonEventManager.subscribeCommonEvent(this.mEventSubscriber);
            this.mIsRegistered = true;
        } catch (RemoteException e) {
            HiLog.error(TAG, "registerDriveEventListener fail. err msg is %{public}s", e.getMessage());
        }
    }

    public void removeDriveEventListener() {
        HiLog.info(TAG, "removeDriveEventListener begin.", new Object[0]);
        try {
            if (this.mIsRegistered) {
                CommonEventManager.unsubscribeCommonEvent(this.mEventSubscriber);
                this.mIsRegistered = false;
                HiLog.info(TAG, "removeDriveEventListener success", new Object[0]);
            }
        } catch (RemoteException e) {
            HiLog.error(TAG, "removeDriveEventListener fail. err msg is %{public}s", e.getMessage());
        }
    }

    public boolean isDrivingSafety(ControlItemEnum controlItemEnum) {
        return this.mDrivingSafety.isDrivingSafety(controlItemEnum);
    }

    public void setDrivingSafety(IDrivingSafety iDrivingSafety) {
        this.mDrivingSafety = iDrivingSafety;
    }

    /* access modifiers changed from: private */
    public class DriveEventSubscriber extends CommonEventSubscriber {
        DriveEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        public void onReceiveEvent(CommonEventData commonEventData) {
            HiLog.debug(DrivingSafetyController.TAG, "onReceiveEvent begin", new Object[0]);
            if (commonEventData == null) {
                HiLog.error(DrivingSafetyController.TAG, "commonEventData is null", new Object[0]);
                return;
            }
            Intent intent = commonEventData.getIntent();
            if (intent == null) {
                HiLog.error(DrivingSafetyController.TAG, "intent is null", new Object[0]);
            } else if (!intent.getBooleanParam(DrivingSafetyController.DRIVE_MODE_INTENT, false)) {
                HiLog.info(DrivingSafetyController.TAG, "current is not drive mode.", new Object[0]);
            } else if (DrivingSafetyController.this.isDrivingSafety(ControlItemEnum.IME)) {
                HiLog.info(DrivingSafetyController.TAG, "current is not safety drive mode.", new Object[0]);
            } else if (!InputMethodController.getInstance().stopInput(1)) {
                HiLog.error(DrivingSafetyController.TAG, "Stop input fail.", new Object[0]);
            }
        }
    }

    private class DrivingSafetyImpl implements IDrivingSafety {
        private DrivingSafetyImpl() {
        }

        @Override // ohos.miscservices.inputmethod.IDrivingSafety
        public boolean isDrivingSafety(ControlItemEnum controlItemEnum) {
            return DrivingSafetyClient.isDrivingSafety(null, controlItemEnum);
        }
    }
}
