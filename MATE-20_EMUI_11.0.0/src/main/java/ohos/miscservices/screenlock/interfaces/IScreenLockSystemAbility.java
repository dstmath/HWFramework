package ohos.miscservices.screenlock.interfaces;

import ohos.app.Context;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IScreenLockSystemAbility extends IRemoteBroker {
    boolean isLocked() throws RemoteException;

    void unlock(Context context, UnlockScreenCallback unlockScreenCallback) throws RemoteException;
}
