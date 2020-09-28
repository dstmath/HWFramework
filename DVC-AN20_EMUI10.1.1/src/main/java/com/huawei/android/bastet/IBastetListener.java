package com.huawei.android.bastet;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBastetListener extends IInterface {
    void onProxyIndicateMessage(int i, int i2, int i3) throws RemoteException;

    public static class Default implements IBastetListener {
        @Override // com.huawei.android.bastet.IBastetListener
        public void onProxyIndicateMessage(int proxyId, int err, int ext) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBastetListener {
        private static final String DESCRIPTOR = "com.huawei.android.bastet.IBastetListener";
        static final int TRANSACTION_onProxyIndicateMessage = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBastetListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBastetListener)) {
                return new Proxy(obj);
            }
            return (IBastetListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onProxyIndicateMessage(data.readInt(), data.readInt(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBastetListener {
            public static IBastetListener sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.huawei.android.bastet.IBastetListener
            public void onProxyIndicateMessage(int proxyId, int err, int ext) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(proxyId);
                    _data.writeInt(err);
                    _data.writeInt(ext);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProxyIndicateMessage(proxyId, err, ext);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBastetListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBastetListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
