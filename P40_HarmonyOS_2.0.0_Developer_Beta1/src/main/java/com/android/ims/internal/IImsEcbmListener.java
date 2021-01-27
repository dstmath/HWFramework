package com.android.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IImsEcbmListener extends IInterface {
    void enteredECBM() throws RemoteException;

    void exitedECBM() throws RemoteException;

    public static class Default implements IImsEcbmListener {
        @Override // com.android.ims.internal.IImsEcbmListener
        public void enteredECBM() throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsEcbmListener
        public void exitedECBM() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImsEcbmListener {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsEcbmListener";
        static final int TRANSACTION_enteredECBM = 1;
        static final int TRANSACTION_exitedECBM = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsEcbmListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsEcbmListener)) {
                return new Proxy(obj);
            }
            return (IImsEcbmListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "enteredECBM";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "exitedECBM";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                enteredECBM();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                exitedECBM();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IImsEcbmListener {
            public static IImsEcbmListener sDefaultImpl;
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

            @Override // com.android.ims.internal.IImsEcbmListener
            public void enteredECBM() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().enteredECBM();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsEcbmListener
            public void exitedECBM() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().exitedECBM();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IImsEcbmListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImsEcbmListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
