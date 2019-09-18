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
        static final int TRANSACTION_reportDeviceSetupComplete = 6;
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
                GateKeeperResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeByteArray(currentPasswordHandle);
                    _data.writeByteArray(currentPassword);
                    _data.writeByteArray(desiredPassword);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = GateKeeperResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public GateKeeperResponse verify(int uid, byte[] enrolledPasswordHandle, byte[] providedPassword) throws RemoteException {
                GateKeeperResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeByteArray(enrolledPasswordHandle);
                    _data.writeByteArray(providedPassword);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = GateKeeperResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public GateKeeperResponse verifyChallenge(int uid, long challenge, byte[] enrolledPasswordHandle, byte[] providedPassword) throws RemoteException {
                GateKeeperResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeLong(challenge);
                    _data.writeByteArray(enrolledPasswordHandle);
                    _data.writeByteArray(providedPassword);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = GateKeeperResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportDeviceSetupComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
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
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        GateKeeperResponse _result = enroll(data.readInt(), data.createByteArray(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result != null) {
                            parcel2.writeInt(1);
                            _result.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        GateKeeperResponse _result2 = verify(data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result2 != null) {
                            parcel2.writeInt(1);
                            _result2.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        GateKeeperResponse _result3 = verifyChallenge(data.readInt(), data.readLong(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result3 != null) {
                            parcel2.writeInt(1);
                            _result3.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result4 = getSecureUserId(data.readInt());
                        reply.writeNoException();
                        parcel2.writeLong(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearSecureUserId(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportDeviceSetupComplete();
                        reply.writeNoException();
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

    void clearSecureUserId(int i) throws RemoteException;

    GateKeeperResponse enroll(int i, byte[] bArr, byte[] bArr2, byte[] bArr3) throws RemoteException;

    long getSecureUserId(int i) throws RemoteException;

    void reportDeviceSetupComplete() throws RemoteException;

    GateKeeperResponse verify(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    GateKeeperResponse verifyChallenge(int i, long j, byte[] bArr, byte[] bArr2) throws RemoteException;
}
