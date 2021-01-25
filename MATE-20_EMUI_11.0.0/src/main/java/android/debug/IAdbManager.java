package android.debug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAdbManager extends IInterface {
    void allowDebugging(boolean z, String str) throws RemoteException;

    void clearDebuggingKeys() throws RemoteException;

    void denyDebugging() throws RemoteException;

    public static class Default implements IAdbManager {
        @Override // android.debug.IAdbManager
        public void allowDebugging(boolean alwaysAllow, String publicKey) throws RemoteException {
        }

        @Override // android.debug.IAdbManager
        public void denyDebugging() throws RemoteException {
        }

        @Override // android.debug.IAdbManager
        public void clearDebuggingKeys() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAdbManager {
        private static final String DESCRIPTOR = "android.debug.IAdbManager";
        static final int TRANSACTION_allowDebugging = 1;
        static final int TRANSACTION_clearDebuggingKeys = 3;
        static final int TRANSACTION_denyDebugging = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAdbManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAdbManager)) {
                return new Proxy(obj);
            }
            return (IAdbManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "allowDebugging";
            }
            if (transactionCode == 2) {
                return "denyDebugging";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "clearDebuggingKeys";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                allowDebugging(data.readInt() != 0, data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                denyDebugging();
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                clearDebuggingKeys();
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
        public static class Proxy implements IAdbManager {
            public static IAdbManager sDefaultImpl;
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

            @Override // android.debug.IAdbManager
            public void allowDebugging(boolean alwaysAllow, String publicKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(alwaysAllow ? 1 : 0);
                    _data.writeString(publicKey);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().allowDebugging(alwaysAllow, publicKey);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.debug.IAdbManager
            public void denyDebugging() throws RemoteException {
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
                    Stub.getDefaultImpl().denyDebugging();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.debug.IAdbManager
            public void clearDebuggingKeys() throws RemoteException {
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
                    Stub.getDefaultImpl().clearDebuggingKeys();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAdbManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAdbManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
