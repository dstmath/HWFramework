package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothStateChangeCallback.Stub;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanSettings.Builder;
import android.content.Context;
import android.net.ProxyInfo;
import android.os.BatteryStats;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SynchronousResultReceiver;
import android.os.SynchronousResultReceiver.Result;
import android.os.SystemProperties;
import android.speech.SpeechRecognizer;
import android.telecom.AudioState;
import android.util.Log;
import android.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class BluetoothAdapter {
    public static final String ACTION_BLE_ACL_CONNECTED = "android.bluetooth.adapter.action.BLE_ACL_CONNECTED";
    public static final String ACTION_BLE_ACL_DISCONNECTED = "android.bluetooth.adapter.action.BLE_ACL_DISCONNECTED";
    public static final String ACTION_BLE_STATE_CHANGED = "android.bluetooth.adapter.action.BLE_STATE_CHANGED";
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_DISCOVERY_FINISHED = "android.bluetooth.adapter.action.DISCOVERY_FINISHED";
    public static final String ACTION_DISCOVERY_STARTED = "android.bluetooth.adapter.action.DISCOVERY_STARTED";
    public static final String ACTION_LOCAL_NAME_CHANGED = "android.bluetooth.adapter.action.LOCAL_NAME_CHANGED";
    public static final String ACTION_RADIO_STATE_CHANGED = "android.bluetooth.adapter.action.RADIO_STATE_CHANGED";
    public static final String ACTION_REQUEST_BLE_SCAN_ALWAYS_AVAILABLE = "android.bluetooth.adapter.action.REQUEST_BLE_SCAN_ALWAYS_AVAILABLE";
    public static final String ACTION_REQUEST_DISCOVERABLE = "android.bluetooth.adapter.action.REQUEST_DISCOVERABLE";
    public static final String ACTION_REQUEST_ENABLE = "android.bluetooth.adapter.action.REQUEST_ENABLE";
    public static final String ACTION_SCAN_MODE_CHANGED = "android.bluetooth.adapter.action.SCAN_MODE_CHANGED";
    public static final String ACTION_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
    private static final int ADDRESS_LENGTH = 17;
    public static final String BLUETOOTH_MANAGER_SERVICE = "bluetooth_manager";
    private static final boolean DBG = true;
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    public static final int ERROR = Integer.MIN_VALUE;
    public static final String EXTRA_CONNECTION_STATE = "android.bluetooth.adapter.extra.CONNECTION_STATE";
    public static final String EXTRA_DISCOVERABLE_DURATION = "android.bluetooth.adapter.extra.DISCOVERABLE_DURATION";
    public static final String EXTRA_LOCAL_NAME = "android.bluetooth.adapter.extra.LOCAL_NAME";
    public static final String EXTRA_PREVIOUS_CONNECTION_STATE = "android.bluetooth.adapter.extra.PREVIOUS_CONNECTION_STATE";
    public static final String EXTRA_PREVIOUS_SCAN_MODE = "android.bluetooth.adapter.extra.PREVIOUS_SCAN_MODE";
    public static final String EXTRA_PREVIOUS_STATE = "android.bluetooth.adapter.extra.PREVIOUS_STATE";
    public static final String EXTRA_SCAN_MODE = "android.bluetooth.adapter.extra.SCAN_MODE";
    public static final String EXTRA_STATE = "android.bluetooth.adapter.extra.STATE";
    public static final int SCAN_MODE_CONNECTABLE = 21;
    public static final int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23;
    public static final int SCAN_MODE_NONE = 20;
    public static final int SOCKET_CHANNEL_AUTO_STATIC_NO_SDP = -2;
    public static final int STATE_BLE_ON = 15;
    public static final int STATE_BLE_TURNING_OFF = 16;
    public static final int STATE_BLE_TURNING_ON = 14;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_DISCONNECTING = 3;
    public static final int STATE_OFF = 10;
    public static final int STATE_ON = 12;
    public static final int STATE_RADIO_OFF = 18;
    public static final int STATE_RADIO_ON = 17;
    public static final int STATE_TURNING_OFF = 13;
    public static final int STATE_TURNING_ON = 11;
    private static final String TAG = "BluetoothAdapter";
    private static final boolean VDBG = false;
    private static BluetoothAdapter sAdapter;
    private static BluetoothLeAdvertiser sBluetoothLeAdvertiser;
    private static BluetoothLeScanner sBluetoothLeScanner;
    private final Map<LeScanCallback, ScanCallback> mLeScanClients;
    private final Object mLock;
    private final IBluetoothManagerCallback mManagerCallback;
    private final IBluetoothManager mManagerService;
    private final ArrayList<IBluetoothManagerCallback> mProxyServiceStateCallbacks;
    private IBluetooth mService;
    private final ReentrantReadWriteLock mServiceLock;
    private final IBinder mToken;

    /* renamed from: android.bluetooth.BluetoothAdapter.2 */
    class AnonymousClass2 extends ScanCallback {
        final /* synthetic */ LeScanCallback val$callback;
        final /* synthetic */ UUID[] val$serviceUuids;

        AnonymousClass2(UUID[] val$serviceUuids, LeScanCallback val$callback) {
            this.val$serviceUuids = val$serviceUuids;
            this.val$callback = val$callback;
        }

        public void onScanResult(int callbackType, ScanResult result) {
            if (callbackType != BluetoothAdapter.STATE_CONNECTING) {
                Log.e(BluetoothAdapter.TAG, "LE Scan has already started");
                return;
            }
            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord != null) {
                if (this.val$serviceUuids != null) {
                    List<ParcelUuid> uuids = new ArrayList();
                    UUID[] uuidArr = this.val$serviceUuids;
                    int length = uuidArr.length;
                    for (int i = BluetoothAdapter.STATE_DISCONNECTED; i < length; i += BluetoothAdapter.STATE_CONNECTING) {
                        uuids.add(new ParcelUuid(uuidArr[i]));
                    }
                    List<ParcelUuid> scanServiceUuids = scanRecord.getServiceUuids();
                    if (scanServiceUuids == null || !scanServiceUuids.containsAll(uuids)) {
                        Log.d(BluetoothAdapter.TAG, "uuids does not match");
                        return;
                    }
                }
                this.val$callback.onLeScan(result.getDevice(), result.getRssi(), scanRecord.getBytes());
            }
        }
    }

    public interface BluetoothStateChangeCallback {
        void onBluetoothStateChange(boolean z);
    }

    public interface LeScanCallback {
        void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr);
    }

    public class StateChangeCallbackWrapper extends Stub {
        private BluetoothStateChangeCallback mCallback;

        StateChangeCallbackWrapper(BluetoothStateChangeCallback callback) {
            this.mCallback = callback;
        }

        public void onBluetoothStateChange(boolean on) {
            this.mCallback.onBluetoothStateChange(on);
        }
    }

    public static synchronized BluetoothAdapter getDefaultAdapter() {
        BluetoothAdapter bluetoothAdapter;
        synchronized (BluetoothAdapter.class) {
            if (sAdapter == null) {
                IBinder b = ServiceManager.getService(BLUETOOTH_MANAGER_SERVICE);
                if (b != null) {
                    sAdapter = new BluetoothAdapter(IBluetoothManager.Stub.asInterface(b));
                } else {
                    Log.e(TAG, "Bluetooth binder is null");
                }
            }
            bluetoothAdapter = sAdapter;
        }
        return bluetoothAdapter;
    }

    BluetoothAdapter(IBluetoothManager managerService) {
        this.mServiceLock = new ReentrantReadWriteLock();
        this.mLock = new Object();
        this.mManagerCallback = new IBluetoothManagerCallback.Stub() {
            public void onBluetoothServiceUp(IBluetooth bluetoothService) {
                BluetoothAdapter.this.mServiceLock.writeLock().lock();
                BluetoothAdapter.this.mService = bluetoothService;
                BluetoothAdapter.this.mServiceLock.writeLock().unlock();
                synchronized (BluetoothAdapter.this.mProxyServiceStateCallbacks) {
                    for (IBluetoothManagerCallback cb : BluetoothAdapter.this.mProxyServiceStateCallbacks) {
                        if (cb != null) {
                            try {
                                cb.onBluetoothServiceUp(bluetoothService);
                            } catch (Exception e) {
                                Log.e(BluetoothAdapter.TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
                            }
                        } else {
                            Log.d(BluetoothAdapter.TAG, "onBluetoothServiceUp: cb is null!!!");
                        }
                    }
                }
            }

            public void onBluetoothServiceDown() {
                try {
                    BluetoothAdapter.this.mServiceLock.writeLock().lock();
                    BluetoothAdapter.this.mService = null;
                    if (BluetoothAdapter.this.mLeScanClients != null) {
                        BluetoothAdapter.this.mLeScanClients.clear();
                    }
                    if (BluetoothAdapter.sBluetoothLeAdvertiser != null) {
                        BluetoothAdapter.sBluetoothLeAdvertiser.cleanup();
                    }
                    if (BluetoothAdapter.sBluetoothLeScanner != null) {
                        BluetoothAdapter.sBluetoothLeScanner.cleanup();
                    }
                    BluetoothAdapter.this.mServiceLock.writeLock().unlock();
                    synchronized (BluetoothAdapter.this.mProxyServiceStateCallbacks) {
                        for (IBluetoothManagerCallback cb : BluetoothAdapter.this.mProxyServiceStateCallbacks) {
                            if (cb != null) {
                                try {
                                    cb.onBluetoothServiceDown();
                                } catch (Exception e) {
                                    Log.e(BluetoothAdapter.TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
                                }
                            } else {
                                Log.d(BluetoothAdapter.TAG, "onBluetoothServiceDown: cb is null!!!");
                            }
                        }
                    }
                } catch (Throwable th) {
                    BluetoothAdapter.this.mServiceLock.writeLock().unlock();
                }
            }

            public void onBrEdrDown() {
            }
        };
        this.mProxyServiceStateCallbacks = new ArrayList();
        if (managerService == null) {
            throw new IllegalArgumentException("bluetooth manager service is null");
        }
        try {
            this.mServiceLock.writeLock().lock();
            this.mService = managerService.registerAdapter(this.mManagerCallback);
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.writeLock().unlock();
        }
        this.mManagerService = managerService;
        this.mLeScanClients = new HashMap();
        this.mToken = new Binder();
    }

    public BluetoothDevice getRemoteDevice(String address) {
        return new BluetoothDevice(address);
    }

    public BluetoothDevice getRemoteDevice(byte[] address) {
        if (address == null || address.length != 6) {
            throw new IllegalArgumentException("Bluetooth address must have 6 bytes");
        }
        return new BluetoothDevice(String.format(Locale.US, "%02X:%02X:%02X:%02X:%02X:%02X", new Object[]{Byte.valueOf(address[STATE_DISCONNECTED]), Byte.valueOf(address[STATE_CONNECTING]), Byte.valueOf(address[STATE_CONNECTED]), Byte.valueOf(address[STATE_DISCONNECTING]), Byte.valueOf(address[4]), Byte.valueOf(address[5])}));
    }

    public BluetoothLeAdvertiser getBluetoothLeAdvertiser() {
        Log.i(TAG, "getBluetoothLeAdvertiser");
        if (!getLeAccess()) {
            return null;
        }
        if (isMultipleAdvertisementSupported() || isPeripheralModeSupported()) {
            synchronized (this.mLock) {
                if (sBluetoothLeAdvertiser == null) {
                    sBluetoothLeAdvertiser = new BluetoothLeAdvertiser(this.mManagerService);
                }
            }
            return sBluetoothLeAdvertiser;
        }
        Log.e(TAG, "Bluetooth LE advertising not supported");
        return null;
    }

    public BluetoothLeScanner getBluetoothLeScanner() {
        Log.i(TAG, "getBluetoothLeScanner");
        if (!getLeAccess()) {
            return null;
        }
        synchronized (this.mLock) {
            if (sBluetoothLeScanner == null) {
                sBluetoothLeScanner = new BluetoothLeScanner(this.mManagerService);
            }
        }
        return sBluetoothLeScanner;
    }

    public boolean isEnabled() {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isEnabled = this.mService.isEnabled();
                return isEnabled;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isLeEnabled() {
        int state = getLeState();
        if (state == STATE_ON) {
            Log.d(TAG, "STATE_ON");
        } else if (state == STATE_BLE_ON) {
            Log.d(TAG, "STATE_BLE_ON");
        } else {
            Log.d(TAG, "STATE_OFF");
            return false;
        }
        return DBG;
    }

    private void notifyUserAction(boolean enable) {
        Log.i(TAG, "notifyUserAction:" + enable);
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService == null) {
                Log.e(TAG, "mService is null");
                return;
            }
            if (enable) {
                this.mService.onLeServiceUp();
            } else {
                this.mService.onBrEdrDown();
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean disableBLE() {
        Log.i(TAG, "disableBLE");
        if (!isBleScanAlwaysAvailable()) {
            return false;
        }
        int state = getLeState();
        if (state == STATE_ON) {
            Log.d(TAG, "STATE_ON: shouldn't disable");
            try {
                this.mManagerService.updateBleAppCount(this.mToken, false);
            } catch (RemoteException e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            }
            return DBG;
        } else if (state == STATE_BLE_ON) {
            Log.d(TAG, "STATE_BLE_ON");
            int bleAppCnt = STATE_DISCONNECTED;
            try {
                bleAppCnt = this.mManagerService.updateBleAppCount(this.mToken, false);
            } catch (RemoteException e2) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e2);
            }
            if (bleAppCnt == 0) {
                notifyUserAction(false);
            }
            return DBG;
        } else {
            Log.d(TAG, "STATE_OFF: Already disabled");
            return false;
        }
    }

    public boolean enableBLE() {
        if (!isBleScanAlwaysAvailable()) {
            return false;
        }
        if (isLeEnabled()) {
            Log.d(TAG, "enableBLE(): BT is already enabled..!");
            try {
                this.mManagerService.updateBleAppCount(this.mToken, DBG);
            } catch (RemoteException e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            }
            return DBG;
        }
        try {
            Log.i(TAG, "Calling enableBLE");
            this.mManagerService.updateBleAppCount(this.mToken, DBG);
            return this.mManagerService.enable();
        } catch (RemoteException e2) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e2);
            return false;
        }
    }

    public boolean isRadioEnabled() {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isRadioEnabled = this.mService.isRadioEnabled();
                return isRadioEnabled;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getState() {
        int state = STATE_OFF;
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                state = this.mService.getState();
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
        }
        if (!(state == STATE_BLE_ON || state == STATE_BLE_TURNING_ON)) {
            if (state != STATE_BLE_TURNING_OFF) {
                return state;
            }
        }
        return STATE_OFF;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getLeState() {
        int state = STATE_OFF;
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                state = this.mService.getState();
            }
            this.mServiceLock.readLock().unlock();
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
        }
        return state;
    }

    boolean getLeAccess() {
        if (getLeState() == STATE_ON || getLeState() == STATE_BLE_ON) {
            return DBG;
        }
        return false;
    }

    public boolean enable() {
        Log.i(TAG, "BT enbale calling pid = " + Process.myPid());
        if (isEnabled()) {
            Log.d(TAG, "enable(): BT is already enabled..!");
            return DBG;
        }
        try {
            Log.i(TAG, "BT-Enable-FW enable():Start to enable Bluetooth!");
            return this.mManagerService.enable();
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public boolean enableRadio() {
        Log.i(TAG, "enableRadio");
        try {
            return this.mManagerService.enableRadio();
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public boolean disable() {
        try {
            Log.i(TAG, "BT-Disable-FW Start to disable Bluetooth!");
            return this.mManagerService.disable(DBG);
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public boolean disableRadio() {
        try {
            Log.i(TAG, "disableRadio");
            return this.mManagerService.disableRadio();
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public boolean disable(boolean persist) {
        try {
            Log.i(TAG, "disable: " + persist);
            return this.mManagerService.disable(persist);
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public String getAddress() {
        try {
            return this.mManagerService.getAddress();
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return null;
        }
    }

    public String getName() {
        try {
            return this.mManagerService.getName();
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return null;
        }
    }

    public boolean configHciSnoopLog(boolean enable) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean configHciSnoopLog = this.mService.configHciSnoopLog(enable);
                return configHciSnoopLog;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean factoryReset() {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean factoryReset = this.mService.factoryReset();
                return factoryReset;
            }
            SystemProperties.set("persist.bluetooth.factoryreset", "true");
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public ParcelUuid[] getUuids() {
        if (getState() != STATE_ON) {
            return null;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                ParcelUuid[] uuids = this.mService.getUuids();
                return uuids;
            }
            this.mServiceLock.readLock().unlock();
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean setName(String name) {
        if (getState() != STATE_ON) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean name2 = this.mService.setName(name);
                return name2;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getScanMode() {
        if (getState() != STATE_ON) {
            return SCAN_MODE_NONE;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                int scanMode = this.mService.getScanMode();
                return scanMode;
            }
            this.mServiceLock.readLock().unlock();
            return SCAN_MODE_NONE;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean setScanMode(int mode, int duration) {
        if (getState() != STATE_ON) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean scanMode = this.mService.setScanMode(mode, duration);
                return scanMode;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean setScanMode(int mode) {
        if (getState() != STATE_ON) {
            return false;
        }
        return setScanMode(mode, getDiscoverableTimeout());
    }

    public int getDiscoverableTimeout() {
        if (getState() != STATE_ON) {
            return -1;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                int discoverableTimeout = this.mService.getDiscoverableTimeout();
                return discoverableTimeout;
            }
            this.mServiceLock.readLock().unlock();
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDiscoverableTimeout(int timeout) {
        if (getState() == STATE_ON) {
            try {
                this.mServiceLock.readLock().lock();
                if (this.mService != null) {
                    this.mService.setDiscoverableTimeout(timeout);
                }
                this.mServiceLock.readLock().unlock();
            } catch (RemoteException e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            } catch (Throwable th) {
                this.mServiceLock.readLock().unlock();
            }
        }
    }

    public boolean startDiscovery() {
        if (getState() != STATE_ON) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean startDiscovery = this.mService.startDiscovery();
                return startDiscovery;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean cancelDiscovery() {
        if (getState() != STATE_ON) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean cancelDiscovery = this.mService.cancelDiscovery();
                return cancelDiscovery;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isDiscovering() {
        if (getState() != STATE_ON) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isDiscovering = this.mService.isDiscovering();
                return isDiscovering;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isMultipleAdvertisementSupported() {
        if (getState() != STATE_ON) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isMultiAdvertisementSupported = this.mService.isMultiAdvertisementSupported();
                return isMultiAdvertisementSupported;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get isMultipleAdvertisementSupported, error: ", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isBleScanAlwaysAvailable() {
        try {
            return this.mManagerService.isBleScanAlwaysAvailable();
        } catch (RemoteException e) {
            Log.e(TAG, "remote expection when calling isBleScanAlwaysAvailable", e);
            return false;
        }
    }

    public boolean isPeripheralModeSupported() {
        if (getState() != STATE_ON) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isPeripheralModeSupported = this.mService.isPeripheralModeSupported();
                return isPeripheralModeSupported;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get peripheral mode capability: ", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isOffloadedFilteringSupported() {
        if (!getLeAccess()) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isOffloadedFilteringSupported = this.mService.isOffloadedFilteringSupported();
                return isOffloadedFilteringSupported;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get isOffloadedFilteringSupported, error: ", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isOffloadedScanBatchingSupported() {
        if (!getLeAccess()) {
            return false;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                boolean isOffloadedScanBatchingSupported = this.mService.isOffloadedScanBatchingSupported();
                return isOffloadedScanBatchingSupported;
            }
            this.mServiceLock.readLock().unlock();
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get isOffloadedScanBatchingSupported, error: ", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public boolean isHardwareTrackingFiltersAvailable() {
        boolean z = false;
        if (!getLeAccess()) {
            return false;
        }
        try {
            IBluetoothGatt iGatt = this.mManagerService.getBluetoothGatt();
            if (iGatt == null) {
                return false;
            }
            if (iGatt.numHwTrackFiltersAvailable() != 0) {
                z = DBG;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    @Deprecated
    public BluetoothActivityEnergyInfo getControllerActivityEnergyInfo(int updateType) {
        SynchronousResultReceiver receiver = new SynchronousResultReceiver();
        requestControllerActivityEnergyInfo(receiver);
        try {
            Result result = receiver.awaitResult(1000);
            if (result.bundle != null) {
                return (BluetoothActivityEnergyInfo) result.bundle.getParcelable(BatteryStats.RESULT_RECEIVER_CONTROLLER_KEY);
            }
        } catch (TimeoutException e) {
            Log.e(TAG, "getControllerActivityEnergyInfo timed out");
        }
        return null;
    }

    public void requestControllerActivityEnergyInfo(ResultReceiver result) {
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                this.mService.requestActivityInfo(result);
                result = null;
            }
            this.mServiceLock.readLock().unlock();
            if (result != null) {
                result.send(STATE_DISCONNECTED, null);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getControllerActivityEnergyInfoCallback: " + e);
            this.mServiceLock.readLock().unlock();
            if (result != null) {
                result.send(STATE_DISCONNECTED, null);
            }
        } catch (Throwable th) {
            this.mServiceLock.readLock().unlock();
            if (result != null) {
                result.send(STATE_DISCONNECTED, null);
            }
        }
    }

    public Set<BluetoothDevice> getBondedDevices() {
        if (getState() != STATE_ON) {
            return toDeviceSet(new BluetoothDevice[STATE_DISCONNECTED]);
        }
        try {
            this.mServiceLock.readLock().lock();
            Set<BluetoothDevice> toDeviceSet;
            if (this.mService != null) {
                toDeviceSet = toDeviceSet(this.mService.getBondedDevices());
                return toDeviceSet;
            }
            toDeviceSet = toDeviceSet(new BluetoothDevice[STATE_DISCONNECTED]);
            this.mServiceLock.readLock().unlock();
            return toDeviceSet;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return null;
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getConnectionState() {
        if (getState() != STATE_ON) {
            return STATE_DISCONNECTED;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                int adapterConnectionState = this.mService.getAdapterConnectionState();
                return adapterConnectionState;
            }
            this.mServiceLock.readLock().unlock();
            return STATE_DISCONNECTED;
        } catch (RemoteException e) {
            Log.e(TAG, "getConnectionState:", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public int getProfileConnectionState(int profile) {
        if (getState() != STATE_ON) {
            return STATE_DISCONNECTED;
        }
        try {
            this.mServiceLock.readLock().lock();
            if (this.mService != null) {
                int profileConnectionState = this.mService.getProfileConnectionState(profile);
                return profileConnectionState;
            }
            this.mServiceLock.readLock().unlock();
            return STATE_DISCONNECTED;
        } catch (RemoteException e) {
            Log.e(TAG, "getProfileConnectionState:", e);
        } finally {
            this.mServiceLock.readLock().unlock();
        }
    }

    public BluetoothServerSocket listenUsingRfcommOn(int channel) throws IOException {
        return listenUsingRfcommOn(channel, false, false);
    }

    public BluetoothServerSocket listenUsingRfcommOn(int channel, boolean mitm, boolean min16DigitPin) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(STATE_CONNECTING, DBG, DBG, channel, mitm, min16DigitPin);
        int errno = socket.mSocket.bindListen();
        if (channel == SOCKET_CHANNEL_AUTO_STATIC_NO_SDP) {
            socket.setChannel(socket.mSocket.getPort());
        }
        if (errno == 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public BluetoothServerSocket listenUsingRfcommWithServiceRecord(String name, UUID uuid) throws IOException {
        return createNewRfcommSocketAndRecord(name, uuid, DBG, DBG);
    }

    public BluetoothServerSocket listenUsingInsecureRfcommWithServiceRecord(String name, UUID uuid) throws IOException {
        return createNewRfcommSocketAndRecord(name, uuid, false, false);
    }

    public BluetoothServerSocket listenUsingEncryptedRfcommWithServiceRecord(String name, UUID uuid) throws IOException {
        return createNewRfcommSocketAndRecord(name, uuid, false, DBG);
    }

    private BluetoothServerSocket createNewRfcommSocketAndRecord(String name, UUID uuid, boolean auth, boolean encrypt) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket((int) STATE_CONNECTING, auth, encrypt, new ParcelUuid(uuid));
        socket.setServiceName(name);
        int errno = socket.mSocket.bindListen();
        if (errno == 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public BluetoothServerSocket listenUsingInsecureRfcommOn(int port) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket((int) STATE_CONNECTING, false, false, port);
        int errno = socket.mSocket.bindListen();
        if (port == SOCKET_CHANNEL_AUTO_STATIC_NO_SDP) {
            socket.setChannel(socket.mSocket.getPort());
        }
        if (errno == 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public BluetoothServerSocket listenUsingEncryptedRfcommOn(int port) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket((int) STATE_CONNECTING, false, (boolean) DBG, port);
        int errno = socket.mSocket.bindListen();
        if (port == SOCKET_CHANNEL_AUTO_STATIC_NO_SDP) {
            socket.setChannel(socket.mSocket.getPort());
        }
        if (errno >= 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public static BluetoothServerSocket listenUsingScoOn() throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket((int) STATE_CONNECTED, false, false, -1);
        return socket.mSocket.bindListen() < 0 ? socket : socket;
    }

    public BluetoothServerSocket listenUsingL2capOn(int port, boolean mitm, boolean min16DigitPin) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(STATE_DISCONNECTING, DBG, DBG, port, mitm, min16DigitPin);
        int errno = socket.mSocket.bindListen();
        if (port == SOCKET_CHANNEL_AUTO_STATIC_NO_SDP) {
            socket.setChannel(socket.mSocket.getPort());
        }
        if (errno == 0) {
            return socket;
        }
        throw new IOException("Error: " + errno);
    }

    public BluetoothServerSocket listenUsingL2capOn(int port) throws IOException {
        return listenUsingL2capOn(port, false, false);
    }

    public Pair<byte[], byte[]> readOutOfBandData() {
        return getState() != STATE_ON ? null : null;
    }

    public boolean getProfileProxy(Context context, ServiceListener listener, int profile) {
        if (context == null || listener == null) {
            return false;
        }
        if (profile == STATE_CONNECTING) {
            BluetoothHeadset headset = new BluetoothHeadset(context, listener);
            return DBG;
        } else if (profile == STATE_CONNECTED) {
            BluetoothA2dp a2dp = new BluetoothA2dp(context, listener);
            return DBG;
        } else if (profile == STATE_TURNING_ON) {
            BluetoothA2dpSink a2dpSink = new BluetoothA2dpSink(context, listener);
            return DBG;
        } else if (profile == STATE_ON) {
            BluetoothAvrcpController avrcp = new BluetoothAvrcpController(context, listener);
            return DBG;
        } else if (profile == 4) {
            BluetoothInputDevice iDev = new BluetoothInputDevice(context, listener);
            return DBG;
        } else if (profile == 5) {
            BluetoothPan pan = new BluetoothPan(context, listener);
            return DBG;
        } else if (profile == STATE_DISCONNECTING) {
            BluetoothHealth health = new BluetoothHealth(context, listener);
            return DBG;
        } else if (profile == 9) {
            BluetoothMap map = new BluetoothMap(context, listener);
            return DBG;
        } else if (profile == STATE_BLE_TURNING_OFF) {
            BluetoothHeadsetClient headsetClient = new BluetoothHeadsetClient(context, listener);
            return DBG;
        } else if (profile == STATE_OFF) {
            BluetoothSap sap = new BluetoothSap(context, listener);
            return DBG;
        } else if (profile != STATE_RADIO_ON) {
            return false;
        } else {
            BluetoothPbapClient pbapClient = new BluetoothPbapClient(context, listener);
            return DBG;
        }
    }

    public void closeProfileProxy(int profile, BluetoothProfile proxy) {
        if (proxy != null) {
            switch (profile) {
                case STATE_CONNECTING /*1*/:
                    ((BluetoothHeadset) proxy).close();
                    break;
                case STATE_CONNECTED /*2*/:
                    ((BluetoothA2dp) proxy).close();
                    break;
                case STATE_DISCONNECTING /*3*/:
                    ((BluetoothHealth) proxy).close();
                    break;
                case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                    ((BluetoothInputDevice) proxy).close();
                    break;
                case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                    ((BluetoothPan) proxy).close();
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
                    ((BluetoothGatt) proxy).close();
                    break;
                case AudioState.ROUTE_SPEAKER /*8*/:
                    ((BluetoothGattServer) proxy).close();
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
                    ((BluetoothMap) proxy).close();
                    break;
                case STATE_OFF /*10*/:
                    ((BluetoothSap) proxy).close();
                    break;
                case STATE_TURNING_ON /*11*/:
                    ((BluetoothA2dpSink) proxy).close();
                    break;
                case STATE_ON /*12*/:
                    ((BluetoothAvrcpController) proxy).close();
                    break;
                case STATE_BLE_TURNING_OFF /*16*/:
                    ((BluetoothHeadsetClient) proxy).close();
                    break;
                case STATE_RADIO_ON /*17*/:
                    ((BluetoothPbapClient) proxy).close();
                    break;
            }
        }
    }

    public boolean enableNoAutoConnect() {
        if (isEnabled()) {
            Log.d(TAG, "enableNoAutoConnect(): BT is already enabled..!");
            return DBG;
        }
        try {
            Log.i(TAG, "enableNoAutoConnect");
            return this.mManagerService.enableNoAutoConnect();
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public boolean changeApplicationBluetoothState(boolean on, BluetoothStateChangeCallback callback) {
        return callback == null ? false : false;
    }

    private Set<BluetoothDevice> toDeviceSet(BluetoothDevice[] devices) {
        return Collections.unmodifiableSet(new HashSet(Arrays.asList(devices)));
    }

    public void unregisterAdapter() {
        try {
            this.mManagerService.unregisterAdapter(this.mManagerCallback);
            Log.d(TAG, "unregisterAdapter");
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.mManagerService.unregisterAdapter(this.mManagerCallback);
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        } finally {
            super.finalize();
        }
    }

    public static boolean checkBluetoothAddress(String address) {
        if (address == null || address.length() != STATE_RADIO_ON) {
            return false;
        }
        for (int i = STATE_DISCONNECTED; i < STATE_RADIO_ON; i += STATE_CONNECTING) {
            char c = address.charAt(i);
            switch (i % STATE_DISCONNECTING) {
                case STATE_DISCONNECTED /*0*/:
                case STATE_CONNECTING /*1*/:
                    if ((c < '0' || c > '9') && (c < 'A' || c > 'F')) {
                        return false;
                    }
                case STATE_CONNECTED /*2*/:
                    if (c == ':') {
                        break;
                    }
                    return false;
                default:
                    break;
            }
        }
        return DBG;
    }

    IBluetoothManager getBluetoothManager() {
        return this.mManagerService;
    }

    IBluetooth getBluetoothService(IBluetoothManagerCallback cb) {
        synchronized (this.mProxyServiceStateCallbacks) {
            if (cb == null) {
                Log.w(TAG, "getBluetoothService() called with no BluetoothManagerCallback");
            } else if (!this.mProxyServiceStateCallbacks.contains(cb)) {
                this.mProxyServiceStateCallbacks.add(cb);
            }
        }
        return this.mService;
    }

    void removeServiceStateCallback(IBluetoothManagerCallback cb) {
        synchronized (this.mProxyServiceStateCallbacks) {
            this.mProxyServiceStateCallbacks.remove(cb);
        }
    }

    @Deprecated
    public boolean startLeScan(LeScanCallback callback) {
        return startLeScan(null, callback);
    }

    @Deprecated
    public boolean startLeScan(UUID[] serviceUuids, LeScanCallback callback) {
        Log.d(TAG, "startLeScan(): " + Arrays.toString(serviceUuids));
        if (callback == null) {
            Log.e(TAG, "startLeScan: null callback");
            return false;
        }
        BluetoothLeScanner scanner = getBluetoothLeScanner();
        if (scanner == null) {
            Log.e(TAG, "startLeScan: cannot get BluetoothLeScanner");
            return false;
        }
        synchronized (this.mLeScanClients) {
            if (this.mLeScanClients.containsKey(callback)) {
                Log.e(TAG, "LE Scan has already started");
                return false;
            }
            try {
                if (this.mManagerService.getBluetoothGatt() == null) {
                    return false;
                }
                ScanCallback scanCallback = new AnonymousClass2(serviceUuids, callback);
                ScanSettings settings = new Builder().setCallbackType(STATE_CONNECTING).setScanMode(STATE_CONNECTED).build();
                List<ScanFilter> filters = new ArrayList();
                if (serviceUuids != null && serviceUuids.length > 0) {
                    filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(serviceUuids[STATE_DISCONNECTED])).build());
                }
                scanner.startScan(filters, settings, scanCallback);
                this.mLeScanClients.put(callback, scanCallback);
                return DBG;
            } catch (RemoteException e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
                return false;
            }
        }
    }

    @Deprecated
    public void stopLeScan(LeScanCallback callback) {
        Log.d(TAG, "stopLeScan()");
        BluetoothLeScanner scanner = getBluetoothLeScanner();
        if (scanner != null) {
            synchronized (this.mLeScanClients) {
                ScanCallback scanCallback = (ScanCallback) this.mLeScanClients.remove(callback);
                if (scanCallback == null) {
                    Log.d(TAG, "scan not started yet");
                    return;
                }
                scanner.stopScan(scanCallback);
            }
        }
    }
}
