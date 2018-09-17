package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IBluetoothGattCallback extends IInterface {

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

        private static class Proxy implements IBluetoothGattCallback {
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

            public void onClientRegistered(int status, int clientIf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(clientIf);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onClientConnectionState(int status, int clientIf, boolean connected, String address) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(clientIf);
                    if (!connected) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(address);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPhyUpdate(String address, int txPhy, int rxPhy, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(txPhy);
                    _data.writeInt(rxPhy);
                    _data.writeInt(status);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPhyRead(String address, int txPhy, int rxPhy, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(txPhy);
                    _data.writeInt(rxPhy);
                    _data.writeInt(status);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSearchComplete(String address, List<BluetoothGattService> services, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeTypedList(services);
                    _data.writeInt(status);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCharacteristicRead(String address, int status, int handle, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCharacteristicWrite(String address, int status, int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onExecuteWrite(String address, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDescriptorRead(String address, int status, int handle, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDescriptorWrite(String address, int status, int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotify(String address, int handle, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onReadRemoteRssi(String address, int rssi, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(rssi);
                    _data.writeInt(status);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConfigureMTU(String address, int mtu, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(mtu);
                    _data.writeInt(status);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectionUpdated(String address, int interval, int latency, int timeout, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(interval);
                    _data.writeInt(latency);
                    _data.writeInt(timeout);
                    _data.writeInt(status);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
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
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

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
}
