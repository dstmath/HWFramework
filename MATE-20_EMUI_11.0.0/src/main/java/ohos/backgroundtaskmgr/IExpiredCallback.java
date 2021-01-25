package ohos.backgroundtaskmgr;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IExpiredCallback extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.resourceschedule.IExpiredCallback";
    public static final int ON_EXPIRED = 1;

    void onExpired() throws RemoteException;
}
