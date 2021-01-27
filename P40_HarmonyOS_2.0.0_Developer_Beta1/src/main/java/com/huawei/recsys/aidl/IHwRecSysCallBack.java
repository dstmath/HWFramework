package com.huawei.recsys.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwRecSysCallBack extends IInterface {
    void onConfigResult(int i, String str) throws RemoteException;

    void onRecResult(HwObjectContainer hwObjectContainer) throws RemoteException;

    public static class Default implements IHwRecSysCallBack {
        @Override // com.huawei.recsys.aidl.IHwRecSysCallBack
        public void onRecResult(HwObjectContainer hwObjectContainer) throws RemoteException {
        }

        @Override // com.huawei.recsys.aidl.IHwRecSysCallBack
        public void onConfigResult(int resCode, String message) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwRecSysCallBack {
        private static final String DESCRIPTOR = "com.huawei.recsys.aidl.IHwRecSysCallBack";
        static final int TRANSACTION_onConfigResult = 2;
        static final int TRANSACTION_onRecResult = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            HwObjectContainer _arg0;
            if (code == TRANSACTION_onRecResult) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = HwObjectContainer.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onRecResult(_arg0);
                reply.writeNoException();
                return true;
            } else if (code == TRANSACTION_onConfigResult) {
                data.enforceInterface(DESCRIPTOR);
                onConfigResult(data.readInt(), data.readString());
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
        public static class Proxy implements IHwRecSysCallBack {
            public static IHwRecSysCallBack sDefaultImpl;
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

            @Override // com.huawei.recsys.aidl.IHwRecSysCallBack
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
                    if (this.mRemote.transact(Stub.TRANSACTION_onRecResult, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRecResult(hwObjectContainer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.recsys.aidl.IHwRecSysCallBack
            public void onConfigResult(int resCode, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resCode);
                    _data.writeString(message);
                    if (this.mRemote.transact(Stub.TRANSACTION_onConfigResult, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onConfigResult(resCode, message);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwRecSysCallBack impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwRecSysCallBack getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
