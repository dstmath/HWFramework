package ohos.bluetooth.ble;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.bluetooth.BluetoothHostProxy;
import ohos.bluetooth.LogHelper;
import ohos.bluetooth.ble.ScannerCallbackWrapper;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

/* access modifiers changed from: package-private */
public class BleCentralManagerProxy {
    private static final int MIN_TRANSACTION_ID = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BleCentralManagerProxy");
    private static final int TRANSACT_ADD_SCANNER_ID = 2;
    private static final int TRANSACT_DO_SCAN = 4;
    private static final int TRANSACT_GET_DEVICES_BY_STATE = 1;
    private static final int TRANSACT_REMOVE_SCANNER_ID = 3;
    private static final int TRANSACT_STOP_SCAN = 7;
    private Object mLock = new Object();
    private IRemoteObject remote;

    BleCentralManagerProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    private void writeInterfaceToken(MessageParcel messageParcel) {
        messageParcel.writeInt(1);
        messageParcel.writeInt(1);
        messageParcel.writeString("android.bluetooth.IBluetoothGatt");
    }

    private void writeFilters(List<BleScanFilter> list, MessageParcel messageParcel) {
        messageParcel.writeInt(list.size());
        for (BleScanFilter bleScanFilter : list) {
            if (bleScanFilter != null) {
                messageParcel.writeInt(1);
                bleScanFilter.marshalling(messageParcel);
            } else {
                messageParcel.writeInt(0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean doScan(int i, List<BleScanFilter> list, ScannerCallbackWrapper.ScanParameter scanParameter) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "doScan : can not get gatt service", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeInt(i);
            scanParameter.writeToParcel(obtain);
            if (list == null) {
                obtain.writeInt(-1);
            } else if (list.isEmpty()) {
                obtain.writeInt(0);
            } else {
                writeFilters(list, obtain);
            }
            obtain.writeInt(-1);
            obtain.writeString("ohos.bluetooth.ble");
            try {
                if (!this.remote.sendRequest(4, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "doScan : call fail", new Object[0]);
                    return false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return true;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "doScan : remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean addScanner(BleCentralManager bleCentralManager) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "addScanner : can not get gatt service", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeRemoteObject(new ScannerCallbackWrapper(bleCentralManager).asObject());
            obtain.writeInt(0);
            try {
                if (!this.remote.sendRequest(2, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "addScanner : call fail, error code", new Object[0]);
                    return false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return true;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "addScanner : remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean stopScan(int i) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "stopScan : can not get gatt service", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeInt(i);
            try {
                if (!this.remote.sendRequest(7, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "stopScan : call fail, error code", new Object[0]);
                    return false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return true;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "stopScan : remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean removeScanner(int i) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "removeScanner : can not get gatt service", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeInt(i);
            try {
                if (!this.remote.sendRequest(3, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "removeScanner : call fail", new Object[0]);
                    return false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return true;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "removeScanner : remote exception occured", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<BlePeripheralDevice> getPeripheralDevicesByStates(int[] iArr) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "getPeripheralDevicesByStates: can not get gatt service", new Object[0]);
                return new ArrayList();
            }
            ArrayList arrayList = new ArrayList();
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeIntArray(iArr);
            try {
                if (!this.remote.sendRequest(1, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getPeripheralDevicesByStates: call fail", new Object[0]);
                    return arrayList;
                }
                List<BlePeripheralDevice> readDeviceList = readDeviceList(obtain2);
                obtain.reclaim();
                obtain2.reclaim();
                return readDeviceList;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getPeripheralDevicesByStates: remote exception occured", new Object[0]);
                return arrayList;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    private List<BlePeripheralDevice> readDeviceList(MessageParcel messageParcel) {
        if (messageParcel.readInt() != 0) {
            HiLog.error(TAG, "readDeviceList got error", new Object[0]);
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList();
        int readInt = messageParcel.readInt();
        for (int i = 0; i < readInt; i++) {
            if (messageParcel.getReadableBytes() <= 0) {
                HiLog.warn(TAG, "readDeviceList: data read failed due to data size mismatch", new Object[0]);
                return arrayList;
            }
            messageParcel.readInt();
            arrayList.add(new BlePeripheralDevice(messageParcel.readString()));
        }
        return arrayList;
    }

    private Optional<IRemoteObject> getRemote() {
        return BluetoothHostProxy.getInstace().getSaProfileProxy(11);
    }
}
