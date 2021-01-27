package ohos.location.callback;

import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.location.Location;
import ohos.location.LocatorCallback;
import ohos.location.common.LBSLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

@SystemApi
public class LocatorCallbackHost extends RemoteObject implements ILocatorCallback {
    private static final HiLogLabel LABEL = new HiLogLabel(3, LBSLog.LOCATOR_LOG_ID, "LocatorCallbackHost");
    private static final String LOCATOR_DESCRIPTOR = "location.ILocator";
    private static final int SYSTEM_UID = 1000;
    private LocatorCallback mCallback;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public LocatorCallbackHost(String str, LocatorCallback locatorCallback) {
        super(str);
        this.mCallback = locatorCallback;
    }

    public LocatorCallbackHost(LocatorCallback locatorCallback) {
        this(ILocatorCallback.DESCRIPTOR, locatorCallback);
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || !LOCATOR_DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            return false;
        }
        int callingUid = getCallingUid();
        if (callingUid > 1000) {
            HiLog.error(LABEL, "invalid uid: %{public}d", Integer.valueOf(callingUid));
            return false;
        }
        if (i == 1) {
            Location location = new Location(0.0d, 0.0d);
            location.unmarshalling(messageParcel);
            onLocationReport(location);
        } else if (i == 2) {
            onStatusChanged(messageParcel.readInt());
        } else if (i == 3) {
            onErrorReport(messageParcel.readInt());
        }
        return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
    }

    @Override // ohos.location.callback.ILocatorCallback
    public void onLocationReport(Location location) throws RemoteException {
        HiLog.info(LABEL, "onLocationReport callback: %{public}s", Integer.toHexString(System.identityHashCode(this.mCallback)));
        this.mCallback.onLocationReport(location);
    }

    @Override // ohos.location.callback.ILocatorCallback
    public void onStatusChanged(int i) throws RemoteException {
        HiLog.debug(LABEL, "onStatusChanged, type: %{public}d", Integer.valueOf(i));
        this.mCallback.onStatusChanged(i);
    }

    @Override // ohos.location.callback.ILocatorCallback
    public void onErrorReport(int i) throws RemoteException {
        HiLog.debug(LABEL, "onErrorReport, type: %{public}d", Integer.valueOf(i));
        this.mCallback.onErrorReport(i);
    }
}
