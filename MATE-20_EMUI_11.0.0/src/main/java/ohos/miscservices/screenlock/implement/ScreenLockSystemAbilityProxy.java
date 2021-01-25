package ohos.miscservices.screenlock.implement;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.screenlock.ScreenLockSystemAbility;
import ohos.miscservices.screenlock.interfaces.IScreenLockSystemAbility;
import ohos.miscservices.screenlock.interfaces.UnlockScreenCallback;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class ScreenLockSystemAbilityProxy implements IScreenLockSystemAbility {
    private static final int COMMAND_IS_LOCKED = 0;
    private static final int COMMAND_UNLOCK = 1;
    private static final int ERR_OK = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "ScreenLockSystemAbilityProxy");
    private final IRemoteObject remote;
    private final ScreenLockSystemAbility screenLockSA = ScreenLockSystemAbility.getInstance();

    public ScreenLockSystemAbilityProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.screenlock.interfaces.IScreenLockSystemAbility
    public boolean isLocked() throws RemoteException {
        HiLog.debug(TAG, "isLocked.", new Object[0]);
        return this.screenLockSA.isLocked();
    }

    @Override // ohos.miscservices.screenlock.interfaces.IScreenLockSystemAbility
    public void unlock(Context context, UnlockScreenCallback unlockScreenCallback) throws RemoteException {
        HiLog.debug(TAG, "unlock.", new Object[0]);
        this.screenLockSA.unlock(context, unlockScreenCallback);
    }
}
