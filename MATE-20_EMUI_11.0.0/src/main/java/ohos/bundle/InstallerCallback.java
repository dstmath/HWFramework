package ohos.bundle;

import ohos.appexecfwk.utils.AppLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class InstallerCallback extends RemoteObject implements IInstallerCallback {
    public IRemoteObject asObject() {
        return this;
    }

    @Override // ohos.bundle.IInstallerCallback
    public abstract void onFinished(int i, String str);

    public InstallerCallback() {
        super(IInstallerCallback.DESCRIPTOR);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            return false;
        }
        AppLog.d("InstallerCallback::onRemoteRequest code: %{public}d", Integer.valueOf(i));
        if (!IInstallerCallback.DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            AppLog.e("InstallerCallback::onRemoteRequest token invalid", new Object[0]);
            return false;
        }
        if (i != 0) {
            AppLog.w("InstallerCallback::onTransact unknown, code: %{public}d", Integer.valueOf(i));
        } else {
            int readInt = messageParcel.readInt();
            String readString = messageParcel.readString();
            AppLog.d("InstallerCallback::onTransact onfinish result: status = %{public}d, statusMessage = %{public}s", Integer.valueOf(readInt), readString);
            onFinished(readInt, readString);
        }
        return InstallerCallback.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
    }
}
