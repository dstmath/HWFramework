package com.huawei.permission;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHsmMaliAppInfoListener extends IInterface {
    void onMaliAppInfoChanged(String str) throws RemoteException;

    public static class Default implements IHsmMaliAppInfoListener {
        @Override // com.huawei.permission.IHsmMaliAppInfoListener
        public void onMaliAppInfoChanged(String packageName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHsmMaliAppInfoListener {
        private static final String DESCRIPTOR = "com.huawei.permission.IHsmMaliAppInfoListener";
        static final int TRANSACTION_onMaliAppInfoChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHsmMaliAppInfoListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHsmMaliAppInfoListener)) {
                return new Proxy(obj);
            }
            return (IHsmMaliAppInfoListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onMaliAppInfoChanged(data.readString());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHsmMaliAppInfoListener {
            public static IHsmMaliAppInfoListener sDefaultImpl;
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

            @Override // com.huawei.permission.IHsmMaliAppInfoListener
            public void onMaliAppInfoChanged(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onMaliAppInfoChanged(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHsmMaliAppInfoListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHsmMaliAppInfoListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
