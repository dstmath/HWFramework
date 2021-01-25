package ohos.ai.engine.cloudstrategy.grs;

import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class GrsCallbackProxy implements IGrsCallback {
    private final IRemoteObject remote;

    GrsCallbackProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.ai.engine.cloudstrategy.grs.IGrsCallback
    public void onGrsResult(int i, String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInterfaceToken(IGrsCallback.DESCRIPTOR);
        obtain.writeInt(i);
        obtain.writeString(str);
        MessageParcel obtain2 = MessageParcel.obtain();
        this.remote.sendRequest(1, obtain, obtain2, new MessageOption());
        obtain2.reclaim();
        obtain.reclaim();
    }
}
