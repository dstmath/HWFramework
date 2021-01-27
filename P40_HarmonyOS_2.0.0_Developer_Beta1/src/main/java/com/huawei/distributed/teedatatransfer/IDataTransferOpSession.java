package com.huawei.distributed.teedatatransfer;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.distributed.teedatatransfer.entity.DataTransferInfo;

public interface IDataTransferOpSession extends IInterface {
    boolean beginOperation(DataTransferInfo dataTransferInfo, Bundle bundle) throws RemoteException;

    void finishOperation(Bundle bundle) throws RemoteException;

    boolean updateOperation(DataTransferInfo dataTransferInfo, Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements IDataTransferOpSession {
        private static final String DESCRIPTOR = "com.huawei.distributed.teedatatransfer.IDataTransferOpSession";
        static final int TRANSACTION_beginOperation = 1;
        static final int TRANSACTION_finishOperation = 3;
        static final int TRANSACTION_updateOperation = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDataTransferOpSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDataTransferOpSession)) {
                return new Proxy(obj);
            }
            return (IDataTransferOpSession) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DataTransferInfo _arg0;
            Bundle _arg1;
            DataTransferInfo _arg02;
            Bundle _arg12;
            Bundle _arg03;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = DataTransferInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                boolean beginOperation = beginOperation(_arg0, _arg1);
                reply.writeNoException();
                reply.writeInt(beginOperation ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = DataTransferInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                if (data.readInt() != 0) {
                    _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                boolean updateOperation = updateOperation(_arg02, _arg12);
                reply.writeNoException();
                reply.writeInt(updateOperation ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                finishOperation(_arg03);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDataTransferOpSession {
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

            @Override // com.huawei.distributed.teedatatransfer.IDataTransferOpSession
            public boolean beginOperation(DataTransferInfo transInfo, Bundle extraBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (transInfo != null) {
                        _data.writeInt(1);
                        transInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extraBundle != null) {
                        _data.writeInt(1);
                        extraBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributed.teedatatransfer.IDataTransferOpSession
            public boolean updateOperation(DataTransferInfo transInfo, Bundle extraBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (transInfo != null) {
                        _data.writeInt(1);
                        transInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extraBundle != null) {
                        _data.writeInt(1);
                        extraBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributed.teedatatransfer.IDataTransferOpSession
            public void finishOperation(Bundle extraBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
