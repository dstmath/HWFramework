package ohos.app;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public interface IGeneralReceiver extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.app.generalReceiver";
    public static final int ON_SEND_RESULT = 1;

    void sendResult(int i, PacMap pacMap) throws RemoteException;
}
