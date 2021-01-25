package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IKaCallback extends IInterface {
    void onKaError(long j, int i) throws RemoteException;

    void onKaResult(long j, int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    public static class Default implements IKaCallback {
        @Override // huawei.android.security.IKaCallback
        public void onKaResult(long authId, int result, byte[] iv, byte[] payload) throws RemoteException {
        }

        @Override // huawei.android.security.IKaCallback
        public void onKaError(long authId, int errorCode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IKaCallback {
        private static final String DESCRIPTOR = "huawei.android.security.IKaCallback";
        static final int TRANSACTION_onKaError = 2;
        static final int TRANSACTION_onKaResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IKaCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IKaCallback)) {
                return new Proxy(obj);
            }
            return (IKaCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onKaResult(data.readLong(), data.readInt(), data.createByteArray(), data.createByteArray());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onKaError(data.readLong(), data.readInt());
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
        public static class Proxy implements IKaCallback {
            public static IKaCallback sDefaultImpl;
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

            @Override // huawei.android.security.IKaCallback
            public void onKaResult(long authId, int result, byte[] iv, byte[] payload) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authId);
                    _data.writeInt(result);
                    _data.writeByteArray(iv);
                    _data.writeByteArray(payload);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onKaResult(authId, result, iv, payload);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IKaCallback
            public void onKaError(long authId, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authId);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onKaError(authId, errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IKaCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IKaCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
