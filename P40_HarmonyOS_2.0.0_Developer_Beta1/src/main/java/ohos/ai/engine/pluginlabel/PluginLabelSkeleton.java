package ohos.ai.engine.pluginlabel;

import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class PluginLabelSkeleton extends RemoteObject implements IPluginLabel {
    public IRemoteObject asObject() {
        return this;
    }

    public PluginLabelSkeleton() {
        super(IPluginLabel.DESCRIPTOR);
    }

    public static Optional<IPluginLabel> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IPluginLabel queryLocalInterface = iRemoteObject.queryLocalInterface(IPluginLabel.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof IPluginLabel)) {
            return Optional.of(new PluginLabelProxy(iRemoteObject));
        }
        return Optional.of(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        switch (i) {
            case 1:
                return messageParcel2.writeString(getRegionLabel());
            case 2:
                return messageParcel2.writeString(getComputationalResourceLabel());
            case 3:
                return messageParcel2.writeString(getXpuLabel());
            case 4:
                return messageParcel2.writeString(getDistanceLabel());
            case 5:
                return messageParcel2.writeString(getCameraLabel());
            case 6:
                messageParcel2.writeSequenceable(getPluginLabelInfo());
                return true;
            default:
                return PluginLabelSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
    }
}
