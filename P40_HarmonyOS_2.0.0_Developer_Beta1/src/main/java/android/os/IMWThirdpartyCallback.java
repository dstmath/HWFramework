package android.os;

public interface IMWThirdpartyCallback extends IInterface {
    void onModeChanged(boolean z) throws RemoteException;

    void onSizeChanged() throws RemoteException;

    void onZoneChanged() throws RemoteException;

    public static class Default implements IMWThirdpartyCallback {
        @Override // android.os.IMWThirdpartyCallback
        public void onModeChanged(boolean aMWStatus) throws RemoteException {
        }

        @Override // android.os.IMWThirdpartyCallback
        public void onZoneChanged() throws RemoteException {
        }

        @Override // android.os.IMWThirdpartyCallback
        public void onSizeChanged() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMWThirdpartyCallback {
        private static final String DESCRIPTOR = "android.os.IMWThirdpartyCallback";
        static final int TRANSACTION_onModeChanged = 1;
        static final int TRANSACTION_onSizeChanged = 3;
        static final int TRANSACTION_onZoneChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMWThirdpartyCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMWThirdpartyCallback)) {
                return new Proxy(obj);
            }
            return (IMWThirdpartyCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onModeChanged";
            }
            if (transactionCode == 2) {
                return "onZoneChanged";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onSizeChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onModeChanged(data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onZoneChanged();
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onSizeChanged();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMWThirdpartyCallback {
            public static IMWThirdpartyCallback sDefaultImpl;
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

            @Override // android.os.IMWThirdpartyCallback
            public void onModeChanged(boolean aMWStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(aMWStatus ? 1 : 0);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onModeChanged(aMWStatus);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IMWThirdpartyCallback
            public void onZoneChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onZoneChanged();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IMWThirdpartyCallback
            public void onSizeChanged() throws RemoteException {
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
                    Stub.getDefaultImpl().onSizeChanged();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMWThirdpartyCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMWThirdpartyCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
