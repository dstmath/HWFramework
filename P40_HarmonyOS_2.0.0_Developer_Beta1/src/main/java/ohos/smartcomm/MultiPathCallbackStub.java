package ohos.smartcomm;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.Parcel;

class MultiPathCallbackStub extends RemoteObject implements IZMultiPathCallback {
    private static final String DESCRIPTOR = "android.emcom.IMultipathCallback";
    private static final HiLogLabel TAG = new HiLogLabel(3, SmartCommConstant.SMART_COMM_DOMAIN, "MultiPathCallback");
    private IMultiPathCallback callback;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public MultiPathCallbackStub(IMultiPathCallback iMultiPathCallback) {
        super("");
        this.callback = iMultiPathCallback;
    }

    private void readInterfaceToken(String str, Parcel parcel) {
        parcel.readInt();
        parcel.readInt();
        parcel.readString();
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        HiLog.debug(TAG, "call MultiPathCallbackStub's onTransact", new Object[0]);
        if (i != 1) {
            HiLog.error(TAG, "MultiPathCallbackStub unsupported transaction code.", new Object[0]);
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        readInterfaceToken(DESCRIPTOR, messageParcel);
        HiLog.debug(TAG, "MultiPathCallbackStub onCallback was invoked.", new Object[0]);
        onCallback(messageParcel.readString());
        messageParcel2.writeInt(0);
        return true;
    }

    @Override // ohos.smartcomm.IZMultiPathCallback
    public void onCallback(String str) {
        IMultiPathCallback iMultiPathCallback = this.callback;
        if (iMultiPathCallback != null) {
            iMultiPathCallback.onCallback(str);
        }
    }
}
