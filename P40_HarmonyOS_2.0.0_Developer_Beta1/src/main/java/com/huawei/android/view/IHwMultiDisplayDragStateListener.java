package com.huawei.android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwMultiDisplayDragStateListener extends IInterface {
    void updateDragState(int i) throws RemoteException;

    public static class Default implements IHwMultiDisplayDragStateListener {
        @Override // com.huawei.android.view.IHwMultiDisplayDragStateListener
        public void updateDragState(int dragState) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwMultiDisplayDragStateListener {
        private static final String DESCRIPTOR = "com.huawei.android.view.IHwMultiDisplayDragStateListener";
        static final int TRANSACTION_updateDragState = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwMultiDisplayDragStateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwMultiDisplayDragStateListener)) {
                return new Proxy(obj);
            }
            return (IHwMultiDisplayDragStateListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "updateDragState";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                updateDragState(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwMultiDisplayDragStateListener {
            public static IHwMultiDisplayDragStateListener sDefaultImpl;
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

            @Override // com.huawei.android.view.IHwMultiDisplayDragStateListener
            public void updateDragState(int dragState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dragState);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateDragState(dragState);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwMultiDisplayDragStateListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwMultiDisplayDragStateListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
