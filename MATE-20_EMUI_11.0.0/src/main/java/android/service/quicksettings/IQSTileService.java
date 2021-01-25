package android.service.quicksettings;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IQSTileService extends IInterface {
    void onClick(IBinder iBinder) throws RemoteException;

    void onStartListening() throws RemoteException;

    void onStopListening() throws RemoteException;

    void onTileAdded() throws RemoteException;

    void onTileRemoved() throws RemoteException;

    void onUnlockComplete() throws RemoteException;

    public static class Default implements IQSTileService {
        @Override // android.service.quicksettings.IQSTileService
        public void onTileAdded() throws RemoteException {
        }

        @Override // android.service.quicksettings.IQSTileService
        public void onTileRemoved() throws RemoteException {
        }

        @Override // android.service.quicksettings.IQSTileService
        public void onStartListening() throws RemoteException {
        }

        @Override // android.service.quicksettings.IQSTileService
        public void onStopListening() throws RemoteException {
        }

        @Override // android.service.quicksettings.IQSTileService
        public void onClick(IBinder wtoken) throws RemoteException {
        }

        @Override // android.service.quicksettings.IQSTileService
        public void onUnlockComplete() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IQSTileService {
        private static final String DESCRIPTOR = "android.service.quicksettings.IQSTileService";
        static final int TRANSACTION_onClick = 5;
        static final int TRANSACTION_onStartListening = 3;
        static final int TRANSACTION_onStopListening = 4;
        static final int TRANSACTION_onTileAdded = 1;
        static final int TRANSACTION_onTileRemoved = 2;
        static final int TRANSACTION_onUnlockComplete = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IQSTileService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IQSTileService)) {
                return new Proxy(obj);
            }
            return (IQSTileService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onTileAdded";
                case 2:
                    return "onTileRemoved";
                case 3:
                    return "onStartListening";
                case 4:
                    return "onStopListening";
                case 5:
                    return "onClick";
                case 6:
                    return "onUnlockComplete";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onTileAdded();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onTileRemoved();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onStartListening();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onStopListening();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onClick(data.readStrongBinder());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onUnlockComplete();
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
        public static class Proxy implements IQSTileService {
            public static IQSTileService sDefaultImpl;
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

            @Override // android.service.quicksettings.IQSTileService
            public void onTileAdded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTileAdded();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.quicksettings.IQSTileService
            public void onTileRemoved() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTileRemoved();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.quicksettings.IQSTileService
            public void onStartListening() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStartListening();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.quicksettings.IQSTileService
            public void onStopListening() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStopListening();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.quicksettings.IQSTileService
            public void onClick(IBinder wtoken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(wtoken);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onClick(wtoken);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.quicksettings.IQSTileService
            public void onUnlockComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUnlockComplete();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IQSTileService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IQSTileService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
