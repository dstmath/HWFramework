package ohos.ai.engine.aimodel;

import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class RecordObserverCallbackProxy implements IRecordObserverCallback {
    private final IRemoteObject remote;

    RecordObserverCallbackProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.ai.engine.aimodel.IRecordObserverCallback
    public void onRecordChanged(CallbackBean callbackBean) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IRecordObserverCallback.DESCRIPTOR);
            obtain.writeSequenceable(callbackBean);
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
