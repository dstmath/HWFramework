package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IBluetoothGattCallback extends IInterface {
    void onCharacteristicRead(String str, int i, int i2, byte[] bArr) throws RemoteException;

    void onCharacteristicWrite(String str, int i, int i2) throws RemoteException;

    void onClientConnectionState(int i, int i2, boolean z, String str) throws RemoteException;

    void onClientRegistered(int i, int i2) throws RemoteException;

    void onConfigureMTU(String str, int i, int i2) throws RemoteException;

    void onConnectionUpdated(String str, int i, int i2, int i3, int i4) throws RemoteException;

    void onDescriptorRead(String str, int i, int i2, byte[] bArr) throws RemoteException;

    void onDescriptorWrite(String str, int i, int i2) throws RemoteException;

    void onExecuteWrite(String str, int i) throws RemoteException;

    void onNotify(String str, int i, byte[] bArr) throws RemoteException;

    void onPhyRead(String str, int i, int i2, int i3) throws RemoteException;

    void onPhyUpdate(String str, int i, int i2, int i3) throws RemoteException;

    void onReadRemoteRssi(String str, int i, int i2) throws RemoteException;

    void onSearchComplete(String str, List<BluetoothGattService> list, int i) throws RemoteException;

    public static class Default implements IBluetoothGattCallback {
        @Override // android.bluetooth.IBluetoothGattCallback
        public void onClientRegistered(int status, int clientIf) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onClientConnectionState(int status, int clientIf, boolean connected, String address) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onPhyUpdate(String address, int txPhy, int rxPhy, int status) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onPhyRead(String address, int txPhy, int rxPhy, int status) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onSearchComplete(String address, List<BluetoothGattService> list, int status) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onCharacteristicRead(String address, int status, int handle, byte[] value) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onCharacteristicWrite(String address, int status, int handle) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onExecuteWrite(String address, int status) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onDescriptorRead(String address, int status, int handle, byte[] value) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onDescriptorWrite(String address, int status, int handle) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onNotify(String address, int handle, byte[] value) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onReadRemoteRssi(String address, int rssi, int status) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onConfigureMTU(String address, int mtu, int status) throws RemoteException {
        }

        @Override // android.bluetooth.IBluetoothGattCallback
        public void onConnectionUpdated(String address, int interval, int latency, int timeout, int status) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBluetoothGattCallback {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothGattCallback";
        static final int TRANSACTION_onCharacteristicRead = 6;
        static final int TRANSACTION_onCharacteristicWrite = 7;
        static final int TRANSACTION_onClientConnectionState = 2;
        static final int TRANSACTION_onClientRegistered = 1;
        static final int TRANSACTION_onConfigureMTU = 13;
        static final int TRANSACTION_onConnectionUpdated = 14;
        static final int TRANSACTION_onDescriptorRead = 9;
        static final int TRANSACTION_onDescriptorWrite = 10;
        static final int TRANSACTION_onExecuteWrite = 8;
        static final int TRANSACTION_onNotify = 11;
        static final int TRANSACTION_onPhyRead = 4;
        static final int TRANSACTION_onPhyUpdate = 3;
        static final int TRANSACTION_onReadRemoteRssi = 12;
        static final int TRANSACTION_onSearchComplete = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothGattCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothGattCallback)) {
                return new Proxy(obj);
            }
            return (IBluetoothGattCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onClientRegistered";
                case 2:
                    return "onClientConnectionState";
                case 3:
                    return "onPhyUpdate";
                case 4:
                    return "onPhyRead";
                case 5:
                    return "onSearchComplete";
                case 6:
                    return "onCharacteristicRead";
                case 7:
                    return "onCharacteristicWrite";
                case 8:
                    return "onExecuteWrite";
                case 9:
                    return "onDescriptorRead";
                case 10:
                    return "onDescriptorWrite";
                case 11:
                    return "onNotify";
                case 12:
                    return "onReadRemoteRssi";
                case 13:
                    return "onConfigureMTU";
                case 14:
                    return "onConnectionUpdated";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onClientRegistered(data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onClientConnectionState(data.readInt(), data.readInt(), data.readInt() != 0, data.readString());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onPhyUpdate(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onPhyRead(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onSearchComplete(data.readString(), data.createTypedArrayList(BluetoothGattService.CREATOR), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onCharacteristicRead(data.readString(), data.readInt(), data.readInt(), data.createByteArray());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onCharacteristicWrite(data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onExecuteWrite(data.readString(), data.readInt());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onDescriptorRead(data.readString(), data.readInt(), data.readInt(), data.createByteArray());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        onDescriptorWrite(data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onNotify(data.readString(), data.readInt(), data.createByteArray());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onReadRemoteRssi(data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        onConfigureMTU(data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        onConnectionUpdated(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBluetoothGattCallback {
            public static IBluetoothGattCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onClientRegistered(int status, int clientIf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(clientIf);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onClientRegistered(status, clientIf);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onClientConnectionState(int status, int clientIf, boolean connected, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(clientIf);
                    _data.writeInt(connected ? 1 : 0);
                    _data.writeString(address);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onClientConnectionState(status, clientIf, connected, address);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onPhyUpdate(String address, int txPhy, int rxPhy, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(txPhy);
                    _data.writeInt(rxPhy);
                    _data.writeInt(status);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPhyUpdate(address, txPhy, rxPhy, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onPhyRead(String address, int txPhy, int rxPhy, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(txPhy);
                    _data.writeInt(rxPhy);
                    _data.writeInt(status);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPhyRead(address, txPhy, rxPhy, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onSearchComplete(String address, List<BluetoothGattService> services, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeTypedList(services);
                    _data.writeInt(status);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSearchComplete(address, services, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onCharacteristicRead(String address, int status, int handle, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCharacteristicRead(address, status, handle, value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onCharacteristicWrite(String address, int status, int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCharacteristicWrite(address, status, handle);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onExecuteWrite(String address, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onExecuteWrite(address, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onDescriptorRead(String address, int status, int handle, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDescriptorRead(address, status, handle, value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onDescriptorWrite(String address, int status, int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDescriptorWrite(address, status, handle);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onNotify(String address, int handle, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNotify(address, handle, value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onReadRemoteRssi(String address, int rssi, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(rssi);
                    _data.writeInt(status);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReadRemoteRssi(address, rssi, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onConfigureMTU(String address, int mtu, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(mtu);
                    _data.writeInt(status);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConfigureMTU(address, mtu, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.bluetooth.IBluetoothGattCallback
            public void onConnectionUpdated(String address, int interval, int latency, int timeout, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(interval);
                    _data.writeInt(latency);
                    _data.writeInt(timeout);
                    _data.writeInt(status);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConnectionUpdated(address, interval, latency, timeout, status);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBluetoothGattCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBluetoothGattCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
