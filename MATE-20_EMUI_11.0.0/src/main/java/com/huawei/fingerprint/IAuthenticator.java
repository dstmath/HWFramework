package com.huawei.fingerprint;

import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.fingerprint.IAuthenticatorListener;

public interface IAuthenticator extends IInterface {
    int cancelVerifyUser(IFingerprintServiceReceiver iFingerprintServiceReceiver, int i) throws RemoteException;

    int verifyUser(IFingerprintServiceReceiver iFingerprintServiceReceiver, IAuthenticatorListener iAuthenticatorListener, int i, byte[] bArr, String str) throws RemoteException;

    public static class Default implements IAuthenticator {
        @Override // com.huawei.fingerprint.IAuthenticator
        public int verifyUser(IFingerprintServiceReceiver client, IAuthenticatorListener callback, int userid, byte[] nonce, String aaid) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.fingerprint.IAuthenticator
        public int cancelVerifyUser(IFingerprintServiceReceiver client, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAuthenticator {
        private static final String DESCRIPTOR = "com.huawei.fingerprint.IAuthenticator";
        static final int TRANSACTION_cancelVerifyUser = 2;
        static final int TRANSACTION_verifyUser = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAuthenticator asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAuthenticator)) {
                return new Proxy(obj);
            }
            return (IAuthenticator) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = verifyUser(IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), IAuthenticatorListener.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.createByteArray(), data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = cancelVerifyUser(IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAuthenticator {
            public static IAuthenticator sDefaultImpl;
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

            @Override // com.huawei.fingerprint.IAuthenticator
            public int verifyUser(IFingerprintServiceReceiver client, IAuthenticatorListener callback, int userid, byte[] nonce, String aaid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userid);
                    _data.writeByteArray(nonce);
                    _data.writeString(aaid);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().verifyUser(client, callback, userid, nonce, aaid);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.fingerprint.IAuthenticator
            public int cancelVerifyUser(IFingerprintServiceReceiver client, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cancelVerifyUser(client, userId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAuthenticator impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAuthenticator getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
