package com.huawei.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAuthenticatorListener extends IInterface {
    void onUserVerificationResult(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    public static class Default implements IAuthenticatorListener {
        @Override // com.huawei.fingerprint.IAuthenticatorListener
        public void onUserVerificationResult(int result, byte[] userid, byte[] encapsulatedResult) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAuthenticatorListener {
        private static final String DESCRIPTOR = "com.huawei.fingerprint.IAuthenticatorListener";
        static final int TRANSACTION_onUserVerificationResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAuthenticatorListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAuthenticatorListener)) {
                return new Proxy(obj);
            }
            return (IAuthenticatorListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onUserVerificationResult(data.readInt(), data.createByteArray(), data.createByteArray());
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
        public static class Proxy implements IAuthenticatorListener {
            public static IAuthenticatorListener sDefaultImpl;
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

            @Override // com.huawei.fingerprint.IAuthenticatorListener
            public void onUserVerificationResult(int result, byte[] userid, byte[] encapsulatedResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    _data.writeByteArray(userid);
                    _data.writeByteArray(encapsulatedResult);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onUserVerificationResult(result, userid, encapsulatedResult);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAuthenticatorListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAuthenticatorListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
