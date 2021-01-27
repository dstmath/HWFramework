package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUidObserver extends IInterface {
    void onUidActive(int i) throws RemoteException;

    void onUidCachedChanged(int i, boolean z) throws RemoteException;

    void onUidGone(int i, boolean z) throws RemoteException;

    void onUidIdle(int i, boolean z) throws RemoteException;

    void onUidStateChanged(int i, int i2, long j) throws RemoteException;

    public static class Default implements IUidObserver {
        @Override // android.app.IUidObserver
        public void onUidGone(int uid, boolean disabled) throws RemoteException {
        }

        @Override // android.app.IUidObserver
        public void onUidActive(int uid) throws RemoteException {
        }

        @Override // android.app.IUidObserver
        public void onUidIdle(int uid, boolean disabled) throws RemoteException {
        }

        @Override // android.app.IUidObserver
        public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {
        }

        @Override // android.app.IUidObserver
        public void onUidCachedChanged(int uid, boolean cached) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IUidObserver {
        private static final String DESCRIPTOR = "android.app.IUidObserver";
        static final int TRANSACTION_onUidActive = 2;
        static final int TRANSACTION_onUidCachedChanged = 5;
        static final int TRANSACTION_onUidGone = 1;
        static final int TRANSACTION_onUidIdle = 3;
        static final int TRANSACTION_onUidStateChanged = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUidObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUidObserver)) {
                return new Proxy(obj);
            }
            return (IUidObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onUidGone";
            }
            if (transactionCode == 2) {
                return "onUidActive";
            }
            if (transactionCode == 3) {
                return "onUidIdle";
            }
            if (transactionCode == 4) {
                return "onUidStateChanged";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onUidCachedChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg1 = false;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = true;
                }
                onUidGone(_arg0, _arg1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onUidActive(data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _arg02 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = true;
                }
                onUidIdle(_arg02, _arg1);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onUidStateChanged(data.readInt(), data.readInt(), data.readLong());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                int _arg03 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = true;
                }
                onUidCachedChanged(_arg03, _arg1);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IUidObserver {
            public static IUidObserver sDefaultImpl;
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

            @Override // android.app.IUidObserver
            public void onUidGone(int uid, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(disabled ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUidGone(uid, disabled);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IUidObserver
            public void onUidActive(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUidActive(uid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IUidObserver
            public void onUidIdle(int uid, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(disabled ? 1 : 0);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUidIdle(uid, disabled);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IUidObserver
            public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(procState);
                    _data.writeLong(procStateSeq);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUidStateChanged(uid, procState, procStateSeq);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IUidObserver
            public void onUidCachedChanged(int uid, boolean cached) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(cached ? 1 : 0);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUidCachedChanged(uid, cached);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IUidObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IUidObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
