package ohos.ai.engine.upgradestrategy;

import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class UpgradeStrategyProxy implements IUpgradeStrategy {
    private final IRemoteObject remoteObject;

    UpgradeStrategyProxy(IRemoteObject iRemoteObject) {
        this.remoteObject = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remoteObject;
    }

    @Override // ohos.ai.engine.upgradestrategy.IUpgradeStrategy
    public void checkHiAiAppUpdate(IUpgradeIndicator iUpgradeIndicator) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IUpgradeStrategy.DESCRIPTOR);
            obtain.writeRemoteObject(iUpgradeIndicator.asObject());
            this.remoteObject.sendRequest(3, obtain, obtain2, messageOption);
            obtain2.readInt();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.upgradestrategy.IUpgradeStrategy
    public void updateHiAiApp() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IUpgradeStrategy.DESCRIPTOR);
            this.remoteObject.sendRequest(4, obtain, obtain2, messageOption);
            obtain2.readInt();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
