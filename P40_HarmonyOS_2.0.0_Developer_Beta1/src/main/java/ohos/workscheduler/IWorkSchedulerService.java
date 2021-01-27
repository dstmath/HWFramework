package ohos.workscheduler;

import java.util.List;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IWorkSchedulerService extends IRemoteBroker {
    WorkInfo getWorkStatus(int i) throws RemoteException;

    boolean isLastWorkTimeOut(int i) throws RemoteException;

    List<WorkInfo> obtainAllWorks() throws RemoteException;

    boolean startWorkNow(WorkInfo workInfo, boolean z) throws RemoteException;

    boolean stopAndClearWorks() throws RemoteException;

    boolean stopWork(WorkInfo workInfo, boolean z, boolean z2) throws RemoteException;
}
