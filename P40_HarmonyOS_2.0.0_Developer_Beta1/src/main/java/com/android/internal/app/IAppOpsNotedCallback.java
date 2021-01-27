package com.android.internal.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAppOpsNotedCallback extends IInterface {
    void opNoted(int i, int i2, String str, int i3) throws RemoteException;

    public static class Default implements IAppOpsNotedCallback {
        @Override // com.android.internal.app.IAppOpsNotedCallback
        public void opNoted(int op, int uid, String packageName, int mode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAppOpsNotedCallback {
        private static final String DESCRIPTOR = "com.android.internal.app.IAppOpsNotedCallback";
        static final int TRANSACTION_opNoted = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppOpsNotedCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppOpsNotedCallback)) {
                return new Proxy(obj);
            }
            return (IAppOpsNotedCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "opNoted";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                opNoted(data.readInt(), data.readInt(), data.readString(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAppOpsNotedCallback {
            public static IAppOpsNotedCallback sDefaultImpl;
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

            @Override // com.android.internal.app.IAppOpsNotedCallback
            public void opNoted(int op, int uid, String packageName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(op);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().opNoted(op, uid, packageName, mode);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAppOpsNotedCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAppOpsNotedCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
