package ohos.bundle;

import ohos.appexecfwk.utils.AppLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class ShellApplicationCallback extends RemoteObject implements IShellApplication {
    public IRemoteObject asObject() {
        return this;
    }

    @Override // ohos.bundle.IShellApplication
    public abstract void onBundleUpdated(BundleInfo bundleInfo);

    public ShellApplicationCallback() {
        super(IShellApplication.DESCRIPTOR);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            return false;
        }
        AppLog.d("ShellApplicationCallback::onRemoteRequest code: %{public}d", Integer.valueOf(i));
        if (!IShellApplication.DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            AppLog.e("ShellApplicationCallback::onRemoteRequest token invalid", new Object[0]);
            return false;
        } else if (i != 0) {
            AppLog.w("ShellApplicationCallback::onRemoteRequest unknown code", new Object[0]);
            return ShellApplicationCallback.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            BundleInfo bundleInfo = new BundleInfo();
            if (!messageParcel.readSequenceable(bundleInfo)) {
                return false;
            }
            onBundleUpdated(bundleInfo);
            return true;
        }
    }
}
