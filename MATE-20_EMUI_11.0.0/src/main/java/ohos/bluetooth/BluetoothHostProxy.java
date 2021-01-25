package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.SequenceUuid;

public class BluetoothHostProxy implements IBluetoothHost {
    private static final int DEFAULT_REMOTE_NUM = 5;
    private static final int MAX_NUM_OF_UUID = 50;
    private static final int REMOTE_OPERATION_FAILED = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "BluetoothHostProxy");
    private static final int TRANSACT_VALUE_BLUETOOTH_REMOVE_PAIR = 108;
    private static final int TRANSACT_VALUE_CANCEL_DISCOVERY = 6;
    private static final int TRANSACT_VALUE_DISABLE = 1;
    private static final int TRANSACT_VALUE_ENABLE = 0;
    private static final int TRANSACT_VALUE_GET_BLE_CAPABILITIES = 11;
    private static final int TRANSACT_VALUE_GET_BLE_MAX_ADV_DATA_LEN = 12;
    private static final int TRANSACT_VALUE_GET_BT_CONNECTION_STATE = 20;
    private static final int TRANSACT_VALUE_GET_DISCOVERY_END_MILLIS = 16;
    private static final int TRANSACT_VALUE_GET_FACTORY_RESET = 14;
    private static final int TRANSACT_VALUE_GET_LOCAL_ADDRESS = 13;
    private static final int TRANSACT_VALUE_GET_LOCAL_UUIDS = 15;
    private static final int TRANSACT_VALUE_GET_MAX_CONN_AUDIO_DEVICES = 17;
    private static final int TRANSACT_VALUE_GET_NAME = 3;
    private static final int TRANSACT_VALUE_GET_PAIRED_DEVICES = 10;
    private static final int TRANSACT_VALUE_GET_PROFILE_CONN_STATE = 9;
    private static final int TRANSACT_VALUE_GET_SA_PROFILE = 1000;
    private static final int TRANSACT_VALUE_GET_SCAN_MODE = 8;
    private static final int TRANSACT_VALUE_GET_STATE = 2;
    private static final int TRANSACT_VALUE_GET_SUPPORTED_PROFILES = 18;
    private static final int TRANSACT_VALUE_IS_DISCOVERING = 7;
    private static final int TRANSACT_VALUE_IS_HEARING_AID_SUPPORTED = 19;
    private static final int TRANSACT_VALUE_REMOTE_BONDED_FROM_LOCAL = 112;
    private static final int TRANSACT_VALUE_REMOTE_CANCEL_PAIRING = 113;
    private static final int TRANSACT_VALUE_REMOTE_GET_ALIAS = 109;
    private static final int TRANSACT_VALUE_REMOTE_GET_BATTERY = 111;
    private static final int TRANSACT_VALUE_REMOTE_GET_CLASS = 102;
    private static final int TRANSACT_VALUE_REMOTE_GET_CONNECTION_STATE = 114;
    private static final int TRANSACT_VALUE_REMOTE_GET_MESSAGE_PERMISSION = 118;
    private static final int TRANSACT_VALUE_REMOTE_GET_NAME = 100;
    private static final int TRANSACT_VALUE_REMOTE_GET_PAIR_STATE = 103;
    private static final int TRANSACT_VALUE_REMOTE_GET_PHONEBOOK_PERMISSION = 116;
    private static final int TRANSACT_VALUE_REMOTE_GET_TYPE = 101;
    private static final int TRANSACT_VALUE_REMOTE_GET_UUIDS = 115;
    private static final int TRANSACT_VALUE_REMOTE_PAIR_CONFIRM = 106;
    private static final int TRANSACT_VALUE_REMOTE_SET_ALIAS = 110;
    private static final int TRANSACT_VALUE_REMOTE_SET_MESSAGE_PERMISSION = 119;
    private static final int TRANSACT_VALUE_REMOTE_SET_PHONEBOOK_PERMISSION = 117;
    private static final int TRANSACT_VALUE_REMOTE_SET_PIN = 105;
    private static final int TRANSACT_VALUE_REMOTE_START_PAIR = 104;
    private static final int TRANSACT_VALUE_SET_NAME = 4;
    private static final int TRANSACT_VALUE_SET_SCAN_MODE = 107;
    private static final int TRANSACT_VALUE_START_DISCOVERY = 5;
    private static BluetoothHostProxy sInstance = null;
    private IRemoteObject mBluetoothService = null;
    private final Object mRemoteLock = new Object();

    private int convertProfile(int i) {
        switch (i) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 13;
            case 4:
                return 14;
            case 5:
                return 15;
            case 6:
                return 7;
            case 7:
                return 11;
            case 8:
                return 12;
            case 9:
                return 9;
            case 10:
                return 16;
            case 11:
                return 4;
            case 12:
                return 5;
            case 13:
                return 6;
            case 14:
            case 15:
            default:
                return 0;
            case 16:
                return 2;
            case 17:
                return 8;
            case 18:
                return 10;
            case 19:
                return 17;
            case 20:
                return 18;
            case 21:
                return 19;
        }
    }

    public static synchronized BluetoothHostProxy getInstace() {
        BluetoothHostProxy bluetoothHostProxy;
        synchronized (BluetoothHostProxy.class) {
            if (sInstance == null) {
                sInstance = new BluetoothHostProxy();
            }
            bluetoothHostProxy = sInstance;
        }
        return bluetoothHostProxy;
    }

    public IRemoteObject asObject() {
        synchronized (this.mRemoteLock) {
            if (this.mBluetoothService != null) {
                return this.mBluetoothService;
            }
            this.mBluetoothService = SysAbilityManager.getSysAbility(1130);
            if (this.mBluetoothService == null) {
                HiLog.error(TAG, "getSysAbility for Bluetooth host failed.", new Object[0]);
            } else {
                this.mBluetoothService.addDeathRecipient(new BluetoothHostDeathRecipient(), 0);
            }
            return this.mBluetoothService;
        }
    }

    /* access modifiers changed from: private */
    public class BluetoothHostDeathRecipient implements IRemoteObject.DeathRecipient {
        private BluetoothHostDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.warn(BluetoothHostProxy.TAG, "BluetoothHostDeathRecipient::onRemoteDied.", new Object[0]);
            BluetoothHostProxy.this.setRemoteObject(null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.mRemoteLock) {
            this.mBluetoothService = iRemoteObject;
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean enableBt(String str) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "enable : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            obtain.writeString(str);
            try {
                if (!asObject.sendRequest(0, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "enable : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "enable : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean disableBt(String str) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            boolean z = false;
            if (asObject == null) {
                HiLog.error(TAG, "disable : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            obtain.writeString(str);
            try {
                if (!asObject.sendRequest(1, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "disable : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    if (readInt == 1) {
                        z = true;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "disable : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getBtState() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getState : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(2, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getState : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getState : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IBluetoothHost
    public Optional<String> getLocalName() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getName : BluetoothService is null", new Object[0]);
                return Optional.empty();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(3, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getName : call fail", new Object[0]);
                    Optional<String> empty = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty;
                }
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt == 0) {
                    HiLog.error(TAG, "getName : got null", new Object[0]);
                    Optional<String> empty2 = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty2;
                } else {
                    Optional<String> ofNullable = Optional.ofNullable(obtain2.readString());
                    obtain.reclaim();
                    obtain2.reclaim();
                    return ofNullable;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getName : call fail", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return Optional.empty();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean setLocalName(String str) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setName : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(str);
                if (!asObject.sendRequest(4, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "setName : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "setName : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean startBtDiscovery(String str) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "startDiscovery : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            obtain.writeString(str);
            try {
                if (!asObject.sendRequest(5, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "startDiscovery : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "startDiscovery : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean cancelBtDiscovery() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            boolean z = false;
            if (asObject == null) {
                HiLog.error(TAG, "cancelDiscovery : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(6, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "cancelDiscovery : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    if (readInt == 1) {
                        z = true;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "cancelDiscovery : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean isBtDiscovering() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            boolean z = false;
            if (asObject == null) {
                HiLog.error(TAG, "isDiscovering : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(7, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "isDiscovering : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    if (readInt == 1) {
                        z = true;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "isDiscovering : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getBtScanMode() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getScanMode : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(8, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getScanMode : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getScanMode : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getProfileConnState(int i) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getProfileConnState : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeInt(i);
                if (!asObject.sendRequest(9, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getProfileConnState : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getProfileConnState : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public List<BluetoothRemoteDevice> getPairedDevices() {
        ArrayList arrayList;
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getPairedDevices : BluetoothService is null", new Object[0]);
                return new ArrayList();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(10, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getPairedDevices : call fail", new Object[0]);
                    return new ArrayList();
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    if (readInt < 0) {
                        arrayList = new ArrayList();
                    } else {
                        arrayList = new ArrayList(5);
                    }
                    for (int i = 0; i < readInt; i++) {
                        if (obtain2.getReadableBytes() <= 0) {
                            HiLog.warn(TAG, "getPairedDevices: data read failed due to data size mismatch", new Object[0]);
                            obtain.reclaim();
                            obtain2.reclaim();
                            return arrayList;
                        }
                        arrayList.add(new BluetoothRemoteDevice(obtain2.readString()));
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return arrayList;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getPairedDevices : call fail", new Object[0]);
                return new ArrayList();
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getBleCapabilities() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getBleCapabilities : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(11, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getBleCapabilities : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getBleCapabilities : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getBleMaxAdvertisingDataLength() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getBleMaxAdvertisingDataLength : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(12, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getBleMaxAdvertisingDataLength : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getBleMaxAdvertisingDataLength : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IBluetoothHost
    public Optional<String> getRemoteName(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getRemoteName : BluetoothService is null", new Object[0]);
                return Optional.empty();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(100, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getRemoteName : call fail", new Object[0]);
                    Optional<String> empty = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty;
                }
                int readInt = obtain2.readInt();
                if (readInt == 3756) {
                    throw new SecurityException("Permission denied");
                } else if (readInt == 0) {
                    HiLog.error(TAG, "getRemoteName : got null", new Object[0]);
                    Optional<String> empty2 = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty2;
                } else {
                    Optional<String> ofNullable = Optional.ofNullable(obtain2.readString());
                    obtain.reclaim();
                    obtain2.reclaim();
                    return ofNullable;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getRemoteName : call fail", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return Optional.empty();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getDeviceType(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getType : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(101, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getType : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getType : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getRemoteDeviceClass(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getRemoteDeviceClass : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(102, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getRemoteDeviceClass : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getRemoteDeviceClass : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getPairState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getPairState : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(103, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getPairState : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getPairState : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean startPair(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "startPair : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(104, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "startPair : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "startPair : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean setDevicePin(BluetoothRemoteDevice bluetoothRemoteDevice, byte[] bArr) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setPin : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                obtain.writeByteArray(bArr);
                asObject.sendRequest(105, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "setPin : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean setDevicePairingConfirmation(BluetoothRemoteDevice bluetoothRemoteDevice, boolean z) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setPairingConfirmation : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                boolean z2 = true;
                obtain.writeInt(z ? 1 : 0);
                asObject.sendRequest(106, obtain, obtain2, new MessageOption(0));
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    if (readInt != 1) {
                        z2 = false;
                    }
                    return z2;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "setPairingConfirmation : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean removePair(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "removePair : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(108, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "removePair : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "removePair : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IBluetoothHost
    public Optional<IRemoteObject> getSaProfileProxy(int i) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getSaProfileProxy : BluetoothService is null", new Object[0]);
                return Optional.empty();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeInt(i);
                if (!asObject.sendRequest(1000, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getSaProfileProxy : call fail", new Object[0]);
                    Optional<IRemoteObject> empty = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty;
                }
                Optional<IRemoteObject> ofNullable = Optional.ofNullable(obtain2.readRemoteObject());
                obtain.reclaim();
                obtain2.reclaim();
                return ofNullable;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getSaProfileProxy : call fail", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return Optional.empty();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean setBtScanMode(int i, int i2) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setScanMode : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            obtain.writeInt(i);
            obtain.writeInt(i2);
            try {
                if (!asObject.sendRequest(107, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "setScanMode : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "setScanMode : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IBluetoothHost
    public Optional<String> getLocalAddress() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getLocalAddress : BluetoothService is null", new Object[0]);
                return Optional.empty();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(13, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getLocalAddress : call fail", new Object[0]);
                    Optional<String> empty = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty;
                } else if (obtain2.readInt() == 3756) {
                    HiLog.error(TAG, "getLocalAddress : permission deny", new Object[0]);
                    Optional<String> empty2 = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty2;
                } else {
                    Optional<String> ofNullable = Optional.ofNullable(obtain2.readString());
                    obtain.reclaim();
                    obtain2.reclaim();
                    return ofNullable;
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getLocalAddress : call fail", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return Optional.empty();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean bluetoothFactoryReset() {
        boolean z = false;
        if (getBtState() != 2) {
            return false;
        }
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "bluetoothFactoryReset : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(14, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "bluetoothFactoryReset : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    if (readInt == 1) {
                        z = true;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "bluetoothFactoryReset : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IBluetoothHost
    public SequenceUuid[] getlocalSupportedUuids() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getlocalSupportedUuids : BluetoothService is null", new Object[0]);
                return new SequenceUuid[0];
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(15, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getlocalSupportedUuids : call fail", new Object[0]);
                    SequenceUuid[] sequenceUuidArr = new SequenceUuid[0];
                    obtain.reclaim();
                    obtain2.reclaim();
                    return sequenceUuidArr;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    SequenceUuid[] createUuidArray = createUuidArray(obtain2, readInt);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return createUuidArray;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getlocalSupportedUuids : call fail", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return new SequenceUuid[0];
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public long getBtDiscoveryEndMillis() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getBtDiscoveryEndMillis : BluetoothService is null", new Object[0]);
                return -1;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(16, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getBtDiscoveryEndMillis : call fail", new Object[0]);
                    return -1;
                } else if (obtain2.readInt() != 3756) {
                    long readLong = obtain2.readLong();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readLong;
                } else {
                    throw new SecurityException("Permission denied");
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getBtDiscoveryEndMillis : call fail", new Object[0]);
                return -1;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getMaxNumConnectedAudioDevices() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getMaxNumConnectedAudioDevices : BluetoothService is null", new Object[0]);
                return 1;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(17, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getMaxNumConnectedAudioDevices : call fail", new Object[0]);
                    return 1;
                }
                int readInt = obtain2.readInt();
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getMaxNumConnectedAudioDevices : call fail", new Object[0]);
                return 1;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IBluetoothHost
    public List<Integer> getProfileList() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getProfileList : BluetoothService is null", new Object[0]);
                return new ArrayList();
            }
            int btState = getBtState();
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                ArrayList arrayList = new ArrayList();
                if (btState == 2) {
                    if (!asObject.sendRequest(18, obtain, obtain2, messageOption)) {
                        HiLog.error(TAG, "getProfileList : call fail", new Object[0]);
                        ArrayList arrayList2 = new ArrayList();
                        obtain.reclaim();
                        obtain2.reclaim();
                        return arrayList2;
                    }
                    long readLong = obtain2.readLong();
                    for (int i = 0; i <= 19; i++) {
                        if ((((long) (1 << i)) & readLong) != 0) {
                            arrayList.add(Integer.valueOf(convertProfile(i)));
                        }
                    }
                } else if (!asObject.sendRequest(19, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "getProfileList : call fail", new Object[0]);
                    ArrayList arrayList3 = new ArrayList();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return arrayList3;
                } else {
                    obtain2.readInt();
                    if (obtain2.readInt() == 1) {
                        arrayList.add(19);
                    }
                }
                obtain.reclaim();
                obtain2.reclaim();
                return arrayList;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getSupportedProfilesList : call fail", new Object[0]);
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

    @Override // ohos.bluetooth.IBluetoothHost
    public int getBtConnectionState() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getBtConnectionState : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(20, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getBtConnectionState : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getBtConnectionState : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IBluetoothHost
    public Optional<String> getRemoteAlias(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getRemoteAlias : BluetoothService is null", new Object[0]);
                return Optional.empty();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(109, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "getRemoteAlias : call fail", new Object[0]);
                    Optional<String> empty = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty;
                }
                Optional<String> ofNullable = Optional.ofNullable(obtain2.readString());
                obtain.reclaim();
                obtain2.reclaim();
                return ofNullable;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getRemoteAlias : call fail", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return Optional.empty();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean setRemoteAlias(BluetoothRemoteDevice bluetoothRemoteDevice, String str) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setRemoteAlias : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                obtain.writeString(str);
                if (!asObject.sendRequest(110, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "setRemoteAlias : call fail", new Object[0]);
                    return false;
                }
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "setRemoteAlias : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getDeviceBatteryLevel(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getDeviceBatteryLevel : BluetoothService is null", new Object[0]);
                return -1;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(111, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "getDeviceBatteryLevel : call fail", new Object[0]);
                    return -1;
                }
                int readInt = obtain2.readInt();
                obtain.reclaim();
                obtain2.reclaim();
                return readInt;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getDeviceBatteryLevel : call fail", new Object[0]);
                return -1;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean isBondedFromLocal(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "isBondedFromLocal : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(112, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "isBondedFromLocal : call fail", new Object[0]);
                    return false;
                }
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            } catch (RemoteException unused) {
                HiLog.error(TAG, "isBondedFromLocal : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean cancelPairing(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "cancelPairing : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(108, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "cancelPairing : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "cancelPairing : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean isAclConnected(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            boolean z = false;
            if (asObject == null) {
                HiLog.error(TAG, "isAclConnected : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(114, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "isAclConnected : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    if (readInt > 0) {
                        z = true;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "isAclConnected : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean isAclEncrypted(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "isAclEncrypted : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(114, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "isAclEncrypted : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt < 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "isAclEncrypted : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IBluetoothHost
    public SequenceUuid[] getDeviceUuids(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getDeviceUuids : BluetoothService is null", new Object[0]);
                return new SequenceUuid[0];
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(115, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "getDeviceUuids : call fail", new Object[0]);
                    SequenceUuid[] sequenceUuidArr = new SequenceUuid[0];
                    obtain.reclaim();
                    obtain2.reclaim();
                    return sequenceUuidArr;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    SequenceUuid[] createUuidArray = createUuidArray(obtain2, readInt);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return createUuidArray;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getDeviceUuids : call fail", new Object[0]);
                obtain.reclaim();
                obtain2.reclaim();
                return new SequenceUuid[0];
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    private SequenceUuid[] createUuidArray(MessageParcel messageParcel, int i) {
        if (i < 0 || i > 50) {
            HiLog.error(TAG, "createUuidArray : wrong uuid result, a empty list will be returned", new Object[0]);
            return new SequenceUuid[0];
        }
        SequenceUuid[] sequenceUuidArr = new SequenceUuid[i];
        for (int i2 = 0; i2 < i; i2++) {
            if (messageParcel.getReadableBytes() <= 0) {
                HiLog.error(TAG, "createUuidArray : wrong uuid result, a empty list will be returned", new Object[0]);
            }
            SequenceUuid sequenceUuid = new SequenceUuid();
            messageParcel.readSequenceable(sequenceUuid);
            sequenceUuidArr[i2] = sequenceUuid;
        }
        return sequenceUuidArr;
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getPhonebookPermission(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getPhonebookPermission : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(116, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "getPhonebookPermission : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getPhonebookPermission : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean setPhonebookPermission(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setPhonebookPermission : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                obtain.writeInt(i);
                if (!asObject.sendRequest(117, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "setPhonebookPermission : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "setPhonebookPermission : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public int getMessagePermission(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getMessagePermission : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(118, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "getMessagePermission : call fail", new Object[0]);
                    return 0;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getMessagePermission : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IBluetoothHost
    public boolean setMessagePermission(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setMessagePermission : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                obtain.writeInt(i);
                if (!asObject.sendRequest(119, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "setMessagePermission : call fail", new Object[0]);
                    return false;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    boolean z = true;
                    if (readInt != 1) {
                        z = false;
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "setMessagePermission : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }
}
