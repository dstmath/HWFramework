package com.huawei.distributed.kms;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.distributed.kms.entity.DistributedKeyInfo;
import com.huawei.distributed.kms.entity.DistributedKeySecurityLevel;

public interface IHwKeyOpSession extends IInterface {
    void closeSession() throws RemoteException;

    boolean requestDeliverKey(DistributedKeyInfo distributedKeyInfo, DistributedKeySecurityLevel distributedKeySecurityLevel, int i, Bundle bundle) throws RemoteException;

    boolean requestEscrowKey(DistributedKeyInfo distributedKeyInfo, DistributedKeySecurityLevel distributedKeySecurityLevel, int i, Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwKeyOpSession {
        private static final String DESCRIPTOR = "com.huawei.distributed.kms.IHwKeyOpSession";
        static final int TRANSACTION_closeSession = 3;
        static final int TRANSACTION_requestDeliverKey = 1;
        static final int TRANSACTION_requestEscrowKey = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwKeyOpSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwKeyOpSession)) {
                return new Proxy(obj);
            }
            return (IHwKeyOpSession) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DistributedKeyInfo _arg0;
            DistributedKeySecurityLevel _arg1;
            Bundle _arg3;
            DistributedKeyInfo _arg02;
            DistributedKeySecurityLevel _arg12;
            Bundle _arg32;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = DistributedKeyInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = DistributedKeySecurityLevel.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                int _arg2 = data.readInt();
                if (data.readInt() != 0) {
                    _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                boolean requestDeliverKey = requestDeliverKey(_arg0, _arg1, _arg2, _arg3);
                reply.writeNoException();
                reply.writeInt(requestDeliverKey ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = DistributedKeyInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                if (data.readInt() != 0) {
                    _arg12 = DistributedKeySecurityLevel.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                int _arg22 = data.readInt();
                if (data.readInt() != 0) {
                    _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg32 = null;
                }
                boolean requestEscrowKey = requestEscrowKey(_arg02, _arg12, _arg22, _arg32);
                reply.writeNoException();
                reply.writeInt(requestEscrowKey ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                closeSession();
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
        public static class Proxy implements IHwKeyOpSession {
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

            @Override // com.huawei.distributed.kms.IHwKeyOpSession
            public boolean requestDeliverKey(DistributedKeyInfo info, DistributedKeySecurityLevel level, int requestCode, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (level != null) {
                        _data.writeInt(1);
                        level.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(requestCode);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
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

            @Override // com.huawei.distributed.kms.IHwKeyOpSession
            public boolean requestEscrowKey(DistributedKeyInfo info, DistributedKeySecurityLevel level, int requestCode, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (level != null) {
                        _data.writeInt(1);
                        level.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(requestCode);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
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

            @Override // com.huawei.distributed.kms.IHwKeyOpSession
            public void closeSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
