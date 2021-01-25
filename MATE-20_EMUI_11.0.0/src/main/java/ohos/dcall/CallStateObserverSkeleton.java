package ohos.dcall;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class CallStateObserverSkeleton extends RemoteObject implements ICallStateObserver {
    private static final String DESCRIPTOR = "ohos.telephony.ICallStateObserver";
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) DistributedCallUtils.LOG_ID_DCALL, "CallStateObserverSkeleton");
    private static final int TRANSACTION_ON_CALL_FORWARDING_INDICATOR_CHANGED = 4;
    private static final int TRANSACTION_ON_CALL_STATE_CHANGED = 6;
    private static final int TRANSACTION_ON_MESSAGE_WAITING_INDICATOR_CHANGED = 3;

    public IRemoteObject asObject() {
        return this;
    }

    public CallStateObserverSkeleton() {
        super(DESCRIPTOR);
    }

    private void enforceInterface(MessageParcel messageParcel) {
        messageParcel.readInterfaceToken();
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        boolean z = false;
        if (i == 3) {
            enforceInterface(messageParcel);
            if (messageParcel.readInt() != 0) {
                z = true;
            }
            onVoiceMailMsgIndicatorUpdated(z);
            return true;
        } else if (i == 4) {
            enforceInterface(messageParcel);
            if (messageParcel.readInt() != 0) {
                z = true;
            }
            onCfuIndicatorUpdated(z);
            return true;
        } else if (i != 6) {
            HiLog.warn(TAG, "call back is not implemented on code: %{public}d", new Object[]{Integer.valueOf(i)});
            return CallStateObserverSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            enforceInterface(messageParcel);
            onCallStateUpdated(messageParcel.readInt(), messageParcel.readString());
            return true;
        }
    }
}
