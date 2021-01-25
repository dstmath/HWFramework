package ohos.ai.engine.aimodel;

import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class RecordObserverCallbackSkeleton extends RemoteObject implements IRecordObserverCallback {
    public IRemoteObject asObject() {
        return this;
    }

    public RecordObserverCallbackSkeleton(String str) {
        super(str);
    }

    public static Optional<IRecordObserverCallback> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IRecordObserverCallback queryLocalInterface = iRemoteObject.queryLocalInterface(IRecordObserverCallback.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IRecordObserverCallback)) {
            return Optional.ofNullable(new RecordObserverCallbackProxy(iRemoteObject));
        }
        return Optional.ofNullable(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        if (i != 1) {
            return RecordObserverCallbackSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        CallbackBean callbackBean = new CallbackBean();
        messageParcel.readSequenceable(callbackBean);
        onRecordChanged(callbackBean);
        return true;
    }
}
