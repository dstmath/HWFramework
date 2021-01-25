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
        obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remoteObject.sendRequest(1, obtain, obtain2, new MessageOption());
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return readString;
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public String getComputationalResourceLabel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remoteObject.sendRequest(2, obtain, obtain2, new MessageOption());
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return readString;
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public String getXpuLabel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remoteObject.sendRequest(3, obtain, obtain2, new MessageOption());
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return readString;
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public String getDistanceLabel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remoteObject.sendRequest(4, obtain, obtain2, new MessageOption());
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return readString;
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public String getCameraLabel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remoteObject.sendRequest(5, obtain, obtain2, new MessageOption());
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return readString;
    }

    @Override // ohos.ai.engine.pluginlabel.IPluginLabel
    public PluginLabelInfo getPluginLabelInfo() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IPluginLabel.DESCRIPTOR);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remoteObject.sendRequest(6, obtain, obtain2, new MessageOption());
        PluginLabelInfo pluginLabelInfo = new PluginLabelInfo();
        obtain2.readSequenceable(pluginLabelInfo);
        obtain2.reclaim();
        obtain.reclaim();
        return pluginLabelInfo;
    }
}
