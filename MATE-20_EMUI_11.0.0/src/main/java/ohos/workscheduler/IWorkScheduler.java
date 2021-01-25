package ohos.workscheduler;

import ohos.aafwk.content.Intent;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface IWorkScheduler extends IRemoteBroker {
    void onCommonEventTriggered(Intent intent) throws RemoteException;

    void onWorkStart(WorkInfo workInfo) throws RemoteException;

    void onWorkStop(WorkInfo workInfo) throws RemoteException;

    boolean sendRemote(IRemoteObject iRemoteObject) throws RemoteException;
}
