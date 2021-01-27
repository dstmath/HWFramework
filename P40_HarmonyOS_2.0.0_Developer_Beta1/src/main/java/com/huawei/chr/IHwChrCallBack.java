package com.huawei.chr;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;

public interface IHwChrCallBack extends IInterface {
    void notifyMsg(int i, PersistableBundle persistableBundle) throws RemoteException;

    public static class Default implements IHwChrCallBack {
        @Override // com.huawei.chr.IHwChrCallBack
        public void notifyMsg(int faultId, PersistableBundle data) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwChrCallBack {
        private static final String DESCRIPTOR = "com.huawei.chr.IHwChrCallBack";
        static final int TRANSACTION_notifyMsg = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwChrCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwChrCallBack)) {
                return new Proxy(obj);
            }
            return (IHwChrCallBack) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PersistableBundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                notifyMsg(_arg0, _arg1);
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
        public static class Proxy implements IHwChrCallBack {
            public static IHwChrCallBack sDefaultImpl;
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

            @Override // com.huawei.chr.IHwChrCallBack
            public void notifyMsg(int faultId, PersistableBundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(faultId);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyMsg(faultId, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwChrCallBack impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwChrCallBack getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
