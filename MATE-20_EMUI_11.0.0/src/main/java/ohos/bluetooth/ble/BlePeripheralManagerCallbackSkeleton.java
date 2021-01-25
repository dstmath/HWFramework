package ohos.bluetooth.ble;

import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class BlePeripheralManagerCallbackSkeleton extends RemoteObject implements IBlePeripheralManagerCallback {
    private static final int COMMAND_CHARACTERISTIC_READ_REQUEST = 4;
    private static final int COMMAND_CHARACTERISTIC_WRITE_REQUEST = 6;
    private static final int COMMAND_CONNECTION_STATEUPDATE = 13;
    private static final int COMMAND_DESCRIPTOR_READ_REQUEST = 5;
    private static final int COMMAND_DESCRIPTOR_WRITE_REQUEST = 7;
    private static final int COMMAND_EXECUTE_WRITE = 8;
    private static final int COMMAND_MTU_UPDATE = 10;
    private static final int COMMAND_NOTIFICATION_SENT = 9;
    private static final int COMMAND_SERVER_REGISTERED = 1;
    private static final int COMMAND_SERVICE_ADDED = 3;
    private static final String DESCRIPTOR = "ohos.bluetooth.IBlePeripheralManagerCallback";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BlePeripheralManagerCallbackSkeleton");

    public BlePeripheralManagerCallbackSkeleton(String str) {
        super(str);
    }

    public IRemoteObject asObject() {
        HiLog.info(TAG, "BlePeripheralManagerCallbackSkeleton asObject", new Object[0]);
        return this;
    }

    private void enforceInterface(MessageParcel messageParcel) {
        messageParcel.readInt();
        messageParcel.readInt();
        messageParcel.readString();
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i == 1) {
            enforceInterface(messageParcel);
            serverRegisteredEvent(messageParcel.readInt(), messageParcel.readInt());
            return true;
        } else if (i != 13) {
            boolean z = false;
            switch (i) {
                case 3:
                    enforceInterface(messageParcel);
                    int readInt = messageParcel.readInt();
                    GattService gattService = new GattService(null, false);
                    if (messageParcel.readInt() == 0) {
                        return false;
                    }
                    gattService.unmarshallingSpecially(messageParcel);
                    serviceAddedEvent(readInt, gattService);
                    return true;
                case 4:
                    enforceInterface(messageParcel);
                    characteristicReadRequestEvent(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt() != 0, messageParcel.readInt());
                    return true;
                case 5:
                    enforceInterface(messageParcel);
                    descriptorReadRequestEvent(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt() != 0, messageParcel.readInt());
                    return true;
                case 6:
                    characteristicWriteRequestEvent(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt() != 0, messageParcel.readInt() != 0, messageParcel.readInt(), messageParcel.readByteArray());
                    return true;
                case 7:
                    enforceInterface(messageParcel);
                    descriptorWriteRequestEvent(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt() != 0, messageParcel.readInt() != 0, messageParcel.readInt(), messageParcel.readByteArray());
                    return true;
                case 8:
                    enforceInterface(messageParcel);
                    String readString = messageParcel.readString();
                    int readInt2 = messageParcel.readInt();
                    if (messageParcel.readInt() != 0) {
                        z = true;
                    }
                    executeWriteEvent(readString, readInt2, z);
                    return true;
                case 9:
                    enforceInterface(messageParcel);
                    notificationSentEvent(messageParcel.readString(), messageParcel.readInt());
                    return true;
                case 10:
                    enforceInterface(messageParcel);
                    mtuUpdateEvent(messageParcel.readString(), messageParcel.readInt());
                    return true;
                default:
                    HiLog.warn(TAG, "call back is not implemented on code: %{public}d", new Object[]{Integer.valueOf(i)});
                    return BlePeripheralManagerCallbackSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            }
        } else {
            enforceInterface(messageParcel);
            connectionStateUpdateEvent(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt());
            return true;
        }
    }
}
