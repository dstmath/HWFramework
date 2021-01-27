package android.hardware.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IContextHubClientCallback extends IInterface {
    void onHubReset() throws RemoteException;

    void onMessageFromNanoApp(NanoAppMessage nanoAppMessage) throws RemoteException;

    void onNanoAppAborted(long j, int i) throws RemoteException;

    void onNanoAppDisabled(long j) throws RemoteException;

    void onNanoAppEnabled(long j) throws RemoteException;

    void onNanoAppLoaded(long j) throws RemoteException;

    void onNanoAppUnloaded(long j) throws RemoteException;

    public static class Default implements IContextHubClientCallback {
        @Override // android.hardware.location.IContextHubClientCallback
        public void onMessageFromNanoApp(NanoAppMessage message) throws RemoteException {
        }

        @Override // android.hardware.location.IContextHubClientCallback
        public void onHubReset() throws RemoteException {
        }

        @Override // android.hardware.location.IContextHubClientCallback
        public void onNanoAppAborted(long nanoAppId, int abortCode) throws RemoteException {
        }

        @Override // android.hardware.location.IContextHubClientCallback
        public void onNanoAppLoaded(long nanoAppId) throws RemoteException {
        }

        @Override // android.hardware.location.IContextHubClientCallback
        public void onNanoAppUnloaded(long nanoAppId) throws RemoteException {
        }

        @Override // android.hardware.location.IContextHubClientCallback
        public void onNanoAppEnabled(long nanoAppId) throws RemoteException {
        }

        @Override // android.hardware.location.IContextHubClientCallback
        public void onNanoAppDisabled(long nanoAppId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IContextHubClientCallback {
        private static final String DESCRIPTOR = "android.hardware.location.IContextHubClientCallback";
        static final int TRANSACTION_onHubReset = 2;
        static final int TRANSACTION_onMessageFromNanoApp = 1;
        static final int TRANSACTION_onNanoAppAborted = 3;
        static final int TRANSACTION_onNanoAppDisabled = 7;
        static final int TRANSACTION_onNanoAppEnabled = 6;
        static final int TRANSACTION_onNanoAppLoaded = 4;
        static final int TRANSACTION_onNanoAppUnloaded = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IContextHubClientCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IContextHubClientCallback)) {
                return new Proxy(obj);
            }
            return (IContextHubClientCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onMessageFromNanoApp";
                case 2:
                    return "onHubReset";
                case 3:
                    return "onNanoAppAborted";
                case 4:
                    return "onNanoAppLoaded";
                case 5:
                    return "onNanoAppUnloaded";
                case 6:
                    return "onNanoAppEnabled";
                case 7:
                    return "onNanoAppDisabled";
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
            NanoAppMessage _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = NanoAppMessage.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onMessageFromNanoApp(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onHubReset();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppAborted(data.readLong(), data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppLoaded(data.readLong());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppUnloaded(data.readLong());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppEnabled(data.readLong());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onNanoAppDisabled(data.readLong());
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
        public static class Proxy implements IContextHubClientCallback {
            public static IContextHubClientCallback sDefaultImpl;
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

            @Override // android.hardware.location.IContextHubClientCallback
            public void onMessageFromNanoApp(NanoAppMessage message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (message != null) {
                        _data.writeInt(1);
                        message.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMessageFromNanoApp(message);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IContextHubClientCallback
            public void onHubReset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onHubReset();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppAborted(long nanoAppId, int abortCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    _data.writeInt(abortCode);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNanoAppAborted(nanoAppId, abortCode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppLoaded(long nanoAppId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNanoAppLoaded(nanoAppId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppUnloaded(long nanoAppId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNanoAppUnloaded(nanoAppId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppEnabled(long nanoAppId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNanoAppEnabled(nanoAppId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IContextHubClientCallback
            public void onNanoAppDisabled(long nanoAppId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nanoAppId);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNanoAppDisabled(nanoAppId);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IContextHubClientCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IContextHubClientCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
