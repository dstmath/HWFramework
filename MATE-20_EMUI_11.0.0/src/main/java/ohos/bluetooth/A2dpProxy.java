package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

class A2dpProxy implements IA2dp {
    private static final int DEFAULT_A2DP_DEVICE_NUM = 5;
    private static final int STRATEGY_ALLOW = 100;
    private static final int STRATEGY_AUTO = 1000;
    private static final int STRATEGY_DISALLOW = 0;
    private static final int STRATEGY_UNKNOWN = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "A2dpProxy");
    private static final int TRANSACT_VALUE_A2DP_SINK_CONNECT_SOURCE_REMOTE = 253;
    private static final int TRANSACT_VALUE_A2DP_SINK_DISCONNECT_SOURCE_REMOTE = 254;
    private static final int TRANSACT_VALUE_A2DP_SINK_GET_CONNECT_STRATEGY = 256;
    private static final int TRANSACT_VALUE_A2DP_SINK_GET_DEVICES_BY_STATE = 250;
    private static final int TRANSACT_VALUE_A2DP_SINK_GET_DEVICE_STATE = 251;
    private static final int TRANSACT_VALUE_A2DP_SINK_GET_PLAYING_STATE = 252;
    private static final int TRANSACT_VALUE_A2DP_SINK_SET_CONNECT_STRATEGY = 255;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_CONNECT_SINK_REMOTE = 203;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_DISCONNECT_SINK_REMOTE = 204;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_ENABLE_DISABLE_OPTIONAL_CODECS = 211;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_GET_ACTIVE_DEVICE = 206;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_GET_CODEC_STATUS = 209;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_GET_CONNECT_STRATEGY = 208;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_GET_DEVICES_BY_STATE = 200;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_GET_DEVICE_STATE = 201;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_GET_OPTIONAL_CODECS_OPTION = 213;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_GET_PLAYING_STATE = 202;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_IS_OPTIONAL_CODECS_SUPPORTED = 212;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_SET_ACTIVE_DEVICE = 205;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_SET_CODEC_PRE = 210;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_SET_CONNECT_STRATEGY = 207;
    private static final int TRANSACT_VALUE_A2DP_SOURCE_SET_OPTIONAL_CODECS_OPTION = 214;
    private static A2dpProxy sInstance = null;
    private IRemoteObject mBluetoothService = null;
    private final Context mContext;
    private final Object mRemoteLock = new Object();

    private int getFixStrategy(int i) {
        if (i == -1) {
            return -1;
        }
        if (i == 0) {
            return 0;
        }
        if (i != 100) {
            return i != 1000 ? -1 : 2;
        }
        return 1;
    }

    private A2dpProxy(Context context) {
        this.mContext = context;
    }

    public static synchronized A2dpProxy getInstace(Context context) {
        A2dpProxy a2dpProxy;
        synchronized (A2dpProxy.class) {
            if (sInstance == null) {
                sInstance = new A2dpProxy(context);
            }
            a2dpProxy = sInstance;
        }
        return a2dpProxy;
    }

    public IRemoteObject asObject() {
        synchronized (this.mRemoteLock) {
            if (this.mBluetoothService != null) {
                return this.mBluetoothService;
            }
            this.mBluetoothService = SysAbilityManager.getSysAbility(1130);
            if (this.mBluetoothService == null) {
                HiLog.error(TAG, "getSysAbility for A2dp failed.", new Object[0]);
            } else {
                this.mBluetoothService.addDeathRecipient(new A2dpDeathRecipient(), 0);
            }
            return this.mBluetoothService;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.mRemoteLock) {
            this.mBluetoothService = iRemoteObject;
        }
    }

    /* access modifiers changed from: private */
    public class A2dpDeathRecipient implements IRemoteObject.DeathRecipient {
        private A2dpDeathRecipient() {
        }

        public void onRemoteDied() {
            HiLog.warn(A2dpProxy.TAG, "A2dpDeathRecipient::onRemoteDied.", new Object[0]);
            A2dpProxy.this.setRemoteObject(null);
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IA2dp
    public List<BluetoothRemoteDevice> getDevicesByStatesForSource(int[] iArr) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getDevicesByStatesForSource : BluetoothService is null", new Object[0]);
                return new ArrayList();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeIntArray(iArr);
                if (!asObject.sendRequest(200, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getDevicesByStatesForSource : call fail", new Object[0]);
                    ArrayList arrayList = new ArrayList();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return arrayList;
                } else if (obtain2.readInt() != 3756) {
                    ArrayList<BluetoothRemoteDevice> createDeviceList = Utils.createDeviceList(obtain2, 5);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return createDeviceList;
                } else {
                    throw new SecurityException("Permission denied");
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getDevicesByStatesForSource : call fail", new Object[0]);
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

    @Override // ohos.bluetooth.IA2dp
    public int getDeviceStateForSource(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getDeviceStateForSource : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(201, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getDeviceStateForSource : call fail", new Object[0]);
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
                HiLog.error(TAG, "getDeviceStateForSource : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public int getPlayingStateForSource(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getPlayingStateForSource : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(202, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getPlayingStateForSource : call fail", new Object[0]);
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
                HiLog.error(TAG, "getPlayingStateForSource : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IA2dp
    public List<BluetoothRemoteDevice> getDevicesByStatesForSink(int[] iArr) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getDevicesByStatesForSink : BluetoothService is null", new Object[0]);
                return new ArrayList();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeIntArray(iArr);
                if (!asObject.sendRequest(250, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getDevicesByStatesForSink : call fail", new Object[0]);
                    ArrayList arrayList = new ArrayList();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return arrayList;
                } else if (obtain2.readInt() != 3756) {
                    ArrayList<BluetoothRemoteDevice> createDeviceList = Utils.createDeviceList(obtain2, 5);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return createDeviceList;
                } else {
                    throw new SecurityException("Permission denied");
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getDevicesByStatesForSink : call fail", new Object[0]);
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

    @Override // ohos.bluetooth.IA2dp
    public int getDeviceStateForSink(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getDeviceStateForSink : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(251, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getDeviceStateForSink : call fail", new Object[0]);
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
                HiLog.error(TAG, "getDeviceStateForSink : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public int getPlayingStateForSink(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getPlayingStateForSink : BluetoothService is null", new Object[0]);
                return 0;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(252, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getPlayingStateForSink : call fail", new Object[0]);
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
                HiLog.error(TAG, "getPlayingStateForSink : call fail", new Object[0]);
                return 0;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public boolean connectSourceDevice(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "connectSourceDevice : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(253, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "connectSourceDevice : call fail", new Object[0]);
                    return false;
                } else if (obtain2.readInt() != 3756) {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                } else {
                    throw new SecurityException("Permission denied");
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "connectSourceDevice : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public boolean disconnectSourceDevice(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "disconnectSourceDevice : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(254, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "disconnectSourceDevice : call fail", new Object[0]);
                    return false;
                } else if (obtain2.readInt() != 3756) {
                    boolean readBoolean = obtain2.readBoolean();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readBoolean;
                } else {
                    throw new SecurityException("Permission denied");
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "disconnectSourceDevice : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public boolean connectSinkDevice(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "connectSinkDevice : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(203, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "connectSinkDevice : call fail", new Object[0]);
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
                HiLog.error(TAG, "connectSinkDevice : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public boolean disconnectSinkDevice(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "disconnectSinkDevice : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(204, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "disconnectSinkDevice : call fail", new Object[0]);
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
                HiLog.error(TAG, "disconnectSinkDevice : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public boolean setActiveDeviceForSource(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setActiveDeviceForSource : BluetoothService is null", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(205, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "setActiveDeviceForSource : call fail", new Object[0]);
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
                HiLog.error(TAG, "setActiveDeviceForSource : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IA2dp
    public Optional<BluetoothRemoteDevice> getActiveDeviceForSource() {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getActiveDeviceForSource : BluetoothService is null", new Object[0]);
                return Optional.empty();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                if (!asObject.sendRequest(206, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getActiveDeviceForSource : call fail", new Object[0]);
                    Optional<BluetoothRemoteDevice> empty = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty;
                } else if (obtain2.readInt() != 3756) {
                    BluetoothRemoteDevice bluetoothRemoteDevice = new BluetoothRemoteDevice();
                    if (!obtain2.readSequenceable(bluetoothRemoteDevice)) {
                        Optional<BluetoothRemoteDevice> empty2 = Optional.empty();
                        obtain.reclaim();
                        obtain2.reclaim();
                        return empty2;
                    }
                    Optional<BluetoothRemoteDevice> ofNullable = Optional.ofNullable(bluetoothRemoteDevice);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return ofNullable;
                } else {
                    throw new SecurityException("Permission denied");
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getActiveDeviceForSource : call fail", new Object[0]);
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

    @Override // ohos.bluetooth.IA2dp
    public boolean setConnectStrategyForSource(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        int i2;
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            boolean z = false;
            if (asObject == null) {
                HiLog.error(TAG, "setConnectStrategyForSource : BluetoothService is null", new Object[0]);
                return false;
            }
            if (i == 1) {
                i2 = 100;
            } else if (i == 0) {
                i2 = 0;
            } else {
                HiLog.error(TAG, "setConnectStrategyForSource : got illegal strategy value", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                obtain.writeInt(i2);
                if (!asObject.sendRequest(207, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "setConnectStrategyForSource : call fail", new Object[0]);
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
                HiLog.error(TAG, "setConnectStrategyForSource : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public int getConnectStrategyForSource(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getConnectStrategyForSource : BluetoothService is null", new Object[0]);
                return -1;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(208, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "getConnectStrategyForSource : call fail", new Object[0]);
                    return -1;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    int fixStrategy = getFixStrategy(readInt);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return fixStrategy;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getConnectStrategyForSource : call error", new Object[0]);
                return -1;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.bluetooth.IA2dp
    public Optional<A2dpCodecStatus> getCodecStatusForSource(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getCodecStatusForSource : BluetoothService is null", new Object[0]);
                return Optional.empty();
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(209, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "getCodecStatusForSource : call fail", new Object[0]);
                    Optional<A2dpCodecStatus> empty = Optional.empty();
                    obtain.reclaim();
                    obtain2.reclaim();
                    return empty;
                } else if (obtain2.readInt() != 3756) {
                    A2dpCodecStatus a2dpCodecStatus = new A2dpCodecStatus();
                    if (!obtain2.readSequenceable(a2dpCodecStatus)) {
                        Optional<A2dpCodecStatus> empty2 = Optional.empty();
                        obtain.reclaim();
                        obtain2.reclaim();
                        return empty2;
                    }
                    Optional<A2dpCodecStatus> ofNullable = Optional.ofNullable(a2dpCodecStatus);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return ofNullable;
                } else {
                    throw new SecurityException("Permission denied");
                }
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getCodecStatusForSource : call fail", new Object[0]);
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

    @Override // ohos.bluetooth.IA2dp
    public void setCodecPreferenceForSource(BluetoothRemoteDevice bluetoothRemoteDevice, A2dpCodecInfo a2dpCodecInfo) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setCodecPreferenceForSource : BluetoothService is null", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                obtain.writeSequenceable(a2dpCodecInfo);
                if (!asObject.sendRequest(210, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "setCodecPreferenceForSource : call fail", new Object[0]);
                }
                if (obtain2.readInt() != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "setCodecPreferenceForSource : call fail", new Object[0]);
                obtain.reclaim();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public void switchOptionalCodecsForSource(BluetoothRemoteDevice bluetoothRemoteDevice, boolean z) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "switchOptionalCodecsForSource : BluetoothService is null", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                obtain.writeBoolean(z);
                if (!asObject.sendRequest(211, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "switchOptionalCodecsForSource : call fail", new Object[0]);
                }
                if (obtain2.readInt() != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "switchOptionalCodecsForSource : call fail", new Object[0]);
                obtain.reclaim();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public int getOptionalCodecsSupportStateForSource(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getOptionalCodecsSupportStateForSource : BluetoothService is null", new Object[0]);
                return -1;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(212, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getOptionalCodecsSupportStateForSource : call fail", new Object[0]);
                    return -1;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getOptionalCodecsSupportStateForSource : call fail", new Object[0]);
                return -1;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public int getOptionalCodecsOptionForSource(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getOptionalCodecsOptionForSource : BluetoothService is null", new Object[0]);
                return -1;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(213, obtain, obtain2, new MessageOption(0))) {
                    HiLog.error(TAG, "getOptionalCodecsOptionForSource : call fail", new Object[0]);
                    return -1;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return readInt;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getOptionalCodecsOptionForSource : call fail", new Object[0]);
                return -1;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public void setOptionalCodecsOptionForSource(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "setOptionalCodecsOptionForSource : BluetoothService is null", new Object[0]);
                return;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                obtain.writeInt(i);
                if (!asObject.sendRequest(214, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "setOptionalCodecsOptionForSource : call fail", new Object[0]);
                }
                if (obtain2.readInt() != 3756) {
                    obtain.reclaim();
                    obtain2.reclaim();
                    return;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "setOptionalCodecsOptionForSource : call fail", new Object[0]);
                obtain.reclaim();
            } catch (Throwable th) {
                obtain.reclaim();
                obtain2.reclaim();
                throw th;
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public boolean setConnectStrategyForSink(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        int i2;
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            boolean z = false;
            if (asObject == null) {
                HiLog.error(TAG, "setConnectStrategyForSink : BluetoothService is null", new Object[0]);
                return false;
            }
            if (i == 1) {
                i2 = 100;
            } else if (i == 0) {
                i2 = 0;
            } else {
                HiLog.error(TAG, "setConnectStrategyForSink : got illegal strategy value", new Object[0]);
                return false;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                obtain.writeInt(i2);
                if (!asObject.sendRequest(255, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "setConnectStrategyForSink : call fail", new Object[0]);
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
                HiLog.error(TAG, "setConnectStrategyForSink : call fail", new Object[0]);
                return false;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }

    @Override // ohos.bluetooth.IA2dp
    public int getConnectStrategyForSink(BluetoothRemoteDevice bluetoothRemoteDevice) {
        synchronized (this.mRemoteLock) {
            IRemoteObject asObject = asObject();
            if (asObject == null) {
                HiLog.error(TAG, "getConnectStrategyForSink : BluetoothService is null", new Object[0]);
                return -1;
            }
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption(0);
            try {
                obtain.writeString(bluetoothRemoteDevice.getDeviceAddr());
                if (!asObject.sendRequest(256, obtain, obtain2, messageOption)) {
                    HiLog.error(TAG, "getConnectStrategyForSink : call fail", new Object[0]);
                    return -1;
                }
                int readInt = obtain2.readInt();
                if (readInt != 3756) {
                    int fixStrategy = getFixStrategy(readInt);
                    obtain.reclaim();
                    obtain2.reclaim();
                    return fixStrategy;
                }
                throw new SecurityException("Permission denied");
            } catch (RemoteException unused) {
                HiLog.error(TAG, "getConnectStrategyForSink : call error", new Object[0]);
                return -1;
            } finally {
                obtain.reclaim();
                obtain2.reclaim();
            }
        }
    }
}
