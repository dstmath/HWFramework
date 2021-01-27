package com.huawei.distributed.teedatatransfer;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.distributed.teedatatransfer.IConnectServiceCallback;
import com.huawei.distributed.teedatatransfer.IDataTransferCallback;

public interface IDataTransferService extends IInterface {
    void connectKeyManagerService(IConnectServiceCallback iConnectServiceCallback) throws RemoteException;

    void createSession(IBinder iBinder, IDataTransferCallback iDataTransferCallback, String str, int i, Bundle bundle) throws RemoteException;

    void finishSession(String str, Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements IDataTransferService {
        private static final String DESCRIPTOR = "com.huawei.distributed.teedatatransfer.IDataTransferService";
        static final int TRANSACTION_connectKeyManagerService = 2;
        static final int TRANSACTION_createSession = 1;
        static final int TRANSACTION_finishSession = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDataTransferService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDataTransferService)) {
                return new Proxy(obj);
            }
            return (IDataTransferService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg4;
            Bundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _arg0 = data.readStrongBinder();
                IDataTransferCallback _arg12 = IDataTransferCallback.Stub.asInterface(data.readStrongBinder());
                String _arg2 = data.readString();
                int _arg3 = data.readInt();
                if (data.readInt() != 0) {
                    _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg4 = null;
                }
                createSession(_arg0, _arg12, _arg2, _arg3, _arg4);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                connectKeyManagerService(IConnectServiceCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                if (data.readInt() != 0) {
                    _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                finishSession(_arg02, _arg1);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDataTransferService {
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

            @Override // com.huawei.distributed.teedatatransfer.IDataTransferService
            public void createSession(IBinder token, IDataTransferCallback callback, String deviceId, int operationType, Bundle extraBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(deviceId);
                    _data.writeInt(operationType);
                    if (extraBundle != null) {
                        _data.writeInt(1);
                        extraBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributed.teedatatransfer.IDataTransferService
            public void connectKeyManagerService(IConnectServiceCallback connectServiceCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connectServiceCallback != null ? connectServiceCallback.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributed.teedatatransfer.IDataTransferService
            public void finishSession(String packageName, Bundle extraBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (extraBundle != null) {
                        _data.writeInt(1);
                        extraBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
