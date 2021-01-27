package ohos.miscservices.screenlock.implement;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.screenlock.interfaces.IScreenLockSystemAbility;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class ScreenLockSystemAbilitySkeleton extends RemoteObject implements IScreenLockSystemAbility {
    private static final int COMMAND_IS_LOCKED = 0;
    private static final int COMMAND_IS_SECURE_MODE = 2;
    private static final int COMMAND_UNLOCK = 1;
    private static final String DESCRIPTOR = "ohos.miscservices.screenlock.interface.IScreenLockSystemAbility";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "ScreenLockSystemAbilitySkeleton");
    private static IScreenLockSystemAbility sProxy;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public ScreenLockSystemAbilitySkeleton() {
        super(DESCRIPTOR);
    }

    public static IScreenLockSystemAbility asInterface(IRemoteObject iRemoteObject) {
        IScreenLockSystemAbility iScreenLockSystemAbility;
        if (iRemoteObject == null) {
            synchronized (ScreenLockSystemAbilitySkeleton.class) {
                if (sProxy == null) {
                    sProxy = new ScreenLockSystemAbilityProxy(null);
                }
                iScreenLockSystemAbility = sProxy;
            }
            return iScreenLockSystemAbility;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new ScreenLockSystemAbilityProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IScreenLockSystemAbility) {
            return (IScreenLockSystemAbility) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i == 0) {
            HiLog.debug(TAG, "onRemoteRequest: COMMAND_IS_LOCKED=%{public}s", 0);
            return true;
        } else if (i == 1) {
            HiLog.debug(TAG, "onRemoteRequest: COMMAND_IS_LOCKED=%{public}s", 1);
            return true;
        } else if (i != 2) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            HiLog.debug(TAG, "onRemoteRequest: COMMAND_IS_SECURE_MODE=%{public}s", 2);
            return true;
        }
    }
}
