package android.os;

public interface ITrustedUIService extends IInterface {
    boolean getTrustedUIStatus() throws RemoteException;

    int sendTUICmd(int i, int i2, int[] iArr) throws RemoteException;

    void sendTUIExitCmd() throws RemoteException;

    public static class Default implements ITrustedUIService {
        @Override // android.os.ITrustedUIService
        public boolean getTrustedUIStatus() throws RemoteException {
            return false;
        }

        @Override // android.os.ITrustedUIService
        public void sendTUIExitCmd() throws RemoteException {
        }

        @Override // android.os.ITrustedUIService
        public int sendTUICmd(int event_type, int value, int[] screen_info) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITrustedUIService {
        private static final String DESCRIPTOR = "android.os.ITrustedUIService";
        static final int TRANSACTION_getTrustedUIStatus = 1;
        static final int TRANSACTION_sendTUICmd = 3;
        static final int TRANSACTION_sendTUIExitCmd = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustedUIService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustedUIService)) {
                return new Proxy(obj);
            }
            return (ITrustedUIService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getTrustedUIStatus";
            }
            if (transactionCode == 2) {
                return "sendTUIExitCmd";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "sendTUICmd";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean trustedUIStatus = getTrustedUIStatus();
                reply.writeNoException();
                reply.writeInt(trustedUIStatus ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                sendTUIExitCmd();
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result = sendTUICmd(data.readInt(), data.readInt(), data.createIntArray());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITrustedUIService {
            public static ITrustedUIService sDefaultImpl;
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

            @Override // android.os.ITrustedUIService
            public boolean getTrustedUIStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTrustedUIStatus();
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

            @Override // android.os.ITrustedUIService
            public void sendTUIExitCmd() throws RemoteException {
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
                    Stub.getDefaultImpl().sendTUIExitCmd();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.ITrustedUIService
            public int sendTUICmd(int event_type, int value, int[] screen_info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event_type);
                    _data.writeInt(value);
                    _data.writeIntArray(screen_info);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendTUICmd(event_type, value, screen_info);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITrustedUIService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITrustedUIService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
