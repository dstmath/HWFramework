package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.IHwLockStateChangeCallback;
import java.util.List;

public interface IHwSfpService extends IInterface {
    int executePolicy(String str, int i) throws RemoteException;

    String getKeyDesc(int i, int i2) throws RemoteException;

    int getLockState(int i, int i2) throws RemoteException;

    List<String> getSensitiveDataPolicyList() throws RemoteException;

    int registerLockStateChangeCallback(int i, IHwLockStateChangeCallback iHwLockStateChangeCallback) throws RemoteException;

    int unregisterLockStateChangeCallback(IHwLockStateChangeCallback iHwLockStateChangeCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwSfpService {
        private static final String DESCRIPTOR = "huawei.android.security.IHwSfpService";
        static final int TRANSACTION_executePolicy = 6;
        static final int TRANSACTION_getKeyDesc = 1;
        static final int TRANSACTION_getLockState = 3;
        static final int TRANSACTION_getSensitiveDataPolicyList = 2;
        static final int TRANSACTION_registerLockStateChangeCallback = 4;
        static final int TRANSACTION_unregisterLockStateChangeCallback = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSfpService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSfpService)) {
                return new Proxy(obj);
            }
            return (IHwSfpService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getKeyDesc(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getSensitiveDataPolicyList();
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getLockState(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = registerLockStateChangeCallback(data.readInt(), IHwLockStateChangeCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = unregisterLockStateChangeCallback(IHwLockStateChangeCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = executePolicy(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwSfpService {
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

            @Override // huawei.android.security.IHwSfpService
            public String getKeyDesc(int userId, int storageType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(storageType);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSfpService
            public List<String> getSensitiveDataPolicyList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSfpService
            public int getLockState(int userId, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(flag);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSfpService
            public int registerLockStateChangeCallback(int flag, IHwLockStateChangeCallback lockStateChangeCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    _data.writeStrongBinder(lockStateChangeCallback != null ? lockStateChangeCallback.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSfpService
            public int unregisterLockStateChangeCallback(IHwLockStateChangeCallback lockStateChangeCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lockStateChangeCallback != null ? lockStateChangeCallback.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSfpService
            public int executePolicy(String path, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
