package ohos.bluetooth.ble;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import ohos.bluetooth.BluetoothHostProxy;
import ohos.bluetooth.BluetoothRemoteDevice;
import ohos.bluetooth.LogHelper;
import ohos.bluetooth.Utils;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.SequenceUuid;

/* access modifiers changed from: package-private */
public class BlePeripheralManagerProxy {
    private static final int DEFAULT_GATT_NUM = 3;
    private static final int MIN_TRANSACTION_ID = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BlePeripheralManagerProxy");
    private static final int TRANSACT_ADD_SCANNER_ID = 2;
    private static final int TRANSACT_ADD_SERVICE = 48;
    private static final int TRANSACT_CLEAR_SERVICES = 50;
    private static final int TRANSACT_DISCONNECT_SERVER = 45;
    private static final int TRANSACT_GETCONNECTIONSTATESBYSTATES = 1;
    private static final int TRANSACT_GET_DEVICES_BY_STATE = 1;
    private static final int TRANSACT_REGISTER_SERVER = 42;
    private static final int TRANSACT_REMOVE_SCANNER_ID = 3;
    private static final int TRANSACT_REMOVE_SERVICE = 49;
    private static final int TRANSACT_SEND_NOTIFICATION = 52;
    private static final int TRANSACT_SNED_RESPONSE = 51;
    private static final int TRANSACT_STOP_SCAN = 7;
    private static final int TRANSACT_UNREGISTER_SERVER = 43;
    private Object mLock = new Object();
    private IRemoteObject remote;

    BlePeripheralManagerProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    private void writeInterfaceToken(MessageParcel messageParcel) {
        messageParcel.writeInt(1);
        messageParcel.writeInt(1);
        messageParcel.writeString("android.bluetooth.IBluetoothGatt");
    }

    /* access modifiers changed from: package-private */
    public void addService(int i, GattService gattService) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "addService : can not get gatt service", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeInt(i);
            if (gattService == null) {
                obtain.writeInt(0);
            } else {
                obtain.writeInt(1);
                gattService.marshallingSpecially(obtain);
            }
            try {
                boolean sendRequest = this.remote.sendRequest(48, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (!sendRequest || readInt != 0) {
                    HiLog.error(TAG, "addService : call fail %{public}b, %{public}d", new Object[]{Boolean.valueOf(sendRequest), Integer.valueOf(readInt)});
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "addService : remote exception occured", new Object[0]);
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectServer(int i, String str) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "disconnectServer : can not get gatt service", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeInt(i);
            obtain.writeString(str);
            obtain.writeInt(0);
            try {
                boolean sendRequest = this.remote.sendRequest(45, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (!sendRequest || readInt != 0) {
                    HiLog.error(TAG, "disconnectServer : call fail %{public}b, %{public}d", new Object[]{Boolean.valueOf(sendRequest), Integer.valueOf(readInt)});
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "disconnectServer : remote exception occured", new Object[0]);
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void registerServer(SequenceUuid sequenceUuid, BlePeripheralManagerCallbackWrapper blePeripheralManagerCallbackWrapper) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "registerServer : can not get gatt service", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            if (sequenceUuid != null) {
                obtain.writeInt(1);
                sequenceUuid.marshalling(obtain);
            } else {
                obtain.writeInt(0);
            }
            obtain.writeRemoteObject(blePeripheralManagerCallbackWrapper.asObject());
            try {
                boolean sendRequest = this.remote.sendRequest(42, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (!sendRequest || readInt != 0) {
                    HiLog.error(TAG, "registerServer : call fail %{public}d, %{public}d", new Object[]{Boolean.valueOf(sendRequest), Integer.valueOf(readInt)});
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "registerServer : remote exception occured", new Object[0]);
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterServer(int i) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "unregisterServer : can not get gatt service", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            obtain.writeInt(i);
            try {
                boolean sendRequest = this.remote.sendRequest(43, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (!sendRequest || readInt != 0) {
                    HiLog.error(TAG, "unregisterServer : call fail %{public}b, %{public}d", new Object[]{Boolean.valueOf(sendRequest), Integer.valueOf(readInt)});
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "unregisterServer : remote exception occured", new Object[0]);
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearServices(int i) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "clearServices : can not get gatt service", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeInt(i);
            try {
                boolean sendRequest = this.remote.sendRequest(50, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (!sendRequest || readInt != 0) {
                    HiLog.error(TAG, "clearServices : call fail %{public}d, %{public}d", new Object[]{Boolean.valueOf(sendRequest), Integer.valueOf(readInt)});
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "clearServices : remote exception occured", new Object[0]);
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendNotification(int i, String str, int i2, boolean z, byte[] bArr) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "sendNotification : can not get gatt service", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeInt(i);
            obtain.writeString(str);
            obtain.writeInt(i2);
            obtain.writeBoolean(z);
            obtain.writeByteArray(bArr);
            try {
                boolean sendRequest = this.remote.sendRequest(52, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (!sendRequest || readInt != 0) {
                    HiLog.error(TAG, "sendNotification : call fail %{public}b, %{public}d", new Object[]{Boolean.valueOf(sendRequest), Integer.valueOf(readInt)});
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "sendNotification : remote exception occured", new Object[0]);
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeService(int i, int i2) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "removeService : can not get gatt service", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeInt(i);
            obtain.writeInt(i2);
            try {
                boolean sendRequest = this.remote.sendRequest(49, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (!sendRequest || readInt != 0) {
                    HiLog.error(TAG, "removeService : call fail %{public}b, %{public}d", new Object[]{Boolean.valueOf(sendRequest), Integer.valueOf(readInt)});
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "removeService : remote exception occured", new Object[0]);
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendResponse(int i, String str, int i2, int i3, int i4, byte[] bArr) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "sendResponse : can not get gatt service", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeInt(i);
            obtain.writeString(str);
            obtain.writeInt(i2);
            obtain.writeInt(i3);
            obtain.writeInt(i4);
            obtain.writeByteArray(bArr);
            try {
                boolean sendRequest = this.remote.sendRequest(51, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (!sendRequest || readInt != 0) {
                    HiLog.error(TAG, "sendResponse : call fail %{public}b, %{public}d", new Object[]{Boolean.valueOf(sendRequest), Integer.valueOf(readInt)});
                    obtain.reclaim();
                    obtain2.reclaim();
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "sendResponse : remote exception occured", new Object[0]);
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public List<BlePeripheralDevice> getDevicesByStates(int[] iArr) {
        synchronized (this.mLock) {
            this.remote = getRemote().orElse(null);
            if (this.remote == null) {
                HiLog.error(TAG, "disconnectServer : can not get gatt service", new Object[0]);
                return new ArrayList();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            writeInterfaceToken(obtain);
            obtain.writeIntArray(iArr);
            try {
                boolean sendRequest = this.remote.sendRequest(1, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (sendRequest) {
                    if (readInt == 0) {
                        ArrayList<BluetoothRemoteDevice> createDeviceList = Utils.createDeviceList(obtain2, 3);
                        ArrayList arrayList = new ArrayList();
                        Iterator<BluetoothRemoteDevice> it = createDeviceList.iterator();
                        while (it.hasNext()) {
                            arrayList.add(new BlePeripheralDevice(it.next().getDeviceAddr()));
                        }
                        obtain.reclaim();
                        obtain2.reclaim();
                        return arrayList;
                    }
                }
                HiLog.error(TAG, "clearServices : call fail %{public}b, %{public}d", new Object[]{Boolean.valueOf(sendRequest), Integer.valueOf(readInt)});
                ArrayList arrayList2 = new ArrayList();
                obtain.reclaim();
                obtain2.reclaim();
                return arrayList2;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "addScanner : remote exception occured", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return new ArrayList();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    private Optional<IRemoteObject> getRemote() {
        return BluetoothHostProxy.getInstace().getSaProfileProxy(11);
    }
}
