package com.huawei.android.inputmethod;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwInputContentListener extends IInterface {
    void onReceivedComposingText(String str) throws RemoteException;

    void onReceivedInputContent(String str) throws RemoteException;

    public static class Default implements IHwInputContentListener {
        @Override // com.huawei.android.inputmethod.IHwInputContentListener
        public void onReceivedInputContent(String content) throws RemoteException {
        }

        @Override // com.huawei.android.inputmethod.IHwInputContentListener
        public void onReceivedComposingText(String content) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwInputContentListener {
        private static final String DESCRIPTOR = "com.huawei.android.inputmethod.IHwInputContentListener";
        static final int TRANSACTION_onReceivedComposingText = 2;
        static final int TRANSACTION_onReceivedInputContent = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwInputContentListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwInputContentListener)) {
                return new Proxy(obj);
            }
            return (IHwInputContentListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onReceivedInputContent";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onReceivedComposingText";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onReceivedInputContent(data.readString());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onReceivedComposingText(data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwInputContentListener {
            public static IHwInputContentListener sDefaultImpl;
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

            @Override // com.huawei.android.inputmethod.IHwInputContentListener
            public void onReceivedInputContent(String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(content);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReceivedInputContent(content);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.inputmethod.IHwInputContentListener
            public void onReceivedComposingText(String content) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(content);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReceivedComposingText(content);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwInputContentListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwInputContentListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
