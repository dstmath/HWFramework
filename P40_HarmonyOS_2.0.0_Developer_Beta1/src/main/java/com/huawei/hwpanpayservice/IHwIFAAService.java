package com.huawei.hwpanpayservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.hwpanpayservice.IHwIFAAServiceCallBack;

public interface IHwIFAAService extends IInterface {
    void processCmd(IHwIFAAServiceCallBack iHwIFAAServiceCallBack, byte[] bArr) throws RemoteException;

    public static class Default implements IHwIFAAService {
        @Override // com.huawei.hwpanpayservice.IHwIFAAService
        public void processCmd(IHwIFAAServiceCallBack callBack, byte[] data) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwIFAAService {
        private static final String DESCRIPTOR = "com.huawei.hwpanpayservice.IHwIFAAService";
        static final int TRANSACTION_processCmd = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwIFAAService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwIFAAService)) {
                return new Proxy(obj);
            }
            return (IHwIFAAService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                processCmd(IHwIFAAServiceCallBack.Stub.asInterface(data.readStrongBinder()), data.createByteArray());
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
        public static class Proxy implements IHwIFAAService {
            public static IHwIFAAService sDefaultImpl;
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

            @Override // com.huawei.hwpanpayservice.IHwIFAAService
            public void processCmd(IHwIFAAServiceCallBack callBack, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    _data.writeByteArray(data);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().processCmd(callBack, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwIFAAService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwIFAAService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
