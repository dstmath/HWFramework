package android.app.trust;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

public interface ITrustListener extends IInterface {
    void onTrustChanged(boolean z, int i, int i2) throws RemoteException;

    void onTrustError(CharSequence charSequence) throws RemoteException;

    void onTrustManagedChanged(boolean z, int i) throws RemoteException;

    public static class Default implements ITrustListener {
        @Override // android.app.trust.ITrustListener
        public void onTrustChanged(boolean enabled, int userId, int flags) throws RemoteException {
        }

        @Override // android.app.trust.ITrustListener
        public void onTrustManagedChanged(boolean managed, int userId) throws RemoteException {
        }

        @Override // android.app.trust.ITrustListener
        public void onTrustError(CharSequence message) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITrustListener {
        private static final String DESCRIPTOR = "android.app.trust.ITrustListener";
        static final int TRANSACTION_onTrustChanged = 1;
        static final int TRANSACTION_onTrustError = 3;
        static final int TRANSACTION_onTrustManagedChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustListener)) {
                return new Proxy(obj);
            }
            return (ITrustListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onTrustChanged";
            }
            if (transactionCode == 2) {
                return "onTrustManagedChanged";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onTrustError";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CharSequence _arg0;
            boolean _arg02 = false;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = true;
                }
                onTrustChanged(_arg02, data.readInt(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = true;
                }
                onTrustManagedChanged(_arg02, data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onTrustError(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITrustListener {
            public static ITrustListener sDefaultImpl;
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

            @Override // android.app.trust.ITrustListener
            public void onTrustChanged(boolean enabled, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTrustChanged(enabled, userId, flags);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.trust.ITrustListener
            public void onTrustManagedChanged(boolean managed, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(managed ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTrustManagedChanged(managed, userId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.trust.ITrustListener
            public void onTrustError(CharSequence message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (message != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(message, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTrustError(message);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITrustListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITrustListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
