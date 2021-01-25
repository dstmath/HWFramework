package android.service.gatekeeper;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGateKeeperService extends IInterface {
    void clearSecureUserId(int i) throws RemoteException;

    GateKeeperResponse enroll(int i, byte[] bArr, byte[] bArr2, byte[] bArr3) throws RemoteException;

    long getSecureUserId(int i) throws RemoteException;

    void reportDeviceSetupComplete() throws RemoteException;

    GateKeeperResponse verify(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    GateKeeperResponse verifyChallenge(int i, long j, byte[] bArr, byte[] bArr2) throws RemoteException;

    public static class Default implements IGateKeeperService {
        @Override // android.service.gatekeeper.IGateKeeperService
        public GateKeeperResponse enroll(int uid, byte[] currentPasswordHandle, byte[] currentPassword, byte[] desiredPassword) throws RemoteException {
            return null;
        }

        @Override // android.service.gatekeeper.IGateKeeperService
        public GateKeeperResponse verify(int uid, byte[] enrolledPasswordHandle, byte[] providedPassword) throws RemoteException {
            return null;
        }

        @Override // android.service.gatekeeper.IGateKeeperService
        public GateKeeperResponse verifyChallenge(int uid, long challenge, byte[] enrolledPasswordHandle, byte[] providedPassword) throws RemoteException {
            return null;
        }

        @Override // android.service.gatekeeper.IGateKeeperService
        public long getSecureUserId(int uid) throws RemoteException {
            return 0;
        }

        @Override // android.service.gatekeeper.IGateKeeperService
        public void clearSecureUserId(int uid) throws RemoteException {
        }

        @Override // android.service.gatekeeper.IGateKeeperService
        public void reportDeviceSetupComplete() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGateKeeperService {
        private static final String DESCRIPTOR = "android.service.gatekeeper.IGateKeeperService";
        static final int TRANSACTION_clearSecureUserId = 5;
        static final int TRANSACTION_enroll = 1;
        static final int TRANSACTION_getSecureUserId = 4;
        static final int TRANSACTION_reportDeviceSetupComplete = 6;
        static final int TRANSACTION_verify = 2;
        static final int TRANSACTION_verifyChallenge = 3;

        public Stub() {
            attachInterface(this, "android.service.gatekeeper.IGateKeeperService");
        }

        public static IGateKeeperService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("android.service.gatekeeper.IGateKeeperService");
            if (iin == null || !(iin instanceof IGateKeeperService)) {
                return new Proxy(obj);
            }
            return (IGateKeeperService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "enroll";
                case 2:
                    return "verify";
                case 3:
                    return "verifyChallenge";
                case 4:
                    return "getSecureUserId";
                case 5:
                    return "clearSecureUserId";
                case 6:
                    return "reportDeviceSetupComplete";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface("android.service.gatekeeper.IGateKeeperService");
                        GateKeeperResponse _result = enroll(data.readInt(), data.createByteArray(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface("android.service.gatekeeper.IGateKeeperService");
                        GateKeeperResponse _result2 = verify(data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface("android.service.gatekeeper.IGateKeeperService");
                        GateKeeperResponse _result3 = verifyChallenge(data.readInt(), data.readLong(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface("android.service.gatekeeper.IGateKeeperService");
                        long _result4 = getSecureUserId(data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 5:
                        data.enforceInterface("android.service.gatekeeper.IGateKeeperService");
                        clearSecureUserId(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface("android.service.gatekeeper.IGateKeeperService");
                        reportDeviceSetupComplete();
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString("android.service.gatekeeper.IGateKeeperService");
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGateKeeperService {
            public static IGateKeeperService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return "android.service.gatekeeper.IGateKeeperService";
            }

            @Override // android.service.gatekeeper.IGateKeeperService
            public GateKeeperResponse enroll(int uid, byte[] currentPasswordHandle, byte[] currentPassword, byte[] desiredPassword) throws RemoteException {
                GateKeeperResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.service.gatekeeper.IGateKeeperService");
                    _data.writeInt(uid);
                    _data.writeByteArray(currentPasswordHandle);
                    _data.writeByteArray(currentPassword);
                    _data.writeByteArray(desiredPassword);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enroll(uid, currentPasswordHandle, currentPassword, desiredPassword);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = GateKeeperResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.gatekeeper.IGateKeeperService
            public GateKeeperResponse verify(int uid, byte[] enrolledPasswordHandle, byte[] providedPassword) throws RemoteException {
                GateKeeperResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.service.gatekeeper.IGateKeeperService");
                    _data.writeInt(uid);
                    _data.writeByteArray(enrolledPasswordHandle);
                    _data.writeByteArray(providedPassword);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().verify(uid, enrolledPasswordHandle, providedPassword);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = GateKeeperResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.gatekeeper.IGateKeeperService
            public GateKeeperResponse verifyChallenge(int uid, long challenge, byte[] enrolledPasswordHandle, byte[] providedPassword) throws RemoteException {
                GateKeeperResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.service.gatekeeper.IGateKeeperService");
                    _data.writeInt(uid);
                    _data.writeLong(challenge);
                    _data.writeByteArray(enrolledPasswordHandle);
                    _data.writeByteArray(providedPassword);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().verifyChallenge(uid, challenge, enrolledPasswordHandle, providedPassword);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = GateKeeperResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.gatekeeper.IGateKeeperService
            public long getSecureUserId(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.service.gatekeeper.IGateKeeperService");
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecureUserId(uid);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.gatekeeper.IGateKeeperService
            public void clearSecureUserId(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.service.gatekeeper.IGateKeeperService");
                    _data.writeInt(uid);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearSecureUserId(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.gatekeeper.IGateKeeperService
            public void reportDeviceSetupComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.service.gatekeeper.IGateKeeperService");
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportDeviceSetupComplete();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGateKeeperService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGateKeeperService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
