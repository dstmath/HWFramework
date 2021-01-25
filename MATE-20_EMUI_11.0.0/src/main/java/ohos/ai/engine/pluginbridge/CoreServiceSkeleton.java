package ohos.ai.engine.pluginbridge;

import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class CoreServiceSkeleton extends RemoteObject implements ICoreService {
    public IRemoteObject asObject() {
        return this;
    }

    public CoreServiceSkeleton() {
        super(ICoreService.DESCRIPTOR);
    }

    public static Optional<ICoreService> asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        ICoreService queryLocalInterface = iRemoteObject.queryLocalInterface(ICoreService.DESCRIPTOR);
        if (queryLocalInterface == null || !(queryLocalInterface instanceof ICoreService)) {
            return Optional.ofNullable(new CoreServiceProxy(iRemoteObject));
        }
        return Optional.ofNullable(queryLocalInterface);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        messageParcel.readInterfaceToken();
        if (i == 1) {
            IRemoteObject reportCoreRemoteObject = getReportCoreRemoteObject();
            messageParcel2.writeInt(0);
            return messageParcel2.writeRemoteObject(reportCoreRemoteObject);
        } else if (i == 2) {
            IRemoteObject healthCoreRemoteObject = getHealthCoreRemoteObject();
            messageParcel2.writeInt(0);
            return messageParcel2.writeRemoteObject(healthCoreRemoteObject);
        } else if (i == 3) {
            IRemoteObject cloudStrategyRemoteObject = getCloudStrategyRemoteObject();
            messageParcel2.writeInt(0);
            return messageParcel2.writeRemoteObject(cloudStrategyRemoteObject);
        } else if (i == 4) {
            IRemoteObject upgradeStrategyRemoteObject = getUpgradeStrategyRemoteObject();
            messageParcel2.writeInt(0);
            return messageParcel2.writeRemoteObject(upgradeStrategyRemoteObject);
        } else if (i != 5) {
            switch (i) {
                case 13:
                    boolean isOpen = isOpen(messageParcel.readInt());
                    messageParcel2.writeInt(0);
                    return messageParcel2.writeInt(isOpen ? 1 : 0);
                case 14:
                    IRemoteObject pluginLabelRemoteObject = getPluginLabelRemoteObject();
                    messageParcel2.writeInt(0);
                    return messageParcel2.writeRemoteObject(pluginLabelRemoteObject);
                case 15:
                    IRemoteObject systemCoreRemoteObject = getSystemCoreRemoteObject();
                    messageParcel2.writeInt(0);
                    return messageParcel2.writeRemoteObject(systemCoreRemoteObject);
                default:
                    return CoreServiceSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            }
        } else {
            IRemoteObject modelCoreRemoteObject = getModelCoreRemoteObject();
            messageParcel2.writeInt(0);
            return messageParcel2.writeRemoteObject(modelCoreRemoteObject);
        }
    }
}
