package ohos.miscservices.inputmethod.internal;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IUriPermission extends IRemoteBroker {
    void release() throws RemoteException;

    void take() throws RemoteException;
}
