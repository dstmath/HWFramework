package android.bluetooth;

import android.bluetooth.IBluetoothCallback;
import android.bluetooth.IBluetoothSocketManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.os.ResultReceiver;

public interface IBluetooth extends IInterface {

    public static abstract class Stub extends Binder implements IBluetooth {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetooth";
        static final int TRANSACTION_cancelBondProcess = 28;
        static final int TRANSACTION_cancelDiscovery = 20;
        static final int TRANSACTION_createBond = 26;
        static final int TRANSACTION_createBondOutOfBand = 27;
        static final int TRANSACTION_disable = 5;
        static final int TRANSACTION_disableRadio = 8;
        static final int TRANSACTION_enable = 3;
        static final int TRANSACTION_enableNoAutoConnect = 4;
        static final int TRANSACTION_enableRadio = 7;
        static final int TRANSACTION_factoryReset = 58;
        static final int TRANSACTION_fetchRemoteUuids = 40;
        static final int TRANSACTION_getAdapterConnectionState = 23;
        static final int TRANSACTION_getAddress = 9;
        static final int TRANSACTION_getBatteryLevel = 42;
        static final int TRANSACTION_getBluetoothClass = 13;
        static final int TRANSACTION_getBondState = 30;
        static final int TRANSACTION_getBondedDevices = 25;
        static final int TRANSACTION_getConnectionState = 33;
        static final int TRANSACTION_getDiscoverableTimeout = 17;
        static final int TRANSACTION_getDiscoveryEndMillis = 22;
        static final int TRANSACTION_getLeMaximumAdvertisingDataLength = 67;
        static final int TRANSACTION_getMaxConnectedAudioDevices = 43;
        static final int TRANSACTION_getMessageAccessPermission = 49;
        static final int TRANSACTION_getName = 12;
        static final int TRANSACTION_getPhonebookAccessPermission = 47;
        static final int TRANSACTION_getProfileConnectionState = 24;
        static final int TRANSACTION_getRemoteAlias = 36;
        static final int TRANSACTION_getRemoteClass = 38;
        static final int TRANSACTION_getRemoteName = 34;
        static final int TRANSACTION_getRemoteType = 35;
        static final int TRANSACTION_getRemoteUuids = 39;
        static final int TRANSACTION_getScanMode = 15;
        static final int TRANSACTION_getSimAccessPermission = 51;
        static final int TRANSACTION_getSocketManager = 56;
        static final int TRANSACTION_getState = 2;
        static final int TRANSACTION_getSupportedProfiles = 32;
        static final int TRANSACTION_getUuids = 10;
        static final int TRANSACTION_isActivityAndEnergyReportingSupported = 62;
        static final int TRANSACTION_isBondingInitiatedLocally = 31;
        static final int TRANSACTION_isDiscovering = 21;
        static final int TRANSACTION_isEnabled = 1;
        static final int TRANSACTION_isLe2MPhySupported = 63;
        static final int TRANSACTION_isLeCodedPhySupported = 64;
        static final int TRANSACTION_isLeExtendedAdvertisingSupported = 65;
        static final int TRANSACTION_isLePeriodicAdvertisingSupported = 66;
        static final int TRANSACTION_isMultiAdvertisementSupported = 59;
        static final int TRANSACTION_isOffloadedFilteringSupported = 60;
        static final int TRANSACTION_isOffloadedScanBatchingSupported = 61;
        static final int TRANSACTION_isRadioEnabled = 6;
        static final int TRANSACTION_onBrEdrDown = 71;
        static final int TRANSACTION_onLeServiceUp = 70;
        static final int TRANSACTION_registerCallback = 54;
        static final int TRANSACTION_removeBond = 29;
        static final int TRANSACTION_reportActivityInfo = 68;
        static final int TRANSACTION_requestActivityInfo = 69;
        static final int TRANSACTION_sdpSearch = 41;
        static final int TRANSACTION_sendConnectionStateChange = 53;
        static final int TRANSACTION_setBluetoothClass = 14;
        static final int TRANSACTION_setDiscoverableTimeout = 18;
        static final int TRANSACTION_setMessageAccessPermission = 50;
        static final int TRANSACTION_setName = 11;
        static final int TRANSACTION_setPairingConfirmation = 46;
        static final int TRANSACTION_setPasskey = 45;
        static final int TRANSACTION_setPhonebookAccessPermission = 48;
        static final int TRANSACTION_setPin = 44;
        static final int TRANSACTION_setRemoteAlias = 37;
        static final int TRANSACTION_setScanMode = 16;
        static final int TRANSACTION_setSimAccessPermission = 52;
        static final int TRANSACTION_shouldRefuseConn = 57;
        static final int TRANSACTION_startDiscovery = 19;
        static final int TRANSACTION_unregisterCallback = 55;

        private static class Proxy implements IBluetooth {
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

            public boolean isEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableNoAutoConnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRadioEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableRadio() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean disableRadio() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAddress() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelUuid[] getUuids() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return (ParcelUuid[]) _reply.createTypedArray(ParcelUuid.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setName(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public BluetoothClass getBluetoothClass() throws RemoteException {
                BluetoothClass _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BluetoothClass.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setBluetoothClass(BluetoothClass bluetoothClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (bluetoothClass != null) {
                        _data.writeInt(1);
                        bluetoothClass.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getScanMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setScanMode(int mode, int duration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeInt(duration);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDiscoverableTimeout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setDiscoverableTimeout(int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(timeout);
                    boolean _result = false;
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startDiscovery() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean cancelDiscovery() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDiscovering() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getDiscoveryEndMillis() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAdapterConnectionState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getProfileConnectionState(int profile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(profile);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public BluetoothDevice[] getBondedDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return (BluetoothDevice[]) _reply.createTypedArray(BluetoothDevice.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean createBond(BluetoothDevice device, int transport) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(transport);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean createBondOutOfBand(BluetoothDevice device, int transport, OobData oobData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(transport);
                    if (oobData != null) {
                        _data.writeInt(1);
                        oobData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean cancelBondProcess(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean removeBond(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBondState(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isBondingInitiatedLocally(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getSupportedProfiles() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getConnectionState(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getRemoteName(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRemoteType(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getRemoteAlias(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setRemoteAlias(BluetoothDevice device, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(name);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRemoteClass(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelUuid[] getRemoteUuids(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    return (ParcelUuid[]) _reply.createTypedArray(ParcelUuid.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean fetchRemoteUuids(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean sdpSearch(BluetoothDevice device, ParcelUuid uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (uuid != null) {
                        _data.writeInt(1);
                        uuid.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getBatteryLevel(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxConnectedAudioDevices() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPin(BluetoothDevice device, boolean accept, int len, byte[] pinCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(accept);
                    _data.writeInt(len);
                    _data.writeByteArray(pinCode);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPasskey(BluetoothDevice device, boolean accept, int len, byte[] passkey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(accept);
                    _data.writeInt(len);
                    _data.writeByteArray(passkey);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPairingConfirmation(BluetoothDevice device, boolean accept) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(accept);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPhonebookAccessPermission(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPhonebookAccessPermission(BluetoothDevice device, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(value);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMessageAccessPermission(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setMessageAccessPermission(BluetoothDevice device, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(value);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSimAccessPermission(BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSimAccessPermission(BluetoothDevice device, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(value);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendConnectionStateChange(BluetoothDevice device, int profile, int state, int prevState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(profile);
                    _data.writeInt(state);
                    _data.writeInt(prevState);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallback(IBluetoothCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallback(IBluetoothCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBluetoothSocketManager getSocketManager() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    return IBluetoothSocketManager.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean shouldRefuseConn(int uid, int pid, long timeStamp, BluetoothDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeLong(timeStamp);
                    boolean _result = true;
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean factoryReset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isMultiAdvertisementSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOffloadedFilteringSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isOffloadedScanBatchingSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isActivityAndEnergyReportingSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isLe2MPhySupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isLeCodedPhySupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isLeExtendedAdvertisingSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isLePeriodicAdvertisingSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getLeMaximumAdvertisingDataLength() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public BluetoothActivityEnergyInfo reportActivityInfo() throws RemoteException {
                BluetoothActivityEnergyInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BluetoothActivityEnergyInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestActivityInfo(ResultReceiver result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(69, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onLeServiceUp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onBrEdrDown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(71, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetooth asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetooth)) {
                return new Proxy(obj);
            }
            return (IBluetooth) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v14, resolved type: android.bluetooth.OobData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v66, resolved type: android.os.ParcelUuid} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v74, resolved type: android.bluetooth.BluetoothDevice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v78, resolved type: android.bluetooth.BluetoothDevice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v82, resolved type: android.bluetooth.BluetoothDevice} */
        /* JADX WARNING: type inference failed for: r1v0 */
        /* JADX WARNING: type inference failed for: r1v3 */
        /* JADX WARNING: type inference failed for: r1v10 */
        /* JADX WARNING: type inference failed for: r1v18 */
        /* JADX WARNING: type inference failed for: r1v22 */
        /* JADX WARNING: type inference failed for: r1v26 */
        /* JADX WARNING: type inference failed for: r1v30 */
        /* JADX WARNING: type inference failed for: r1v34 */
        /* JADX WARNING: type inference failed for: r1v38 */
        /* JADX WARNING: type inference failed for: r1v42 */
        /* JADX WARNING: type inference failed for: r1v46 */
        /* JADX WARNING: type inference failed for: r1v50 */
        /* JADX WARNING: type inference failed for: r1v54 */
        /* JADX WARNING: type inference failed for: r1v58 */
        /* JADX WARNING: type inference failed for: r1v62 */
        /* JADX WARNING: type inference failed for: r1v70 */
        /* JADX WARNING: type inference failed for: r1v86 */
        /* JADX WARNING: type inference failed for: r1v90 */
        /* JADX WARNING: type inference failed for: r1v94 */
        /* JADX WARNING: type inference failed for: r1v98 */
        /* JADX WARNING: type inference failed for: r1v102 */
        /* JADX WARNING: type inference failed for: r1v106 */
        /* JADX WARNING: type inference failed for: r1v110 */
        /* JADX WARNING: type inference failed for: r1v114, types: [android.os.IBinder] */
        /* JADX WARNING: type inference failed for: r1v118 */
        /* JADX WARNING: type inference failed for: r1v121 */
        /* JADX WARNING: type inference failed for: r1v122 */
        /* JADX WARNING: type inference failed for: r1v123 */
        /* JADX WARNING: type inference failed for: r1v124 */
        /* JADX WARNING: type inference failed for: r1v125 */
        /* JADX WARNING: type inference failed for: r1v126 */
        /* JADX WARNING: type inference failed for: r1v127 */
        /* JADX WARNING: type inference failed for: r1v128 */
        /* JADX WARNING: type inference failed for: r1v129 */
        /* JADX WARNING: type inference failed for: r1v130 */
        /* JADX WARNING: type inference failed for: r1v131 */
        /* JADX WARNING: type inference failed for: r1v132 */
        /* JADX WARNING: type inference failed for: r1v133 */
        /* JADX WARNING: type inference failed for: r1v134 */
        /* JADX WARNING: type inference failed for: r1v135 */
        /* JADX WARNING: type inference failed for: r1v136 */
        /* JADX WARNING: type inference failed for: r1v137 */
        /* JADX WARNING: type inference failed for: r1v138 */
        /* JADX WARNING: type inference failed for: r1v139 */
        /* JADX WARNING: type inference failed for: r1v140 */
        /* JADX WARNING: type inference failed for: r1v141 */
        /* JADX WARNING: type inference failed for: r1v142 */
        /* JADX WARNING: type inference failed for: r1v143 */
        /* JADX WARNING: type inference failed for: r1v144 */
        /* JADX WARNING: type inference failed for: r1v145 */
        /* JADX WARNING: type inference failed for: r1v146 */
        /* JADX WARNING: type inference failed for: r1v147 */
        /* JADX WARNING: type inference failed for: r1v148 */
        /* JADX WARNING: type inference failed for: r1v149 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BluetoothDevice _arg0;
            BluetoothDevice _arg02;
            BluetoothDevice _arg3;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg1 = false;
                ? _arg03 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result = isEnabled();
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result2 = getState();
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = enable();
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result4 = enableNoAutoConnect();
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result5 = disable();
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result6 = isRadioEnabled();
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result7 = enableRadio();
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result8 = disableRadio();
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result9 = getAddress();
                        reply.writeNoException();
                        parcel2.writeString(_result9);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParcelUuid[] _result10 = getUuids();
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result10, 1);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result11 = setName(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result12 = getName();
                        reply.writeNoException();
                        parcel2.writeString(_result12);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        BluetoothClass _result13 = getBluetoothClass();
                        reply.writeNoException();
                        if (_result13 != null) {
                            parcel2.writeInt(1);
                            _result13.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothClass.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result14 = setBluetoothClass(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result15 = getScanMode();
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result16 = setScanMode(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result17 = getDiscoverableTimeout();
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result18 = setDiscoverableTimeout(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result19 = startDiscovery();
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result20 = cancelDiscovery();
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result21 = isDiscovering();
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result22 = getDiscoveryEndMillis();
                        reply.writeNoException();
                        parcel2.writeLong(_result22);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result23 = getAdapterConnectionState();
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result24 = getProfileConnectionState(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        BluetoothDevice[] _result25 = getBondedDevices();
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result25, 1);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result26 = createBond(_arg03, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result26);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg03 = OobData.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result27 = createBondOutOfBand(_arg0, _arg12, _arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result27);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result28 = cancelBondProcess(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result28);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result29 = removeBond(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result29);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result30 = getBondState(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result30);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result31 = isBondingInitiatedLocally(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result31);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result32 = getSupportedProfiles();
                        reply.writeNoException();
                        parcel2.writeLong(_result32);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result33 = getConnectionState(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result33);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        String _result34 = getRemoteName(_arg03);
                        reply.writeNoException();
                        parcel2.writeString(_result34);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result35 = getRemoteType(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result35);
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        String _result36 = getRemoteAlias(_arg03);
                        reply.writeNoException();
                        parcel2.writeString(_result36);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result37 = setRemoteAlias(_arg03, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result37);
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result38 = getRemoteClass(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result38);
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        ParcelUuid[] _result39 = getRemoteUuids(_arg03);
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result39, 1);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result40 = fetchRemoteUuids(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result40);
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg03 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result41 = sdpSearch(_arg02, _arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result41);
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result42 = getBatteryLevel(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result42);
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result43 = getMaxConnectedAudioDevices();
                        reply.writeNoException();
                        parcel2.writeInt(_result43);
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result44 = setPin(_arg03, _arg1, data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result44);
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result45 = setPasskey(_arg03, _arg1, data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result45);
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result46 = setPairingConfirmation(_arg03, _arg1);
                        reply.writeNoException();
                        parcel2.writeInt(_result46);
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result47 = getPhonebookAccessPermission(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result47);
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result48 = setPhonebookAccessPermission(_arg03, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result48);
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result49 = getMessageAccessPermission(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result49);
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result50 = setMessageAccessPermission(_arg03, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result50);
                        return true;
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        int _result51 = getSimAccessPermission(_arg03);
                        reply.writeNoException();
                        parcel2.writeInt(_result51);
                        return true;
                    case 52:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result52 = setSimAccessPermission(_arg03, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result52);
                        return true;
                    case 53:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        sendConnectionStateChange(_arg03, data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerCallback(IBluetoothCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 55:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterCallback(IBluetoothCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 56:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBluetoothSocketManager _result53 = getSocketManager();
                        reply.writeNoException();
                        if (_result53 != null) {
                            _arg03 = _result53.asBinder();
                        }
                        parcel2.writeStrongBinder(_arg03);
                        return true;
                    case 57:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        int _arg13 = data.readInt();
                        long _arg2 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg3 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        boolean _result54 = shouldRefuseConn(_arg04, _arg13, _arg2, _arg3);
                        reply.writeNoException();
                        parcel2.writeInt(_result54);
                        return true;
                    case 58:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result55 = factoryReset();
                        reply.writeNoException();
                        parcel2.writeInt(_result55);
                        return true;
                    case 59:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result56 = isMultiAdvertisementSupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result56);
                        return true;
                    case 60:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result57 = isOffloadedFilteringSupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result57);
                        return true;
                    case 61:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result58 = isOffloadedScanBatchingSupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result58);
                        return true;
                    case 62:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result59 = isActivityAndEnergyReportingSupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result59);
                        return true;
                    case 63:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result60 = isLe2MPhySupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result60);
                        return true;
                    case 64:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result61 = isLeCodedPhySupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result61);
                        return true;
                    case 65:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result62 = isLeExtendedAdvertisingSupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result62);
                        return true;
                    case 66:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result63 = isLePeriodicAdvertisingSupported();
                        reply.writeNoException();
                        parcel2.writeInt(_result63);
                        return true;
                    case 67:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result64 = getLeMaximumAdvertisingDataLength();
                        reply.writeNoException();
                        parcel2.writeInt(_result64);
                        return true;
                    case 68:
                        parcel.enforceInterface(DESCRIPTOR);
                        BluetoothActivityEnergyInfo _result65 = reportActivityInfo();
                        reply.writeNoException();
                        if (_result65 != null) {
                            parcel2.writeInt(1);
                            _result65.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 69:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(parcel);
                        }
                        requestActivityInfo(_arg03);
                        return true;
                    case 70:
                        parcel.enforceInterface(DESCRIPTOR);
                        onLeServiceUp();
                        reply.writeNoException();
                        return true;
                    case 71:
                        parcel.enforceInterface(DESCRIPTOR);
                        onBrEdrDown();
                        reply.writeNoException();
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

    boolean cancelBondProcess(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean cancelDiscovery() throws RemoteException;

    boolean createBond(BluetoothDevice bluetoothDevice, int i) throws RemoteException;

    boolean createBondOutOfBand(BluetoothDevice bluetoothDevice, int i, OobData oobData) throws RemoteException;

    boolean disable() throws RemoteException;

    boolean disableRadio() throws RemoteException;

    boolean enable() throws RemoteException;

    boolean enableNoAutoConnect() throws RemoteException;

    boolean enableRadio() throws RemoteException;

    boolean factoryReset() throws RemoteException;

    boolean fetchRemoteUuids(BluetoothDevice bluetoothDevice) throws RemoteException;

    int getAdapterConnectionState() throws RemoteException;

    String getAddress() throws RemoteException;

    int getBatteryLevel(BluetoothDevice bluetoothDevice) throws RemoteException;

    BluetoothClass getBluetoothClass() throws RemoteException;

    int getBondState(BluetoothDevice bluetoothDevice) throws RemoteException;

    BluetoothDevice[] getBondedDevices() throws RemoteException;

    int getConnectionState(BluetoothDevice bluetoothDevice) throws RemoteException;

    int getDiscoverableTimeout() throws RemoteException;

    long getDiscoveryEndMillis() throws RemoteException;

    int getLeMaximumAdvertisingDataLength() throws RemoteException;

    int getMaxConnectedAudioDevices() throws RemoteException;

    int getMessageAccessPermission(BluetoothDevice bluetoothDevice) throws RemoteException;

    String getName() throws RemoteException;

    int getPhonebookAccessPermission(BluetoothDevice bluetoothDevice) throws RemoteException;

    int getProfileConnectionState(int i) throws RemoteException;

    String getRemoteAlias(BluetoothDevice bluetoothDevice) throws RemoteException;

    int getRemoteClass(BluetoothDevice bluetoothDevice) throws RemoteException;

    String getRemoteName(BluetoothDevice bluetoothDevice) throws RemoteException;

    int getRemoteType(BluetoothDevice bluetoothDevice) throws RemoteException;

    ParcelUuid[] getRemoteUuids(BluetoothDevice bluetoothDevice) throws RemoteException;

    int getScanMode() throws RemoteException;

    int getSimAccessPermission(BluetoothDevice bluetoothDevice) throws RemoteException;

    IBluetoothSocketManager getSocketManager() throws RemoteException;

    int getState() throws RemoteException;

    long getSupportedProfiles() throws RemoteException;

    ParcelUuid[] getUuids() throws RemoteException;

    boolean isActivityAndEnergyReportingSupported() throws RemoteException;

    boolean isBondingInitiatedLocally(BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean isDiscovering() throws RemoteException;

    boolean isEnabled() throws RemoteException;

    boolean isLe2MPhySupported() throws RemoteException;

    boolean isLeCodedPhySupported() throws RemoteException;

    boolean isLeExtendedAdvertisingSupported() throws RemoteException;

    boolean isLePeriodicAdvertisingSupported() throws RemoteException;

    boolean isMultiAdvertisementSupported() throws RemoteException;

    boolean isOffloadedFilteringSupported() throws RemoteException;

    boolean isOffloadedScanBatchingSupported() throws RemoteException;

    boolean isRadioEnabled() throws RemoteException;

    void onBrEdrDown() throws RemoteException;

    void onLeServiceUp() throws RemoteException;

    void registerCallback(IBluetoothCallback iBluetoothCallback) throws RemoteException;

    boolean removeBond(BluetoothDevice bluetoothDevice) throws RemoteException;

    BluetoothActivityEnergyInfo reportActivityInfo() throws RemoteException;

    void requestActivityInfo(ResultReceiver resultReceiver) throws RemoteException;

    boolean sdpSearch(BluetoothDevice bluetoothDevice, ParcelUuid parcelUuid) throws RemoteException;

    void sendConnectionStateChange(BluetoothDevice bluetoothDevice, int i, int i2, int i3) throws RemoteException;

    boolean setBluetoothClass(BluetoothClass bluetoothClass) throws RemoteException;

    boolean setDiscoverableTimeout(int i) throws RemoteException;

    boolean setMessageAccessPermission(BluetoothDevice bluetoothDevice, int i) throws RemoteException;

    boolean setName(String str) throws RemoteException;

    boolean setPairingConfirmation(BluetoothDevice bluetoothDevice, boolean z) throws RemoteException;

    boolean setPasskey(BluetoothDevice bluetoothDevice, boolean z, int i, byte[] bArr) throws RemoteException;

    boolean setPhonebookAccessPermission(BluetoothDevice bluetoothDevice, int i) throws RemoteException;

    boolean setPin(BluetoothDevice bluetoothDevice, boolean z, int i, byte[] bArr) throws RemoteException;

    boolean setRemoteAlias(BluetoothDevice bluetoothDevice, String str) throws RemoteException;

    boolean setScanMode(int i, int i2) throws RemoteException;

    boolean setSimAccessPermission(BluetoothDevice bluetoothDevice, int i) throws RemoteException;

    boolean shouldRefuseConn(int i, int i2, long j, BluetoothDevice bluetoothDevice) throws RemoteException;

    boolean startDiscovery() throws RemoteException;

    void unregisterCallback(IBluetoothCallback iBluetoothCallback) throws RemoteException;
}
