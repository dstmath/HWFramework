package android.service.gatekeeper;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGateKeeperService extends IInterface {

    public static abstract class Stub extends Binder implements IGateKeeperService {
        private static final String DESCRIPTOR = "android.service.gatekeeper.IGateKeeperService";
        static final int TRANSACTION_clearSecureUserId = 5;
        static final int TRANSACTION_enroll = 1;
        static final int TRANSACTION_getSecureUserId = 4;
        static final int TRANSACTION_verify = 2;
        static final int TRANSACTION_verifyChallenge = 3;

        private static class Proxy implements IGateKeeperService {
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

            public GateKeeperResponse enroll(int uid, byte[] currentPasswordHandle, byte[] currentPassword, byte[] desiredPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    GateKeeperResponse gateKeeperResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeByteArray(currentPasswordHandle);
                    _data.writeByteArray(currentPassword);
                    _data.writeByteArray(desiredPassword);
                    this.mRemote.transact(Stub.TRANSACTION_enroll, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        gateKeeperResponse = (GateKeeperResponse) GateKeeperResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        gateKeeperResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return gateKeeperResponse;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public GateKeeperResponse verify(int uid, byte[] enrolledPasswordHandle, byte[] providedPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    GateKeeperResponse gateKeeperResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeByteArray(enrolledPasswordHandle);
                    _data.writeByteArray(providedPassword);
                    this.mRemote.transact(Stub.TRANSACTION_verify, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        gateKeeperResponse = (GateKeeperResponse) GateKeeperResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        gateKeeperResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return gateKeeperResponse;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public GateKeeperResponse verifyChallenge(int uid, long challenge, byte[] enrolledPasswordHandle, byte[] providedPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    GateKeeperResponse gateKeeperResponse;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeLong(challenge);
                    _data.writeByteArray(enrolledPasswordHandle);
                    _data.writeByteArray(providedPassword);
                    this.mRemote.transact(Stub.TRANSACTION_verifyChallenge, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        gateKeeperResponse = (GateKeeperResponse) GateKeeperResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        gateKeeperResponse = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return gateKeeperResponse;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getSecureUserId(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_getSecureUserId, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearSecureUserId(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_clearSecureUserId, _data, _reply, 0);
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

        public static IGateKeeperService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGateKeeperService)) {
                return new Proxy(obj);
            }
            return (IGateKeeperService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            GateKeeperResponse _result;
            switch (code) {
                case TRANSACTION_enroll /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enroll(data.readInt(), data.createByteArray(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_enroll);
                        _result.writeToParcel(reply, TRANSACTION_enroll);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_verify /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = verify(data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_enroll);
                        _result.writeToParcel(reply, TRANSACTION_enroll);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_verifyChallenge /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = verifyChallenge(data.readInt(), data.readLong(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_enroll);
                        _result.writeToParcel(reply, TRANSACTION_enroll);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getSecureUserId /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _result2 = getSecureUserId(data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    return true;
                case TRANSACTION_clearSecureUserId /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearSecureUserId(data.readInt());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void clearSecureUserId(int i) throws RemoteException;

    GateKeeperResponse enroll(int i, byte[] bArr, byte[] bArr2, byte[] bArr3) throws RemoteException;

    long getSecureUserId(int i) throws RemoteException;

    GateKeeperResponse verify(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    GateKeeperResponse verifyChallenge(int i, long j, byte[] bArr, byte[] bArr2) throws RemoteException;
}
