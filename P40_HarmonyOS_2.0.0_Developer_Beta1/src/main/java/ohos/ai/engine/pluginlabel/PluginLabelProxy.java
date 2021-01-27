package ohos.ai.engine.pluginlabel;

import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class PluginLabelProxy implements IPluginLabel {
    private final IRemoteObject remoteObject;

    PluginLabelProxy(IRemoteObject iRemoteObject) {
        this.remoteObject = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remoteObject;
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public String getRegionLabel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
            this.remoteObject.sendRequest(1, obtain, obtain2, messageOption);
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public String getComputationalResourceLabel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
            this.remoteObject.sendRequest(2, obtain, obtain2, messageOption);
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public String getXpuLabel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
            this.remoteObject.sendRequest(3, obtain, obtain2, messageOption);
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public String getDistanceLabel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
            this.remoteObject.sendRequest(4, obtain, obtain2, messageOption);
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public String getCameraLabel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
            this.remoteObject.sendRequest(5, obtain, obtain2, messageOption);
            return obtain2.readString();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public PluginLabelInfo getPluginLabelInfo() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
            this.remoteObject.sendRequest(6, obtain, obtain2, messageOption);
            PluginLabelInfo pluginLabelInfo = new PluginLabelInfo();
            obtain2.readSequenceable(pluginLabelInfo);
            return pluginLabelInfo;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
