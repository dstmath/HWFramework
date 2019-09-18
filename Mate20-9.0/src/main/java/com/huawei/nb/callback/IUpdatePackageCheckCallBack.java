package com.huawei.nb.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nb.container.ObjectContainer;

public interface IUpdatePackageCheckCallBack extends IInterface {

    public static abstract class Stub extends Binder implements IUpdatePackageCheckCallBack {
        private static final String DESCRIPTOR = "com.huawei.nb.callback.IUpdatePackageCheckCallBack";
        static final int TRANSACTION_onFinish = 1;

        private static class Proxy implements IUpdatePackageCheckCallBack {
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

            public void onFinish(ObjectContainer oc, int networkType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oc != null) {
                        _data.writeInt(1);
                        oc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(networkType);
                    this.mRemote.transact(1, _data, _reply, 0);
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

        public static IUpdatePackageCheckCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUpdatePackageCheckCallBack)) {
                return new Proxy(obj);
            }
            return (IUpdatePackageCheckCallBack) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ObjectContainer _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onFinish(_arg0, data.readInt());
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

    void onFinish(ObjectContainer objectContainer, int i) throws RemoteException;
}
