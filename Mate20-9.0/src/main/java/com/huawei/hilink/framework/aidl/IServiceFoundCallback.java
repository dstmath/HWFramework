package com.huawei.hilink.framework.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IServiceFoundCallback extends IInterface {

    public static abstract class Stub extends Binder implements IServiceFoundCallback {
        private static final String DESCRIPTOR = "com.huawei.hilink.framework.aidl.IServiceFoundCallback";
        static final int TRANSACTION_onFoundError = 2;
        static final int TRANSACTION_onFoundService = 1;

        private static class Proxy implements IServiceFoundCallback {
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

            public void onFoundService(ServiceRecord serviceRecord) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (serviceRecord != null) {
                        _data.writeInt(1);
                        serviceRecord.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onFoundError(int errorCode) throws RemoteException {
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

        public static IServiceFoundCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IServiceFoundCallback)) {
                return new Proxy(obj);
            }
            return (IServiceFoundCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ServiceRecord _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ServiceRecord.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onFoundService(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onFoundError(data.readInt());
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

    void onFoundError(int i) throws RemoteException;

    void onFoundService(ServiceRecord serviceRecord) throws RemoteException;
}
