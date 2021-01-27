package com.android.internal.policy;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IKeyguardDismissCallback extends IInterface {
    void onDismissCancelled() throws RemoteException;

    void onDismissError() throws RemoteException;

    void onDismissSucceeded() throws RemoteException;

    public static class Default implements IKeyguardDismissCallback {
        @Override // com.android.internal.policy.IKeyguardDismissCallback
        public void onDismissError() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardDismissCallback
        public void onDismissSucceeded() throws RemoteException {
        }

        @Override // com.android.internal.policy.IKeyguardDismissCallback
        public void onDismissCancelled() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IKeyguardDismissCallback {
        private static final String DESCRIPTOR = "com.android.internal.policy.IKeyguardDismissCallback";
        static final int TRANSACTION_onDismissCancelled = 3;
        static final int TRANSACTION_onDismissError = 1;
        static final int TRANSACTION_onDismissSucceeded = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IKeyguardDismissCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IKeyguardDismissCallback)) {
                return new Proxy(obj);
            }
            return (IKeyguardDismissCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onDismissError";
            }
            if (transactionCode == 2) {
                return "onDismissSucceeded";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onDismissCancelled";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onDismissError();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onDismissSucceeded();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onDismissCancelled();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IKeyguardDismissCallback {
            public static IKeyguardDismissCallback sDefaultImpl;
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

            @Override // com.android.internal.policy.IKeyguardDismissCallback
            public void onDismissError() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDismissError();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardDismissCallback
            public void onDismissSucceeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDismissSucceeded();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IKeyguardDismissCallback
            public void onDismissCancelled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDismissCancelled();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IKeyguardDismissCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IKeyguardDismissCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
