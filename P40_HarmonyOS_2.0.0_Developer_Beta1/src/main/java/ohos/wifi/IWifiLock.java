package ohos.wifi;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface IWifiLock extends IRemoteBroker {
    int acquire(IRemoteObject iRemoteObject, String str, int i, String str2) throws RemoteException;

    int release(IRemoteObject iRemoteObject, int i, String str) throws RemoteException;
}
