package android.bluetooth;

import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.os.WorkSource;
import java.util.List;

public interface IBluetoothGatt extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothGatt {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothGatt";
        static final int TRANSACTION_addCharacteristic = 33;
        static final int TRANSACTION_addDescriptor = 34;
        static final int TRANSACTION_addIncludedService = 32;
        static final int TRANSACTION_beginReliableWrite = 22;
        static final int TRANSACTION_beginServiceDeclaration = 31;
        static final int TRANSACTION_clearServices = 37;
        static final int TRANSACTION_clientConnect = 12;
        static final int TRANSACTION_clientDisconnect = 14;
        static final int TRANSACTION_configureMTU = 25;
        static final int TRANSACTION_connectionParameterUpdate = 26;
        static final int TRANSACTION_disconnectAll = 40;
        static final int TRANSACTION_discoverServices = 16;
        static final int TRANSACTION_endReliableWrite = 23;
        static final int TRANSACTION_endServiceDeclaration = 35;
        static final int TRANSACTION_fastClientConnect = 13;
        static final int TRANSACTION_flushPendingBatchResults = 5;
        static final int TRANSACTION_getDevicesMatchingConnectionStates = 1;
        static final int TRANSACTION_numHwTrackFiltersAvailable = 42;
        static final int TRANSACTION_readCharacteristic = 17;
        static final int TRANSACTION_readDescriptor = 19;
        static final int TRANSACTION_readRemoteRssi = 24;
        static final int TRANSACTION_refreshDevice = 15;
        static final int TRANSACTION_registerClient = 10;
        static final int TRANSACTION_registerForNotification = 21;
        static final int TRANSACTION_registerServer = 27;
        static final int TRANSACTION_removeService = 36;
        static final int TRANSACTION_sendNotification = 39;
        static final int TRANSACTION_sendResponse = 38;
        static final int TRANSACTION_serverConnect = 29;
        static final int TRANSACTION_serverDisconnect = 30;
        static final int TRANSACTION_startMultiAdvertising = 6;
        static final int TRANSACTION_startScan = 2;
        static final int TRANSACTION_stopMultiAdvertising = 7;
        static final int TRANSACTION_stopScan = 3;
        static final int TRANSACTION_unregAll = 41;
        static final int TRANSACTION_unregisterClient = 11;
        static final int TRANSACTION_unregisterServer = 28;
        static final int TRANSACTION_updateAdvertiseData = 9;
        static final int TRANSACTION_updateAdvertiseInterval = 8;
        static final int TRANSACTION_updateScanParams = 4;
        static final int TRANSACTION_writeCharacteristic = 18;
        static final int TRANSACTION_writeDescriptor = 20;

        private static class Proxy implements IBluetoothGatt {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(states);
                    this.mRemote.transact(Stub.TRANSACTION_getDevicesMatchingConnectionStates, _data, _reply, 0);
                    _reply.readException();
                    List<BluetoothDevice> _result = _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startScan(int appIf, boolean isServer, ScanSettings settings, List<ScanFilter> filters, WorkSource workSource, List scanStorages, String callingPackage) throws RemoteException {
                int i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    if (!isServer) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (settings != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        settings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(filters);
                    if (workSource != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeList(scanStorages);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_startScan, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopScan(int appIf, boolean isServer) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    if (isServer) {
                        i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_stopScan, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateScanParams(int appIf, boolean isServer, int window, int interval) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    if (isServer) {
                        i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                    }
                    _data.writeInt(i);
                    _data.writeInt(window);
                    _data.writeInt(interval);
                    this.mRemote.transact(Stub.TRANSACTION_updateScanParams, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void flushPendingBatchResults(int appIf, boolean isServer) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    if (isServer) {
                        i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_flushPendingBatchResults, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startMultiAdvertising(int appIf, AdvertiseData advertiseData, AdvertiseData scanResponse, AdvertiseSettings settings) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    if (advertiseData != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        advertiseData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (scanResponse != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        scanResponse.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (settings != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        settings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startMultiAdvertising, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopMultiAdvertising(int appIf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    this.mRemote.transact(Stub.TRANSACTION_stopMultiAdvertising, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAdvertiseInterval(int appIf, int interval) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    _data.writeInt(interval);
                    this.mRemote.transact(Stub.TRANSACTION_updateAdvertiseInterval, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAdvertiseData(int appIf, AdvertiseData advertiseData, boolean isScanResponse) throws RemoteException {
                int i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    if (advertiseData != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        advertiseData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!isScanResponse) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_updateAdvertiseData, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerClient(ParcelUuid appId, IBluetoothGattCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (appId != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        appId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerClient, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterClient(int clientIf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterClient, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clientConnect(int clientIf, String address, boolean isDirect, int transport) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    if (isDirect) {
                        i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                    }
                    _data.writeInt(i);
                    _data.writeInt(transport);
                    this.mRemote.transact(Stub.TRANSACTION_clientConnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fastClientConnect(int clientIf, String address, boolean isDirect, int transport) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    if (isDirect) {
                        i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                    }
                    _data.writeInt(i);
                    _data.writeInt(transport);
                    this.mRemote.transact(Stub.TRANSACTION_fastClientConnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clientDisconnect(int clientIf, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    this.mRemote.transact(Stub.TRANSACTION_clientDisconnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshDevice(int clientIf, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    this.mRemote.transact(Stub.TRANSACTION_refreshDevice, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void discoverServices(int clientIf, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    this.mRemote.transact(Stub.TRANSACTION_discoverServices, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void readCharacteristic(int clientIf, String address, int handle, int authReq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeInt(authReq);
                    this.mRemote.transact(Stub.TRANSACTION_readCharacteristic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void writeCharacteristic(int clientIf, String address, int handle, int writeType, int authReq, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeInt(writeType);
                    _data.writeInt(authReq);
                    _data.writeByteArray(value);
                    this.mRemote.transact(Stub.TRANSACTION_writeCharacteristic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void readDescriptor(int clientIf, String address, int handle, int authReq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeInt(authReq);
                    this.mRemote.transact(Stub.TRANSACTION_readDescriptor, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void writeDescriptor(int clientIf, String address, int handle, int writeType, int authReq, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeInt(writeType);
                    _data.writeInt(authReq);
                    _data.writeByteArray(value);
                    this.mRemote.transact(Stub.TRANSACTION_writeDescriptor, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerForNotification(int clientIf, String address, int handle, boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    if (enable) {
                        i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_registerForNotification, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void beginReliableWrite(int clientIf, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    this.mRemote.transact(Stub.TRANSACTION_beginReliableWrite, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endReliableWrite(int clientIf, String address, boolean execute) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    if (execute) {
                        i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_endReliableWrite, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void readRemoteRssi(int clientIf, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    this.mRemote.transact(Stub.TRANSACTION_readRemoteRssi, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void configureMTU(int clientIf, String address, int mtu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(mtu);
                    this.mRemote.transact(Stub.TRANSACTION_configureMTU, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void connectionParameterUpdate(int clientIf, String address, int connectionPriority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(connectionPriority);
                    this.mRemote.transact(Stub.TRANSACTION_connectionParameterUpdate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerServer(ParcelUuid appId, IBluetoothGattServerCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (appId != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        appId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerServer, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterServer(int serverIf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterServer, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serverConnect(int servertIf, String address, boolean isDirect, int transport) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(servertIf);
                    _data.writeString(address);
                    if (isDirect) {
                        i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                    }
                    _data.writeInt(i);
                    _data.writeInt(transport);
                    this.mRemote.transact(Stub.TRANSACTION_serverConnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serverDisconnect(int serverIf, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    _data.writeString(address);
                    this.mRemote.transact(Stub.TRANSACTION_serverDisconnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void beginServiceDeclaration(int serverIf, int srvcType, int srvcInstanceId, int minHandles, ParcelUuid srvcId, boolean advertisePreferred) throws RemoteException {
                int i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    _data.writeInt(srvcType);
                    _data.writeInt(srvcInstanceId);
                    _data.writeInt(minHandles);
                    if (srvcId != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        srvcId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!advertisePreferred) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_beginServiceDeclaration, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addIncludedService(int serverIf, int srvcType, int srvcInstanceId, ParcelUuid srvcId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    _data.writeInt(srvcType);
                    _data.writeInt(srvcInstanceId);
                    if (srvcId != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        srvcId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addIncludedService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addCharacteristic(int serverIf, ParcelUuid charId, int properties, int permissions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    if (charId != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        charId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(properties);
                    _data.writeInt(permissions);
                    this.mRemote.transact(Stub.TRANSACTION_addCharacteristic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addDescriptor(int serverIf, ParcelUuid descId, int permissions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    if (descId != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        descId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(permissions);
                    this.mRemote.transact(Stub.TRANSACTION_addDescriptor, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endServiceDeclaration(int serverIf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    this.mRemote.transact(Stub.TRANSACTION_endServiceDeclaration, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeService(int serverIf, int srvcType, int srvcInstanceId, ParcelUuid srvcId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    _data.writeInt(srvcType);
                    _data.writeInt(srvcInstanceId);
                    if (srvcId != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        srvcId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_removeService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearServices(int serverIf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    this.mRemote.transact(Stub.TRANSACTION_clearServices, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendResponse(int serverIf, String address, int requestId, int status, int offset, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    _data.writeString(address);
                    _data.writeInt(requestId);
                    _data.writeInt(status);
                    _data.writeInt(offset);
                    _data.writeByteArray(value);
                    this.mRemote.transact(Stub.TRANSACTION_sendResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendNotification(int serverIf, String address, int srvcType, int srvcInstanceId, ParcelUuid srvcId, int charInstanceId, ParcelUuid charId, boolean confirm, byte[] value) throws RemoteException {
                int i = Stub.TRANSACTION_getDevicesMatchingConnectionStates;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    _data.writeString(address);
                    _data.writeInt(srvcType);
                    _data.writeInt(srvcInstanceId);
                    if (srvcId != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        srvcId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(charInstanceId);
                    if (charId != null) {
                        _data.writeInt(Stub.TRANSACTION_getDevicesMatchingConnectionStates);
                        charId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!confirm) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeByteArray(value);
                    this.mRemote.transact(Stub.TRANSACTION_sendNotification, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disconnectAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_disconnectAll, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_unregAll, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int numHwTrackFiltersAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_numHwTrackFiltersAvailable, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothGatt asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothGatt)) {
                return new Proxy(obj);
            }
            return (IBluetoothGatt) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            AdvertiseData advertiseData;
            ParcelUuid parcelUuid;
            int _arg1;
            int _arg2;
            int _arg3;
            ParcelUuid parcelUuid2;
            ParcelUuid parcelUuid3;
            ParcelUuid parcelUuid4;
            switch (code) {
                case TRANSACTION_getDevicesMatchingConnectionStates /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<BluetoothDevice> _result = getDevicesMatchingConnectionStates(data.createIntArray());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case TRANSACTION_startScan /*2*/:
                    ScanSettings scanSettings;
                    WorkSource workSource;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    boolean _arg12 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        scanSettings = (ScanSettings) ScanSettings.CREATOR.createFromParcel(data);
                    } else {
                        scanSettings = null;
                    }
                    List<ScanFilter> _arg32 = data.createTypedArrayList(ScanFilter.CREATOR);
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    startScan(_arg0, _arg12, scanSettings, _arg32, workSource, data.readArrayList(getClass().getClassLoader()), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopScan /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopScan(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateScanParams /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateScanParams(data.readInt(), data.readInt() != 0, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_flushPendingBatchResults /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    flushPendingBatchResults(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startMultiAdvertising /*6*/:
                    AdvertiseData advertiseData2;
                    AdvertiseSettings advertiseSettings;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        advertiseData = (AdvertiseData) AdvertiseData.CREATOR.createFromParcel(data);
                    } else {
                        advertiseData = null;
                    }
                    if (data.readInt() != 0) {
                        advertiseData2 = (AdvertiseData) AdvertiseData.CREATOR.createFromParcel(data);
                    } else {
                        advertiseData2 = null;
                    }
                    if (data.readInt() != 0) {
                        advertiseSettings = (AdvertiseSettings) AdvertiseSettings.CREATOR.createFromParcel(data);
                    } else {
                        advertiseSettings = null;
                    }
                    startMultiAdvertising(_arg0, advertiseData, advertiseData2, advertiseSettings);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopMultiAdvertising /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopMultiAdvertising(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateAdvertiseInterval /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateAdvertiseInterval(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateAdvertiseData /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        advertiseData = (AdvertiseData) AdvertiseData.CREATOR.createFromParcel(data);
                    } else {
                        advertiseData = null;
                    }
                    updateAdvertiseData(_arg0, advertiseData, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerClient /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelUuid = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid = null;
                    }
                    registerClient(parcelUuid, android.bluetooth.IBluetoothGattCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterClient /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterClient(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clientConnect /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    clientConnect(data.readInt(), data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_fastClientConnect /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    fastClientConnect(data.readInt(), data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clientDisconnect /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    clientDisconnect(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_refreshDevice /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    refreshDevice(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_discoverServices /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    discoverServices(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_readCharacteristic /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    readCharacteristic(data.readInt(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_writeCharacteristic /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    writeCharacteristic(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_readDescriptor /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    readDescriptor(data.readInt(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_writeDescriptor /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    writeDescriptor(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerForNotification /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerForNotification(data.readInt(), data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_beginReliableWrite /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    beginReliableWrite(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_endReliableWrite /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    endReliableWrite(data.readInt(), data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_readRemoteRssi /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    readRemoteRssi(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_configureMTU /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    configureMTU(data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_connectionParameterUpdate /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    connectionParameterUpdate(data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_registerServer /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelUuid = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid = null;
                    }
                    registerServer(parcelUuid, android.bluetooth.IBluetoothGattServerCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterServer /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterServer(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_serverConnect /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    serverConnect(data.readInt(), data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_serverDisconnect /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    serverDisconnect(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_beginServiceDeclaration /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    _arg3 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid2 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid2 = null;
                    }
                    beginServiceDeclaration(_arg0, _arg1, _arg2, _arg3, parcelUuid2, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addIncludedService /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid3 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid3 = null;
                    }
                    addIncludedService(_arg0, _arg1, _arg2, parcelUuid3);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addCharacteristic /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid4 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid4 = null;
                    }
                    addCharacteristic(_arg0, parcelUuid4, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addDescriptor /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid4 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid4 = null;
                    }
                    addDescriptor(_arg0, parcelUuid4, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_endServiceDeclaration /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    endServiceDeclaration(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeService /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid3 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid3 = null;
                    }
                    removeService(_arg0, _arg1, _arg2, parcelUuid3);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearServices /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearServices(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendResponse /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendResponse(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendNotification /*39*/:
                    ParcelUuid parcelUuid5;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    String _arg13 = data.readString();
                    _arg2 = data.readInt();
                    _arg3 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid2 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid2 = null;
                    }
                    int _arg5 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid5 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid5 = null;
                    }
                    sendNotification(_arg0, _arg13, _arg2, _arg3, parcelUuid2, _arg5, parcelUuid5, data.readInt() != 0, data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disconnectAll /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    disconnectAll();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregAll /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregAll();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_numHwTrackFiltersAvailable /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = numHwTrackFiltersAvailable();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addCharacteristic(int i, ParcelUuid parcelUuid, int i2, int i3) throws RemoteException;

    void addDescriptor(int i, ParcelUuid parcelUuid, int i2) throws RemoteException;

    void addIncludedService(int i, int i2, int i3, ParcelUuid parcelUuid) throws RemoteException;

    void beginReliableWrite(int i, String str) throws RemoteException;

    void beginServiceDeclaration(int i, int i2, int i3, int i4, ParcelUuid parcelUuid, boolean z) throws RemoteException;

    void clearServices(int i) throws RemoteException;

    void clientConnect(int i, String str, boolean z, int i2) throws RemoteException;

    void clientDisconnect(int i, String str) throws RemoteException;

    void configureMTU(int i, String str, int i2) throws RemoteException;

    void connectionParameterUpdate(int i, String str, int i2) throws RemoteException;

    void disconnectAll() throws RemoteException;

    void discoverServices(int i, String str) throws RemoteException;

    void endReliableWrite(int i, String str, boolean z) throws RemoteException;

    void endServiceDeclaration(int i) throws RemoteException;

    void fastClientConnect(int i, String str, boolean z, int i2) throws RemoteException;

    void flushPendingBatchResults(int i, boolean z) throws RemoteException;

    List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] iArr) throws RemoteException;

    int numHwTrackFiltersAvailable() throws RemoteException;

    void readCharacteristic(int i, String str, int i2, int i3) throws RemoteException;

    void readDescriptor(int i, String str, int i2, int i3) throws RemoteException;

    void readRemoteRssi(int i, String str) throws RemoteException;

    void refreshDevice(int i, String str) throws RemoteException;

    void registerClient(ParcelUuid parcelUuid, IBluetoothGattCallback iBluetoothGattCallback) throws RemoteException;

    void registerForNotification(int i, String str, int i2, boolean z) throws RemoteException;

    void registerServer(ParcelUuid parcelUuid, IBluetoothGattServerCallback iBluetoothGattServerCallback) throws RemoteException;

    void removeService(int i, int i2, int i3, ParcelUuid parcelUuid) throws RemoteException;

    void sendNotification(int i, String str, int i2, int i3, ParcelUuid parcelUuid, int i4, ParcelUuid parcelUuid2, boolean z, byte[] bArr) throws RemoteException;

    void sendResponse(int i, String str, int i2, int i3, int i4, byte[] bArr) throws RemoteException;

    void serverConnect(int i, String str, boolean z, int i2) throws RemoteException;

    void serverDisconnect(int i, String str) throws RemoteException;

    void startMultiAdvertising(int i, AdvertiseData advertiseData, AdvertiseData advertiseData2, AdvertiseSettings advertiseSettings) throws RemoteException;

    void startScan(int i, boolean z, ScanSettings scanSettings, List<ScanFilter> list, WorkSource workSource, List list2, String str) throws RemoteException;

    void stopMultiAdvertising(int i) throws RemoteException;

    void stopScan(int i, boolean z) throws RemoteException;

    void unregAll() throws RemoteException;

    void unregisterClient(int i) throws RemoteException;

    void unregisterServer(int i) throws RemoteException;

    void updateAdvertiseData(int i, AdvertiseData advertiseData, boolean z) throws RemoteException;

    void updateAdvertiseInterval(int i, int i2) throws RemoteException;

    void updateScanParams(int i, boolean z, int i2, int i3) throws RemoteException;

    void writeCharacteristic(int i, String str, int i2, int i3, int i4, byte[] bArr) throws RemoteException;

    void writeDescriptor(int i, String str, int i2, int i3, int i4, byte[] bArr) throws RemoteException;
}
