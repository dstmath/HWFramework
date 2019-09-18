package com.huawei.nb.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUpdatePackageCallBack extends IInterface {

    public static abstract class Stub extends Binder implements IUpdatePackageCallBack {
        private static final String DESCRIPTOR = "com.huawei.nb.callback.IUpdatePackageCallBack";
        static final int TRANSACTION_onRefresh = 1;

        private static class Proxy implements IUpdatePackageCallBack {
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

            public int onRefresh(int updateStatus, long totalSize, long downloadedSize, int totalPackages, int downloadedPackages, int errorCode, String errorMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(updateStatus);
                    _data.writeLong(totalSize);
                    _data.writeLong(downloadedSize);
                    _data.writeInt(totalPackages);
                    _data.writeInt(downloadedPackages);
                    _data.writeInt(errorCode);
                    _data.writeString(errorMessage);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUpdatePackageCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUpdatePackageCallBack)) {
                return new Proxy(obj);
            }
            return (IUpdatePackageCallBack) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    int _result = onRefresh(data.readInt(), data.readLong(), data.readLong(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int onRefresh(int i, long j, long j2, int i2, int i3, int i4, String str) throws RemoteException;
}
