package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAuthCallback extends IInterface {

    public static abstract class Stub extends Binder implements IAuthCallback {
        private static final String DESCRIPTOR = "huawei.android.security.IAuthCallback";
        static final int TRANSACTION_onAuthAck = 5;
        static final int TRANSACTION_onAuthAckError = 6;
        static final int TRANSACTION_onAuthError = 2;
        static final int TRANSACTION_onAuthExited = 9;
        static final int TRANSACTION_onAuthSync = 1;
        static final int TRANSACTION_onAuthSyncAck = 3;
        static final int TRANSACTION_onAuthSyncAckError = 4;
        static final int TRANSACTION_requestPK = 7;
        static final int TRANSACTION_responsePK = 8;

        private static class Proxy implements IAuthCallback {
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

            public void onAuthSync(long authID, byte[] tcisId, int pkVersion, int taVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeByteArray(tcisId);
                    _data.writeInt(pkVersion);
                    _data.writeInt(taVersion);
                    _data.writeLong(nonce);
                    _data.writeInt(authKeyAlgoType);
                    _data.writeByteArray(authKeyInfo);
                    _data.writeByteArray(authKeyInfoSign);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAuthError(long authID, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeInt(errorCode);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeByteArray(tcisIdSlave);
                    _data.writeInt(pkVersionSlave);
                    _data.writeLong(nonceSlave);
                    _data.writeByteArray(mac);
                    _data.writeInt(authKeyAlgoType);
                    _data.writeByteArray(authKeyInfo);
                    _data.writeByteArray(authKeyInfoSign);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAuthSyncAckError(long authID, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeInt(errorCode);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAuthAck(long authID, int result, byte[] sessionKeyIV, byte[] sessionKey, byte[] mac) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeInt(result);
                    _data.writeByteArray(sessionKeyIV);
                    _data.writeByteArray(sessionKey);
                    _data.writeByteArray(mac);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAuthAckError(long authID, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeInt(errorCode);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestPK() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void responsePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeInt(authKeyAlgoType);
                    _data.writeByteArray(authKeyData);
                    _data.writeByteArray(authKeyDataSign);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAuthExited(long authID, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeInt(reason);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAuthCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAuthCallback)) {
                return new Proxy(obj);
            }
            return (IAuthCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthSync(data.readLong(), data.createByteArray(), data.readInt(), data.readInt(), data.readLong(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthError(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthSyncAck(data.readLong(), data.createByteArray(), data.readInt(), data.readLong(), data.createByteArray(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthSyncAckError(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthAck(data.readLong(), data.readInt(), data.createByteArray(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthAckError(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    requestPK();
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    responsePK(data.readLong(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    onAuthExited(data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAuthAck(long j, int i, byte[] bArr, byte[] bArr2, byte[] bArr3) throws RemoteException;

    void onAuthAckError(long j, int i) throws RemoteException;

    void onAuthError(long j, int i) throws RemoteException;

    void onAuthExited(long j, int i) throws RemoteException;

    void onAuthSync(long j, byte[] bArr, int i, int i2, long j2, int i3, byte[] bArr2, byte[] bArr3) throws RemoteException;

    void onAuthSyncAck(long j, byte[] bArr, int i, long j2, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4) throws RemoteException;

    void onAuthSyncAckError(long j, int i) throws RemoteException;

    void requestPK() throws RemoteException;

    void responsePK(long j, int i, byte[] bArr, byte[] bArr2) throws RemoteException;
}
