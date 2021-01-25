package com.huawei.android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwMultiDisplayPhoneOperateListener extends IInterface {
    void onOperateOnPhone() throws RemoteException;

    public static class Default implements IHwMultiDisplayPhoneOperateListener {
        @Override // com.huawei.android.view.IHwMultiDisplayPhoneOperateListener
        public void onOperateOnPhone() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwMultiDisplayPhoneOperateListener {
        private static final String DESCRIPTOR = "com.huawei.android.view.IHwMultiDisplayPhoneOperateListener";
        static final int TRANSACTION_onOperateOnPhone = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwMultiDisplayPhoneOperateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwMultiDisplayPhoneOperateListener)) {
                return new Proxy(obj);
            }
            return (IHwMultiDisplayPhoneOperateListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onOperateOnPhone";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onOperateOnPhone();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwMultiDisplayPhoneOperateListener {
            public static IHwMultiDisplayPhoneOperateListener sDefaultImpl;
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

            @Override // com.huawei.android.view.IHwMultiDisplayPhoneOperateListener
            public void onOperateOnPhone() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onOperateOnPhone();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwMultiDisplayPhoneOperateListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwMultiDisplayPhoneOperateListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
