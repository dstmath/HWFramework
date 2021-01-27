package ohos.miscservices.screenlock;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.screenlock.implement.ScreenLockSystemAbilitySkeleton;
import ohos.miscservices.screenlock.interfaces.IScreenLockSystemAbility;
import ohos.miscservices.screenlock.interfaces.UnlockScreenCallback;
import ohos.rpc.RemoteException;

public class ScreenLockController {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "ScreenLockController");
    private IScreenLockSystemAbility screenLockProxy;

    private ScreenLockController() {
        this.screenLockProxy = ScreenLockSystemAbilitySkeleton.asInterface(null);
    }

    public static ScreenLockController getInstance() {
        HiLog.debug(TAG, "getInstance: get an instance of ScreenLockController", new Object[0]);
        return ScreenLockControllerInner.singleton;
    }

    private static class ScreenLockControllerInner {
        private static ScreenLockController singleton = new ScreenLockController();

        private ScreenLockControllerInner() {
        }
    }

    public boolean isScreenLocked() {
        try {
            HiLog.debug(TAG, "IPC screen lock system ability proxy's isScreenLocked", new Object[0]);
            return this.screenLockProxy.isLocked();
        } catch (RemoteException e) {
            HiLog.error(TAG, "isScreenLocked RemoteException msg=%{public}s", e.getLocalizedMessage());
            return true;
        }
    }

    public void unlockScreen(Context context, UnlockScreenCallback unlockScreenCallback) {
        try {
            HiLog.debug(TAG, "Unlock screen from ability.", new Object[0]);
            this.screenLockProxy.unlock(context, unlockScreenCallback);
        } catch (RemoteException e) {
            HiLog.error(TAG, "unlockScreen RemoteException msg=%{public}s", e.getLocalizedMessage());
        }
    }

    public boolean isSecureMode() {
        try {
            HiLog.debug(TAG, "enter isSecureMode.", new Object[0]);
            return this.screenLockProxy.isSecureMode();
        } catch (RemoteException e) {
            HiLog.error(TAG, "isSecureMode RemoteException msg=%{public}s", e.getLocalizedMessage());
            return false;
        }
    }
}
