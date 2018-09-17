package com.huawei.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFidoAuthenticationCallback extends IInterface {

    public static abstract class Stub extends Binder implements IFidoAuthenticationCallback {
        private static final String DESCRIPTOR = "com.huawei.fingerprint.IFidoAuthenticationCallback";
        static final int TRANSACTION_onUserVerificationResult = 1;

        private static class Proxy implements IFidoAuthenticationCallback {
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

            public int onUserVerificationResult(int result, long opId, byte[] userid, byte[] encapsulatedResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    _data.writeLong(opId);
                    _data.writeByteArray(userid);
                    _data.writeByteArray(encapsulatedResult);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    int _result = onUserVerificationResult(data.readInt(), data.readLong(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int onUserVerificationResult(int i, long j, byte[] bArr, byte[] bArr2) throws RemoteException;
}
