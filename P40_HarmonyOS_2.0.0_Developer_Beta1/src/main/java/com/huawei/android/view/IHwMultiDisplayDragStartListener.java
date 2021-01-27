package com.huawei.android.view;

import android.content.ClipData;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwMultiDisplayDragStartListener extends IInterface {
    void onDragStart(ClipData clipData) throws RemoteException;

    public static class Default implements IHwMultiDisplayDragStartListener {
        @Override // com.huawei.android.view.IHwMultiDisplayDragStartListener
        public void onDragStart(ClipData data) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwMultiDisplayDragStartListener {
        private static final String DESCRIPTOR = "com.huawei.android.view.IHwMultiDisplayDragStartListener";
        static final int TRANSACTION_onDragStart = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwMultiDisplayDragStartListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwMultiDisplayDragStartListener)) {
                return new Proxy(obj);
            }
            return (IHwMultiDisplayDragStartListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onDragStart";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ClipData _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ClipData.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onDragStart(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwMultiDisplayDragStartListener {
            public static IHwMultiDisplayDragStartListener sDefaultImpl;
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

            @Override // com.huawei.android.view.IHwMultiDisplayDragStartListener
            public void onDragStart(ClipData data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDragStart(data);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwMultiDisplayDragStartListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwMultiDisplayDragStartListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
