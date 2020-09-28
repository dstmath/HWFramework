package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISearchManagerCallback extends IInterface {
    void onCancel() throws RemoteException;

    void onDismiss() throws RemoteException;

    public static class Default implements ISearchManagerCallback {
        @Override // android.app.ISearchManagerCallback
        public void onDismiss() throws RemoteException {
        }

        @Override // android.app.ISearchManagerCallback
        public void onCancel() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISearchManagerCallback {
        private static final String DESCRIPTOR = "android.app.ISearchManagerCallback";
        static final int TRANSACTION_onCancel = 2;
        static final int TRANSACTION_onDismiss = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISearchManagerCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISearchManagerCallback)) {
                return new Proxy(obj);
            }
            return (ISearchManagerCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onDismiss";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onCancel";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onDismiss();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onCancel();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISearchManagerCallback {
            public static ISearchManagerCallback sDefaultImpl;
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

            @Override // android.app.ISearchManagerCallback
            public void onDismiss() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDismiss();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.ISearchManagerCallback
            public void onCancel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCancel();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISearchManagerCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISearchManagerCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
