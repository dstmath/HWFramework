package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBluetoothGattServerCallback extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothGattServerCallback {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothGattServerCallback";
        static final int TRANSACTION_onCharacteristicReadRequest = 4;
        static final int TRANSACTION_onCharacteristicWriteRequest = 6;
        static final int TRANSACTION_onConnectionUpdated = 13;
        static final int TRANSACTION_onDescriptorReadRequest = 5;
        static final int TRANSACTION_onDescriptorWriteRequest = 7;
        static final int TRANSACTION_onExecuteWrite = 8;
        static final int TRANSACTION_onMtuChanged = 10;
        static final int TRANSACTION_onNotificationSent = 9;
        static final int TRANSACTION_onPhyRead = 12;
        static final int TRANSACTION_onPhyUpdate = 11;
        static final int TRANSACTION_onServerConnectionState = 2;
        static final int TRANSACTION_onServerRegistered = 1;
        static final int TRANSACTION_onServiceAdded = 3;

        private static class Proxy implements IBluetoothGattServerCallback {
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

            public void onServerRegistered(int status, int serverIf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(serverIf);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onServerConnectionState(int status, int serverIf, boolean connected, String address) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(serverIf);
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

            public void onServiceAdded(int status, BluetoothGattService service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCharacteristicReadRequest(String address, int transId, int offset, boolean isLong, int handle) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(transId);
                    _data.writeInt(offset);
                    if (!isLong) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(handle);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDescriptorReadRequest(String address, int transId, int offset, boolean isLong, int handle) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(transId);
                    _data.writeInt(offset);
                    if (!isLong) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(handle);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCharacteristicWriteRequest(String address, int transId, int offset, int length, boolean isPrep, boolean needRsp, int handle, byte[] value) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(transId);
                    _data.writeInt(offset);
                    _data.writeInt(length);
                    _data.writeInt(isPrep ? 1 : 0);
                    if (!needRsp) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDescriptorWriteRequest(String address, int transId, int offset, int length, boolean isPrep, boolean needRsp, int handle, byte[] value) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(transId);
                    _data.writeInt(offset);
                    _data.writeInt(length);
                    _data.writeInt(isPrep ? 1 : 0);
                    if (!needRsp) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onExecuteWrite(String address, int transId, boolean execWrite) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(transId);
                    if (!execWrite) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotificationSent(String address, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMtuChanged(String address, int mtu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(mtu);
                    this.mRemote.transact(10, _data, null, 1);
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
                    this.mRemote.transact(11, _data, null, 1);
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
                    this.mRemote.transact(12, _data, null, 1);
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
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothGattServerCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothGattServerCallback)) {
                return new Proxy(obj);
            }
            return (IBluetoothGattServerCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onServerRegistered(data.readInt(), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onServerConnectionState(data.readInt(), data.readInt(), data.readInt() != 0, data.readString());
                    return true;
                case 3:
                    BluetoothGattService _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = (BluetoothGattService) BluetoothGattService.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onServiceAdded(_arg0, _arg1);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onCharacteristicReadRequest(data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onDescriptorReadRequest(data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt());
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    onCharacteristicWriteRequest(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt(), data.createByteArray());
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    onDescriptorWriteRequest(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt(), data.createByteArray());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    onExecuteWrite(data.readString(), data.readInt(), data.readInt() != 0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationSent(data.readString(), data.readInt());
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    onMtuChanged(data.readString(), data.readInt());
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    onPhyUpdate(data.readString(), data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    onPhyRead(data.readString(), data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 13:
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

    void onCharacteristicReadRequest(String str, int i, int i2, boolean z, int i3) throws RemoteException;

    void onCharacteristicWriteRequest(String str, int i, int i2, int i3, boolean z, boolean z2, int i4, byte[] bArr) throws RemoteException;

    void onConnectionUpdated(String str, int i, int i2, int i3, int i4) throws RemoteException;

    void onDescriptorReadRequest(String str, int i, int i2, boolean z, int i3) throws RemoteException;

    void onDescriptorWriteRequest(String str, int i, int i2, int i3, boolean z, boolean z2, int i4, byte[] bArr) throws RemoteException;

    void onExecuteWrite(String str, int i, boolean z) throws RemoteException;

    void onMtuChanged(String str, int i) throws RemoteException;

    void onNotificationSent(String str, int i) throws RemoteException;

    void onPhyRead(String str, int i, int i2, int i3) throws RemoteException;

    void onPhyUpdate(String str, int i, int i2, int i3) throws RemoteException;

    void onServerConnectionState(int i, int i2, boolean z, String str) throws RemoteException;

    void onServerRegistered(int i, int i2) throws RemoteException;

    void onServiceAdded(int i, BluetoothGattService bluetoothGattService) throws RemoteException;
}
