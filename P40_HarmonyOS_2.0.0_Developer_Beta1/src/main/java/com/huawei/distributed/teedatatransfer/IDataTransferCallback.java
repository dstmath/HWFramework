package com.huawei.distributed.teedatatransfer;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.distributed.teedatatransfer.IDataTransferOpSession;
import com.huawei.distributed.teedatatransfer.entity.DataTransferInfo;

public interface IDataTransferCallback extends IInterface {
    void onResult(int i, DataTransferInfo dataTransferInfo, int i2, Bundle bundle) throws RemoteException;

    void onSessionCreate(IDataTransferOpSession iDataTransferOpSession) throws RemoteException;

    public static abstract class Stub extends Binder implements IDataTransferCallback {
        private static final String DESCRIPTOR = "com.huawei.distributed.teedatatransfer.IDataTransferCallback";
        static final int TRANSACTION_onResult = 2;
        static final int TRANSACTION_onSessionCreate = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDataTransferCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDataTransferCallback)) {
                return new Proxy(obj);
            }
            return (IDataTransferCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DataTransferInfo _arg1;
            Bundle _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onSessionCreate(IDataTransferOpSession.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = DataTransferInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                int _arg2 = data.readInt();
                if (data.readInt() != 0) {
                    _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                onResult(_arg0, _arg1, _arg2, _arg3);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDataTransferCallback {
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

            @Override // com.huawei.distributed.teedatatransfer.IDataTransferCallback
            public void onSessionCreate(IDataTransferOpSession session) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(session != null ? session.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributed.teedatatransfer.IDataTransferCallback
            public void onResult(int type, DataTransferInfo transInfo, int resultCode, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (transInfo != null) {
                        _data.writeInt(1);
                        transInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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
