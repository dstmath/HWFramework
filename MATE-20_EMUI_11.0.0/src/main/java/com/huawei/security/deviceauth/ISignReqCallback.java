package com.huawei.security.deviceauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISignReqCallback extends IInterface {
    void onSignFinish(long j, int i, byte[] bArr) throws RemoteException;

    public static abstract class Stub extends Binder implements ISignReqCallback {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.ISignReqCallback";
        static final int TRANSACTION_onSignFinish = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISignReqCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISignReqCallback)) {
                return new Proxy(obj);
            }
            return (ISignReqCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onSignFinish(data.readLong(), data.readInt(), data.createByteArray());
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
        public static class Proxy implements ISignReqCallback {
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

            @Override // com.huawei.security.deviceauth.ISignReqCallback
            public void onSignFinish(long signReqId, int authForm, byte[] signReturn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(signReqId);
                    _data.writeInt(authForm);
                    _data.writeByteArray(signReturn);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
