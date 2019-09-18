package com.huawei.hilink.framework.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IResponseCallback extends IInterface {

    public static abstract class Stub extends Binder implements IResponseCallback {
        private static final String DESCRIPTOR = "com.huawei.hilink.framework.aidl.IResponseCallback";
        static final int TRANSACTION_onRecieveError = 2;
        static final int TRANSACTION_onRecieveResponse = 1;

        private static class Proxy implements IResponseCallback {
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

            public void onRecieveResponse(int requestID, String payload) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestID);
                    _data.writeString(payload);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRecieveError(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IResponseCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IResponseCallback)) {
                return new Proxy(obj);
            }
            return (IResponseCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onRecieveResponse(data.readInt(), data.readString());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onRecieveError(data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onRecieveError(int i) throws RemoteException;

    void onRecieveResponse(int i, String str) throws RemoteException;
}
