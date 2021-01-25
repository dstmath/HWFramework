package com.huawei.android.view;

import android.content.ClipData;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwMultiDisplayBasicModeDragStartListener extends IInterface {
    void onDragStart(ClipData clipData, Bitmap bitmap) throws RemoteException;

    public static class Default implements IHwMultiDisplayBasicModeDragStartListener {
        @Override // com.huawei.android.view.IHwMultiDisplayBasicModeDragStartListener
        public void onDragStart(ClipData data, Bitmap bitmap) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwMultiDisplayBasicModeDragStartListener {
        private static final String DESCRIPTOR = "com.huawei.android.view.IHwMultiDisplayBasicModeDragStartListener";
        static final int TRANSACTION_onDragStart = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwMultiDisplayBasicModeDragStartListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwMultiDisplayBasicModeDragStartListener)) {
                return new Proxy(obj);
            }
            return (IHwMultiDisplayBasicModeDragStartListener) iin;
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
            Bitmap _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ClipData.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = Bitmap.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onDragStart(_arg0, _arg1);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwMultiDisplayBasicModeDragStartListener {
            public static IHwMultiDisplayBasicModeDragStartListener sDefaultImpl;
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

            @Override // com.huawei.android.view.IHwMultiDisplayBasicModeDragStartListener
            public void onDragStart(ClipData data, Bitmap bitmap) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bitmap != null) {
                        _data.writeInt(1);
                        bitmap.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDragStart(data, bitmap);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwMultiDisplayBasicModeDragStartListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwMultiDisplayBasicModeDragStartListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
