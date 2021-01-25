package android.hardware.fingerprint;

import android.hardware.biometrics.IBiometricServiceLockoutResetCallback;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.fingerprint.IFingerprintClientActiveCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IFingerprintService extends IInterface {
    void addClientActiveCallback(IFingerprintClientActiveCallback iFingerprintClientActiveCallback) throws RemoteException;

    void addLockoutResetCallback(IBiometricServiceLockoutResetCallback iBiometricServiceLockoutResetCallback) throws RemoteException;

    void authenticate(IBinder iBinder, long j, int i, IFingerprintServiceReceiver iFingerprintServiceReceiver, int i2, String str) throws RemoteException;

    void cancelAuthentication(IBinder iBinder, String str) throws RemoteException;

    void cancelAuthenticationFromService(IBinder iBinder, String str, int i, int i2, int i3, boolean z) throws RemoteException;

    void cancelEnrollment(IBinder iBinder) throws RemoteException;

    void enroll(IBinder iBinder, byte[] bArr, int i, IFingerprintServiceReceiver iFingerprintServiceReceiver, int i2, String str) throws RemoteException;

    void enumerate(IBinder iBinder, int i, IFingerprintServiceReceiver iFingerprintServiceReceiver) throws RemoteException;

    long getAuthenticatorId(String str) throws RemoteException;

    List<Fingerprint> getEnrolledFingerprints(int i, String str) throws RemoteException;

    int getRemainingNum() throws RemoteException;

    long getRemainingTime() throws RemoteException;

    boolean hasEnrolledFingerprints(int i, String str) throws RemoteException;

    boolean isClientActive() throws RemoteException;

    boolean isHardwareDetected(long j, String str) throws RemoteException;

    int postEnroll(IBinder iBinder) throws RemoteException;

    long preEnroll(IBinder iBinder) throws RemoteException;

    void prepareForAuthentication(IBinder iBinder, long j, int i, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, String str, int i2, int i3, int i4, int i5) throws RemoteException;

    void remove(IBinder iBinder, int i, int i2, int i3, IFingerprintServiceReceiver iFingerprintServiceReceiver) throws RemoteException;

    void removeClientActiveCallback(IFingerprintClientActiveCallback iFingerprintClientActiveCallback) throws RemoteException;

    void rename(int i, int i2, String str) throws RemoteException;

    void resetTimeout(byte[] bArr) throws RemoteException;

    void setActiveUser(int i) throws RemoteException;

    void startPreparedClient(int i) throws RemoteException;

    public static class Default implements IFingerprintService {
        @Override // android.hardware.fingerprint.IFingerprintService
        public void authenticate(IBinder token, long sessionId, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void prepareForAuthentication(IBinder token, long sessionId, int userId, IBiometricServiceReceiverInternal wrapperReceiver, String opPackageName, int cookie, int callingUid, int callingPid, int callingUserId) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void startPreparedClient(int cookie) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void cancelAuthentication(IBinder token, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void cancelAuthenticationFromService(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void enroll(IBinder token, byte[] cryptoToken, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void cancelEnrollment(IBinder token) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void rename(int fingerId, int groupId, String name) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public List<Fingerprint> getEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
            return null;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public boolean isHardwareDetected(long deviceId, String opPackageName) throws RemoteException {
            return false;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public long preEnroll(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int postEnroll(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public boolean hasEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
            return false;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public long getAuthenticatorId(String opPackageName) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void resetTimeout(byte[] cryptoToken) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void setActiveUser(int uid) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void enumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public boolean isClientActive() throws RemoteException {
            return false;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void addClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public void removeClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public int getRemainingNum() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.fingerprint.IFingerprintService
        public long getRemainingTime() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFingerprintService {
        private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintService";
        static final int TRANSACTION_addClientActiveCallback = 21;
        static final int TRANSACTION_addLockoutResetCallback = 17;
        static final int TRANSACTION_authenticate = 1;
        static final int TRANSACTION_cancelAuthentication = 4;
        static final int TRANSACTION_cancelAuthenticationFromService = 5;
        static final int TRANSACTION_cancelEnrollment = 7;
        static final int TRANSACTION_enroll = 6;
        static final int TRANSACTION_enumerate = 19;
        static final int TRANSACTION_getAuthenticatorId = 15;
        static final int TRANSACTION_getEnrolledFingerprints = 10;
        static final int TRANSACTION_getRemainingNum = 23;
        static final int TRANSACTION_getRemainingTime = 24;
        static final int TRANSACTION_hasEnrolledFingerprints = 14;
        static final int TRANSACTION_isClientActive = 20;
        static final int TRANSACTION_isHardwareDetected = 11;
        static final int TRANSACTION_postEnroll = 13;
        static final int TRANSACTION_preEnroll = 12;
        static final int TRANSACTION_prepareForAuthentication = 2;
        static final int TRANSACTION_remove = 8;
        static final int TRANSACTION_removeClientActiveCallback = 22;
        static final int TRANSACTION_rename = 9;
        static final int TRANSACTION_resetTimeout = 16;
        static final int TRANSACTION_setActiveUser = 18;
        static final int TRANSACTION_startPreparedClient = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFingerprintService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFingerprintService)) {
                return new Proxy(obj);
            }
            return (IFingerprintService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "authenticate";
                case 2:
                    return "prepareForAuthentication";
                case 3:
                    return "startPreparedClient";
                case 4:
                    return "cancelAuthentication";
                case 5:
                    return "cancelAuthenticationFromService";
                case 6:
                    return "enroll";
                case 7:
                    return "cancelEnrollment";
                case 8:
                    return "remove";
                case 9:
                    return "rename";
                case 10:
                    return "getEnrolledFingerprints";
                case 11:
                    return "isHardwareDetected";
                case 12:
                    return "preEnroll";
                case 13:
                    return "postEnroll";
                case 14:
                    return "hasEnrolledFingerprints";
                case 15:
                    return "getAuthenticatorId";
                case 16:
                    return "resetTimeout";
                case 17:
                    return "addLockoutResetCallback";
                case 18:
                    return "setActiveUser";
                case 19:
                    return "enumerate";
                case 20:
                    return "isClientActive";
                case 21:
                    return "addClientActiveCallback";
                case 22:
                    return "removeClientActiveCallback";
                case 23:
                    return "getRemainingNum";
                case 24:
                    return "getRemainingTime";
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
                        data.enforceInterface(DESCRIPTOR);
                        authenticate(data.readStrongBinder(), data.readLong(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        prepareForAuthentication(data.readStrongBinder(), data.readLong(), data.readInt(), IBiometricServiceReceiverInternal.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        startPreparedClient(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAuthentication(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAuthenticationFromService(data.readStrongBinder(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        enroll(data.readStrongBinder(), data.createByteArray(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        cancelEnrollment(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        remove(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        rename(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        List<Fingerprint> _result = getEnrolledFingerprints(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHardwareDetected = isHardwareDetected(data.readLong(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isHardwareDetected ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        long _result2 = preEnroll(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeLong(_result2);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = postEnroll(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasEnrolledFingerprints = hasEnrolledFingerprints(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(hasEnrolledFingerprints ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        long _result4 = getAuthenticatorId(data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        resetTimeout(data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        addLockoutResetCallback(IBiometricServiceLockoutResetCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        setActiveUser(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        enumerate(data.readStrongBinder(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isClientActive = isClientActive();
                        reply.writeNoException();
                        reply.writeInt(isClientActive ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        addClientActiveCallback(IFingerprintClientActiveCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        removeClientActiveCallback(IFingerprintClientActiveCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getRemainingNum();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        long _result6 = getRemainingTime();
                        reply.writeNoException();
                        reply.writeLong(_result6);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IFingerprintService {
            public static IFingerprintService sDefaultImpl;
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

            @Override // android.hardware.fingerprint.IFingerprintService
            public void authenticate(IBinder token, long sessionId, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(sessionId);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flags);
                        _data.writeString(opPackageName);
                        if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().authenticate(token, sessionId, userId, receiver, flags, opPackageName);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void prepareForAuthentication(IBinder token, long sessionId, int userId, IBiometricServiceReceiverInternal wrapperReceiver, String opPackageName, int cookie, int callingUid, int callingPid, int callingUserId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                        _data.writeLong(sessionId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        _data.writeStrongBinder(wrapperReceiver != null ? wrapperReceiver.asBinder() : null);
                        _data.writeString(opPackageName);
                        _data.writeInt(cookie);
                        _data.writeInt(callingUid);
                        _data.writeInt(callingPid);
                        _data.writeInt(callingUserId);
                        if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().prepareForAuthentication(token, sessionId, userId, wrapperReceiver, opPackageName, cookie, callingUid, callingPid, callingUserId);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void startPreparedClient(int cookie) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cookie);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startPreparedClient(cookie);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void cancelAuthentication(IBinder token, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelAuthentication(token, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void cancelAuthenticationFromService(IBinder token, String opPackageName, int callingUid, int callingPid, int callingUserId, boolean fromClient) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(opPackageName);
                        try {
                            _data.writeInt(callingUid);
                            try {
                                _data.writeInt(callingPid);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(callingUserId);
                        _data.writeInt(fromClient ? 1 : 0);
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().cancelAuthenticationFromService(token, opPackageName, callingUid, callingPid, callingUserId, fromClient);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void enroll(IBinder token, byte[] cryptoToken, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(cryptoToken);
                        try {
                            _data.writeInt(groupId);
                            _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                            try {
                                _data.writeInt(flags);
                                try {
                                    _data.writeString(opPackageName);
                                } catch (Throwable th3) {
                                    th = th3;
                                    _reply.recycle();
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().enroll(token, cryptoToken, groupId, receiver, flags, opPackageName);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void cancelEnrollment(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelEnrollment(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().remove(token, fingerId, groupId, userId, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void rename(int fingerId, int groupId, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeString(name);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().rename(fingerId, groupId, name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public List<Fingerprint> getEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(groupId);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnrolledFingerprints(groupId, opPackageName);
                    }
                    _reply.readException();
                    List<Fingerprint> _result = _reply.createTypedArrayList(Fingerprint.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public boolean isHardwareDetected(long deviceId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeString(opPackageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHardwareDetected(deviceId, opPackageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public long preEnroll(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().preEnroll(token);
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

            @Override // android.hardware.fingerprint.IFingerprintService
            public int postEnroll(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().postEnroll(token);
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

            @Override // android.hardware.fingerprint.IFingerprintService
            public boolean hasEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(groupId);
                    _data.writeString(opPackageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasEnrolledFingerprints(groupId, opPackageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public long getAuthenticatorId(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAuthenticatorId(opPackageName);
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

            @Override // android.hardware.fingerprint.IFingerprintService
            public void resetTimeout(byte[] cryptoToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(cryptoToken);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetTimeout(cryptoToken);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void addLockoutResetCallback(IBiometricServiceLockoutResetCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addLockoutResetCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void setActiveUser(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setActiveUser(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void enumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enumerate(token, userId, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public boolean isClientActive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isClientActive();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void addClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addClientActiveCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public void removeClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeClientActiveCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.fingerprint.IFingerprintService
            public int getRemainingNum() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRemainingNum();
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

            @Override // android.hardware.fingerprint.IFingerprintService
            public long getRemainingTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRemainingTime();
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
        }

        public static boolean setDefaultImpl(IFingerprintService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFingerprintService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
