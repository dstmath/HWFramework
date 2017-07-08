package android.hardware.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFingerprintDaemon extends IInterface {

    public static abstract class Stub extends Binder implements IFingerprintDaemon {
        private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintDaemon";
        static final int TRANSACTION_authenticate = 1;
        static final int TRANSACTION_cancelAuthentication = 2;
        static final int TRANSACTION_cancelEnrollment = 4;
        static final int TRANSACTION_cancelEnumeration = 14;
        static final int TRANSACTION_closeHal = 10;
        static final int TRANSACTION_enroll = 3;
        static final int TRANSACTION_enumerate = 13;
        static final int TRANSACTION_getAuthenticatorId = 7;
        static final int TRANSACTION_init = 11;
        static final int TRANSACTION_openHal = 9;
        static final int TRANSACTION_postEnroll = 12;
        static final int TRANSACTION_preEnroll = 5;
        static final int TRANSACTION_remove = 6;
        static final int TRANSACTION_setActiveGroup = 8;

        private static class Proxy implements IFingerprintDaemon {
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

            public int authenticate(long sessionId, int groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sessionId);
                    _data.writeInt(groupId);
                    this.mRemote.transact(Stub.TRANSACTION_authenticate, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelAuthentication() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_cancelAuthentication, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enroll(byte[] token, int groupId, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(token);
                    _data.writeInt(groupId);
                    _data.writeInt(timeout);
                    this.mRemote.transact(Stub.TRANSACTION_enroll, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelEnrollment() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_cancelEnrollment, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long preEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_preEnroll, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int remove(int fingerId, int groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    this.mRemote.transact(Stub.TRANSACTION_remove, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getAuthenticatorId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAuthenticatorId, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setActiveGroup(int groupId, byte[] path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(groupId);
                    _data.writeByteArray(path);
                    this.mRemote.transact(Stub.TRANSACTION_setActiveGroup, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long openHal() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_openHal, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int closeHal() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_closeHal, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void init(IFingerprintDaemonCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_init, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int postEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_postEnroll, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enumerate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_enumerate, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelEnumeration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_cancelEnumeration, _data, _reply, 0);
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

        public static IFingerprintDaemon asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFingerprintDaemon)) {
                return new Proxy(obj);
            }
            return (IFingerprintDaemon) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            long _result2;
            switch (code) {
                case TRANSACTION_authenticate /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = authenticate(data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_cancelAuthentication /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = cancelAuthentication();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_enroll /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enroll(data.createByteArray(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_cancelEnrollment /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = cancelEnrollment();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_preEnroll /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = preEnroll();
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    return true;
                case TRANSACTION_remove /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = remove(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getAuthenticatorId /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAuthenticatorId();
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    return true;
                case TRANSACTION_setActiveGroup /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setActiveGroup(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_openHal /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = openHal();
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    return true;
                case TRANSACTION_closeHal /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = closeHal();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_init /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    init(android.hardware.fingerprint.IFingerprintDaemonCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_postEnroll /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = postEnroll();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_enumerate /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enumerate();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_cancelEnumeration /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = cancelEnumeration();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int authenticate(long j, int i) throws RemoteException;

    int cancelAuthentication() throws RemoteException;

    int cancelEnrollment() throws RemoteException;

    int cancelEnumeration() throws RemoteException;

    int closeHal() throws RemoteException;

    int enroll(byte[] bArr, int i, int i2) throws RemoteException;

    int enumerate() throws RemoteException;

    long getAuthenticatorId() throws RemoteException;

    void init(IFingerprintDaemonCallback iFingerprintDaemonCallback) throws RemoteException;

    long openHal() throws RemoteException;

    int postEnroll() throws RemoteException;

    long preEnroll() throws RemoteException;

    int remove(int i, int i2) throws RemoteException;

    int setActiveGroup(int i, byte[] bArr) throws RemoteException;
}
