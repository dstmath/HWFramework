package android.bluetooth;

import android.annotation.UnsupportedAppUsage;
import android.bluetooth.IBluetoothGattCallback;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class BluetoothGatt implements BluetoothProfile {
    static final int AUTHENTICATION_MITM = 2;
    static final int AUTHENTICATION_NONE = 0;
    static final int AUTHENTICATION_NO_MITM = 1;
    private static final int AUTH_RETRY_STATE_IDLE = 0;
    private static final int AUTH_RETRY_STATE_MITM = 2;
    private static final int AUTH_RETRY_STATE_NO_MITM = 1;
    public static final int CONNECTION_PRIORITY_BALANCED = 0;
    public static final int CONNECTION_PRIORITY_HIGH = 1;
    public static final int CONNECTION_PRIORITY_LOW_POWER = 2;
    private static final int CONN_STATE_CLOSED = 4;
    private static final int CONN_STATE_CONNECTED = 2;
    private static final int CONN_STATE_CONNECTING = 1;
    private static final int CONN_STATE_DISCONNECTING = 3;
    private static final int CONN_STATE_IDLE = 0;
    private static final boolean DBG = true;
    public static final int GATT_CONNECTION_CONGESTED = 143;
    public static final int GATT_FAILURE = 257;
    public static final int GATT_INSUFFICIENT_AUTHENTICATION = 5;
    public static final int GATT_INSUFFICIENT_ENCRYPTION = 15;
    public static final int GATT_INVALID_ATTRIBUTE_LENGTH = 13;
    public static final int GATT_INVALID_OFFSET = 7;
    public static final int GATT_READ_NOT_PERMITTED = 2;
    public static final int GATT_REQUEST_NOT_SUPPORTED = 6;
    public static final int GATT_SUCCESS = 0;
    public static final int GATT_WRITE_NOT_PERMITTED = 3;
    private static final String TAG = "BluetoothGatt";
    private static final boolean VDBG = true;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mAuthRetryState;
    @UnsupportedAppUsage
    private boolean mAutoConnect;
    private final IBluetoothGattCallback mBluetoothGattCallback = new IBluetoothGattCallback.Stub() {
        /* class android.bluetooth.BluetoothGatt.AnonymousClass1 */

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onClientRegistered(int status, int clientIf) {
            Log.d(BluetoothGatt.TAG, "onClientRegistered() - status=" + status + " clientIf=" + clientIf);
            synchronized (BluetoothGatt.this.mStateLock) {
                if (BluetoothGatt.this.mConnState != 1) {
                    Log.e(BluetoothGatt.TAG, "Bad connection state: " + BluetoothGatt.this.mConnState);
                }
            }
            BluetoothGatt.this.mClientIf = clientIf;
            boolean z = false;
            if (status != 0) {
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            callback.onConnectionStateChange(BluetoothGatt.this, 257, 0);
                        }
                    }
                });
                synchronized (BluetoothGatt.this.mStateLock) {
                    BluetoothGatt.this.mConnState = 0;
                }
                return;
            }
            try {
                if (12 == (BluetoothGatt.this.mTransport & 12)) {
                    IBluetoothGatt iBluetoothGatt = BluetoothGatt.this.mService;
                    int i = BluetoothGatt.this.mClientIf;
                    String address = BluetoothGatt.this.mDevice.getAddress();
                    if (!BluetoothGatt.this.mAutoConnect) {
                        z = true;
                    }
                    iBluetoothGatt.fastClientConnect(i, address, z, BluetoothGatt.this.mTransport);
                    return;
                }
                BluetoothGatt.this.mService.clientConnect(BluetoothGatt.this.mClientIf, BluetoothGatt.this.mDevice.getAddress(), !BluetoothGatt.this.mAutoConnect, BluetoothGatt.this.mTransport, BluetoothGatt.this.mOpportunistic, BluetoothGatt.this.mPhy);
            } catch (RemoteException e) {
                Log.e(BluetoothGatt.TAG, "", e);
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onPhyUpdate(String address, final int txPhy, final int rxPhy, final int status) {
            Log.d(BluetoothGatt.TAG, "onPhyUpdate() - status=" + status + " address=" + BluetoothGatt.getPartAddress(address) + " txPhy=" + txPhy + " rxPhy=" + rxPhy);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            callback.onPhyUpdate(BluetoothGatt.this, txPhy, rxPhy, status);
                        }
                    }
                });
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onPhyRead(String address, final int txPhy, final int rxPhy, final int status) {
            Log.d(BluetoothGatt.TAG, "onPhyRead() - status=" + status + " address=" + BluetoothGatt.getPartAddress(address) + " txPhy=" + txPhy + " rxPhy=" + rxPhy);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            callback.onPhyRead(BluetoothGatt.this, txPhy, rxPhy, status);
                        }
                    }
                });
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onClientConnectionState(final int status, int clientIf, boolean connected, String address) {
            final int profileState;
            Log.d(BluetoothGatt.TAG, "onClientConnectionState() - status=" + status + " clientIf=" + clientIf + " device=" + BluetoothGatt.getPartAddress(address));
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                if (connected) {
                    profileState = 2;
                } else {
                    profileState = 0;
                }
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass4 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            callback.onConnectionStateChange(BluetoothGatt.this, status, profileState);
                        }
                    }
                });
                synchronized (BluetoothGatt.this.mStateLock) {
                    if (connected) {
                        BluetoothGatt.this.mConnState = 2;
                    } else {
                        BluetoothGatt.this.mConnState = 0;
                    }
                }
                synchronized (BluetoothGatt.this.mDeviceBusyLock) {
                    BluetoothGatt.this.mDeviceBusy = false;
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onSearchComplete(String address, List<BluetoothGattService> services, final int status) {
            Log.d(BluetoothGatt.TAG, "onSearchComplete() = Device=" + BluetoothGatt.getPartAddress(address) + " Status=" + status);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                for (BluetoothGattService s : services) {
                    s.setDevice(BluetoothGatt.this.mDevice);
                }
                BluetoothGatt.this.mServices.addAll(services);
                for (BluetoothGattService fixedService : BluetoothGatt.this.mServices) {
                    ArrayList<BluetoothGattService> includedServices = new ArrayList<>(fixedService.getIncludedServices());
                    fixedService.getIncludedServices().clear();
                    Iterator<BluetoothGattService> it = includedServices.iterator();
                    while (it.hasNext()) {
                        BluetoothGattService brokenRef = it.next();
                        BluetoothGatt bluetoothGatt = BluetoothGatt.this;
                        BluetoothGattService includedService = bluetoothGatt.getService(bluetoothGatt.mDevice, brokenRef.getUuid(), brokenRef.getInstanceId());
                        if (includedService != null) {
                            fixedService.addIncludedService(includedService);
                        } else {
                            Log.e(BluetoothGatt.TAG, "Broken GATT database: can't find included service.");
                        }
                    }
                }
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass5 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            callback.onServicesDiscovered(BluetoothGatt.this, status);
                        }
                    }
                });
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onCharacteristicRead(String address, final int status, int handle, final byte[] value) {
            Log.d(BluetoothGatt.TAG, "onCharacteristicRead() - Device=" + BluetoothGatt.getPartAddress(address) + " handle=" + handle + " Status=" + status);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                synchronized (BluetoothGatt.this.mDeviceBusyLock) {
                    BluetoothGatt.this.mDeviceBusy = false;
                }
                if (status == 5 || status == 15) {
                    int authReq = 2;
                    if (BluetoothGatt.this.mAuthRetryState != 2) {
                        try {
                            if (BluetoothGatt.this.mAuthRetryState == 0) {
                                authReq = 1;
                            }
                            BluetoothGatt.this.mService.readCharacteristic(BluetoothGatt.this.mClientIf, address, handle, authReq);
                            BluetoothGatt.access$1508(BluetoothGatt.this);
                            return;
                        } catch (RemoteException e) {
                            Log.e(BluetoothGatt.TAG, "", e);
                        }
                    }
                }
                BluetoothGatt.this.mAuthRetryState = 0;
                BluetoothGatt bluetoothGatt = BluetoothGatt.this;
                final BluetoothGattCharacteristic characteristic = bluetoothGatt.getCharacteristicById(bluetoothGatt.mDevice, handle);
                if (characteristic == null) {
                    Log.w(BluetoothGatt.TAG, "onCharacteristicRead() failed to find characteristic!");
                } else {
                    BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                        /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass6 */

                        @Override // java.lang.Runnable
                        public void run() {
                            BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                            if (callback != null) {
                                if (status == 0) {
                                    characteristic.setValue(value);
                                }
                                callback.onCharacteristicRead(BluetoothGatt.this, characteristic, status);
                            }
                        }
                    });
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onCharacteristicWrite(String address, final int status, int handle) {
            Log.d(BluetoothGatt.TAG, "onCharacteristicWrite() - Device=" + BluetoothGatt.getPartAddress(address) + " handle=" + handle + " Status=" + status);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                synchronized (BluetoothGatt.this.mDeviceBusyLock) {
                    BluetoothGatt.this.mDeviceBusy = false;
                }
                BluetoothGatt bluetoothGatt = BluetoothGatt.this;
                final BluetoothGattCharacteristic characteristic = bluetoothGatt.getCharacteristicById(bluetoothGatt.mDevice, handle);
                if (characteristic != null) {
                    if (status == 5 || status == 15) {
                        int authReq = 2;
                        if (BluetoothGatt.this.mAuthRetryState != 2) {
                            try {
                                if (BluetoothGatt.this.mAuthRetryState == 0) {
                                    authReq = 1;
                                }
                                BluetoothGatt.this.mService.writeCharacteristic(BluetoothGatt.this.mClientIf, address, handle, characteristic.getWriteType(), authReq, characteristic.getValue());
                                BluetoothGatt.access$1508(BluetoothGatt.this);
                                return;
                            } catch (RemoteException e) {
                                Log.e(BluetoothGatt.TAG, "", e);
                            }
                        }
                    }
                    BluetoothGatt.this.mAuthRetryState = 0;
                    BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                        /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass7 */

                        @Override // java.lang.Runnable
                        public void run() {
                            BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                            if (callback != null) {
                                callback.onCharacteristicWrite(BluetoothGatt.this, characteristic, status);
                            }
                        }
                    });
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:3:0x0033, code lost:
            r0 = (r0 = r3.this$0).getCharacteristicById(r0.mDevice, r5);
         */
        @Override // android.bluetooth.IBluetoothGattCallback
        public void onNotify(String address, int handle, final byte[] value) {
            final BluetoothGattCharacteristic characteristic;
            Log.d(BluetoothGatt.TAG, "onNotify() - Device=" + BluetoothGatt.getPartAddress(address) + " handle=" + handle);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress()) && characteristic != null) {
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass8 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            characteristic.setValue(value);
                            callback.onCharacteristicChanged(BluetoothGatt.this, characteristic);
                        }
                    }
                });
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onDescriptorRead(String address, final int status, int handle, final byte[] value) {
            Log.d(BluetoothGatt.TAG, "onDescriptorRead() - Device=" + BluetoothGatt.getPartAddress(address) + " handle=" + handle);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                synchronized (BluetoothGatt.this.mDeviceBusyLock) {
                    BluetoothGatt.this.mDeviceBusy = false;
                }
                BluetoothGatt bluetoothGatt = BluetoothGatt.this;
                final BluetoothGattDescriptor descriptor = bluetoothGatt.getDescriptorById(bluetoothGatt.mDevice, handle);
                if (descriptor != null) {
                    if (status == 5 || status == 15) {
                        int authReq = 2;
                        if (BluetoothGatt.this.mAuthRetryState != 2) {
                            try {
                                if (BluetoothGatt.this.mAuthRetryState == 0) {
                                    authReq = 1;
                                }
                                BluetoothGatt.this.mService.readDescriptor(BluetoothGatt.this.mClientIf, address, handle, authReq);
                                BluetoothGatt.access$1508(BluetoothGatt.this);
                                return;
                            } catch (RemoteException e) {
                                Log.e(BluetoothGatt.TAG, "", e);
                            }
                        }
                    }
                    BluetoothGatt.this.mAuthRetryState = 0;
                    BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                        /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass9 */

                        @Override // java.lang.Runnable
                        public void run() {
                            BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                            if (callback != null) {
                                if (status == 0) {
                                    descriptor.setValue(value);
                                }
                                callback.onDescriptorRead(BluetoothGatt.this, descriptor, status);
                            }
                        }
                    });
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onDescriptorWrite(String address, final int status, int handle) {
            Log.d(BluetoothGatt.TAG, "onDescriptorWrite() - Device=" + BluetoothGatt.getPartAddress(address) + " handle=" + handle);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                synchronized (BluetoothGatt.this.mDeviceBusyLock) {
                    BluetoothGatt.this.mDeviceBusy = false;
                }
                BluetoothGatt bluetoothGatt = BluetoothGatt.this;
                final BluetoothGattDescriptor descriptor = bluetoothGatt.getDescriptorById(bluetoothGatt.mDevice, handle);
                if (descriptor != null) {
                    if (status == 5 || status == 15) {
                        int authReq = 2;
                        if (BluetoothGatt.this.mAuthRetryState != 2) {
                            try {
                                if (BluetoothGatt.this.mAuthRetryState == 0) {
                                    authReq = 1;
                                }
                                BluetoothGatt.this.mService.writeDescriptor(BluetoothGatt.this.mClientIf, address, handle, authReq, descriptor.getValue());
                                BluetoothGatt.access$1508(BluetoothGatt.this);
                                return;
                            } catch (RemoteException e) {
                                Log.e(BluetoothGatt.TAG, "", e);
                            }
                        }
                    }
                    BluetoothGatt.this.mAuthRetryState = 0;
                    BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                        /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass10 */

                        @Override // java.lang.Runnable
                        public void run() {
                            BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                            if (callback != null) {
                                callback.onDescriptorWrite(BluetoothGatt.this, descriptor, status);
                            }
                        }
                    });
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onExecuteWrite(String address, final int status) {
            Log.d(BluetoothGatt.TAG, "onExecuteWrite() - Device=" + BluetoothGatt.getPartAddress(address) + " status=" + status);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                synchronized (BluetoothGatt.this.mDeviceBusyLock) {
                    BluetoothGatt.this.mDeviceBusy = false;
                }
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass11 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            callback.onReliableWriteCompleted(BluetoothGatt.this, status);
                        }
                    }
                });
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onReadRemoteRssi(String address, final int rssi, final int status) {
            Log.d(BluetoothGatt.TAG, "onReadRemoteRssi() - Device=" + BluetoothGatt.getPartAddress(address) + " rssi=" + rssi + " status=" + status);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass12 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            callback.onReadRemoteRssi(BluetoothGatt.this, rssi, status);
                        }
                    }
                });
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onConfigureMTU(String address, final int mtu, final int status) {
            Log.d(BluetoothGatt.TAG, "onConfigureMTU() - Device=" + BluetoothGatt.getPartAddress(address) + " mtu=" + mtu + " status=" + status);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass13 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            callback.onMtuChanged(BluetoothGatt.this, mtu, status);
                        }
                    }
                });
            }
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onConnectionUpdated(String address, final int interval, final int latency, final int timeout, final int status) {
            Log.d(BluetoothGatt.TAG, "onConnectionUpdated() - Device=" + BluetoothGatt.getPartAddress(address) + " interval=" + interval + " latency=" + latency + " timeout=" + timeout + " status=" + status);
            if (address.equals(BluetoothGatt.this.mDevice.getAddress())) {
                BluetoothGatt.this.runOrQueueCallback(new Runnable() {
                    /* class android.bluetooth.BluetoothGatt.AnonymousClass1.AnonymousClass14 */

                    @Override // java.lang.Runnable
                    public void run() {
                        BluetoothGattCallback callback = BluetoothGatt.this.mCallback;
                        if (callback != null) {
                            callback.onConnectionUpdated(BluetoothGatt.this, interval, latency, timeout, status);
                        }
                    }
                });
            }
        }
    };
    @UnsupportedAppUsage
    private volatile BluetoothGattCallback mCallback;
    @UnsupportedAppUsage
    private int mClientIf;
    private int mConnState;
    private BluetoothDevice mDevice;
    @UnsupportedAppUsage
    private Boolean mDeviceBusy = false;
    private final Object mDeviceBusyLock = new Object();
    private Handler mHandler;
    private boolean mOpportunistic;
    private int mPhy;
    @UnsupportedAppUsage
    private IBluetoothGatt mService;
    private List<BluetoothGattService> mServices;
    private final Object mStateLock = new Object();
    @UnsupportedAppUsage
    private int mTransport;

    static /* synthetic */ int access$1508(BluetoothGatt x0) {
        int i = x0.mAuthRetryState;
        x0.mAuthRetryState = i + 1;
        return i;
    }

    BluetoothGatt(IBluetoothGatt iGatt, BluetoothDevice device, int transport, boolean opportunistic, int phy) {
        this.mService = iGatt;
        this.mDevice = device;
        this.mTransport = transport;
        this.mPhy = phy;
        this.mOpportunistic = opportunistic;
        this.mServices = new ArrayList();
        this.mConnState = 0;
        this.mAuthRetryState = 0;
    }

    public void close() {
        Log.d(TAG, "close()");
        unregisterApp();
        synchronized (this.mStateLock) {
            this.mConnState = 4;
        }
        this.mAuthRetryState = 0;
    }

    /* access modifiers changed from: package-private */
    public BluetoothGattService getService(BluetoothDevice device, UUID uuid, int instanceId) {
        for (BluetoothGattService svc : this.mServices) {
            if (svc.getDevice().equals(device) && svc.getInstanceId() == instanceId && svc.getUuid().equals(uuid)) {
                return svc;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public BluetoothGattCharacteristic getCharacteristicById(BluetoothDevice device, int instanceId) {
        for (BluetoothGattService svc : this.mServices) {
            Iterator<BluetoothGattCharacteristic> it = svc.getCharacteristics().iterator();
            while (true) {
                if (it.hasNext()) {
                    BluetoothGattCharacteristic charac = it.next();
                    if (charac.getInstanceId() == instanceId) {
                        return charac;
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public BluetoothGattDescriptor getDescriptorById(BluetoothDevice device, int instanceId) {
        for (BluetoothGattService svc : this.mServices) {
            Iterator<BluetoothGattCharacteristic> it = svc.getCharacteristics().iterator();
            while (true) {
                if (it.hasNext()) {
                    Iterator<BluetoothGattDescriptor> it2 = it.next().getDescriptors().iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            BluetoothGattDescriptor desc = it2.next();
                            if (desc.getInstanceId() == instanceId) {
                                return desc;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void runOrQueueCallback(Runnable cb) {
        Handler handler = this.mHandler;
        if (handler == null) {
            try {
                cb.run();
            } catch (Exception ex) {
                Log.w(TAG, "Unhandled exception in callback", ex);
            }
        } else {
            handler.post(cb);
        }
    }

    private boolean registerApp(BluetoothGattCallback callback, Handler handler) {
        Log.d(TAG, "registerApp()");
        if (this.mService == null) {
            return false;
        }
        this.mCallback = callback;
        this.mHandler = handler;
        UUID uuid = UUID.randomUUID();
        Log.d(TAG, "registerApp() - UUID=" + uuid);
        try {
            this.mService.registerClient(new ParcelUuid(uuid), this.mBluetoothGattCallback);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    private void unregisterApp() {
        int i;
        Log.d(TAG, "unregisterApp() - mClientIf=" + this.mClientIf);
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt != null && (i = this.mClientIf) != 0) {
            try {
                this.mCallback = null;
                iBluetoothGatt.unregisterClient(i);
                this.mClientIf = 0;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean connect(Boolean autoConnect, BluetoothGattCallback callback, Handler handler) {
        Log.d(TAG, "connect() - device: " + getPartAddress(this.mDevice) + ", auto: " + autoConnect);
        synchronized (this.mStateLock) {
            if (this.mConnState == 0) {
                this.mConnState = 1;
            } else {
                throw new IllegalStateException("Not idle");
            }
        }
        this.mAutoConnect = autoConnect.booleanValue();
        if (registerApp(callback, handler)) {
            return true;
        }
        synchronized (this.mStateLock) {
            this.mConnState = 0;
        }
        Log.e(TAG, "Failed to register callback");
        return false;
    }

    public void disconnect() {
        int i;
        Log.d(TAG, "cancelOpen() - device: " + getPartAddress(this.mDevice));
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt != null && (i = this.mClientIf) != 0) {
            try {
                iBluetoothGatt.clientDisconnect(i, this.mDevice.getAddress());
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }
    }

    public boolean connect() {
        Log.i(TAG, "connect is called");
        try {
            this.mService.clientConnect(this.mClientIf, this.mDevice.getAddress(), false, this.mTransport, this.mOpportunistic, this.mPhy);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public void setPreferredPhy(int txPhy, int rxPhy, int phyOptions) {
        try {
            this.mService.clientSetPreferredPhy(this.mClientIf, this.mDevice.getAddress(), txPhy, rxPhy, phyOptions);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
    }

    public void readPhy() {
        try {
            this.mService.clientReadPhy(this.mClientIf, this.mDevice.getAddress());
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public boolean discoverServices() {
        Log.d(TAG, "discoverServices() - device: " + getPartAddress(this.mDevice));
        if (this.mService == null || this.mClientIf == 0) {
            return false;
        }
        this.mServices.clear();
        try {
            this.mService.discoverServices(this.mClientIf, this.mDevice.getAddress());
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean discoverServiceByUuid(UUID uuid) {
        Log.d(TAG, "discoverServiceByUuid() - device: " + getPartAddress(this.mDevice));
        if (this.mService == null || this.mClientIf == 0) {
            return false;
        }
        this.mServices.clear();
        try {
            this.mService.discoverServiceByUuid(this.mClientIf, this.mDevice.getAddress(), new ParcelUuid(uuid));
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public List<BluetoothGattService> getServices() {
        List<BluetoothGattService> result = new ArrayList<>();
        for (BluetoothGattService service : this.mServices) {
            if (service.getDevice().equals(this.mDevice)) {
                result.add(service);
            }
        }
        return result;
    }

    public BluetoothGattService getService(UUID uuid) {
        for (BluetoothGattService service : this.mServices) {
            if (service.getDevice().equals(this.mDevice) && service.getUuid().equals(uuid)) {
                return service;
            }
        }
        return null;
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        BluetoothGattService service;
        BluetoothDevice device;
        if ((characteristic.getProperties() & 2) == 0) {
            return false;
        }
        Log.d(TAG, "readCharacteristic() - uuid: " + characteristic.getUuid());
        if (this.mService == null || this.mClientIf == 0 || (service = characteristic.getService()) == null || (device = service.getDevice()) == null) {
            return false;
        }
        synchronized (this.mDeviceBusyLock) {
            if (this.mDeviceBusy.booleanValue()) {
                return false;
            }
            this.mDeviceBusy = true;
            try {
                this.mService.readCharacteristic(this.mClientIf, device.getAddress(), characteristic.getInstanceId(), 0);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                this.mDeviceBusy = false;
                return false;
            }
        }
    }

    public boolean readUsingCharacteristicUuid(UUID uuid, int startHandle, int endHandle) {
        Log.d(TAG, "readUsingCharacteristicUuid() - uuid: " + uuid);
        if (this.mService == null || this.mClientIf == 0) {
            return false;
        }
        synchronized (this.mDeviceBusyLock) {
            if (this.mDeviceBusy.booleanValue()) {
                return false;
            }
            this.mDeviceBusy = true;
            try {
                this.mService.readUsingCharacteristicUuid(this.mClientIf, this.mDevice.getAddress(), new ParcelUuid(uuid), startHandle, endHandle, 0);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                this.mDeviceBusy = false;
                return false;
            }
        }
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        BluetoothGattService service;
        BluetoothDevice device;
        if ((characteristic.getProperties() & 8) == 0 && (characteristic.getProperties() & 4) == 0) {
            return false;
        }
        Log.d(TAG, "writeCharacteristic() - uuid: " + characteristic.getUuid());
        if (this.mService == null || this.mClientIf == 0 || characteristic.getValue() == null || (service = characteristic.getService()) == null || (device = service.getDevice()) == null) {
            return false;
        }
        synchronized (this.mDeviceBusyLock) {
            if (this.mDeviceBusy.booleanValue()) {
                return false;
            }
            this.mDeviceBusy = true;
            try {
                this.mService.writeCharacteristic(this.mClientIf, device.getAddress(), characteristic.getInstanceId(), characteristic.getWriteType(), 0, characteristic.getValue());
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                this.mDeviceBusy = false;
                return false;
            }
        }
    }

    public boolean readDescriptor(BluetoothGattDescriptor descriptor) {
        BluetoothGattCharacteristic characteristic;
        BluetoothGattService service;
        BluetoothDevice device;
        Log.d(TAG, "readDescriptor() - uuid: " + descriptor.getUuid());
        if (this.mService == null || this.mClientIf == 0 || (characteristic = descriptor.getCharacteristic()) == null || (service = characteristic.getService()) == null || (device = service.getDevice()) == null) {
            return false;
        }
        synchronized (this.mDeviceBusyLock) {
            if (this.mDeviceBusy.booleanValue()) {
                return false;
            }
            this.mDeviceBusy = true;
            try {
                this.mService.readDescriptor(this.mClientIf, device.getAddress(), descriptor.getInstanceId(), 0);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                this.mDeviceBusy = false;
                return false;
            }
        }
    }

    public boolean writeDescriptor(BluetoothGattDescriptor descriptor) {
        BluetoothGattCharacteristic characteristic;
        BluetoothGattService service;
        BluetoothDevice device;
        Log.d(TAG, "writeDescriptor() - uuid: " + descriptor.getUuid());
        if (this.mService == null || this.mClientIf == 0 || descriptor.getValue() == null || (characteristic = descriptor.getCharacteristic()) == null || (service = characteristic.getService()) == null || (device = service.getDevice()) == null) {
            return false;
        }
        synchronized (this.mDeviceBusyLock) {
            if (this.mDeviceBusy.booleanValue()) {
                return false;
            }
            this.mDeviceBusy = true;
            try {
                this.mService.writeDescriptor(this.mClientIf, device.getAddress(), descriptor.getInstanceId(), 0, descriptor.getValue());
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                this.mDeviceBusy = false;
                return false;
            }
        }
    }

    public boolean beginReliableWrite() {
        int i;
        Log.d(TAG, "beginReliableWrite() - device: " + getPartAddress(this.mDevice));
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt == null || (i = this.mClientIf) == 0) {
            return false;
        }
        try {
            iBluetoothGatt.beginReliableWrite(i, this.mDevice.getAddress());
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean executeReliableWrite() {
        Log.d(TAG, "executeReliableWrite() - device: " + getPartAddress(this.mDevice));
        if (this.mService == null || this.mClientIf == 0) {
            return false;
        }
        synchronized (this.mDeviceBusyLock) {
            if (this.mDeviceBusy.booleanValue()) {
                return false;
            }
            this.mDeviceBusy = true;
            try {
                this.mService.endReliableWrite(this.mClientIf, this.mDevice.getAddress(), true);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                this.mDeviceBusy = false;
                return false;
            }
        }
    }

    public void abortReliableWrite() {
        int i;
        Log.d(TAG, "abortReliableWrite() - device: " + getPartAddress(this.mDevice));
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt != null && (i = this.mClientIf) != 0) {
            try {
                iBluetoothGatt.endReliableWrite(i, this.mDevice.getAddress(), false);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }
    }

    @Deprecated
    public void abortReliableWrite(BluetoothDevice mDevice2) {
        abortReliableWrite();
    }

    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        BluetoothGattService service;
        BluetoothDevice device;
        Log.d(TAG, "setCharacteristicNotification() - uuid: " + characteristic.getUuid() + " enable: " + enable);
        if (this.mService == null || this.mClientIf == 0 || (service = characteristic.getService()) == null || (device = service.getDevice()) == null) {
            return false;
        }
        try {
            this.mService.registerForNotification(this.mClientIf, device.getAddress(), characteristic.getInstanceId(), enable);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean refresh() {
        int i;
        Log.d(TAG, "refresh() - device: " + getPartAddress(this.mDevice));
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt == null || (i = this.mClientIf) == 0) {
            return false;
        }
        try {
            iBluetoothGatt.refreshDevice(i, this.mDevice.getAddress());
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean readRemoteRssi() {
        int i;
        Log.d(TAG, "readRssi() - device: " + getPartAddress(this.mDevice));
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt == null || (i = this.mClientIf) == 0) {
            return false;
        }
        try {
            iBluetoothGatt.readRemoteRssi(i, this.mDevice.getAddress());
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean requestMtu(int mtu) {
        int i;
        Log.d(TAG, "configureMTU() - device: " + getPartAddress(this.mDevice) + " mtu: " + mtu);
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt == null || (i = this.mClientIf) == 0) {
            return false;
        }
        try {
            iBluetoothGatt.configureMTU(i, this.mDevice.getAddress(), mtu);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean requestConnectionPriority(int connectionPriority) {
        int i;
        if (connectionPriority < 0 || connectionPriority > 2) {
            throw new IllegalArgumentException("connectionPriority not within valid range");
        }
        Log.d(TAG, "requestConnectionPriority() - params: " + connectionPriority);
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt == null || (i = this.mClientIf) == 0) {
            return false;
        }
        try {
            iBluetoothGatt.connectionParameterUpdate(i, this.mDevice.getAddress(), connectionPriority);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean requestLeConnectionUpdate(int minConnectionInterval, int maxConnectionInterval, int slaveLatency, int supervisionTimeout, int minConnectionEventLen, int maxConnectionEventLen) {
        int i;
        String str;
        RemoteException e;
        Log.d(TAG, "requestLeConnectionUpdate() - min=(" + minConnectionInterval + ")" + (((double) minConnectionInterval) * 1.25d) + "msec, max=(" + maxConnectionInterval + ")" + (((double) maxConnectionInterval) * 1.25d) + "msec, latency=" + slaveLatency + ", timeout=" + supervisionTimeout + "msec, min_ce=" + minConnectionEventLen + ", max_ce=" + maxConnectionEventLen);
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt == null || (i = this.mClientIf) == 0) {
            return false;
        }
        try {
            String address = this.mDevice.getAddress();
            str = TAG;
            try {
                iBluetoothGatt.leConnectionUpdate(i, address, minConnectionInterval, maxConnectionInterval, slaveLatency, supervisionTimeout, minConnectionEventLen, maxConnectionEventLen);
                return true;
            } catch (RemoteException e2) {
                e = e2;
                Log.e(str, "", e);
                return false;
            }
        } catch (RemoteException e3) {
            e = e3;
            str = TAG;
            Log.e(str, "", e);
            return false;
        }
    }

    @Override // android.bluetooth.BluetoothProfile
    public int getConnectionState(BluetoothDevice device) {
        throw new UnsupportedOperationException("Use BluetoothManager#getConnectionState instead.");
    }

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getConnectedDevices() {
        throw new UnsupportedOperationException("Use BluetoothManager#getConnectedDevices instead.");
    }

    @Override // android.bluetooth.BluetoothProfile
    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        throw new UnsupportedOperationException("Use BluetoothManager#getDevicesMatchingConnectionStates instead.");
    }

    /* access modifiers changed from: private */
    public static String getPartAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return "";
        }
        return address.substring(0, address.length() / 2) + ":**:**:**";
    }

    private static String getPartAddress(BluetoothDevice device) {
        if (device == null) {
            return "";
        }
        return getPartAddress(device.getAddress());
    }
}
