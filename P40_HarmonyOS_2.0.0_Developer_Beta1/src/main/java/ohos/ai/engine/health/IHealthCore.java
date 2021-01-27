package ohos.ai.engine.health;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public interface IHealthCore extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.ai.engine.health.IHealthCore";
    public static final int TRANSACTION_CALL = 3;
    public static final int TRANSACTION_GETPROCESSPRIORITY = 4;
    public static final int TRANSACTION_REPORTCOMPLETED = 2;
    public static final int TRANSACTION_REQUESTRUNNING = 1;

    boolean call(String str, String str2) throws RemoteException;

    int getProcessPriority(String str) throws RemoteException;

    int reportCompleted(int i, String str, PacMap pacMap) throws RemoteException;

    int requestRunning(int i, String str, PacMap pacMap) throws RemoteException;
}
