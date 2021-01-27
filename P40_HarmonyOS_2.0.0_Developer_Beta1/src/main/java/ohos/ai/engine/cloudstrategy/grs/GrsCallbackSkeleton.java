package ohos.ai.engine.cloudstrategy.grs;

import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class GrsCallbackSkeleton extends RemoteObject implements IGrsCallback {
    public IRemoteObject asObject() {
        return this;
    }

    public GrsCallbackSkeleton() {
        super(IGrsCallback.DESCRIPTOR);
    }

    public static Optional<IGrsCallback> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IGrsCallback queryLocalInterface = iRemoteObject.queryLocalInterface(IGrsCallback.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IGrsCallback)) {
            return Optional.of(new GrsCallbackProxy(iRemoteObject));
        }
        return Optional.of(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        if (i != 1) {
            return GrsCallbackSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        onGrsResult(messageParcel.readInt(), messageParcel.readString());
        return true;
    }
}
