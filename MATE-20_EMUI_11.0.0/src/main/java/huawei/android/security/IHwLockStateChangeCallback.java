package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwLockStateChangeCallback extends IInterface {
    int innerHashCode() throws RemoteException;

    boolean isInnerEquals(IHwLockStateChangeCallback iHwLockStateChangeCallback) throws RemoteException;

    void onLockStateChanged(int i, int i2) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwLockStateChangeCallback {
        private static final String DESCRIPTOR = "huawei.android.security.IHwLockStateChangeCallback";
        static final int TRANSACTION_innerHashCode = 2;
        static final int TRANSACTION_isInnerEquals = 3;
        static final int TRANSACTION_onLockStateChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwLockStateChangeCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwLockStateChangeCallback)) {
                return new Proxy(obj);
            }
            return (IHwLockStateChangeCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onLockStateChanged(data.readInt(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result = innerHashCode();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean isInnerEquals = isInnerEquals(asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(isInnerEquals ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwLockStateChangeCallback {
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

            @Override // huawei.android.security.IHwLockStateChangeCallback
            public void onLockStateChanged(int userId, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(state);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwLockStateChangeCallback
            public int innerHashCode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwLockStateChangeCallback
            public boolean isInnerEquals(IHwLockStateChangeCallback otherCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(otherCallback != null ? otherCallback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
