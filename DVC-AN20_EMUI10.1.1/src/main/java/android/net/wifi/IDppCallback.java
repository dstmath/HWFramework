package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDppCallback extends IInterface {
    void onFailure(int i) throws RemoteException;

    void onProgress(int i) throws RemoteException;

    void onSuccess(int i) throws RemoteException;

    void onSuccessConfigReceived(int i) throws RemoteException;

    public static class Default implements IDppCallback {
        @Override // android.net.wifi.IDppCallback
        public void onSuccessConfigReceived(int newNetworkId) throws RemoteException {
        }

        @Override // android.net.wifi.IDppCallback
        public void onSuccess(int status) throws RemoteException {
        }

        @Override // android.net.wifi.IDppCallback
        public void onFailure(int status) throws RemoteException {
        }

        @Override // android.net.wifi.IDppCallback
        public void onProgress(int status) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDppCallback {
        private static final String DESCRIPTOR = "android.net.wifi.IDppCallback";
        static final int TRANSACTION_onFailure = 3;
        static final int TRANSACTION_onProgress = 4;
        static final int TRANSACTION_onSuccess = 2;
        static final int TRANSACTION_onSuccessConfigReceived = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDppCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDppCallback)) {
                return new Proxy(obj);
            }
            return (IDppCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onSuccessConfigReceived";
            }
            if (transactionCode == 2) {
                return "onSuccess";
            }
            if (transactionCode == 3) {
                return "onFailure";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onProgress";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onSuccessConfigReceived(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onSuccess(data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onFailure(data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onProgress(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDppCallback {
            public static IDppCallback sDefaultImpl;
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

            @Override // android.net.wifi.IDppCallback
            public void onSuccessConfigReceived(int newNetworkId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(newNetworkId);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSuccessConfigReceived(newNetworkId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IDppCallback
            public void onSuccess(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSuccess(status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IDppCallback
            public void onFailure(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFailure(status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IDppCallback
            public void onProgress(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProgress(status);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDppCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDppCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
