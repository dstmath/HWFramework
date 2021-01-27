package android.bluetooth;

import android.app.job.JobInfo;
import android.bluetooth.IBluetoothGattServerCallback;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class BluetoothGattServer implements BluetoothProfile {
    private static final int CALLBACK_REG_TIMEOUT = 10000;
    private static final boolean DBG = false;
    private static final String TAG = "BluetoothGattServer";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter;
    private final IBluetoothGattServerCallback mBluetoothGattServerCallback = new IBluetoothGattServerCallback.Stub() {
        /* class android.bluetooth.BluetoothGattServer.AnonymousClass1 */

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onServerRegistered(int status, int serverIf) {
            synchronized (BluetoothGattServer.this.mServerIfLock) {
                if (BluetoothGattServer.this.mCallback != null) {
                    BluetoothGattServer.this.mServerIf = serverIf;
                    BluetoothGattServer.this.mServerIfLock.notify();
                } else {
                    Log.e(BluetoothGattServer.TAG, "onServerRegistered: mCallback is null");
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onServerConnectionState(int status, int serverIf, boolean connected, String address) {
            int i;
            try {
                BluetoothGattServerCallback bluetoothGattServerCallback = BluetoothGattServer.this.mCallback;
                BluetoothDevice remoteDevice = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
                if (connected) {
                    i = 2;
                } else {
                    i = 0;
                }
                bluetoothGattServerCallback.onConnectionStateChange(remoteDevice, status, i);
            } catch (Exception ex) {
                Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onServiceAdded(int status, BluetoothGattService service) {
            if (BluetoothGattServer.this.mPendingService != null) {
                BluetoothGattService tmp = BluetoothGattServer.this.mPendingService;
                BluetoothGattServer.this.mPendingService = null;
                tmp.setInstanceId(service.getInstanceId());
                List<BluetoothGattCharacteristic> temp_chars = tmp.getCharacteristics();
                List<BluetoothGattCharacteristic> svc_chars = service.getCharacteristics();
                for (int i = 0; i < svc_chars.size(); i++) {
                    BluetoothGattCharacteristic temp_char = temp_chars.get(i);
                    BluetoothGattCharacteristic svc_char = svc_chars.get(i);
                    temp_char.setInstanceId(svc_char.getInstanceId());
                    List<BluetoothGattDescriptor> temp_descs = temp_char.getDescriptors();
                    List<BluetoothGattDescriptor> svc_descs = svc_char.getDescriptors();
                    for (int j = 0; j < svc_descs.size(); j++) {
                        temp_descs.get(j).setInstanceId(svc_descs.get(j).getInstanceId());
                    }
                }
                BluetoothGattServer.this.mServices.add(tmp);
                try {
                    BluetoothGattServer.this.mCallback.onServiceAdded(status, tmp);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onCharacteristicReadRequest(String address, int transId, int offset, boolean isLong, int handle) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            BluetoothGattCharacteristic characteristic = BluetoothGattServer.this.getCharacteristicByHandle(handle);
            if (characteristic == null) {
                Log.w(BluetoothGattServer.TAG, "onCharacteristicReadRequest() no char for handle " + handle);
                return;
            }
            try {
                BluetoothGattServer.this.mCallback.onCharacteristicReadRequest(device, transId, offset, characteristic);
            } catch (Exception ex) {
                Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onDescriptorReadRequest(String address, int transId, int offset, boolean isLong, int handle) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            BluetoothGattDescriptor descriptor = BluetoothGattServer.this.getDescriptorByHandle(handle);
            if (descriptor == null) {
                Log.w(BluetoothGattServer.TAG, "onDescriptorReadRequest() no desc for handle " + handle);
                return;
            }
            try {
                BluetoothGattServer.this.mCallback.onDescriptorReadRequest(device, transId, offset, descriptor);
            } catch (Exception ex) {
                Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onCharacteristicWriteRequest(String address, int transId, int offset, int length, boolean isPrep, boolean needRsp, int handle, byte[] value) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            BluetoothGattCharacteristic characteristic = BluetoothGattServer.this.getCharacteristicByHandle(handle);
            if (characteristic == null) {
                Log.w(BluetoothGattServer.TAG, "onCharacteristicWriteRequest() no char for handle " + handle);
                return;
            }
            try {
                BluetoothGattServer.this.mCallback.onCharacteristicWriteRequest(device, transId, characteristic, isPrep, needRsp, offset, value);
            } catch (Exception ex) {
                Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onDescriptorWriteRequest(String address, int transId, int offset, int length, boolean isPrep, boolean needRsp, int handle, byte[] value) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            BluetoothGattDescriptor descriptor = BluetoothGattServer.this.getDescriptorByHandle(handle);
            if (descriptor == null) {
                Log.w(BluetoothGattServer.TAG, "onDescriptorWriteRequest() no desc for handle " + handle);
                return;
            }
            try {
                BluetoothGattServer.this.mCallback.onDescriptorWriteRequest(device, transId, descriptor, isPrep, needRsp, offset, value);
            } catch (Exception ex) {
                Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onExecuteWrite(String address, int transId, boolean execWrite) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onExecuteWrite(device, transId, execWrite);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onNotificationSent(String address, int status) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onNotificationSent(device, status);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception: " + ex);
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onMtuChanged(String address, int mtu) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onMtuChanged(device, mtu);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception: " + ex);
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onPhyUpdate(String address, int txPhy, int rxPhy, int status) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onPhyUpdate(device, txPhy, rxPhy, status);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception: " + ex);
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onPhyRead(String address, int txPhy, int rxPhy, int status) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onPhyRead(device, txPhy, rxPhy, status);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception: " + ex);
                }
            }
        }

        @Override // android.bluetooth.IBluetoothGattServerCallback
        public void onConnectionUpdated(String address, int interval, int latency, int timeout, int status) {
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onConnectionUpdated(device, interval, latency, timeout, status);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception: " + ex);
                }
            }
        }
    };
    private BluetoothGattServerCallback mCallback;
    private BluetoothGattService mPendingService;
    private int mServerIf;
    private Object mServerIfLock = new Object();
    private IBluetoothGatt mService;
    private List<BluetoothGattService> mServices;
    private int mTransport;

    BluetoothGattServer(IBluetoothGatt iGatt, int transport) {
        this.mService = iGatt;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mCallback = null;
        this.mServerIf = 0;
        this.mTransport = transport;
        this.mServices = new ArrayList();
    }

    /* access modifiers changed from: package-private */
    public BluetoothGattCharacteristic getCharacteristicByHandle(int handle) {
        for (BluetoothGattService svc : this.mServices) {
            Iterator<BluetoothGattCharacteristic> it = svc.getCharacteristics().iterator();
            while (true) {
                if (it.hasNext()) {
                    BluetoothGattCharacteristic charac = it.next();
                    if (charac.getInstanceId() == handle) {
                        return charac;
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public BluetoothGattDescriptor getDescriptorByHandle(int handle) {
        for (BluetoothGattService svc : this.mServices) {
            Iterator<BluetoothGattCharacteristic> it = svc.getCharacteristics().iterator();
            while (true) {
                if (it.hasNext()) {
                    Iterator<BluetoothGattDescriptor> it2 = it.next().getDescriptors().iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            BluetoothGattDescriptor desc = it2.next();
                            if (desc.getInstanceId() == handle) {
                                return desc;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void close() {
        unregisterCallback();
    }

    /* access modifiers changed from: package-private */
    public boolean registerCallback(BluetoothGattServerCallback callback) {
        if (this.mService == null) {
            Log.e(TAG, "GATT service not available");
            return false;
        }
        UUID uuid = UUID.randomUUID();
        synchronized (this.mServerIfLock) {
            if (this.mCallback != null) {
                Log.e(TAG, "App can register callback only once");
                return false;
            }
            this.mCallback = callback;
            try {
                this.mService.registerServer(new ParcelUuid(uuid), this.mBluetoothGattServerCallback);
                try {
                    this.mServerIfLock.wait(JobInfo.MIN_BACKOFF_MILLIS);
                } catch (InterruptedException e) {
                    Log.e(TAG, "" + e);
                    this.mCallback = null;
                }
                if (this.mServerIf != 0) {
                    return true;
                }
                this.mCallback = null;
                return false;
            } catch (RemoteException | IllegalStateException e2) {
                Log.e(TAG, "", e2);
                this.mCallback = null;
                return false;
            }
        }
    }

    private void unregisterCallback() {
        int i;
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt != null && (i = this.mServerIf) != 0) {
            try {
                this.mCallback = null;
                iBluetoothGatt.unregisterServer(i);
                this.mServerIf = 0;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public BluetoothGattService getService(UUID uuid, int instanceId, int type) {
        for (BluetoothGattService svc : this.mServices) {
            if (svc.getType() == type && svc.getInstanceId() == instanceId && svc.getUuid().equals(uuid)) {
                return svc;
            }
        }
        return null;
    }

    public boolean connect(BluetoothDevice device, boolean autoConnect) {
        int i;
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt == null || (i = this.mServerIf) == 0) {
            return false;
        }
        try {
            iBluetoothGatt.serverConnect(i, device.getAddress(), !autoConnect, this.mTransport);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public void cancelConnection(BluetoothDevice device) {
        int i;
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt != null && (i = this.mServerIf) != 0) {
            try {
                iBluetoothGatt.serverDisconnect(i, device.getAddress());
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }
    }

    public void setPreferredPhy(BluetoothDevice device, int txPhy, int rxPhy, int phyOptions) {
        try {
            this.mService.serverSetPreferredPhy(this.mServerIf, device.getAddress(), txPhy, rxPhy, phyOptions);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
    }

    public void readPhy(BluetoothDevice device) {
        try {
            this.mService.serverReadPhy(this.mServerIf, device.getAddress());
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
    }

    public boolean sendResponse(BluetoothDevice device, int requestId, int status, int offset, byte[] value) {
        int i;
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt == null || (i = this.mServerIf) == 0) {
            return false;
        }
        try {
            iBluetoothGatt.sendResponse(i, device.getAddress(), requestId, status, offset, value);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean notifyCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic, boolean confirm) {
        if (this.mService == null || this.mServerIf == 0 || characteristic.getService() == null) {
            return false;
        }
        if (characteristic.getValue() != null) {
            try {
                this.mService.sendNotification(this.mServerIf, device.getAddress(), characteristic.getInstanceId(), confirm, characteristic.getValue());
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
                return false;
            }
        } else {
            throw new IllegalArgumentException("Chracteristic value is empty. Use BluetoothGattCharacteristic#setvalue to update");
        }
    }

    public boolean addService(BluetoothGattService service) {
        int i;
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt == null || (i = this.mServerIf) == 0) {
            return false;
        }
        this.mPendingService = service;
        try {
            iBluetoothGatt.addService(i, service);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean removeService(BluetoothGattService service) {
        BluetoothGattService intService;
        if (this.mService == null || this.mServerIf == 0 || (intService = getService(service.getUuid(), service.getInstanceId(), service.getType())) == null) {
            return false;
        }
        try {
            this.mService.removeService(this.mServerIf, service.getInstanceId());
            this.mServices.remove(intService);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public void clearServices() {
        int i;
        IBluetoothGatt iBluetoothGatt = this.mService;
        if (iBluetoothGatt != null && (i = this.mServerIf) != 0) {
            try {
                iBluetoothGatt.clearServices(i);
                this.mServices.clear();
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }
    }

    public List<BluetoothGattService> getServices() {
        return this.mServices;
    }

    public BluetoothGattService getService(UUID uuid) {
        for (BluetoothGattService service : this.mServices) {
            if (service.getUuid().equals(uuid)) {
                return service;
            }
        }
        return null;
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

    private static String getPartAddress(String address) {
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
