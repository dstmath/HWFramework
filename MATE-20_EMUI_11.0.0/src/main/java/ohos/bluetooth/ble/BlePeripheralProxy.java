package ohos.bluetooth.ble;

import java.util.Optional;
import java.util.UUID;
import ohos.bluetooth.BluetoothHostProxy;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.SequenceUuid;

public class BlePeripheralProxy {
    private static final int COMMAND_CLIENT_CONNECT = 23;
    private static final int COMMAND_CLIENT_DISCONNECT = 24;
    private static final int COMMAND_CONFIG_MTU = 39;
    private static final int COMMAND_CONNECTION_PARAM_UPDATE = 40;
    private static final int COMMAND_DISCOVER_SERVICES = 28;
    private static final int COMMAND_READ_CHARACTERISTIC = 30;
    private static final int COMMAND_READ_DESCRIPTOR = 33;
    private static final int COMMAND_READ_RSSI = 38;
    private static final int COMMAND_REGISTER_CLIENT = 21;
    private static final int COMMAND_REGISTER_FOR_NOTIFICATION = 35;
    private static final int COMMAND_UNREGISTER_CLIENT = 22;
    private static final int COMMAND_WRITE_CHARACTERISTIC = 32;
    private static final int COMMAND_WRITE_DESCRIPTOR = 34;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetoothGatt";
    private static final int NO_EXCEPTION = 0;
    private static final int PROFILE_BLE = 11;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BlePeripheralProxy");

    /* access modifiers changed from: package-private */
    public void registerClient(BlePeripheralCallbackWrapper blePeripheralCallbackWrapper) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(1);
        createDataWithToken.writeSequenceable(new SequenceUuid(UUID.randomUUID()));
        createDataWithToken.writeRemoteObject(blePeripheralCallbackWrapper.asObject());
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(21, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "registerClient got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "registerClient call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void clientConnect(int i, String str, boolean z) {
        try {
            IRemoteObject proxy = getProxy();
            MessageParcel createDataWithToken = createDataWithToken();
            createDataWithToken.writeInt(i);
            createDataWithToken.writeString(str);
            createDataWithToken.writeInt(!z ? 1 : 0);
            createDataWithToken.writeInt(2);
            createDataWithToken.writeInt(0);
            createDataWithToken.writeInt(1);
            MessageParcel obtain = MessageParcel.obtain();
            try {
                proxy.sendRequest(23, createDataWithToken, obtain, new MessageOption(0));
            } catch (RemoteException unused) {
                HiLog.error(TAG, "clientConnect call fail", new Object[0]);
            } catch (Throwable th) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                throw th;
            }
            createDataWithToken.reclaim();
            obtain.reclaim();
        } catch (RemoteException unused2) {
        }
    }

    /* access modifiers changed from: package-private */
    public void discoverServices(int i, String str) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(28, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "discoverServices got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "discoverServices call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void clientDisconnect(int i, String str) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(24, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "clientDisconnect got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "clientDisconnect call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterClient(int i) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        MessageParcel obtain = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        createDataWithToken.writeInt(i);
        try {
            proxy.sendRequest(22, createDataWithToken, obtain, messageOption);
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "unregisterClient got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "unregisterClient call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void readCharacteristic(int i, String str, int i2, int i3) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        createDataWithToken.writeInt(i2);
        createDataWithToken.writeInt(i3);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(30, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "readCharacteristic got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "readCharacteristic call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void writeCharacteristic(int i, String str, int i2, GattCharacteristic gattCharacteristic) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        int writeType = gattCharacteristic.getWriteType();
        int handle = gattCharacteristic.getHandle();
        byte[] value = gattCharacteristic.getValue();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        createDataWithToken.writeInt(handle);
        createDataWithToken.writeInt(writeType);
        createDataWithToken.writeInt(i2);
        createDataWithToken.writeByteArray(value);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(32, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "writeCharacteristic got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "writeCharacteristic call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void registerForNotification(int i, String str, int i2, boolean z) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        createDataWithToken.writeInt(i2);
        createDataWithToken.writeInt(z ? 1 : 0);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(35, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "registerForNotification got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "registerForNotification call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void readDescriptor(int i, String str, int i2, int i3) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        createDataWithToken.writeInt(i2);
        createDataWithToken.writeInt(i3);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(33, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "readDescriptor got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "readDescriptor call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void writeDescriptor(int i, String str, int i2, int i3, byte[] bArr) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        createDataWithToken.writeInt(i2);
        createDataWithToken.writeInt(i3);
        createDataWithToken.writeByteArray(bArr);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(34, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "writeDescriptor got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "writeDescriptor call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void readRemoteRssiValue(int i, String str) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(38, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "readRemoteRssiValue got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "readRemoteRssiValue call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void requestBleConnectionPriority(int i, String str, int i2) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        createDataWithToken.writeInt(i2);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(40, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "requestBleConnectionPriority got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "requestBleConnectionPriority call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void requestBleMtuSize(int i, String str, int i2) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeInt(i);
        createDataWithToken.writeString(str);
        createDataWithToken.writeInt(i2);
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(39, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "requestBleMtuSize got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "requestBleMtuSize call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    private IRemoteObject getProxy() throws RemoteException {
        Optional<IRemoteObject> saProfileProxy = BluetoothHostProxy.getInstace().getSaProfileProxy(11);
        if (saProfileProxy.isPresent()) {
            return saProfileProxy.get();
        }
        HiLog.error(TAG, "ble peripheral proxy is null", new Object[0]);
        throw new RemoteException();
    }

    private MessageParcel createDataWithToken() {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInt(1);
        obtain.writeInt(1);
        obtain.writeString(DESCRIPTOR);
        return obtain;
    }
}
