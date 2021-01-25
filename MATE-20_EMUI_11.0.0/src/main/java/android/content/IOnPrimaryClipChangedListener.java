package android.content;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOnPrimaryClipChangedListener extends IInterface {
    void dispatchPrimaryClipChanged() throws RemoteException;

    public static class Default implements IOnPrimaryClipChangedListener {
        @Override // android.content.IOnPrimaryClipChangedListener
        public void dispatchPrimaryClipChanged() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOnPrimaryClipChangedListener {
        private static final String DESCRIPTOR = "android.content.IOnPrimaryClipChangedListener";
        static final int TRANSACTION_dispatchPrimaryClipChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnPrimaryClipChangedListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnPrimaryClipChangedListener)) {
                return new Proxy(obj);
            }
            return (IOnPrimaryClipChangedListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "dispatchPrimaryClipChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                dispatchPrimaryClipChanged();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOnPrimaryClipChangedListener {
            public static IOnPrimaryClipChangedListener sDefaultImpl;
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

            @Override // android.content.IOnPrimaryClipChangedListener
            public void dispatchPrimaryClipChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchPrimaryClipChanged();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOnPrimaryClipChangedListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOnPrimaryClipChangedListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
