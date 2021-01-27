package ohos.ai.engine.pluginbridge;

import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class CoreServiceProxy implements ICoreService {
    private final IRemoteObject remote;

    CoreServiceProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.ai.engine.pluginbridge.ICoreService
    public IRemoteObject getReportCoreRemoteObject() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICoreService.DESCRIPTOR);
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readRemoteObject();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginbridge.ICoreService
    public IRemoteObject getHealthCoreRemoteObject() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICoreService.DESCRIPTOR);
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readRemoteObject();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginbridge.ICoreService
    public IRemoteObject getCloudStrategyRemoteObject() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICoreService.DESCRIPTOR);
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readRemoteObject();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginbridge.ICoreService
    public IRemoteObject getUpgradeStrategyRemoteObject() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICoreService.DESCRIPTOR);
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readRemoteObject();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginbridge.ICoreService
    public IRemoteObject getModelCoreRemoteObject() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICoreService.DESCRIPTOR);
            this.remote.sendRequest(5, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readRemoteObject();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginbridge.ICoreService
    public IRemoteObject getPluginLabelRemoteObject() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICoreService.DESCRIPTOR);
            this.remote.sendRequest(14, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readRemoteObject();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginbridge.ICoreService
    public IRemoteObject getSystemCoreRemoteObject() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICoreService.DESCRIPTOR);
            this.remote.sendRequest(15, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readRemoteObject();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginbridge.ICoreService
    public boolean isOpen(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(ICoreService.DESCRIPTOR);
            obtain.writeInt(i);
            this.remote.sendRequest(13, obtain, obtain2, messageOption);
            obtain2.readInt();
            return obtain2.readInt() != 0;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
