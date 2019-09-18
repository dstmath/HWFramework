package com.huawei.fingerprint;

import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.fingerprint.IAuthenticatorListener;

public interface IAuthenticator extends IInterface {

    public static abstract class Stub extends Binder implements IAuthenticator {
        private static final String DESCRIPTOR = "com.huawei.fingerprint.IAuthenticator";
        static final int TRANSACTION_cancelVerifyUser = 2;
        static final int TRANSACTION_verifyUser = 1;

        private static class Proxy implements IAuthenticator {
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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelVerifyUser(IFingerprintServiceReceiver client, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(userId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result = verifyUser(IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), IAuthenticatorListener.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.createByteArray(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result2 = cancelVerifyUser(IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    int cancelVerifyUser(IFingerprintServiceReceiver iFingerprintServiceReceiver, int i) throws RemoteException;

    int verifyUser(IFingerprintServiceReceiver iFingerprintServiceReceiver, IAuthenticatorListener iAuthenticatorListener, int i, byte[] bArr, String str) throws RemoteException;
}
