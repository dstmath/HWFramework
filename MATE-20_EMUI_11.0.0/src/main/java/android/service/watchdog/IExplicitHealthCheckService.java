package android.service.watchdog;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteCallback;
import android.os.RemoteException;

public interface IExplicitHealthCheckService extends IInterface {
    void cancel(String str) throws RemoteException;

    void getRequestedPackages(RemoteCallback remoteCallback) throws RemoteException;

    void getSupportedPackages(RemoteCallback remoteCallback) throws RemoteException;

    void request(String str) throws RemoteException;

    void setCallback(RemoteCallback remoteCallback) throws RemoteException;

    public static class Default implements IExplicitHealthCheckService {
        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void setCallback(RemoteCallback callback) throws RemoteException {
        }

        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void request(String packageName) throws RemoteException {
        }

        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void cancel(String packageName) throws RemoteException {
        }

        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void getSupportedPackages(RemoteCallback callback) throws RemoteException {
        }

        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void getRequestedPackages(RemoteCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IExplicitHealthCheckService {
        private static final String DESCRIPTOR = "android.service.watchdog.IExplicitHealthCheckService";
        static final int TRANSACTION_cancel = 3;
        static final int TRANSACTION_getRequestedPackages = 5;
        static final int TRANSACTION_getSupportedPackages = 4;
        static final int TRANSACTION_request = 2;
        static final int TRANSACTION_setCallback = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IExplicitHealthCheckService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IExplicitHealthCheckService)) {
                return new Proxy(obj);
            }
            return (IExplicitHealthCheckService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "setCallback";
            }
            if (transactionCode == 2) {
                return "request";
            }
            if (transactionCode == 3) {
                return "cancel";
            }
            if (transactionCode == 4) {
                return "getSupportedPackages";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "getRequestedPackages";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RemoteCallback _arg0;
            RemoteCallback _arg02;
            RemoteCallback _arg03;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = RemoteCallback.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                setCallback(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                request(data.readString());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                cancel(data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = RemoteCallback.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                getSupportedPackages(_arg02);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = RemoteCallback.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                getRequestedPackages(_arg03);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IExplicitHealthCheckService {
            public static IExplicitHealthCheckService sDefaultImpl;
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

            @Override // android.service.watchdog.IExplicitHealthCheckService
            public void setCallback(RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setCallback(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.watchdog.IExplicitHealthCheckService
            public void request(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().request(packageName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.watchdog.IExplicitHealthCheckService
            public void cancel(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancel(packageName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.watchdog.IExplicitHealthCheckService
            public void getSupportedPackages(RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getSupportedPackages(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.watchdog.IExplicitHealthCheckService
            public void getRequestedPackages(RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getRequestedPackages(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IExplicitHealthCheckService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IExplicitHealthCheckService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
