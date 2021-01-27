package ohos.location.callback;

import ohos.annotation.SystemApi;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.location.SwitchCallback;
import ohos.location.common.LBSLog;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

@SystemApi
public class SwitchCallbackHost extends RemoteObject implements ISwitchCallback {
    private static final HiLogLabel LABEL = new HiLogLabel(3, LBSLog.LOCATOR_LOG_ID, "SwitchCallbackHost");
    private static final String LOCATOR_DESCRIPTOR = "location.ILocator";
    private static final int SYSTEM_UID = 1000;
    private SwitchCallback mCallback;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public SwitchCallbackHost(String str, SwitchCallback switchCallback) {
        super(str);
        this.mCallback = switchCallback;
    }

    public SwitchCallbackHost(SwitchCallback switchCallback) {
        this(ISwitchCallback.DESCRIPTOR, switchCallback);
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
            onSwitchChange(messageParcel.readInt());
        }
        return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
    }

    @Override // ohos.location.callback.ISwitchCallback
    public void onSwitchChange(int i) throws RemoteException {
        HiLog.debug(LABEL, "onSwitchChange, switchState: %{public}d", Integer.valueOf(i));
        this.mCallback.onSwitchChange(i);
    }
}
