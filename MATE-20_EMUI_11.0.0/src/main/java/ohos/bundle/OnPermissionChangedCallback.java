package ohos.bundle;

import ohos.appexecfwk.utils.AppLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class OnPermissionChangedCallback extends RemoteObject implements OnPermissionChanged {
    public IRemoteObject asObject() {
        return this;
    }

    @Override // ohos.bundle.OnPermissionChanged
    public abstract void onChanged(int i);

    public OnPermissionChangedCallback() {
        super(OnPermissionChanged.DESCRIPTOR);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            return false;
        }
        AppLog.d("OnPermissionChangedCallback::onRemoteRequest code: %{public}d", Integer.valueOf(i));
        if (!OnPermissionChanged.DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            AppLog.e("OnPermissionChangedCallback::onRemoteRequest token invalid", new Object[0]);
            return false;
        } else if (i != 0) {
            AppLog.w("OnPermissionChangedCallback::onRemoteRequest unknown code", new Object[0]);
            return OnPermissionChangedCallback.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            onChanged(messageParcel.readInt());
            return true;
        }
    }
}
