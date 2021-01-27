package ohos.ai.engine.upgradestrategy;

import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class UpgradeStrategySkeleton extends RemoteObject implements IUpgradeStrategy {
    public IRemoteObject asObject() {
        return this;
    }

    public UpgradeStrategySkeleton() {
        super(IUpgradeStrategy.DESCRIPTOR);
    }

    public static Optional<IUpgradeStrategy> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IUpgradeStrategy queryLocalInterface = iRemoteObject.queryLocalInterface(IUpgradeStrategy.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IUpgradeStrategy)) {
            return Optional.of(new UpgradeStrategyProxy(iRemoteObject));
        }
        return Optional.of(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        if (i == 3) {
            checkHiAiAppUpdate(UpgradeIndicatorSkeleton.asInterface(messageParcel.readRemoteObject()).orElse(null));
            messageParcel2.writeInt(0);
            return true;
        } else if (i != 4) {
            return UpgradeStrategySkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            updateHiAiApp();
            messageParcel2.writeInt(0);
            return true;
        }
    }
}
