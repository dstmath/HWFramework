package android.os;

public interface IVoldListener extends IInterface {

    public static abstract class Stub extends Binder implements IVoldListener {
        private static final String DESCRIPTOR = "android.os.IVoldListener";
        static final int TRANSACTION_onCheckVolumeCompleted = 7;
        static final int TRANSACTION_onCryptsdMessage = 15;
        static final int TRANSACTION_onDiskCreated = 1;
        static final int TRANSACTION_onDiskDestroyed = 4;
        static final int TRANSACTION_onDiskMetadataChanged = 3;
        static final int TRANSACTION_onDiskScanned = 2;
        static final int TRANSACTION_onLockedDiskAdd = 12;
        static final int TRANSACTION_onLockedDiskRemove = 13;
        static final int TRANSACTION_onSdHealthReport = 14;
        static final int TRANSACTION_onVolumeCreated = 5;
        static final int TRANSACTION_onVolumeDestroyed = 11;
        static final int TRANSACTION_onVolumeInternalPathChanged = 10;
        static final int TRANSACTION_onVolumeMetadataChanged = 8;
        static final int TRANSACTION_onVolumePathChanged = 9;
        static final int TRANSACTION_onVolumeStateChanged = 6;

        private static class Proxy implements IVoldListener {
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

            public void onDiskCreated(String diskId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    _data.writeInt(flags);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDiskScanned(String diskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDiskMetadataChanged(String diskId, long sizeBytes, String label, String sysPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    _data.writeLong(sizeBytes);
                    _data.writeString(label);
                    _data.writeString(sysPath);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDiskDestroyed(String diskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onVolumeCreated(String volId, int type, String diskId, String partGuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(type);
                    _data.writeString(diskId);
                    _data.writeString(partGuid);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onVolumeStateChanged(String volId, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(state);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCheckVolumeCompleted(String volId, String diskId, String partGuid, int isSucc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(diskId);
                    _data.writeString(partGuid);
                    _data.writeInt(isSucc);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onVolumeMetadataChanged(String volId, String fsType, String fsUuid, String fsLabel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(fsType);
                    _data.writeString(fsUuid);
                    _data.writeString(fsLabel);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onVolumePathChanged(String volId, String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(path);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onVolumeInternalPathChanged(String volId, String internalPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(internalPath);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onVolumeDestroyed(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onLockedDiskAdd() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onLockedDiskRemove() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSdHealthReport(String volId, int newState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(newState);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCryptsdMessage(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoldListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoldListener)) {
                return new Proxy(obj);
            }
            return (IVoldListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        onDiskCreated(data.readString(), data.readInt());
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        onDiskScanned(data.readString());
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        onDiskMetadataChanged(data.readString(), data.readLong(), data.readString(), data.readString());
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        onDiskDestroyed(data.readString());
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        onVolumeCreated(data.readString(), data.readInt(), data.readString(), data.readString());
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        onVolumeStateChanged(data.readString(), data.readInt());
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        onCheckVolumeCompleted(data.readString(), data.readString(), data.readString(), data.readInt());
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        onVolumeMetadataChanged(data.readString(), data.readString(), data.readString(), data.readString());
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        onVolumePathChanged(data.readString(), data.readString());
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        onVolumeInternalPathChanged(data.readString(), data.readString());
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        onVolumeDestroyed(data.readString());
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        onLockedDiskAdd();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        onLockedDiskRemove();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        onSdHealthReport(data.readString(), data.readInt());
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        onCryptsdMessage(data.readString());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onCheckVolumeCompleted(String str, String str2, String str3, int i) throws RemoteException;

    void onCryptsdMessage(String str) throws RemoteException;

    void onDiskCreated(String str, int i) throws RemoteException;

    void onDiskDestroyed(String str) throws RemoteException;

    void onDiskMetadataChanged(String str, long j, String str2, String str3) throws RemoteException;

    void onDiskScanned(String str) throws RemoteException;

    void onLockedDiskAdd() throws RemoteException;

    void onLockedDiskRemove() throws RemoteException;

    void onSdHealthReport(String str, int i) throws RemoteException;

    void onVolumeCreated(String str, int i, String str2, String str3) throws RemoteException;

    void onVolumeDestroyed(String str) throws RemoteException;

    void onVolumeInternalPathChanged(String str, String str2) throws RemoteException;

    void onVolumeMetadataChanged(String str, String str2, String str3, String str4) throws RemoteException;

    void onVolumePathChanged(String str, String str2) throws RemoteException;

    void onVolumeStateChanged(String str, int i) throws RemoteException;
}
