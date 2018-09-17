package com.huawei.fingerprint;

import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAuthenticator extends IInterface {

    public static abstract class Stub extends Binder implements IAuthenticator {
        private static final String DESCRIPTOR = "com.huawei.fingerprint.IAuthenticator";
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
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IBinder asBinder;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        asBinder = client.asBinder();
                    } else {
                        asBinder = null;
                    }
                    _data.writeStrongBinder(asBinder);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userid);
                    _data.writeByteArray(nonce);
                    _data.writeString(aaid);
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
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    int _result = verifyUser(android.hardware.fingerprint.IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), com.huawei.fingerprint.IAuthenticatorListener.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.createByteArray(), data.readString());
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

    int verifyUser(IFingerprintServiceReceiver iFingerprintServiceReceiver, IAuthenticatorListener iAuthenticatorListener, int i, byte[] bArr, String str) throws RemoteException;
}
