package android.os;

public interface IVoldListener extends IInterface {
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

    public static class Default implements IVoldListener {
        @Override // android.os.IVoldListener
        public void onDiskCreated(String diskId, int flags) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onDiskScanned(String diskId) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onDiskMetadataChanged(String diskId, long sizeBytes, String label, String sysPath) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onDiskDestroyed(String diskId) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onVolumeCreated(String volId, int type, String diskId, String partGuid) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onVolumeStateChanged(String volId, int state) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onCheckVolumeCompleted(String volId, String diskId, String partGuid, int isSucc) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onVolumeMetadataChanged(String volId, String fsType, String fsUuid, String fsLabel) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onVolumePathChanged(String volId, String path) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onVolumeInternalPathChanged(String volId, String internalPath) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onVolumeDestroyed(String volId) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onLockedDiskAdd() throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onLockedDiskRemove() throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onSdHealthReport(String volId, int newState) throws RemoteException {
        }

        @Override // android.os.IVoldListener
        public void onCryptsdMessage(String message) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onDiskCreated";
                case 2:
                    return "onDiskScanned";
                case 3:
                    return "onDiskMetadataChanged";
                case 4:
                    return "onDiskDestroyed";
                case 5:
                    return "onVolumeCreated";
                case 6:
                    return "onVolumeStateChanged";
                case 7:
                    return "onCheckVolumeCompleted";
                case 8:
                    return "onVolumeMetadataChanged";
                case 9:
                    return "onVolumePathChanged";
                case 10:
                    return "onVolumeInternalPathChanged";
                case 11:
                    return "onVolumeDestroyed";
                case 12:
                    return "onLockedDiskAdd";
                case 13:
                    return "onLockedDiskRemove";
                case 14:
                    return "onSdHealthReport";
                case 15:
                    return "onCryptsdMessage";
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
                        onDiskCreated(data.readString(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onDiskScanned(data.readString());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onDiskMetadataChanged(data.readString(), data.readLong(), data.readString(), data.readString());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onDiskDestroyed(data.readString());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onVolumeCreated(data.readString(), data.readInt(), data.readString(), data.readString());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onVolumeStateChanged(data.readString(), data.readInt());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onCheckVolumeCompleted(data.readString(), data.readString(), data.readString(), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onVolumeMetadataChanged(data.readString(), data.readString(), data.readString(), data.readString());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onVolumePathChanged(data.readString(), data.readString());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        onVolumeInternalPathChanged(data.readString(), data.readString());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onVolumeDestroyed(data.readString());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onLockedDiskAdd();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        onLockedDiskRemove();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        onSdHealthReport(data.readString(), data.readInt());
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
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

        /* access modifiers changed from: private */
        public static class Proxy implements IVoldListener {
            public static IVoldListener sDefaultImpl;
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

            @Override // android.os.IVoldListener
            public void onDiskCreated(String diskId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDiskCreated(diskId, flags);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onDiskScanned(String diskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDiskScanned(diskId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onDiskMetadataChanged(String diskId, long sizeBytes, String label, String sysPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    _data.writeLong(sizeBytes);
                    _data.writeString(label);
                    _data.writeString(sysPath);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDiskMetadataChanged(diskId, sizeBytes, label, sysPath);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onDiskDestroyed(String diskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDiskDestroyed(diskId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onVolumeCreated(String volId, int type, String diskId, String partGuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(type);
                    _data.writeString(diskId);
                    _data.writeString(partGuid);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVolumeCreated(volId, type, diskId, partGuid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onVolumeStateChanged(String volId, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(state);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVolumeStateChanged(volId, state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onCheckVolumeCompleted(String volId, String diskId, String partGuid, int isSucc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(diskId);
                    _data.writeString(partGuid);
                    _data.writeInt(isSucc);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCheckVolumeCompleted(volId, diskId, partGuid, isSucc);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onVolumeMetadataChanged(String volId, String fsType, String fsUuid, String fsLabel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(fsType);
                    _data.writeString(fsUuid);
                    _data.writeString(fsLabel);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVolumeMetadataChanged(volId, fsType, fsUuid, fsLabel);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onVolumePathChanged(String volId, String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(path);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVolumePathChanged(volId, path);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onVolumeInternalPathChanged(String volId, String internalPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(internalPath);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVolumeInternalPathChanged(volId, internalPath);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onVolumeDestroyed(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onVolumeDestroyed(volId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onLockedDiskAdd() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLockedDiskAdd();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onLockedDiskRemove() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLockedDiskRemove();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onSdHealthReport(String volId, int newState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(newState);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSdHealthReport(volId, newState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IVoldListener
            public void onCryptsdMessage(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCryptsdMessage(message);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoldListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoldListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
