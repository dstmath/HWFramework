package ohos.ai.engine.health;

import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public class HealthCoreProxy implements IHealthCore {
    private final IRemoteObject remote;

    HealthCoreProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.ai.engine.health.IHealthCore
    public int requestRunning(int i, String str, PacMap pacMap) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IHealthCore.DESCRIPTOR);
        obtain.writeInt(i);
        obtain.writeString(str);
        obtain.writeSequenceable(pacMap);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remote.sendRequest(1, obtain, obtain2, new MessageOption());
        int readInt = obtain2.readInt();
        obtain2.reclaim();
        obtain.reclaim();
        return readInt;
    }

    @Override // ohos.ai.engine.health.IHealthCore
    public int reportCompleted(int i, String str, PacMap pacMap) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IHealthCore.DESCRIPTOR);
        obtain.writeInt(i);
        obtain.writeString(str);
        obtain.writeSequenceable(pacMap);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remote.sendRequest(2, obtain, obtain2, new MessageOption());
        int readInt = obtain2.readInt();
        obtain2.reclaim();
        obtain.reclaim();
        return readInt;
    }

    @Override // ohos.ai.engine.health.IHealthCore
    public boolean call(String str, String str2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IHealthCore.DESCRIPTOR);
        obtain.writeString(str);
        obtain.writeString(str2);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remote.sendRequest(3, obtain, obtain2, new MessageOption());
        boolean readBoolean = obtain2.readBoolean();
        obtain2.reclaim();
        obtain.reclaim();
        return readBoolean;
    }

    @Override // ohos.ai.engine.health.IHealthCore
    public int getProcessPriority(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        obtain.writeInterfaceToken(IHealthCore.DESCRIPTOR);
        obtain.writeString(str);
        this.remote.sendRequest(4, obtain, obtain2, messageOption);
        int readInt = obtain2.readInt();
        obtain2.reclaim();
        obtain.reclaim();
        return readInt;
    }
}
