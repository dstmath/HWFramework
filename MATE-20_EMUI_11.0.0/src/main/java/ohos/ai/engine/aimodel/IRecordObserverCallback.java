package ohos.ai.engine.aimodel;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IRecordObserverCallback extends IRemoteBroker {
    public static final String DESCRIPTOR = "ohos.ai.engine.aimodel.IRecordObserverCallback";
    public static final int ON_RECORD_CHANGED = 1;

    void onRecordChanged(CallbackBean callbackBean) throws RemoteException;
}
