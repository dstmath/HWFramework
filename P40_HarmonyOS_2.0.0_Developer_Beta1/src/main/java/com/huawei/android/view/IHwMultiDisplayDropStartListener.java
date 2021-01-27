package com.huawei.android.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.DragEvent;

public interface IHwMultiDisplayDropStartListener extends IInterface {
    void onDropStart(DragEvent dragEvent) throws RemoteException;

    void setOriginalDropPoint(float f, float f2) throws RemoteException;

    public static class Default implements IHwMultiDisplayDropStartListener {
        @Override // com.huawei.android.view.IHwMultiDisplayDropStartListener
        public void onDropStart(DragEvent evt) throws RemoteException {
        }

        @Override // com.huawei.android.view.IHwMultiDisplayDropStartListener
        public void setOriginalDropPoint(float x, float y) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwMultiDisplayDropStartListener {
        private static final String DESCRIPTOR = "com.huawei.android.view.IHwMultiDisplayDropStartListener";
        static final int TRANSACTION_onDropStart = 1;
        static final int TRANSACTION_setOriginalDropPoint = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwMultiDisplayDropStartListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwMultiDisplayDropStartListener)) {
                return new Proxy(obj);
            }
            return (IHwMultiDisplayDropStartListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onDropStart";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "setOriginalDropPoint";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DragEvent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = DragEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onDropStart(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                setOriginalDropPoint(data.readFloat(), data.readFloat());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwMultiDisplayDropStartListener {
            public static IHwMultiDisplayDropStartListener sDefaultImpl;
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

            @Override // com.huawei.android.view.IHwMultiDisplayDropStartListener
            public void onDropStart(DragEvent evt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (evt != null) {
                        _data.writeInt(1);
                        evt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDropStart(evt);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.view.IHwMultiDisplayDropStartListener
            public void setOriginalDropPoint(float x, float y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(x);
                    _data.writeFloat(y);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setOriginalDropPoint(x, y);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwMultiDisplayDropStartListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwMultiDisplayDropStartListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
