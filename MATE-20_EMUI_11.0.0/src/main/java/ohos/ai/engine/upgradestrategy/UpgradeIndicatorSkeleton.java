package ohos.ai.engine.upgradestrategy;

import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class UpgradeIndicatorSkeleton extends RemoteObject implements IUpgradeIndicator {
    public IRemoteObject asObject() {
        return this;
    }

    public UpgradeIndicatorSkeleton() {
        super(IUpgradeIndicator.DESCRIPTOR);
    }

    public static Optional<IUpgradeIndicator> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IUpgradeIndicator queryLocalInterface = iRemoteObject.queryLocalInterface(IUpgradeIndicator.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IUpgradeIndicator)) {
            return Optional.ofNullable(new UpgradeIndicatorProxy(iRemoteObject));
        }
        return Optional.ofNullable(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        if (i != 1) {
            return UpgradeIndicatorSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        onUpdate(messageParcel.readInt() != 0);
        messageParcel2.writeInt(0);
        return true;
    }
}
