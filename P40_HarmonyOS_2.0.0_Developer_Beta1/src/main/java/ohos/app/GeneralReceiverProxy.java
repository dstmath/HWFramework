package ohos.app;

import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

/* access modifiers changed from: package-private */
/* compiled from: GeneralReceiverSkeleton */
public class GeneralReceiverProxy implements IGeneralReceiver {
    private final IRemoteObject remote;

    GeneralReceiverProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.app.IGeneralReceiver
    public void sendResult(int i, PacMap pacMap) throws RemoteException {
        if (this.remote != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            if (obtain.writeInterfaceToken(IGeneralReceiver.DESCRIPTOR) && obtain.writeInt(i)) {
                obtain.writeSequenceable(pacMap);
                try {
                    this.remote.sendRequest(1, obtain, obtain2, messageOption);
                } finally {
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            }
        }
    }
}
