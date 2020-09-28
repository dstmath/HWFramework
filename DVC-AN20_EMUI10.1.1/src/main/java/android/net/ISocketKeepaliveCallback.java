package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISocketKeepaliveCallback extends IInterface {
    void onDataReceived() throws RemoteException;

    void onError(int i) throws RemoteException;

    void onStarted(int i) throws RemoteException;

    void onStopped() throws RemoteException;

    public static class Default implements ISocketKeepaliveCallback {
        @Override // android.net.ISocketKeepaliveCallback
        public void onStarted(int slot) throws RemoteException {
        }

        @Override // android.net.ISocketKeepaliveCallback
        public void onStopped() throws RemoteException {
        }

        @Override // android.net.ISocketKeepaliveCallback
        public void onError(int error) throws RemoteException {
        }

        @Override // android.net.ISocketKeepaliveCallback
        public void onDataReceived() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISocketKeepaliveCallback {
        private static final String DESCRIPTOR = "android.net.ISocketKeepaliveCallback";
        static final int TRANSACTION_onDataReceived = 4;
        static final int TRANSACTION_onError = 3;
        static final int TRANSACTION_onStarted = 1;
        static final int TRANSACTION_onStopped = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISocketKeepaliveCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISocketKeepaliveCallback)) {
                return new Proxy(obj);
            }
            return (ISocketKeepaliveCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onStarted";
            }
            if (transactionCode == 2) {
                return "onStopped";
            }
            if (transactionCode == 3) {
                return "onError";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onDataReceived";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onStarted(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onStopped();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onError(data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onDataReceived();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISocketKeepaliveCallback {
            public static ISocketKeepaliveCallback sDefaultImpl;
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

            @Override // android.net.ISocketKeepaliveCallback
            public void onStarted(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStarted(slot);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ISocketKeepaliveCallback
            public void onStopped() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStopped();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ISocketKeepaliveCallback
            public void onError(int error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(error);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError(error);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ISocketKeepaliveCallback
            public void onDataReceived() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDataReceived();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISocketKeepaliveCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISocketKeepaliveCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
