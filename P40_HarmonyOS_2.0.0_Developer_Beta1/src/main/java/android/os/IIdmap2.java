package android.os;

public interface IIdmap2 extends IInterface {
    public static final int POLICY_ODM_PARTITION = 32;
    public static final int POLICY_OEM_PARTITION = 64;
    public static final int POLICY_PRODUCT_PARTITION = 8;
    public static final int POLICY_PUBLIC = 1;
    public static final int POLICY_SIGNATURE = 16;
    public static final int POLICY_SYSTEM_PARTITION = 2;
    public static final int POLICY_VENDOR_PARTITION = 4;

    String createIdmap(String str, String str2, int i, boolean z, int i2) throws RemoteException;

    String getIdmapPath(String str, int i) throws RemoteException;

    boolean removeIdmap(String str, int i) throws RemoteException;

    boolean verifyIdmap(String str, int i, boolean z, int i2) throws RemoteException;

    public static class Default implements IIdmap2 {
        @Override // android.os.IIdmap2
        public String getIdmapPath(String overlayApkPath, int userId) throws RemoteException {
            return null;
        }

        @Override // android.os.IIdmap2
        public boolean removeIdmap(String overlayApkPath, int userId) throws RemoteException {
            return false;
        }

        @Override // android.os.IIdmap2
        public boolean verifyIdmap(String overlayApkPath, int fulfilledPolicies, boolean enforceOverlayable, int userId) throws RemoteException {
            return false;
        }

        @Override // android.os.IIdmap2
        public String createIdmap(String targetApkPath, String overlayApkPath, int fulfilledPolicies, boolean enforceOverlayable, int userId) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIdmap2 {
        private static final String DESCRIPTOR = "android.os.IIdmap2";
        static final int TRANSACTION_createIdmap = 4;
        static final int TRANSACTION_getIdmapPath = 1;
        static final int TRANSACTION_removeIdmap = 2;
        static final int TRANSACTION_verifyIdmap = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIdmap2 asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIdmap2)) {
                return new Proxy(obj);
            }
            return (IIdmap2) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _result = getIdmapPath(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code != 2) {
                boolean _arg2 = false;
                if (code == 3) {
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = true;
                    }
                    boolean verifyIdmap = verifyIdmap(_arg0, _arg1, _arg2, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(verifyIdmap ? 1 : 0);
                    return true;
                } else if (code == 4) {
                    data.enforceInterface(DESCRIPTOR);
                    String _result2 = createIdmap(data.readString(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                } else if (code != 1598968902) {
                    return super.onTransact(code, data, reply, flags);
                } else {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
            } else {
                data.enforceInterface(DESCRIPTOR);
                boolean removeIdmap = removeIdmap(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(removeIdmap ? 1 : 0);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IIdmap2 {
            public static IIdmap2 sDefaultImpl;
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

            @Override // android.os.IIdmap2
            public String getIdmapPath(String overlayApkPath, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(overlayApkPath);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIdmapPath(overlayApkPath, userId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IIdmap2
            public boolean removeIdmap(String overlayApkPath, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(overlayApkPath);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeIdmap(overlayApkPath, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IIdmap2
            public boolean verifyIdmap(String overlayApkPath, int fulfilledPolicies, boolean enforceOverlayable, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(overlayApkPath);
                    _data.writeInt(fulfilledPolicies);
                    boolean _result = true;
                    _data.writeInt(enforceOverlayable ? 1 : 0);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().verifyIdmap(overlayApkPath, fulfilledPolicies, enforceOverlayable, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IIdmap2
            public String createIdmap(String targetApkPath, String overlayApkPath, int fulfilledPolicies, boolean enforceOverlayable, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetApkPath);
                    _data.writeString(overlayApkPath);
                    _data.writeInt(fulfilledPolicies);
                    _data.writeInt(enforceOverlayable ? 1 : 0);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createIdmap(targetApkPath, overlayApkPath, fulfilledPolicies, enforceOverlayable, userId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIdmap2 impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIdmap2 getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
