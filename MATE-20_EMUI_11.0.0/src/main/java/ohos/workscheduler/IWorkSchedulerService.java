package ohos.workscheduler;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IWorkSchedulerService extends IRemoteBroker {
    WorkInfo getWorkStatus(int i) throws RemoteException;

    boolean startWorkNow(WorkInfo workInfo, boolean z) throws RemoteException;

    boolean stopWork(WorkInfo workInfo, boolean z, boolean z2) throws RemoteException;
}
