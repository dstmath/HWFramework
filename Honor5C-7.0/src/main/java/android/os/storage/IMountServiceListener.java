package android.os.storage;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMountServiceListener extends IInterface {

    public static abstract class Stub extends Binder implements IMountServiceListener {
        private static final String DESCRIPTOR = "IMountServiceListener";
        static final int TRANSACTION_onDiskDestroyed = 7;
        static final int TRANSACTION_onDiskScanned = 6;
        static final int TRANSACTION_onStorageStateChanged = 2;
        static final int TRANSACTION_onUsbMassStorageConnectionChanged = 1;
        static final int TRANSACTION_onVolumeForgotten = 5;
        static final int TRANSACTION_onVolumeRecordChanged = 4;
        static final int TRANSACTION_onVolumeStateChanged = 3;

        private static class Proxy implements IMountServiceListener {
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

            public void onUsbMassStorageConnectionChanged(boolean connected) throws RemoteException {
                int i = Stub.TRANSACTION_onUsbMassStorageConnectionChanged;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!connected) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onUsbMassStorageConnectionChanged, _data, _reply, Stub.TRANSACTION_onUsbMassStorageConnectionChanged);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onStorageStateChanged(String path, String oldState, String newState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeString(oldState);
                    _data.writeString(newState);
                    this.mRemote.transact(Stub.TRANSACTION_onStorageStateChanged, _data, _reply, Stub.TRANSACTION_onUsbMassStorageConnectionChanged);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeParcelable(vol, 0);
                    _data.writeInt(oldState);
                    _data.writeInt(newState);
                    this.mRemote.transact(Stub.TRANSACTION_onVolumeStateChanged, _data, _reply, Stub.TRANSACTION_onUsbMassStorageConnectionChanged);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onVolumeRecordChanged(VolumeRecord rec) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeParcelable(rec, 0);
                    this.mRemote.transact(Stub.TRANSACTION_onVolumeRecordChanged, _data, _reply, Stub.TRANSACTION_onUsbMassStorageConnectionChanged);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onVolumeForgotten(String fsUuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fsUuid);
                    this.mRemote.transact(Stub.TRANSACTION_onVolumeForgotten, _data, _reply, Stub.TRANSACTION_onUsbMassStorageConnectionChanged);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onDiskScanned(DiskInfo disk, int volumeCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeParcelable(disk, 0);
                    _data.writeInt(volumeCount);
                    this.mRemote.transact(Stub.TRANSACTION_onDiskScanned, _data, _reply, Stub.TRANSACTION_onUsbMassStorageConnectionChanged);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onDiskDestroyed(DiskInfo disk) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeParcelable(disk, 0);
                    this.mRemote.transact(Stub.TRANSACTION_onDiskDestroyed, _data, _reply, Stub.TRANSACTION_onUsbMassStorageConnectionChanged);
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

        public static IMountServiceListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMountServiceListener)) {
                return new Proxy(obj);
            }
            return (IMountServiceListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onUsbMassStorageConnectionChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUsbMassStorageConnectionChanged(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onStorageStateChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onStorageStateChanged(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onVolumeStateChanged /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onVolumeStateChanged((VolumeInfo) data.readParcelable(null), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onVolumeRecordChanged /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onVolumeRecordChanged((VolumeRecord) data.readParcelable(null));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onVolumeForgotten /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onVolumeForgotten(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onDiskScanned /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDiskScanned((DiskInfo) data.readParcelable(null), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onDiskDestroyed /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDiskDestroyed((DiskInfo) data.readParcelable(null));
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onDiskDestroyed(DiskInfo diskInfo) throws RemoteException;

    void onDiskScanned(DiskInfo diskInfo, int i) throws RemoteException;

    void onStorageStateChanged(String str, String str2, String str3) throws RemoteException;

    void onUsbMassStorageConnectionChanged(boolean z) throws RemoteException;

    void onVolumeForgotten(String str) throws RemoteException;

    void onVolumeRecordChanged(VolumeRecord volumeRecord) throws RemoteException;

    void onVolumeStateChanged(VolumeInfo volumeInfo, int i, int i2) throws RemoteException;
}
