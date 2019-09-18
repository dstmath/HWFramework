package android.hardware.fingerprint;

import android.hardware.biometrics.IBiometricPromptReceiver;
import android.hardware.fingerprint.IFingerprintClientActiveCallback;
import android.hardware.fingerprint.IFingerprintServiceLockoutResetCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IFingerprintService extends IInterface {

    public static abstract class Stub extends Binder implements IFingerprintService {
        private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintService";
        static final int TRANSACTION_addClientActiveCallback = 18;
        static final int TRANSACTION_addLockoutResetCallback = 14;
        static final int TRANSACTION_authenticate = 1;
        static final int TRANSACTION_cancelAuthentication = 2;
        static final int TRANSACTION_cancelEnrollment = 4;
        static final int TRANSACTION_enroll = 3;
        static final int TRANSACTION_enumerate = 16;
        static final int TRANSACTION_getAuthenticatorId = 12;
        static final int TRANSACTION_getEnrolledFingerprints = 7;
        static final int TRANSACTION_getRemainingNum = 20;
        static final int TRANSACTION_getRemainingTime = 21;
        static final int TRANSACTION_hasEnrolledFingerprints = 11;
        static final int TRANSACTION_isClientActive = 17;
        static final int TRANSACTION_isHardwareDetected = 8;
        static final int TRANSACTION_postEnroll = 10;
        static final int TRANSACTION_preEnroll = 9;
        static final int TRANSACTION_remove = 5;
        static final int TRANSACTION_removeClientActiveCallback = 19;
        static final int TRANSACTION_rename = 6;
        static final int TRANSACTION_resetTimeout = 13;
        static final int TRANSACTION_setActiveUser = 15;

        private static class Proxy implements IFingerprintService {
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

            public void authenticate(IBinder token, long sessionId, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName, Bundle bundle, IBiometricPromptReceiver dialogReceiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeLong(sessionId);
                    _data.writeInt(userId);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    _data.writeInt(flags);
                    _data.writeString(opPackageName);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (dialogReceiver != null) {
                        iBinder = dialogReceiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelAuthentication(IBinder token, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enroll(IBinder token, byte[] cryptoToken, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeByteArray(cryptoToken);
                    _data.writeInt(groupId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    _data.writeInt(flags);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelEnrollment(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rename(int fingerId, int groupId, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fingerId);
                    _data.writeInt(groupId);
                    _data.writeString(name);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<Fingerprint> getEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(groupId);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(Fingerprint.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isHardwareDetected(long deviceId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(deviceId);
                    _data.writeString(opPackageName);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long preEnroll(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int postEnroll(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasEnrolledFingerprints(int groupId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(groupId);
                    _data.writeString(opPackageName);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getAuthenticatorId(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resetTimeout(byte[] cryptoToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(cryptoToken);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addLockoutResetCallback(IFingerprintServiceLockoutResetCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setActiveUser(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enumerate(IBinder token, int userId, IFingerprintServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isClientActive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeClientActiveCallback(IFingerprintClientActiveCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRemainingNum() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getRemainingTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle bundle;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        IBinder _arg0 = data.readStrongBinder();
                        long _arg1 = data.readLong();
                        int _arg2 = data.readInt();
                        IFingerprintServiceReceiver _arg3 = IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder());
                        int _arg4 = data.readInt();
                        String _arg5 = data.readString();
                        if (data.readInt() != 0) {
                            bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            bundle = null;
                        }
                        Bundle _arg6 = bundle;
                        authenticate(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, IBiometricPromptReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelAuthentication(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        enroll(data.readStrongBinder(), data.createByteArray(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        cancelEnrollment(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        remove(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        rename(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<Fingerprint> _result = getEnrolledFingerprints(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = isHardwareDetected(data.readLong(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result3 = preEnroll(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeLong(_result3);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result4 = postEnroll(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result5 = hasEnrolledFingerprints(data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result6 = getAuthenticatorId(data.readString());
                        reply.writeNoException();
                        parcel2.writeLong(_result6);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        resetTimeout(data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        addLockoutResetCallback(IFingerprintServiceLockoutResetCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        setActiveUser(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        enumerate(data.readStrongBinder(), data.readInt(), IFingerprintServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result7 = isClientActive();
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        addClientActiveCallback(IFingerprintClientActiveCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeClientActiveCallback(IFingerprintClientActiveCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result8 = getRemainingNum();
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result9 = getRemainingTime();
                        reply.writeNoException();
                        parcel2.writeLong(_result9);
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

    void addClientActiveCallback(IFingerprintClientActiveCallback iFingerprintClientActiveCallback) throws RemoteException;

    void addLockoutResetCallback(IFingerprintServiceLockoutResetCallback iFingerprintServiceLockoutResetCallback) throws RemoteException;

    void authenticate(IBinder iBinder, long j, int i, IFingerprintServiceReceiver iFingerprintServiceReceiver, int i2, String str, Bundle bundle, IBiometricPromptReceiver iBiometricPromptReceiver) throws RemoteException;

    void cancelAuthentication(IBinder iBinder, String str) throws RemoteException;

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

    void remove(IBinder iBinder, int i, int i2, int i3, IFingerprintServiceReceiver iFingerprintServiceReceiver) throws RemoteException;

    void removeClientActiveCallback(IFingerprintClientActiveCallback iFingerprintClientActiveCallback) throws RemoteException;

    void rename(int i, int i2, String str) throws RemoteException;

    void resetTimeout(byte[] bArr) throws RemoteException;

    void setActiveUser(int i) throws RemoteException;
}
