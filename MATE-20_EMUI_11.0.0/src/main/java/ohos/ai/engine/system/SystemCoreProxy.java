package ohos.ai.engine.system;

import java.util.Optional;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class SystemCoreProxy implements ISystemCore {
    private final IRemoteObject remoteObject;

    SystemCoreProxy(IRemoteObject iRemoteObject) {
        this.remoteObject = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remoteObject;
    }

    @Override // ohos.ai.engine.system.ISystemCore
    public Optional<String> getProp(String str, String str2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(ISystemCore.DESCRIPTOR);
        obtain.writeString(str);
        obtain.writeString(str2);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remoteObject.sendRequest(1, obtain, obtain2, new MessageOption());
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return Optional.of(readString);
    }

    @Override // ohos.ai.engine.system.ISystemCore
    public Optional<String> getUdid() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(ISystemCore.DESCRIPTOR);
        this.remoteObject.sendRequest(2, obtain, obtain2, messageOption);
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return Optional.of(readString);
    }

    @Override // ohos.ai.engine.system.ISystemCore
    public Optional<String> getSerialNumber() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(ISystemCore.DESCRIPTOR);
        this.remoteObject.sendRequest(3, obtain, obtain2, messageOption);
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return Optional.of(readString);
    }

    @Override // ohos.ai.engine.system.ISystemCore
    public Optional<String> getSystemVersion() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(ISystemCore.DESCRIPTOR);
        this.remoteObject.sendRequest(4, obtain, obtain2, messageOption);
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return Optional.of(readString);
    }

    @Override // ohos.ai.engine.system.ISystemCore
    public Optional<String> getSystemModel() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(ISystemCore.DESCRIPTOR);
        this.remoteObject.sendRequest(5, obtain, obtain2, messageOption);
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return Optional.of(readString);
    }

    @Override // ohos.ai.engine.system.ISystemCore
    public Optional<String> getDeviceBrand() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(ISystemCore.DESCRIPTOR);
        this.remoteObject.sendRequest(6, obtain, obtain2, messageOption);
        String readString = obtain2.readString();
        obtain2.reclaim();
        obtain.reclaim();
        return Optional.of(readString);
    }
}
