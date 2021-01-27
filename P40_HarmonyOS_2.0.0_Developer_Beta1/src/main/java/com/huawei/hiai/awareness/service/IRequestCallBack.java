package com.huawei.hiai.awareness.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRequestCallBack extends IInterface {
    void onRequestResult(RequestResult requestResult) throws RemoteException;

    public static abstract class Stub extends Binder implements IRequestCallBack {
        private static final String DESCRIPTOR = "com.huawei.hiai.awareness.service.IRequestCallBack";
        static final int TRANSACTION_onRequestResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRequestCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRequestCallBack)) {
                return new Proxy(obj);
            }
            return (IRequestCallBack) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RequestResult _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = RequestResult.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onRequestResult(_arg0);
                reply.writeNoException();
                if (_arg0 != null) {
                    reply.writeInt(1);
                    _arg0.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IRequestCallBack {
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

            @Override // com.huawei.hiai.awareness.service.IRequestCallBack
            public void onRequestResult(RequestResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        result.readFromParcel(_reply);
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
