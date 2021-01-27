package ohos.bundle;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IInstallerCallback extends IRemoteBroker {
    public static final String DESCRIPTOR = "OHOS.Appexecfwk.IStatusReceiver";
    public static final int ON_FINISHED = 0;

    void onFinished(int i, String str) throws RemoteException;
}
