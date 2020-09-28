package com.huawei.android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGameObserver extends IInterface {
    void onGameListChanged() throws RemoteException;

    void onGameStatusChanged(String str, int i) throws RemoteException;

    public static class Default implements IGameObserver {
        @Override // com.huawei.android.app.IGameObserver
        public void onGameListChanged() throws RemoteException {
        }

        @Override // com.huawei.android.app.IGameObserver
        public void onGameStatusChanged(String packageName, int event) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGameObserver {
        private static final String DESCRIPTOR = "com.huawei.android.app.IGameObserver";
        static final int TRANSACTION_onGameListChanged = 1;
        static final int TRANSACTION_onGameStatusChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGameObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGameObserver)) {
                return new Proxy(obj);
            }
            return (IGameObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onGameListChanged";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onGameStatusChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onGameListChanged();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onGameStatusChanged(data.readString(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGameObserver {
            public static IGameObserver sDefaultImpl;
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

            @Override // com.huawei.android.app.IGameObserver
            public void onGameListChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGameListChanged();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IGameObserver
            public void onGameStatusChanged(String packageName, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(event);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGameStatusChanged(packageName, event);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGameObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGameObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
