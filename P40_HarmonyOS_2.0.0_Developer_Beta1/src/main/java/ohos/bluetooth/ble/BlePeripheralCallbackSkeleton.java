package ohos.bluetooth.ble;

import java.util.ArrayList;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class BlePeripheralCallbackSkeleton extends RemoteObject implements IBlePeripheralCallback {
    private static final int COMMAND_ON_CHARACTERISTIC_READ = 6;
    private static final int COMMAND_ON_CHARACTERISTIC_WRITE = 7;
    private static final int COMMAND_ON_CLIENT_CONNECTION_STATE = 2;
    private static final int COMMAND_ON_CLIENT_REGISTERED = 1;
    private static final int COMMAND_ON_CONFIGURE_M_T_U = 13;
    private static final int COMMAND_ON_CONNECTION_UPDATED = 14;
    private static final int COMMAND_ON_DESCRIPTOR_READ = 9;
    private static final int COMMAND_ON_DESCRIPTOR_WRITE = 10;
    private static final int COMMAND_ON_EXECUTE_WRITE = 8;
    private static final int COMMAND_ON_NOTIFY = 11;
    private static final int COMMAND_ON_PHY_READ = 4;
    private static final int COMMAND_ON_PHY_UPDATE = 3;
    private static final int COMMAND_ON_READ_REMOTE_RSSI = 12;
    private static final int COMMAND_ON_SEARCH_COMPLETE = 5;
    private static final int DEFAULT_SERVICES_NUM = 3;
    private static final String DESCRIPTOR = "ohos.bluetooth.IBluetoothGattCallback";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;
    private static final int MIN_TRANSACTION_ID = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BlePeripheralCallbackSkeleton");

    public BlePeripheralCallbackSkeleton(String str) {
        super(str);
    }

    public IRemoteObject asObject() {
        HiLog.info(TAG, "BlePeripheralCallbackSkeleton asObject", new Object[0]);
        return this;
    }

    private void enforceInterface(MessageParcel messageParcel) {
        messageParcel.readInt();
        messageParcel.readInt();
        messageParcel.readString();
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        switch (i) {
            case 1:
                enforceInterface(messageParcel);
                onClientRegistered(messageParcel.readInt(), messageParcel.readInt());
                messageParcel2.writeInt(0);
                return true;
            case 2:
                enforceInterface(messageParcel);
                onClientConnectionState(messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt() == 1, messageParcel.readString());
                messageParcel2.writeInt(0);
                return true;
            case 3:
            case 4:
            case 8:
            default:
                HiLog.warn(TAG, "call back is not implemented on code: %{public}d", new Object[]{Integer.valueOf(i)});
                return BlePeripheralCallbackSkeleton.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            case 5:
                enforceInterface(messageParcel);
                String readString = messageParcel.readString();
                int readInt = messageParcel.readInt();
                if (readInt < 0) {
                    return false;
                }
                ArrayList arrayList = new ArrayList(3);
                for (int i2 = 0; i2 < readInt; i2++) {
                    if (messageParcel.getReadableBytes() <= 0) {
                        HiLog.warn(TAG, "onRemoteRequest: data read failed due to data size mismatch", new Object[0]);
                        return false;
                    }
                    GattService gattService = new GattService(null, false);
                    messageParcel.readInt();
                    gattService.unmarshallingSpecially(messageParcel);
                    arrayList.add(gattService);
                }
                onSearchComplete(readString, arrayList, messageParcel.readInt());
                messageParcel2.writeInt(0);
                return true;
            case 6:
                enforceInterface(messageParcel);
                onCharacteristicRead(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readByteArray());
                messageParcel2.writeInt(0);
                return true;
            case 7:
                enforceInterface(messageParcel);
                onCharacteristicWrite(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt());
                messageParcel2.writeInt(0);
                return true;
            case 9:
                enforceInterface(messageParcel);
                onDescriptorRead(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readByteArray());
                messageParcel2.writeInt(0);
                return true;
            case 10:
                enforceInterface(messageParcel);
                onDescriptorWrite(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt());
                messageParcel2.writeInt(0);
                return true;
            case 11:
                enforceInterface(messageParcel);
                onNotify(messageParcel.readString(), messageParcel.readInt(), messageParcel.readByteArray());
                messageParcel2.writeInt(0);
                return true;
            case 12:
                enforceInterface(messageParcel);
                onReadRemoteRssi(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt());
                messageParcel2.writeInt(0);
                return true;
            case 13:
                enforceInterface(messageParcel);
                onConfigureMTU(messageParcel.readString(), messageParcel.readInt(), messageParcel.readInt());
                messageParcel2.writeInt(0);
                return true;
        }
    }
}
