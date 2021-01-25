package android.os;

public interface IDumpstateListener extends IInterface {
    public static final int BUGREPORT_ERROR_ANOTHER_REPORT_IN_PROGRESS = 5;
    public static final int BUGREPORT_ERROR_INVALID_INPUT = 1;
    public static final int BUGREPORT_ERROR_RUNTIME_ERROR = 2;
    public static final int BUGREPORT_ERROR_USER_CONSENT_TIMED_OUT = 4;
    public static final int BUGREPORT_ERROR_USER_DENIED_CONSENT = 3;

    void onError(int i) throws RemoteException;

    void onFinished() throws RemoteException;

    void onMaxProgressUpdated(int i) throws RemoteException;

    void onProgress(int i) throws RemoteException;

    void onProgressUpdated(int i) throws RemoteException;

    void onSectionComplete(String str, int i, int i2, int i3) throws RemoteException;

    public static class Default implements IDumpstateListener {
        @Override // android.os.IDumpstateListener
        public void onProgress(int progress) throws RemoteException {
        }

        @Override // android.os.IDumpstateListener
        public void onError(int errorCode) throws RemoteException {
        }

        @Override // android.os.IDumpstateListener
        public void onFinished() throws RemoteException {
        }

        @Override // android.os.IDumpstateListener
        public void onProgressUpdated(int progress) throws RemoteException {
        }

        @Override // android.os.IDumpstateListener
        public void onMaxProgressUpdated(int maxProgress) throws RemoteException {
        }

        @Override // android.os.IDumpstateListener
        public void onSectionComplete(String name, int status, int size, int durationMs) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDumpstateListener {
        private static final String DESCRIPTOR = "android.os.IDumpstateListener";
        static final int TRANSACTION_onError = 2;
        static final int TRANSACTION_onFinished = 3;
        static final int TRANSACTION_onMaxProgressUpdated = 5;
        static final int TRANSACTION_onProgress = 1;
        static final int TRANSACTION_onProgressUpdated = 4;
        static final int TRANSACTION_onSectionComplete = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDumpstateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDumpstateListener)) {
                return new Proxy(obj);
            }
            return (IDumpstateListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onProgress(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onError(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onFinished();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onProgressUpdated(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onMaxProgressUpdated(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onSectionComplete(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
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
        public static class Proxy implements IDumpstateListener {
            public static IDumpstateListener sDefaultImpl;
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

            @Override // android.os.IDumpstateListener
            public void onProgress(int progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(progress);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onProgress(progress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDumpstateListener
            public void onError(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onError(errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDumpstateListener
            public void onFinished() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onFinished();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDumpstateListener
            public void onProgressUpdated(int progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(progress);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onProgressUpdated(progress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDumpstateListener
            public void onMaxProgressUpdated(int maxProgress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxProgress);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onMaxProgressUpdated(maxProgress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IDumpstateListener
            public void onSectionComplete(String name, int status, int size, int durationMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    _data.writeInt(status);
                    _data.writeInt(size);
                    _data.writeInt(durationMs);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSectionComplete(name, status, size, durationMs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDumpstateListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDumpstateListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
