package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAuthSessionListener extends IInterface {
    void onError(long j, int i, int i2, String str) throws RemoteException;

    void onFinish(long j, int i, String str) throws RemoteException;

    String onRequest(long j, int i, String str) throws RemoteException;

    boolean onTransmit(long j, byte[] bArr) throws RemoteException;

    public static class Default implements IAuthSessionListener {
        @Override // huawei.android.security.IAuthSessionListener
        public String onRequest(long authReqId, int authForm, String reqParams) throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.IAuthSessionListener
        public boolean onTransmit(long authReqId, byte[] data) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.IAuthSessionListener
        public void onFinish(long authReqId, int authForm, String authReturn) throws RemoteException {
        }

        @Override // huawei.android.security.IAuthSessionListener
        public void onError(long authReqId, int authForm, int errorCode, String errorReturn) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAuthSessionListener {
        private static final String DESCRIPTOR = "huawei.android.security.IAuthSessionListener";
        static final int TRANSACTION_onError = 4;
        static final int TRANSACTION_onFinish = 3;
        static final int TRANSACTION_onRequest = 1;
        static final int TRANSACTION_onTransmit = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAuthSessionListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAuthSessionListener)) {
                return new Proxy(obj);
            }
            return (IAuthSessionListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _result = onRequest(data.readLong(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean onTransmit = onTransmit(data.readLong(), data.createByteArray());
                reply.writeNoException();
                reply.writeInt(onTransmit ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onFinish(data.readLong(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onError(data.readLong(), data.readInt(), data.readInt(), data.readString());
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
        public static class Proxy implements IAuthSessionListener {
            public static IAuthSessionListener sDefaultImpl;
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

            @Override // huawei.android.security.IAuthSessionListener
            public String onRequest(long authReqId, int authForm, String reqParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authReqId);
                    _data.writeInt(authForm);
                    _data.writeString(reqParams);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onRequest(authReqId, authForm, reqParams);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IAuthSessionListener
            public boolean onTransmit(long authReqId, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authReqId);
                    _data.writeByteArray(data);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onTransmit(authReqId, data);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IAuthSessionListener
            public void onFinish(long authReqId, int authForm, String authReturn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authReqId);
                    _data.writeInt(authForm);
                    _data.writeString(authReturn);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onFinish(authReqId, authForm, authReturn);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IAuthSessionListener
            public void onError(long authReqId, int authForm, int errorCode, String errorReturn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authReqId);
                    _data.writeInt(authForm);
                    _data.writeInt(errorCode);
                    _data.writeString(errorReturn);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onError(authReqId, authForm, errorCode, errorReturn);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAuthSessionListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAuthSessionListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
