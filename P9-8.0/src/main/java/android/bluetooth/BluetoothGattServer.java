package android.bluetooth;

import android.app.job.JobInfo;
import android.bluetooth.IBluetoothGattServerCallback.Stub;
import android.net.ProxyInfo;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BluetoothGattServer implements BluetoothProfile {
    private static final int CALLBACK_REG_TIMEOUT = 10000;
    private static final boolean DBG = true;
    private static final String TAG = "BluetoothGattServer";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter;
    private final IBluetoothGattServerCallback mBluetoothGattServerCallback = new Stub() {
        public void onServerRegistered(int status, int serverIf) {
            Log.d(BluetoothGattServer.TAG, "onServerRegistered() - status=" + status + " serverIf=" + serverIf);
            synchronized (BluetoothGattServer.this.mServerIfLock) {
                if (BluetoothGattServer.this.mCallback != null) {
                    BluetoothGattServer.this.mServerIf = serverIf;
                    BluetoothGattServer.this.mServerIfLock.notify();
                } else {
                    Log.e(BluetoothGattServer.TAG, "onServerRegistered: mCallback is null");
                }
            }
        }

        public void onServerConnectionState(int status, int serverIf, boolean connected, String address) {
            Log.d(BluetoothGattServer.TAG, "onServerConnectionState() - status=" + status + " serverIf=" + serverIf + " device=" + address);
            try {
                int i;
                BluetoothGattServerCallback -get1 = BluetoothGattServer.this.mCallback;
                BluetoothDevice remoteDevice = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
                if (connected) {
                    i = 2;
                } else {
                    i = 0;
                }
                -get1.onConnectionStateChange(remoteDevice, status, i);
            } catch (Exception ex) {
                Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
            }
        }

        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.d(BluetoothGattServer.TAG, "onServiceAdded() - handle=" + service.getInstanceId() + " uuid=" + service.getUuid() + " status=" + status);
            if (BluetoothGattServer.this.mPendingService != null) {
                BluetoothGattService tmp = BluetoothGattServer.this.mPendingService;
                BluetoothGattServer.this.mPendingService = null;
                tmp.setInstanceId(service.getInstanceId());
                List<BluetoothGattCharacteristic> temp_chars = tmp.getCharacteristics();
                List<BluetoothGattCharacteristic> svc_chars = service.getCharacteristics();
                for (int i = 0; i < svc_chars.size(); i++) {
                    BluetoothGattCharacteristic temp_char = (BluetoothGattCharacteristic) temp_chars.get(i);
                    BluetoothGattCharacteristic svc_char = (BluetoothGattCharacteristic) svc_chars.get(i);
                    temp_char.setInstanceId(svc_char.getInstanceId());
                    List<BluetoothGattDescriptor> temp_descs = temp_char.getDescriptors();
                    List<BluetoothGattDescriptor> svc_descs = svc_char.getDescriptors();
                    for (int j = 0; j < svc_descs.size(); j++) {
                        ((BluetoothGattDescriptor) temp_descs.get(j)).setInstanceId(((BluetoothGattDescriptor) svc_descs.get(j)).getInstanceId());
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

        public void onExecuteWrite(String address, int transId, boolean execWrite) {
            Log.d(BluetoothGattServer.TAG, "onExecuteWrite() - device=" + address + ", transId=" + transId + "execWrite=" + execWrite);
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onExecuteWrite(device, transId, execWrite);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
                }
            }
        }

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

        public void onMtuChanged(String address, int mtu) {
            Log.d(BluetoothGattServer.TAG, "onMtuChanged() - device=" + address + ", mtu=" + mtu);
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onMtuChanged(device, mtu);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception: " + ex);
                }
            }
        }

        public void onPhyUpdate(String address, int txPhy, int rxPhy, int status) {
            Log.d(BluetoothGattServer.TAG, "onPhyUpdate() - device=" + address + ", txPHy=" + txPhy + ", rxPHy=" + rxPhy);
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onPhyUpdate(device, txPhy, rxPhy, status);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception: " + ex);
                }
            }
        }

        public void onPhyRead(String address, int txPhy, int rxPhy, int status) {
            Log.d(BluetoothGattServer.TAG, "onPhyUpdate() - device=" + address + ", txPHy=" + txPhy + ", rxPHy=" + rxPhy);
            BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
            if (device != null) {
                try {
                    BluetoothGattServer.this.mCallback.onPhyRead(device, txPhy, rxPhy, status);
                } catch (Exception ex) {
                    Log.w(BluetoothGattServer.TAG, "Unhandled exception: " + ex);
                }
            }
        }

        public void onConnectionUpdated(String address, int interval, int latency, int timeout, int status) {
            Log.d(BluetoothGattServer.TAG, "onConnectionUpdated() - Device=" + address + " interval=" + interval + " latency=" + latency + " timeout=" + timeout + " status=" + status);
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

    BluetoothGattCharacteristic getCharacteristicByHandle(int handle) {
        for (BluetoothGattService svc : this.mServices) {
            for (BluetoothGattCharacteristic charac : svc.getCharacteristics()) {
                if (charac.getInstanceId() == handle) {
                    return charac;
                }
            }
        }
        return null;
    }

    BluetoothGattDescriptor getDescriptorByHandle(int handle) {
        for (BluetoothGattService svc : this.mServices) {
            for (BluetoothGattCharacteristic charac : svc.getCharacteristics()) {
                for (BluetoothGattDescriptor desc : charac.getDescriptors()) {
                    if (desc.getInstanceId() == handle) {
                        return desc;
                    }
                }
            }
        }
        return null;
    }

    public void close() {
        Log.d(TAG, "close()");
        unregisterCallback();
    }

    boolean registerCallback(BluetoothGattServerCallback callback) {
        Log.d(TAG, "registerCallback()");
        if (this.mService == null) {
            Log.e(TAG, "GATT service not available");
            return false;
        }
        UUID uuid = UUID.randomUUID();
        Log.d(TAG, "registerCallback() - UUID=" + uuid);
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
                    Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST + e);
                    this.mCallback = null;
                }
                if (this.mServerIf == 0) {
                    this.mCallback = null;
                    return false;
                }
                return true;
            } catch (RemoteException e2) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e2);
                this.mCallback = null;
                return false;
            }
        }
    }

    private void unregisterCallback() {
        Log.d(TAG, "unregisterCallback() - mServerIf=" + this.mServerIf);
        if (this.mService != null && this.mServerIf != 0) {
            try {
                this.mCallback = null;
                this.mService.unregisterServer(this.mServerIf);
                this.mServerIf = 0;
            } catch (RemoteException e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            }
        }
    }

    BluetoothGattService getService(UUID uuid, int instanceId, int type) {
        for (BluetoothGattService svc : this.mServices) {
            if (svc.getType() == type && svc.getInstanceId() == instanceId && svc.getUuid().equals(uuid)) {
                return svc;
            }
        }
        return null;
    }

    public boolean connect(BluetoothDevice device, boolean autoConnect) {
        Log.d(TAG, "connect() - device: " + device.getAddress() + ", auto: " + autoConnect);
        if (this.mService == null || this.mServerIf == 0) {
            return false;
        }
        try {
            this.mService.serverConnect(this.mServerIf, device.getAddress(), !autoConnect, this.mTransport);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public void cancelConnection(BluetoothDevice device) {
        Log.d(TAG, "cancelConnection() - device: " + device.getAddress());
        if (this.mService != null && this.mServerIf != 0) {
            try {
                this.mService.serverDisconnect(this.mServerIf, device.getAddress());
            } catch (RemoteException e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            }
        }
    }

    public void setPreferredPhy(BluetoothDevice device, int txPhy, int rxPhy, int phyOptions) {
        try {
            this.mService.serverSetPreferredPhy(this.mServerIf, device.getAddress(), txPhy, rxPhy, phyOptions);
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        }
    }

    public void readPhy(BluetoothDevice device) {
        try {
            this.mService.serverReadPhy(this.mServerIf, device.getAddress());
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
        }
    }

    public boolean sendResponse(BluetoothDevice device, int requestId, int status, int offset, byte[] value) {
        if (this.mService == null || this.mServerIf == 0) {
            return false;
        }
        try {
            this.mService.sendResponse(this.mServerIf, device.getAddress(), requestId, status, offset, value);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean notifyCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic, boolean confirm) {
        if (this.mService == null || this.mServerIf == 0 || characteristic.getService() == null) {
            return false;
        }
        if (characteristic.getValue() == null) {
            throw new IllegalArgumentException("Chracteristic value is empty. Use BluetoothGattCharacteristic#setvalue to update");
        }
        try {
            this.mService.sendNotification(this.mServerIf, device.getAddress(), characteristic.getInstanceId(), confirm, characteristic.getValue());
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public boolean addService(BluetoothGattService service) {
        Log.d(TAG, "addService() - service: " + service.getUuid());
        if (this.mService == null || this.mServerIf == 0) {
            return false;
        }
        this.mPendingService = service;
        try {
            this.mService.addService(this.mServerIf, service);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public boolean removeService(BluetoothGattService service) {
        Log.d(TAG, "removeService() - service: " + service.getUuid());
        if (this.mService == null || this.mServerIf == 0) {
            return false;
        }
        BluetoothGattService intService = getService(service.getUuid(), service.getInstanceId(), service.getType());
        if (intService == null) {
            return false;
        }
        try {
            this.mService.removeService(this.mServerIf, service.getInstanceId());
            this.mServices.remove(intService);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public void clearServices() {
        Log.d(TAG, "clearServices()");
        if (this.mService != null && this.mServerIf != 0) {
            try {
                this.mService.clearServices(this.mServerIf);
                this.mServices.clear();
            } catch (RemoteException e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
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

    public int getConnectionState(BluetoothDevice device) {
        throw new UnsupportedOperationException("Use BluetoothManager#getConnectionState instead.");
    }

    public List<BluetoothDevice> getConnectedDevices() {
        throw new UnsupportedOperationException("Use BluetoothManager#getConnectedDevices instead.");
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        throw new UnsupportedOperationException("Use BluetoothManager#getDevicesMatchingConnectionStates instead.");
    }
}
