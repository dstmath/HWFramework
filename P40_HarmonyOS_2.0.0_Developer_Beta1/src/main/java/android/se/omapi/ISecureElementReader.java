package android.se.omapi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.se.omapi.ISecureElementSession;

public interface ISecureElementReader extends IInterface {
    void closeSessions() throws RemoteException;

    boolean isSecureElementPresent() throws RemoteException;

    ISecureElementSession openSession() throws RemoteException;

    boolean reset() throws RemoteException;

    public static class Default implements ISecureElementReader {
        @Override // android.se.omapi.ISecureElementReader
        public boolean isSecureElementPresent() throws RemoteException {
            return false;
        }

        @Override // android.se.omapi.ISecureElementReader
        public ISecureElementSession openSession() throws RemoteException {
            return null;
        }

        @Override // android.se.omapi.ISecureElementReader
        public void closeSessions() throws RemoteException {
        }

        @Override // android.se.omapi.ISecureElementReader
        public boolean reset() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISecureElementReader {
        private static final String DESCRIPTOR = "android.se.omapi.ISecureElementReader";
        static final int TRANSACTION_closeSessions = 3;
        static final int TRANSACTION_isSecureElementPresent = 1;
        static final int TRANSACTION_openSession = 2;
        static final int TRANSACTION_reset = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecureElementReader asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecureElementReader)) {
                return new Proxy(obj);
            }
            return (ISecureElementReader) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "isSecureElementPresent";
            }
            if (transactionCode == 2) {
                return "openSession";
            }
            if (transactionCode == 3) {
                return "closeSessions";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "reset";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean isSecureElementPresent = isSecureElementPresent();
                reply.writeNoException();
                reply.writeInt(isSecureElementPresent ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                ISecureElementSession _result = openSession();
                reply.writeNoException();
                reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                closeSessions();
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                boolean reset = reset();
                reply.writeNoException();
                reply.writeInt(reset ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISecureElementReader {
            public static ISecureElementReader sDefaultImpl;
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

            @Override // android.se.omapi.ISecureElementReader
            public boolean isSecureElementPresent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSecureElementPresent();
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

            @Override // android.se.omapi.ISecureElementReader
            public ISecureElementSession openSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openSession();
                    }
                    _reply.readException();
                    ISecureElementSession _result = ISecureElementSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.se.omapi.ISecureElementReader
            public void closeSessions() throws RemoteException {
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
                    Stub.getDefaultImpl().closeSessions();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.se.omapi.ISecureElementReader
            public boolean reset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reset();
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
        }

        public static boolean setDefaultImpl(ISecureElementReader impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISecureElementReader getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
