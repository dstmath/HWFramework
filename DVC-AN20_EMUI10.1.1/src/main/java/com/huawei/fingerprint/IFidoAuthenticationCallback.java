package com.huawei.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFidoAuthenticationCallback extends IInterface {
    int onUserVerificationResult(int i, long j, byte[] bArr, byte[] bArr2) throws RemoteException;

    public static class Default implements IFidoAuthenticationCallback {
        @Override // com.huawei.fingerprint.IFidoAuthenticationCallback
        public int onUserVerificationResult(int result, long opId, byte[] userid, byte[] encapsulatedResult) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFidoAuthenticationCallback {
        private static final String DESCRIPTOR = "com.huawei.fingerprint.IFidoAuthenticationCallback";
        static final int TRANSACTION_onUserVerificationResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFidoAuthenticationCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFidoAuthenticationCallback)) {
                return new Proxy(obj);
            }
            return (IFidoAuthenticationCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = onUserVerificationResult(data.readInt(), data.readLong(), data.createByteArray(), data.createByteArray());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IFidoAuthenticationCallback {
            public static IFidoAuthenticationCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.huawei.fingerprint.IFidoAuthenticationCallback
            public int onUserVerificationResult(int result, long opId, byte[] userid, byte[] encapsulatedResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    _data.writeLong(opId);
                    _data.writeByteArray(userid);
                    _data.writeByteArray(encapsulatedResult);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onUserVerificationResult(result, opId, userid, encapsulatedResult);
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

        public static boolean setDefaultImpl(IFidoAuthenticationCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFidoAuthenticationCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
