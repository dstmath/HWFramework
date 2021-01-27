package ohos.telephony;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class CellularDataStateObserverSkeleton extends RemoteObject implements ICellularDataStateObserver {
    private static final String DESCRIPTOR = "ohos.telephony.ICellularDataStateObserver";
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "CellularDataStateObserverSkeleton");
    private static final int TRANSACTION_ID_BASE = 1;
    private static final int TRANSACTION_ON_CELLULAR_DATA_CONNECT_STATE_UPDATED = 7;
    private static final int TRANSACTION_ON_CELLULAR_DATA_FLOW = 8;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public CellularDataStateObserverSkeleton() {
        super(DESCRIPTOR);
    }

    private boolean enforceInterface(MessageParcel messageParcel) {
        return TelephonyUtils.SRC_DESCRIPTOR.equals(messageParcel.readInterfaceToken());
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i != 7) {
            if (i != 8) {
                HiLog.warn(TAG, "call back is not implemented on code: %{public}d", Integer.valueOf(i));
                return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else if (!enforceInterface(messageParcel)) {
                return true;
            } else {
                onCellularDataFlow(messageParcel.readInt());
            }
        } else if (!enforceInterface(messageParcel)) {
            return true;
        } else {
            onCellularDataConnectStateUpdated(messageParcel.readInt(), TelephonyProxy.getInstance().getRadioTechnologyType(messageParcel.readInt()));
        }
        return true;
    }
}
