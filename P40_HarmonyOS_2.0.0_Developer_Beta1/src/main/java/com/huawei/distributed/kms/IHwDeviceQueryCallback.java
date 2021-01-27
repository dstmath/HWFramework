package com.huawei.distributed.kms;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.distributed.kms.entity.DistributedDeviceInfo;
import java.util.List;

public interface IHwDeviceQueryCallback extends IInterface {
    void onGetDistributedDeviceInfoList(List<DistributedDeviceInfo> list) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwDeviceQueryCallback {
        private static final String DESCRIPTOR = "com.huawei.distributed.kms.IHwDeviceQueryCallback";
        static final int TRANSACTION_onGetDistributedDeviceInfoList = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwDeviceQueryCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwDeviceQueryCallback)) {
                return new Proxy(obj);
            }
            return (IHwDeviceQueryCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onGetDistributedDeviceInfoList(data.createTypedArrayList(DistributedDeviceInfo.CREATOR));
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IHwDeviceQueryCallback {
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

            @Override // com.huawei.distributed.kms.IHwDeviceQueryCallback
            public void onGetDistributedDeviceInfoList(List<DistributedDeviceInfo> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(list);
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
