package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUserSwitchObserver extends IInterface {
    void onForegroundProfileSwitch(int i) throws RemoteException;

    void onLockedBootComplete(int i) throws RemoteException;

    void onUserSwitchComplete(int i) throws RemoteException;

    void onUserSwitching(int i, IRemoteCallback iRemoteCallback) throws RemoteException;

    public static class Default implements IUserSwitchObserver {
        @Override // android.app.IUserSwitchObserver
        public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
        }

        @Override // android.app.IUserSwitchObserver
        public void onUserSwitchComplete(int newUserId) throws RemoteException {
        }

        @Override // android.app.IUserSwitchObserver
        public void onForegroundProfileSwitch(int newProfileId) throws RemoteException {
        }

        @Override // android.app.IUserSwitchObserver
        public void onLockedBootComplete(int newUserId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IUserSwitchObserver {
        private static final String DESCRIPTOR = "android.app.IUserSwitchObserver";
        static final int TRANSACTION_onForegroundProfileSwitch = 3;
        static final int TRANSACTION_onLockedBootComplete = 4;
        static final int TRANSACTION_onUserSwitchComplete = 2;
        static final int TRANSACTION_onUserSwitching = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUserSwitchObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUserSwitchObserver)) {
                return new Proxy(obj);
            }
            return (IUserSwitchObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onUserSwitching";
            }
            if (transactionCode == 2) {
                return "onUserSwitchComplete";
            }
            if (transactionCode == 3) {
                return "onForegroundProfileSwitch";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onLockedBootComplete";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onUserSwitching(data.readInt(), IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onUserSwitchComplete(data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onForegroundProfileSwitch(data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onLockedBootComplete(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IUserSwitchObserver {
            public static IUserSwitchObserver sDefaultImpl;
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

            @Override // android.app.IUserSwitchObserver
            public void onUserSwitching(int newUserId, IRemoteCallback reply) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newUserId);
                    _data.writeStrongBinder(reply != null ? reply.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUserSwitching(newUserId, reply);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IUserSwitchObserver
            public void onUserSwitchComplete(int newUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newUserId);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUserSwitchComplete(newUserId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IUserSwitchObserver
            public void onForegroundProfileSwitch(int newProfileId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newProfileId);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onForegroundProfileSwitch(newProfileId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IUserSwitchObserver
            public void onLockedBootComplete(int newUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newUserId);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLockedBootComplete(newUserId);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IUserSwitchObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IUserSwitchObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
