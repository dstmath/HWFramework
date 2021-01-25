package ohos.miscservices.screenlock;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.screenlock.implement.ScreenLockSystemAbilitySkeleton;
import ohos.miscservices.screenlock.interfaces.UnlockScreenCallback;
import ohos.rpc.RemoteException;

public class ScreenLockSystemAbility extends ScreenLockSystemAbilitySkeleton {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "ScreenLockSystemAbility");
    private final KeyguardManagerAdapter keyguardManagerAdapter;

    private ScreenLockSystemAbility() {
        this.keyguardManagerAdapter = KeyguardManagerAdapter.getInstance();
    }

    public static ScreenLockSystemAbility getInstance() {
        HiLog.debug(TAG, "getInstance: get an instance of ScreenLockSystemAbility", new Object[0]);
        return ScreenLockSystemAbilityImplInner.singleton;
    }

    private static class ScreenLockSystemAbilityImplInner {
        private static ScreenLockSystemAbility singleton = new ScreenLockSystemAbility();

        private ScreenLockSystemAbilityImplInner() {
        }
    }

    @Override // ohos.miscservices.screenlock.interfaces.IScreenLockSystemAbility
    public boolean isLocked() throws RemoteException {
        return this.keyguardManagerAdapter.isLocked();
    }

    @Override // ohos.miscservices.screenlock.interfaces.IScreenLockSystemAbility
    public void unlock(Context context, UnlockScreenCallback unlockScreenCallback) throws RemoteException {
        this.keyguardManagerAdapter.unlock(context, unlockScreenCallback);
    }
}
