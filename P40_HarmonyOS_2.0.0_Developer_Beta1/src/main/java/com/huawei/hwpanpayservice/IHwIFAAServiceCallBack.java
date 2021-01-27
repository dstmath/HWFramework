package com.huawei.hwpanpayservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwIFAAServiceCallBack extends IInterface {
    void processCmdResult(int i, byte[] bArr) throws RemoteException;

    public static class Default implements IHwIFAAServiceCallBack {
        @Override // com.huawei.hwpanpayservice.IHwIFAAServiceCallBack
        public void processCmdResult(int ret, byte[] data) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwIFAAServiceCallBack {
        private static final String DESCRIPTOR = "com.huawei.hwpanpayservice.IHwIFAAServiceCallBack";
        static final int TRANSACTION_processCmdResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwIFAAServiceCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwIFAAServiceCallBack)) {
                return new Proxy(obj);
            }
            return (IHwIFAAServiceCallBack) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                processCmdResult(data.readInt(), data.createByteArray());
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
        public static class Proxy implements IHwIFAAServiceCallBack {
            public static IHwIFAAServiceCallBack sDefaultImpl;
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

            @Override // com.huawei.hwpanpayservice.IHwIFAAServiceCallBack
            public void processCmdResult(int ret, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ret);
                    _data.writeByteArray(data);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().processCmdResult(ret, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwIFAAServiceCallBack impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwIFAAServiceCallBack getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
