package com.huawei.distributed.kms;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.distributed.kms.IHwDeviceQueryCallback;
import com.huawei.distributed.kms.IHwKeyOpCallback;
import com.huawei.distributed.kms.IHwKeyOpSession;
import com.huawei.distributed.kms.entity.DistributedDeviceInfo;

public interface IHwKeyManagerService extends IInterface {
    IHwKeyOpSession createKeySession(IBinder iBinder, IHwKeyOpCallback iHwKeyOpCallback, DistributedDeviceInfo distributedDeviceInfo, Bundle bundle) throws RemoteException;

    void getDistributedDeviceInfo(int i, IHwDeviceQueryCallback iHwDeviceQueryCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwKeyManagerService {
        private static final String DESCRIPTOR = "com.huawei.distributed.kms.IHwKeyManagerService";
        static final int TRANSACTION_createKeySession = 1;
        static final int TRANSACTION_getDistributedDeviceInfo = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwKeyManagerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwKeyManagerService)) {
                return new Proxy(obj);
            }
            return (IHwKeyManagerService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DistributedDeviceInfo _arg2;
            Bundle _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _arg0 = data.readStrongBinder();
                IHwKeyOpCallback _arg1 = IHwKeyOpCallback.Stub.asInterface(data.readStrongBinder());
                if (data.readInt() != 0) {
                    _arg2 = DistributedDeviceInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                if (data.readInt() != 0) {
                    _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                IHwKeyOpSession _result = createKeySession(_arg0, _arg1, _arg2, _arg3);
                reply.writeNoException();
                reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                getDistributedDeviceInfo(data.readInt(), IHwDeviceQueryCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IHwKeyManagerService {
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

            @Override // com.huawei.distributed.kms.IHwKeyManagerService
            public IHwKeyOpSession createKeySession(IBinder token, IHwKeyOpCallback callback, DistributedDeviceInfo deviceInfo, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (deviceInfo != null) {
                        _data.writeInt(1);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return IHwKeyOpSession.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributed.kms.IHwKeyManagerService
            public void getDistributedDeviceInfo(int relationType, IHwDeviceQueryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(relationType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
