package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.RemoteException;

public interface IBluetoothGattServerCallback extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothGattServerCallback {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothGattServerCallback";
        static final int TRANSACTION_onCharacteristicReadRequest = 5;
        static final int TRANSACTION_onCharacteristicWriteRequest = 7;
        static final int TRANSACTION_onDescriptorReadRequest = 6;
        static final int TRANSACTION_onDescriptorWriteRequest = 8;
        static final int TRANSACTION_onExecuteWrite = 9;
        static final int TRANSACTION_onMtuChanged = 11;
        static final int TRANSACTION_onNotificationSent = 10;
        static final int TRANSACTION_onScanResult = 2;
        static final int TRANSACTION_onServerConnectionState = 3;
        static final int TRANSACTION_onServerRegistered = 1;
        static final int TRANSACTION_onServiceAdded = 4;

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
                    this.mRemote.transact(Stub.TRANSACTION_onServerRegistered, _data, null, Stub.TRANSACTION_onServerRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onScanResult(String address, int rssi, byte[] advData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(rssi);
                    _data.writeByteArray(advData);
                    this.mRemote.transact(Stub.TRANSACTION_onScanResult, _data, null, Stub.TRANSACTION_onServerRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onServerConnectionState(int status, int serverIf, boolean connected, String address) throws RemoteException {
                int i = Stub.TRANSACTION_onServerRegistered;
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
                    this.mRemote.transact(Stub.TRANSACTION_onServerConnectionState, _data, null, Stub.TRANSACTION_onServerRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onServiceAdded(int status, int srvcType, int srvcInstId, ParcelUuid srvcId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(srvcType);
                    _data.writeInt(srvcInstId);
                    if (srvcId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        srvcId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onServiceAdded, _data, null, Stub.TRANSACTION_onServerRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onCharacteristicReadRequest(String address, int transId, int offset, boolean isLong, int srvcType, int srvcInstId, ParcelUuid srvcId, int charInstId, ParcelUuid charId) throws RemoteException {
                int i = Stub.TRANSACTION_onServerRegistered;
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
                    _data.writeInt(srvcType);
                    _data.writeInt(srvcInstId);
                    if (srvcId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        srvcId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(charInstId);
                    if (charId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        charId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onCharacteristicReadRequest, _data, null, Stub.TRANSACTION_onServerRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onDescriptorReadRequest(String address, int transId, int offset, boolean isLong, int srvcType, int srvcInstId, ParcelUuid srvcId, int charInstId, ParcelUuid charId, ParcelUuid descrId) throws RemoteException {
                int i = Stub.TRANSACTION_onServerRegistered;
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
                    _data.writeInt(srvcType);
                    _data.writeInt(srvcInstId);
                    if (srvcId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        srvcId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(charInstId);
                    if (charId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        charId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (descrId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        descrId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onDescriptorReadRequest, _data, null, Stub.TRANSACTION_onServerRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onCharacteristicWriteRequest(String address, int transId, int offset, int length, boolean isPrep, boolean needRsp, int srvcType, int srvcInstId, ParcelUuid srvcId, int charInstId, ParcelUuid charId, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(transId);
                    _data.writeInt(offset);
                    _data.writeInt(length);
                    _data.writeInt(isPrep ? Stub.TRANSACTION_onServerRegistered : 0);
                    _data.writeInt(needRsp ? Stub.TRANSACTION_onServerRegistered : 0);
                    _data.writeInt(srvcType);
                    _data.writeInt(srvcInstId);
                    if (srvcId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        srvcId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(charInstId);
                    if (charId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        charId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(value);
                    this.mRemote.transact(Stub.TRANSACTION_onCharacteristicWriteRequest, _data, null, Stub.TRANSACTION_onServerRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onDescriptorWriteRequest(String address, int transId, int offset, int length, boolean isPrep, boolean needRsp, int srvcType, int srvcInstId, ParcelUuid srvcId, int charInstId, ParcelUuid charId, ParcelUuid descrId, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(transId);
                    _data.writeInt(offset);
                    _data.writeInt(length);
                    _data.writeInt(isPrep ? Stub.TRANSACTION_onServerRegistered : 0);
                    _data.writeInt(needRsp ? Stub.TRANSACTION_onServerRegistered : 0);
                    _data.writeInt(srvcType);
                    _data.writeInt(srvcInstId);
                    if (srvcId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        srvcId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(charInstId);
                    if (charId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        charId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (descrId != null) {
                        _data.writeInt(Stub.TRANSACTION_onServerRegistered);
                        descrId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(value);
                    this.mRemote.transact(Stub.TRANSACTION_onDescriptorWriteRequest, _data, null, Stub.TRANSACTION_onServerRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onExecuteWrite(String address, int transId, boolean execWrite) throws RemoteException {
                int i = Stub.TRANSACTION_onServerRegistered;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(transId);
                    if (!execWrite) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onExecuteWrite, _data, null, Stub.TRANSACTION_onServerRegistered);
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
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationSent, _data, null, Stub.TRANSACTION_onServerRegistered);
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
                    this.mRemote.transact(Stub.TRANSACTION_onMtuChanged, _data, null, Stub.TRANSACTION_onServerRegistered);
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
            int _arg1;
            int _arg2;
            String _arg0;
            boolean _arg3;
            int _arg4;
            int _arg5;
            ParcelUuid parcelUuid;
            int _arg7;
            ParcelUuid parcelUuid2;
            int _arg32;
            boolean _arg42;
            boolean _arg52;
            int _arg6;
            int _arg9;
            ParcelUuid parcelUuid3;
            switch (code) {
                case TRANSACTION_onServerRegistered /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onServerRegistered(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onScanResult /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onScanResult(data.readString(), data.readInt(), data.createByteArray());
                    return true;
                case TRANSACTION_onServerConnectionState /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onServerConnectionState(data.readInt(), data.readInt(), data.readInt() != 0, data.readString());
                    return true;
                case TRANSACTION_onServiceAdded /*4*/:
                    ParcelUuid parcelUuid4;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg02 = data.readInt();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid4 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid4 = null;
                    }
                    onServiceAdded(_arg02, _arg1, _arg2, parcelUuid4);
                    return true;
                case TRANSACTION_onCharacteristicReadRequest /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    _arg3 = data.readInt() != 0;
                    _arg4 = data.readInt();
                    _arg5 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid = null;
                    }
                    _arg7 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid2 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid2 = null;
                    }
                    onCharacteristicReadRequest(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, parcelUuid, _arg7, parcelUuid2);
                    return true;
                case TRANSACTION_onDescriptorReadRequest /*6*/:
                    ParcelUuid parcelUuid5;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    _arg3 = data.readInt() != 0;
                    _arg4 = data.readInt();
                    _arg5 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid = null;
                    }
                    _arg7 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid2 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid2 = null;
                    }
                    if (data.readInt() != 0) {
                        parcelUuid5 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid5 = null;
                    }
                    onDescriptorReadRequest(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, parcelUuid, _arg7, parcelUuid2, parcelUuid5);
                    return true;
                case TRANSACTION_onCharacteristicWriteRequest /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    _arg32 = data.readInt();
                    _arg42 = data.readInt() != 0;
                    _arg52 = data.readInt() != 0;
                    _arg6 = data.readInt();
                    _arg7 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid2 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid2 = null;
                    }
                    _arg9 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid3 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid3 = null;
                    }
                    onCharacteristicWriteRequest(_arg0, _arg1, _arg2, _arg32, _arg42, _arg52, _arg6, _arg7, parcelUuid2, _arg9, parcelUuid3, data.createByteArray());
                    return true;
                case TRANSACTION_onDescriptorWriteRequest /*8*/:
                    ParcelUuid parcelUuid6;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readInt();
                    _arg2 = data.readInt();
                    _arg32 = data.readInt();
                    _arg42 = data.readInt() != 0;
                    _arg52 = data.readInt() != 0;
                    _arg6 = data.readInt();
                    _arg7 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid2 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid2 = null;
                    }
                    _arg9 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelUuid3 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid3 = null;
                    }
                    if (data.readInt() != 0) {
                        parcelUuid6 = (ParcelUuid) ParcelUuid.CREATOR.createFromParcel(data);
                    } else {
                        parcelUuid6 = null;
                    }
                    onDescriptorWriteRequest(_arg0, _arg1, _arg2, _arg32, _arg42, _arg52, _arg6, _arg7, parcelUuid2, _arg9, parcelUuid3, parcelUuid6, data.createByteArray());
                    return true;
                case TRANSACTION_onExecuteWrite /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    onExecuteWrite(data.readString(), data.readInt(), data.readInt() != 0);
                    return true;
                case TRANSACTION_onNotificationSent /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationSent(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_onMtuChanged /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    onMtuChanged(data.readString(), data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onCharacteristicReadRequest(String str, int i, int i2, boolean z, int i3, int i4, ParcelUuid parcelUuid, int i5, ParcelUuid parcelUuid2) throws RemoteException;

    void onCharacteristicWriteRequest(String str, int i, int i2, int i3, boolean z, boolean z2, int i4, int i5, ParcelUuid parcelUuid, int i6, ParcelUuid parcelUuid2, byte[] bArr) throws RemoteException;

    void onDescriptorReadRequest(String str, int i, int i2, boolean z, int i3, int i4, ParcelUuid parcelUuid, int i5, ParcelUuid parcelUuid2, ParcelUuid parcelUuid3) throws RemoteException;

    void onDescriptorWriteRequest(String str, int i, int i2, int i3, boolean z, boolean z2, int i4, int i5, ParcelUuid parcelUuid, int i6, ParcelUuid parcelUuid2, ParcelUuid parcelUuid3, byte[] bArr) throws RemoteException;

    void onExecuteWrite(String str, int i, boolean z) throws RemoteException;

    void onMtuChanged(String str, int i) throws RemoteException;

    void onNotificationSent(String str, int i) throws RemoteException;

    void onScanResult(String str, int i, byte[] bArr) throws RemoteException;

    void onServerConnectionState(int i, int i2, boolean z, String str) throws RemoteException;

    void onServerRegistered(int i, int i2) throws RemoteException;

    void onServiceAdded(int i, int i2, int i3, ParcelUuid parcelUuid) throws RemoteException;
}
