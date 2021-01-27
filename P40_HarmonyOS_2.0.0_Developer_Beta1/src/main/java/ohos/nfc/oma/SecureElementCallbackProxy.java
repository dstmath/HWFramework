package ohos.nfc.oma;

import ohos.nfc.oma.SEService;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public final class SecureElementCallbackProxy extends RemoteObject implements ISecureElementCallback {
    private static final int ON_SERVICE_CONNECTED = 1;
    private static final int ON_SERVICE_DISCONNECTD = 2;
    private SEService.OnCallback mCallback = null;
    private SEService mService = null;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    @Override // ohos.nfc.oma.ISecureElementCallback
    public void onServiceDisconnected() {
    }

    public SecureElementCallbackProxy(SEService sEService, SEService.OnCallback onCallback, String str) {
        super(str);
        this.mService = sEService;
        this.mCallback = onCallback;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i == 1) {
            onServiceConnected();
            return true;
        } else if (i != 2) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            onServiceDisconnected();
            return true;
        }
    }

    @Override // ohos.nfc.oma.ISecureElementCallback
    public void onServiceConnected() {
        SEService.OnCallback onCallback = this.mCallback;
        if (onCallback != null) {
            onCallback.serviceConnected();
        }
        SEService sEService = this.mService;
        if (sEService != null) {
            sEService.notifyServiceConnected(true);
        }
    }
}
