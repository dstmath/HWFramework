package com.huawei.recsys.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwRecSysCallBack extends IInterface {

    public static abstract class Stub extends Binder implements IHwRecSysCallBack {
        private static final String DESCRIPTOR = "com.huawei.recsys.aidl.IHwRecSysCallBack";
        static final int TRANSACTION_onConfigResult = 2;
        static final int TRANSACTION_onRecResult = 1;

        private static class Proxy implements IHwRecSysCallBack {
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

            public void onRecResult(HwObjectContainer hwObjectContainer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hwObjectContainer != null) {
                        _data.writeInt(Stub.TRANSACTION_onRecResult);
                        hwObjectContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onRecResult, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onConfigResult(int resCode, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resCode);
                    _data.writeString(message);
                    this.mRemote.transact(Stub.TRANSACTION_onConfigResult, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwRecSysCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwRecSysCallBack)) {
                return new Proxy(obj);
            }
            return (IHwRecSysCallBack) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onRecResult /*1*/:
                    HwObjectContainer _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (HwObjectContainer) HwObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onRecResult(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onConfigResult /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onConfigResult(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onConfigResult(int i, String str) throws RemoteException;

    void onRecResult(HwObjectContainer hwObjectContainer) throws RemoteException;
}
