package android.bluetooth;

import android.app.PendingIntent;
import android.bluetooth.IBluetoothGattCallback;
import android.bluetooth.IBluetoothGattServerCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.IAdvertisingSetCallback;
import android.bluetooth.le.IPeriodicAdvertisingCallback;
import android.bluetooth.le.IScannerCallback;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
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
        static final int TRANSACTION_addService = 54;
        static final int TRANSACTION_beginReliableWrite = 42;
        static final int TRANSACTION_clearServices = 56;
        static final int TRANSACTION_clientConnect = 29;
        static final int TRANSACTION_clientDisconnect = 30;
        static final int TRANSACTION_clientReadPhy = 32;
        static final int TRANSACTION_clientSetPreferredPhy = 31;
        static final int TRANSACTION_configureMTU = 45;
        static final int TRANSACTION_connectionParameterUpdate = 46;
        static final int TRANSACTION_disconnectAll = 59;
        static final int TRANSACTION_discoverServiceByUuid = 35;
        static final int TRANSACTION_discoverServices = 34;
        static final int TRANSACTION_enableAdvertisingSet = 14;
        static final int TRANSACTION_endReliableWrite = 43;
        static final int TRANSACTION_fastClientConnect = 27;
        static final int TRANSACTION_flushPendingBatchResults = 10;
        static final int TRANSACTION_getDevicesMatchingConnectionStates = 1;
        static final int TRANSACTION_getOwnAddress = 13;
        static final int TRANSACTION_leConnectionUpdate = 47;
        static final int TRANSACTION_numHwTrackFiltersAvailable = 61;
        static final int TRANSACTION_readCharacteristic = 36;
        static final int TRANSACTION_readDescriptor = 39;
        static final int TRANSACTION_readRemoteRssi = 44;
        static final int TRANSACTION_readUsingCharacteristicUuid = 37;
        static final int TRANSACTION_refreshDevice = 33;
        static final int TRANSACTION_registerClient = 23;
        static final int TRANSACTION_registerForNotification = 41;
        static final int TRANSACTION_registerScanner = 2;
        static final int TRANSACTION_registerServer = 48;
        static final int TRANSACTION_registerSync = 21;
        static final int TRANSACTION_removeService = 55;
        static final int TRANSACTION_sendNotification = 58;
        static final int TRANSACTION_sendResponse = 57;
        static final int TRANSACTION_serverConnect = 50;
        static final int TRANSACTION_serverDisconnect = 51;
        static final int TRANSACTION_serverReadPhy = 53;
        static final int TRANSACTION_serverSetPreferredPhy = 52;
        static final int TRANSACTION_setAdvertisingData = 15;
        static final int TRANSACTION_setAdvertisingParameters = 17;
        static final int TRANSACTION_setPeriodicAdvertisingData = 19;
        static final int TRANSACTION_setPeriodicAdvertisingEnable = 20;
        static final int TRANSACTION_setPeriodicAdvertisingParameters = 18;
        static final int TRANSACTION_setScanResponseData = 16;
        static final int TRANSACTION_startAdvertisingSet = 11;
        static final int TRANSACTION_startScan = 4;
        static final int TRANSACTION_startScanByPkg = 7;
        static final int TRANSACTION_startScanForIntent = 5;
        static final int TRANSACTION_stopAdvertisingSet = 12;
        static final int TRANSACTION_stopScan = 9;
        static final int TRANSACTION_stopScanByPkg = 8;
        static final int TRANSACTION_stopScanForIntent = 6;
        static final int TRANSACTION_unregAll = 60;
        static final int TRANSACTION_unregisterClient = 28;
        static final int TRANSACTION_unregisterScanner = 3;
        static final int TRANSACTION_unregisterServer = 49;
        static final int TRANSACTION_unregisterSync = 22;
        static final int TRANSACTION_updateAdvertiseData = 26;
        static final int TRANSACTION_updateAdvertiseInterval = 25;
        static final int TRANSACTION_updateScanParams = 24;
        static final int TRANSACTION_writeCharacteristic = 38;
        static final int TRANSACTION_writeDescriptor = 40;

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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(BluetoothDevice.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerScanner(IScannerCallback callback, WorkSource workSource) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (workSource != null) {
                        _data.writeInt(1);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterScanner(int scannerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scannerId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startScan(int scannerId, ScanSettings settings, List<ScanFilter> filters, List scanStorages, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scannerId);
                    if (settings != null) {
                        _data.writeInt(1);
                        settings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(filters);
                    _data.writeList(scanStorages);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startScanForIntent(PendingIntent intent, ScanSettings settings, List<ScanFilter> filters, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (settings != null) {
                        _data.writeInt(1);
                        settings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(filters);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopScanForIntent(PendingIntent intent, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startScanByPkg(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopScanByPkg(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopScan(int scannerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scannerId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void flushPendingBatchResults(int scannerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scannerId);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startAdvertisingSet(AdvertisingSetParameters parameters, AdvertiseData advertiseData, AdvertiseData scanResponse, PeriodicAdvertisingParameters periodicParameters, AdvertiseData periodicData, int duration, int maxExtAdvEvents, IAdvertisingSetCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (parameters != null) {
                        _data.writeInt(1);
                        parameters.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (advertiseData != null) {
                        _data.writeInt(1);
                        advertiseData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (scanResponse != null) {
                        _data.writeInt(1);
                        scanResponse.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (periodicParameters != null) {
                        _data.writeInt(1);
                        periodicParameters.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (periodicData != null) {
                        _data.writeInt(1);
                        periodicData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(duration);
                    _data.writeInt(maxExtAdvEvents);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopAdvertisingSet(IAdvertisingSetCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getOwnAddress(int advertiserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableAdvertisingSet(int advertiserId, boolean enable, int duration, int maxExtAdvEvents) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    _data.writeInt(enable);
                    _data.writeInt(duration);
                    _data.writeInt(maxExtAdvEvents);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAdvertisingData(int advertiserId, AdvertiseData data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setScanResponseData(int advertiserId, AdvertiseData data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAdvertisingParameters(int advertiserId, AdvertisingSetParameters parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    if (parameters != null) {
                        _data.writeInt(1);
                        parameters.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPeriodicAdvertisingParameters(int advertiserId, PeriodicAdvertisingParameters parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    if (parameters != null) {
                        _data.writeInt(1);
                        parameters.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPeriodicAdvertisingData(int advertiserId, AdvertiseData data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPeriodicAdvertisingEnable(int advertiserId, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(advertiserId);
                    _data.writeInt(enable);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerSync(ScanResult scanResult, int skip, int timeout, IPeriodicAdvertisingCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (scanResult != null) {
                        _data.writeInt(1);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(skip);
                    _data.writeInt(timeout);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterSync(IPeriodicAdvertisingCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerClient(ParcelUuid appId, IBluetoothGattCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (appId != null) {
                        _data.writeInt(1);
                        appId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateScanParams(int appIf, boolean isServer, int window, int interval) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    _data.writeInt(isServer);
                    _data.writeInt(window);
                    _data.writeInt(interval);
                    this.mRemote.transact(24, _data, _reply, 0);
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
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateAdvertiseData(int appIf, AdvertiseData advertiseData, boolean isScanResponse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appIf);
                    if (advertiseData != null) {
                        _data.writeInt(1);
                        advertiseData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(isScanResponse);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fastClientConnect(int clientIf, String address, boolean isDirect, int transport) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(isDirect);
                    _data.writeInt(transport);
                    this.mRemote.transact(27, _data, _reply, 0);
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
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clientConnect(int clientIf, String address, boolean isDirect, int transport, boolean opportunistic, int phy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(isDirect);
                    _data.writeInt(transport);
                    _data.writeInt(opportunistic);
                    _data.writeInt(phy);
                    this.mRemote.transact(29, _data, _reply, 0);
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
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clientSetPreferredPhy(int clientIf, String address, int txPhy, int rxPhy, int phyOptions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(txPhy);
                    _data.writeInt(rxPhy);
                    _data.writeInt(phyOptions);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clientReadPhy(int clientIf, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    this.mRemote.transact(32, _data, _reply, 0);
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
                    this.mRemote.transact(33, _data, _reply, 0);
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
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void discoverServiceByUuid(int clientIf, String address, ParcelUuid uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    if (uuid != null) {
                        _data.writeInt(1);
                        uuid.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(35, _data, _reply, 0);
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
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void readUsingCharacteristicUuid(int clientIf, String address, ParcelUuid uuid, int startHandle, int endHandle, int authReq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    if (uuid != null) {
                        _data.writeInt(1);
                        uuid.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(startHandle);
                    _data.writeInt(endHandle);
                    _data.writeInt(authReq);
                    this.mRemote.transact(37, _data, _reply, 0);
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
                    this.mRemote.transact(38, _data, _reply, 0);
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
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void writeDescriptor(int clientIf, String address, int handle, int authReq, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeInt(authReq);
                    _data.writeByteArray(value);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerForNotification(int clientIf, String address, int handle, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeInt(enable);
                    this.mRemote.transact(41, _data, _reply, 0);
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
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endReliableWrite(int clientIf, String address, boolean execute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(execute);
                    this.mRemote.transact(43, _data, _reply, 0);
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
                    this.mRemote.transact(44, _data, _reply, 0);
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
                    this.mRemote.transact(45, _data, _reply, 0);
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
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void leConnectionUpdate(int clientIf, String address, int minInterval, int maxInterval, int slaveLatency, int supervisionTimeout, int minConnectionEventLen, int maxConnectionEventLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(minInterval);
                    _data.writeInt(maxInterval);
                    _data.writeInt(slaveLatency);
                    _data.writeInt(supervisionTimeout);
                    _data.writeInt(minConnectionEventLen);
                    _data.writeInt(maxConnectionEventLen);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerServer(ParcelUuid appId, IBluetoothGattServerCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (appId != null) {
                        _data.writeInt(1);
                        appId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(48, _data, _reply, 0);
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
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serverConnect(int serverIf, String address, boolean isDirect, int transport) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    _data.writeString(address);
                    _data.writeInt(isDirect);
                    _data.writeInt(transport);
                    this.mRemote.transact(50, _data, _reply, 0);
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
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serverSetPreferredPhy(int clientIf, String address, int txPhy, int rxPhy, int phyOptions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    _data.writeInt(txPhy);
                    _data.writeInt(rxPhy);
                    _data.writeInt(phyOptions);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void serverReadPhy(int clientIf, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(clientIf);
                    _data.writeString(address);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addService(int serverIf, BluetoothGattService service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeService(int serverIf, int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    _data.writeInt(handle);
                    this.mRemote.transact(55, _data, _reply, 0);
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
                    this.mRemote.transact(56, _data, _reply, 0);
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
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendNotification(int serverIf, String address, int handle, boolean confirm, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serverIf);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeInt(confirm);
                    _data.writeByteArray(value);
                    this.mRemote.transact(58, _data, _reply, 0);
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
                    this.mRemote.transact(59, _data, _reply, 0);
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
                    this.mRemote.transact(60, _data, _reply, 0);
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
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v1, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.os.WorkSource} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v5, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: android.bluetooth.le.ScanSettings} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v8, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v20, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v11, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v12, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v13, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v16, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v19, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v24, resolved type: android.bluetooth.le.AdvertisingSetParameters} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v22, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v28, resolved type: android.bluetooth.le.PeriodicAdvertisingParameters} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v25, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v28, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v62, resolved type: android.bluetooth.le.ScanResult} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v31, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v68, resolved type: android.os.ParcelUuid} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v34, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v41, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v22, resolved type: android.os.ParcelUuid} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v48, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v105, resolved type: android.os.ParcelUuid} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v52, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v71, resolved type: android.bluetooth.BluetoothGattService} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v57, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v58, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v59, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v60, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v61, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v62, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v63, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v64, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v65, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v66, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v67, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v68, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v69, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v70, resolved type: android.bluetooth.le.AdvertiseData} */
        /* JADX WARNING: type inference failed for: r5v3, types: [android.os.WorkSource] */
        /* JADX WARNING: type inference failed for: r5v7, types: [android.bluetooth.le.ScanSettings] */
        /* JADX WARNING: type inference failed for: r5v10, types: [android.app.PendingIntent] */
        /* JADX WARNING: type inference failed for: r5v21, types: [android.bluetooth.le.AdvertisingSetParameters] */
        /* JADX WARNING: type inference failed for: r5v24, types: [android.bluetooth.le.PeriodicAdvertisingParameters] */
        /* JADX WARNING: type inference failed for: r5v30, types: [android.bluetooth.le.ScanResult] */
        /* JADX WARNING: type inference failed for: r5v33, types: [android.os.ParcelUuid] */
        /* JADX WARNING: type inference failed for: r5v43, types: [android.os.ParcelUuid] */
        /* JADX WARNING: type inference failed for: r5v50, types: [android.os.ParcelUuid] */
        /* JADX WARNING: type inference failed for: r5v54, types: [android.bluetooth.BluetoothGattService] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ScanSettings _arg1;
            PendingIntent _arg0;
            AdvertisingSetParameters _arg02;
            AdvertiseData _arg12;
            AdvertiseData _arg2;
            PeriodicAdvertisingParameters _arg3;
            ParcelUuid _arg22;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg23 = false;
                AdvertiseData _arg13 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<BluetoothDevice> _result = getDevicesMatchingConnectionStates(data.createIntArray());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        IScannerCallback _arg03 = IScannerCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg13 = (WorkSource) WorkSource.CREATOR.createFromParcel(parcel);
                        }
                        registerScanner(_arg03, _arg13);
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterScanner(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ScanSettings.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        startScan(_arg04, _arg1, parcel.createTypedArrayList(ScanFilter.CREATOR), parcel.readArrayList(getClass().getClassLoader()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PendingIntent.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = ScanSettings.CREATOR.createFromParcel(parcel);
                        }
                        startScanForIntent(_arg0, _arg13, parcel.createTypedArrayList(ScanFilter.CREATOR), data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg13 = PendingIntent.CREATOR.createFromParcel(parcel);
                        }
                        stopScanForIntent(_arg13, data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        startScanByPkg(data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopScanByPkg(data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopScan(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        flushPendingBatchResults(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = AdvertisingSetParameters.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = AdvertiseData.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = AdvertiseData.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = PeriodicAdvertisingParameters.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = AdvertiseData.CREATOR.createFromParcel(parcel);
                        }
                        startAdvertisingSet(_arg02, _arg12, _arg2, _arg3, _arg13, data.readInt(), data.readInt(), IAdvertisingSetCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        stopAdvertisingSet(IAdvertisingSetCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        getOwnAddress(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        enableAdvertisingSet(_arg05, _arg23, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = AdvertiseData.CREATOR.createFromParcel(parcel);
                        }
                        setAdvertisingData(_arg06, _arg13);
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = AdvertiseData.CREATOR.createFromParcel(parcel);
                        }
                        setScanResponseData(_arg07, _arg13);
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = AdvertisingSetParameters.CREATOR.createFromParcel(parcel);
                        }
                        setAdvertisingParameters(_arg08, _arg13);
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = PeriodicAdvertisingParameters.CREATOR.createFromParcel(parcel);
                        }
                        setPeriodicAdvertisingParameters(_arg09, _arg13);
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = AdvertiseData.CREATOR.createFromParcel(parcel);
                        }
                        setPeriodicAdvertisingData(_arg010, _arg13);
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        setPeriodicAdvertisingEnable(_arg011, _arg23);
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg13 = ScanResult.CREATOR.createFromParcel(parcel);
                        }
                        registerSync(_arg13, data.readInt(), data.readInt(), IPeriodicAdvertisingCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterSync(IPeriodicAdvertisingCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg13 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(parcel);
                        }
                        registerClient(_arg13, IBluetoothGattCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        updateScanParams(_arg012, _arg23, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateAdvertiseInterval(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg013 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = AdvertiseData.CREATOR.createFromParcel(parcel);
                        }
                        AdvertiseData _arg14 = _arg13;
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        updateAdvertiseData(_arg013, _arg14, _arg23);
                        reply.writeNoException();
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg014 = data.readInt();
                        String _arg15 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        fastClientConnect(_arg014, _arg15, _arg23, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterClient(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        clientConnect(data.readInt(), data.readString(), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        clientDisconnect(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        clientSetPreferredPhy(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        clientReadPhy(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        refreshDevice(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        discoverServices(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg015 = data.readInt();
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(parcel);
                        }
                        discoverServiceByUuid(_arg015, _arg16, _arg13);
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        readCharacteristic(data.readInt(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg016 = data.readInt();
                        String _arg17 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        readUsingCharacteristicUuid(_arg016, _arg17, _arg22, data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        writeCharacteristic(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        readDescriptor(data.readInt(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        writeDescriptor(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg017 = data.readInt();
                        String _arg18 = data.readString();
                        int _arg24 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        registerForNotification(_arg017, _arg18, _arg24, _arg23);
                        reply.writeNoException();
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        beginReliableWrite(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg018 = data.readInt();
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        endReliableWrite(_arg018, _arg19, _arg23);
                        reply.writeNoException();
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        readRemoteRssi(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        configureMTU(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        connectionParameterUpdate(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        leConnectionUpdate(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg13 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(parcel);
                        }
                        registerServer(_arg13, IBluetoothGattServerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterServer(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg019 = data.readInt();
                        String _arg110 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        serverConnect(_arg019, _arg110, _arg23, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        serverDisconnect(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 52:
                        parcel.enforceInterface(DESCRIPTOR);
                        serverSetPreferredPhy(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 53:
                        parcel.enforceInterface(DESCRIPTOR);
                        serverReadPhy(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 54:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg020 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = BluetoothGattService.CREATOR.createFromParcel(parcel);
                        }
                        addService(_arg020, _arg13);
                        reply.writeNoException();
                        return true;
                    case 55:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeService(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 56:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearServices(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 57:
                        parcel.enforceInterface(DESCRIPTOR);
                        sendResponse(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 58:
                        parcel.enforceInterface(DESCRIPTOR);
                        sendNotification(data.readInt(), data.readString(), data.readInt(), data.readInt() != 0, data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 59:
                        parcel.enforceInterface(DESCRIPTOR);
                        disconnectAll();
                        reply.writeNoException();
                        return true;
                    case 60:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregAll();
                        reply.writeNoException();
                        return true;
                    case 61:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result2 = numHwTrackFiltersAvailable();
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void addService(int i, BluetoothGattService bluetoothGattService) throws RemoteException;

    void beginReliableWrite(int i, String str) throws RemoteException;

    void clearServices(int i) throws RemoteException;

    void clientConnect(int i, String str, boolean z, int i2, boolean z2, int i3) throws RemoteException;

    void clientDisconnect(int i, String str) throws RemoteException;

    void clientReadPhy(int i, String str) throws RemoteException;

    void clientSetPreferredPhy(int i, String str, int i2, int i3, int i4) throws RemoteException;

    void configureMTU(int i, String str, int i2) throws RemoteException;

    void connectionParameterUpdate(int i, String str, int i2) throws RemoteException;

    void disconnectAll() throws RemoteException;

    void discoverServiceByUuid(int i, String str, ParcelUuid parcelUuid) throws RemoteException;

    void discoverServices(int i, String str) throws RemoteException;

    void enableAdvertisingSet(int i, boolean z, int i2, int i3) throws RemoteException;

    void endReliableWrite(int i, String str, boolean z) throws RemoteException;

    void fastClientConnect(int i, String str, boolean z, int i2) throws RemoteException;

    void flushPendingBatchResults(int i) throws RemoteException;

    List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] iArr) throws RemoteException;

    void getOwnAddress(int i) throws RemoteException;

    void leConnectionUpdate(int i, String str, int i2, int i3, int i4, int i5, int i6, int i7) throws RemoteException;

    int numHwTrackFiltersAvailable() throws RemoteException;

    void readCharacteristic(int i, String str, int i2, int i3) throws RemoteException;

    void readDescriptor(int i, String str, int i2, int i3) throws RemoteException;

    void readRemoteRssi(int i, String str) throws RemoteException;

    void readUsingCharacteristicUuid(int i, String str, ParcelUuid parcelUuid, int i2, int i3, int i4) throws RemoteException;

    void refreshDevice(int i, String str) throws RemoteException;

    void registerClient(ParcelUuid parcelUuid, IBluetoothGattCallback iBluetoothGattCallback) throws RemoteException;

    void registerForNotification(int i, String str, int i2, boolean z) throws RemoteException;

    void registerScanner(IScannerCallback iScannerCallback, WorkSource workSource) throws RemoteException;

    void registerServer(ParcelUuid parcelUuid, IBluetoothGattServerCallback iBluetoothGattServerCallback) throws RemoteException;

    void registerSync(ScanResult scanResult, int i, int i2, IPeriodicAdvertisingCallback iPeriodicAdvertisingCallback) throws RemoteException;

    void removeService(int i, int i2) throws RemoteException;

    void sendNotification(int i, String str, int i2, boolean z, byte[] bArr) throws RemoteException;

    void sendResponse(int i, String str, int i2, int i3, int i4, byte[] bArr) throws RemoteException;

    void serverConnect(int i, String str, boolean z, int i2) throws RemoteException;

    void serverDisconnect(int i, String str) throws RemoteException;

    void serverReadPhy(int i, String str) throws RemoteException;

    void serverSetPreferredPhy(int i, String str, int i2, int i3, int i4) throws RemoteException;

    void setAdvertisingData(int i, AdvertiseData advertiseData) throws RemoteException;

    void setAdvertisingParameters(int i, AdvertisingSetParameters advertisingSetParameters) throws RemoteException;

    void setPeriodicAdvertisingData(int i, AdvertiseData advertiseData) throws RemoteException;

    void setPeriodicAdvertisingEnable(int i, boolean z) throws RemoteException;

    void setPeriodicAdvertisingParameters(int i, PeriodicAdvertisingParameters periodicAdvertisingParameters) throws RemoteException;

    void setScanResponseData(int i, AdvertiseData advertiseData) throws RemoteException;

    void startAdvertisingSet(AdvertisingSetParameters advertisingSetParameters, AdvertiseData advertiseData, AdvertiseData advertiseData2, PeriodicAdvertisingParameters periodicAdvertisingParameters, AdvertiseData advertiseData3, int i, int i2, IAdvertisingSetCallback iAdvertisingSetCallback) throws RemoteException;

    void startScan(int i, ScanSettings scanSettings, List<ScanFilter> list, List list2, String str) throws RemoteException;

    void startScanByPkg(String str) throws RemoteException;

    void startScanForIntent(PendingIntent pendingIntent, ScanSettings scanSettings, List<ScanFilter> list, String str) throws RemoteException;

    void stopAdvertisingSet(IAdvertisingSetCallback iAdvertisingSetCallback) throws RemoteException;

    void stopScan(int i) throws RemoteException;

    void stopScanByPkg(String str) throws RemoteException;

    void stopScanForIntent(PendingIntent pendingIntent, String str) throws RemoteException;

    void unregAll() throws RemoteException;

    void unregisterClient(int i) throws RemoteException;

    void unregisterScanner(int i) throws RemoteException;

    void unregisterServer(int i) throws RemoteException;

    void unregisterSync(IPeriodicAdvertisingCallback iPeriodicAdvertisingCallback) throws RemoteException;

    void updateAdvertiseData(int i, AdvertiseData advertiseData, boolean z) throws RemoteException;

    void updateAdvertiseInterval(int i, int i2) throws RemoteException;

    void updateScanParams(int i, boolean z, int i2, int i3) throws RemoteException;

    void writeCharacteristic(int i, String str, int i2, int i3, int i4, byte[] bArr) throws RemoteException;

    void writeDescriptor(int i, String str, int i2, int i3, byte[] bArr) throws RemoteException;
}
