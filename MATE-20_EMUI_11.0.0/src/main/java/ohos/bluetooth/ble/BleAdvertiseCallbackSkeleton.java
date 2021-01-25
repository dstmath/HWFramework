package ohos.bluetooth.ble;

import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class BleAdvertiseCallbackSkeleton extends RemoteObject implements IBleAdvertiseCallback {
    private static final int COMMAND_ON_ADV_SET_STARTED = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BleAdvertiseCallbackSkeleton");

    public BleAdvertiseCallbackSkeleton(String str) {
        super(str);
    }

    public IRemoteObject asObject() {
        HiLog.info(TAG, "BleAdvertiseCallbackSkeleton asObject", new Object[0]);
        return this;
    }

    private void enforceInterface(MessageParcel messageParcel) {
        messageParcel.readInt();
        messageParcel.readInt();
        messageParcel.readString();
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i != 1) {
            HiLog.warn(TAG, "call back is not implemented on code: %{public}d", new Object[]{Integer.valueOf(i)});
            return BleAdvertiseCallbackSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        enforceInterface(messageParcel);
        messageParcel.readInt();
        messageParcel.readInt();
        onAdvertisingSetStarted(messageParcel.readInt());
        return true;
    }
}
