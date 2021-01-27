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
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInterfaceToken(IGrsCallback.DESCRIPTOR);
            obtain.writeInt(i);
            obtain.writeString(str);
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
