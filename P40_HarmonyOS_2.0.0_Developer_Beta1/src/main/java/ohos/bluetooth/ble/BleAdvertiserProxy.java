package ohos.bluetooth.ble;

import java.util.Optional;
import ohos.bluetooth.BluetoothHostProxy;
import ohos.bluetooth.LogHelper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

class BleAdvertiserProxy {
    private static final int COMMAND_START_ADV = 9;
    private static final int COMMAND_STOP_ADV = 10;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetoothGatt";
    private static final int MIN_TRANSACTION_ID = 1;
    private static final int NO_EXCEPTION = 0;
    private static final int PROFILE_BLE = 11;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BleAdvertiserProxy");
    private BleAdvertiseCallback mBleAdvertiserCallback;
    private BleAdvertiseCallbackWrapper mCallbackWrapper;

    BleAdvertiserProxy(BleAdvertiseCallback bleAdvertiseCallback) {
        this.mBleAdvertiserCallback = bleAdvertiseCallback;
        this.mCallbackWrapper = new BleAdvertiseCallbackWrapper(bleAdvertiseCallback, "ohos.bluetooth.ble.IBleAdvertiseCallback");
    }

    private IRemoteObject getProxy() throws RemoteException {
        Optional<IRemoteObject> saProfileProxy = BluetoothHostProxy.getInstace().getSaProfileProxy(11);
        if (saProfileProxy.isPresent()) {
            return saProfileProxy.get();
        }
        HiLog.error(TAG, "ble advertise proxy is null", new Object[0]);
        throw new RemoteException();
    }

    private MessageParcel createDataWithToken() {
        MessageParcel obtain = MessageParcel.obtain();
        obtain.writeInt(1);
        obtain.writeInt(1);
        obtain.writeString(DESCRIPTOR);
        return obtain;
    }

    public void startAdvertising(BleAdvertiseSettings bleAdvertiseSettings, BleAdvertiseData bleAdvertiseData, BleAdvertiseData bleAdvertiseData2) throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        if (bleAdvertiseSettings == null) {
            createDataWithToken.writeInt(0);
        } else {
            createDataWithToken.writeInt(1);
            bleAdvertiseSettings.marshalling(createDataWithToken);
        }
        if (bleAdvertiseData == null) {
            createDataWithToken.writeInt(0);
        } else {
            createDataWithToken.writeInt(1);
            bleAdvertiseData.marshalling(createDataWithToken);
        }
        if (bleAdvertiseData2 == null) {
            createDataWithToken.writeInt(0);
        } else {
            createDataWithToken.writeInt(1);
            bleAdvertiseData2.marshalling(createDataWithToken);
        }
        createDataWithToken.writeInt(0);
        createDataWithToken.writeInt(0);
        createDataWithToken.writeInt(0);
        createDataWithToken.writeInt(0);
        createDataWithToken.writeRemoteObject(this.mCallbackWrapper.asObject());
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(9, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "startAdvertising got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "startAdvertising call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }

    public void stopAdvertising() throws RemoteException {
        IRemoteObject proxy = getProxy();
        MessageParcel createDataWithToken = createDataWithToken();
        createDataWithToken.writeRemoteObject(this.mCallbackWrapper.asObject());
        MessageParcel obtain = MessageParcel.obtain();
        try {
            proxy.sendRequest(10, createDataWithToken, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                createDataWithToken.reclaim();
                obtain.reclaim();
                return;
            }
            HiLog.error(TAG, "stopAdvertising got error", new Object[0]);
            throw new RemoteException();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "stopAdvertising call fail", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            createDataWithToken.reclaim();
            obtain.reclaim();
            throw th;
        }
    }
}
