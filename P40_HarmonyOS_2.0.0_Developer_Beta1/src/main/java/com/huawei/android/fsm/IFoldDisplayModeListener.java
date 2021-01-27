package com.huawei.android.fsm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFoldDisplayModeListener extends IInterface {
    void onScreenDisplayModeChange(int i) throws RemoteException;

    public static class Default implements IFoldDisplayModeListener {
        @Override // com.huawei.android.fsm.IFoldDisplayModeListener
        public void onScreenDisplayModeChange(int displayMode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFoldDisplayModeListener {
        private static final String DESCRIPTOR = "com.huawei.android.fsm.IFoldDisplayModeListener";
        static final int TRANSACTION_onScreenDisplayModeChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFoldDisplayModeListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFoldDisplayModeListener)) {
                return new Proxy(obj);
            }
            return (IFoldDisplayModeListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onScreenDisplayModeChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onScreenDisplayModeChange(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IFoldDisplayModeListener {
            public static IFoldDisplayModeListener sDefaultImpl;
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

            @Override // com.huawei.android.fsm.IFoldDisplayModeListener
            public void onScreenDisplayModeChange(int displayMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayMode);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onScreenDisplayModeChange(displayMode);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IFoldDisplayModeListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFoldDisplayModeListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
