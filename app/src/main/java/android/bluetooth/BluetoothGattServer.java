package android.bluetooth;

import android.bluetooth.IBluetoothGattServerCallback.Stub;
import android.content.Context;
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
    private final IBluetoothGattServerCallback mBluetoothGattServerCallback;
    private BluetoothGattServerCallback mCallback;
    private final Context mContext;
    private int mServerIf;
    private Object mServerIfLock;
    private IBluetoothGatt mService;
    private List<BluetoothGattService> mServices;
    private int mTransport;

    BluetoothGattServer(Context context, IBluetoothGatt iGatt, int transport) {
        this.mServerIfLock = new Object();
        this.mBluetoothGattServerCallback = new Stub() {
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

            public void onScanResult(String address, int rssi, byte[] advData) {
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

            public void onServiceAdded(int status, int srvcType, int srvcInstId, ParcelUuid srvcId) {
                UUID srvcUuid = srvcId.getUuid();
                Log.d(BluetoothGattServer.TAG, "onServiceAdded() - service=" + srvcUuid + "status=" + status);
                BluetoothGattService service = BluetoothGattServer.this.getService(srvcUuid, srvcInstId, srvcType);
                if (service != null) {
                    try {
                        BluetoothGattServer.this.mCallback.onServiceAdded(status, service);
                    } catch (Exception ex) {
                        Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
                    }
                }
            }

            public void onCharacteristicReadRequest(String address, int transId, int offset, boolean isLong, int srvcType, int srvcInstId, ParcelUuid srvcId, int charInstId, ParcelUuid charId) {
                UUID srvcUuid = srvcId.getUuid();
                UUID charUuid = charId.getUuid();
                BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
                BluetoothGattService service = BluetoothGattServer.this.getService(srvcUuid, srvcInstId, srvcType);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(charUuid);
                    if (characteristic != null) {
                        try {
                            BluetoothGattServer.this.mCallback.onCharacteristicReadRequest(device, transId, offset, characteristic);
                        } catch (Exception ex) {
                            Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
                        }
                    }
                }
            }

            public void onDescriptorReadRequest(String address, int transId, int offset, boolean isLong, int srvcType, int srvcInstId, ParcelUuid srvcId, int charInstId, ParcelUuid charId, ParcelUuid descrId) {
                UUID srvcUuid = srvcId.getUuid();
                UUID charUuid = charId.getUuid();
                UUID descrUuid = descrId.getUuid();
                BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
                BluetoothGattService service = BluetoothGattServer.this.getService(srvcUuid, srvcInstId, srvcType);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(charUuid);
                    if (characteristic != null) {
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descrUuid);
                        if (descriptor != null) {
                            try {
                                BluetoothGattServer.this.mCallback.onDescriptorReadRequest(device, transId, offset, descriptor);
                            } catch (Exception ex) {
                                Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
                            }
                        }
                    }
                }
            }

            public void onCharacteristicWriteRequest(String address, int transId, int offset, int length, boolean isPrep, boolean needRsp, int srvcType, int srvcInstId, ParcelUuid srvcId, int charInstId, ParcelUuid charId, byte[] value) {
                UUID srvcUuid = srvcId.getUuid();
                UUID charUuid = charId.getUuid();
                BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
                BluetoothGattService service = BluetoothGattServer.this.getService(srvcUuid, srvcInstId, srvcType);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(charUuid);
                    if (characteristic != null) {
                        try {
                            BluetoothGattServer.this.mCallback.onCharacteristicWriteRequest(device, transId, characteristic, isPrep, needRsp, offset, value);
                        } catch (Exception ex) {
                            Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
                        }
                    }
                }
            }

            public void onDescriptorWriteRequest(String address, int transId, int offset, int length, boolean isPrep, boolean needRsp, int srvcType, int srvcInstId, ParcelUuid srvcId, int charInstId, ParcelUuid charId, ParcelUuid descrId, byte[] value) {
                UUID srvcUuid = srvcId.getUuid();
                UUID charUuid = charId.getUuid();
                UUID descrUuid = descrId.getUuid();
                BluetoothDevice device = BluetoothGattServer.this.mAdapter.getRemoteDevice(address);
                BluetoothGattService service = BluetoothGattServer.this.getService(srvcUuid, srvcInstId, srvcType);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(charUuid);
                    if (characteristic != null) {
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descrUuid);
                        if (descriptor != null) {
                            try {
                                BluetoothGattServer.this.mCallback.onDescriptorWriteRequest(device, transId, descriptor, isPrep, needRsp, offset, value);
                            } catch (Exception ex) {
                                Log.w(BluetoothGattServer.TAG, "Unhandled exception in callback", ex);
                            }
                        }
                    }
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
        };
        this.mContext = context;
        this.mService = iGatt;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mCallback = null;
        this.mServerIf = 0;
        this.mTransport = transport;
        this.mServices = new ArrayList();
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
                    this.mServerIfLock.wait(10000);
                } catch (InterruptedException e) {
                    Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST + e);
                    this.mCallback = null;
                }
                if (this.mServerIf == 0) {
                    this.mCallback = null;
                    return false;
                }
                return DBG;
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
            this.mService.serverConnect(this.mServerIf, device.getAddress(), autoConnect ? false : DBG, this.mTransport);
            return DBG;
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

    public boolean sendResponse(BluetoothDevice device, int requestId, int status, int offset, byte[] value) {
        if (this.mService == null || this.mServerIf == 0) {
            return false;
        }
        try {
            this.mService.sendResponse(this.mServerIf, device.getAddress(), requestId, status, offset, value);
            return DBG;
        } catch (RemoteException e) {
            Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            return false;
        }
    }

    public boolean notifyCharacteristicChanged(BluetoothDevice device, BluetoothGattCharacteristic characteristic, boolean confirm) {
        if (this.mService == null || this.mServerIf == 0) {
            return false;
        }
        BluetoothGattService service = characteristic.getService();
        if (service == null) {
            return false;
        }
        if (characteristic.getValue() == null) {
            throw new IllegalArgumentException("Chracteristic value is empty. Use BluetoothGattCharacteristic#setvalue to update");
        }
        try {
            this.mService.sendNotification(this.mServerIf, device.getAddress(), service.getType(), service.getInstanceId(), new ParcelUuid(service.getUuid()), characteristic.getInstanceId(), new ParcelUuid(characteristic.getUuid()), confirm, characteristic.getValue());
            return DBG;
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
        this.mServices.add(service);
        try {
            this.mService.beginServiceDeclaration(this.mServerIf, service.getType(), service.getInstanceId(), service.getHandles(), new ParcelUuid(service.getUuid()), service.isAdvertisePreferred());
            for (BluetoothGattService includedService : service.getIncludedServices()) {
                this.mService.addIncludedService(this.mServerIf, includedService.getType(), includedService.getInstanceId(), new ParcelUuid(includedService.getUuid()));
            }
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                this.mService.addCharacteristic(this.mServerIf, new ParcelUuid(characteristic.getUuid()), characteristic.getProperties(), ((characteristic.getKeySize() - 7) << 12) + characteristic.getPermissions());
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    this.mService.addDescriptor(this.mServerIf, new ParcelUuid(descriptor.getUuid()), ((characteristic.getKeySize() - 7) << 12) + descriptor.getPermissions());
                }
            }
            this.mService.endServiceDeclaration(this.mServerIf);
            return DBG;
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
            this.mService.removeService(this.mServerIf, service.getType(), service.getInstanceId(), new ParcelUuid(service.getUuid()));
            this.mServices.remove(intService);
            return DBG;
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
