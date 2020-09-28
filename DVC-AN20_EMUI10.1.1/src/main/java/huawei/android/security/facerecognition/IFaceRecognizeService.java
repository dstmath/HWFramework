package huawei.android.security.facerecognition;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.coauthservice.pool.SecureRegCallBack;
import huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver;
import java.util.List;

public interface IFaceRecognizeService extends IInterface {
    void authenticate(IBinder iBinder, long j, int i, int i2, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver, String str) throws RemoteException;

    void cancelAuthentication(IBinder iBinder, String str) throws RemoteException;

    void cancelEnrollment(IBinder iBinder) throws RemoteException;

    void enroll(IBinder iBinder, byte[] bArr, int i, int i2, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver, String str) throws RemoteException;

    int getAngleDim(IBinder iBinder) throws RemoteException;

    long getAuthenticatorId(IBinder iBinder) throws RemoteException;

    List<FaceRecognition> getEnrolledFaceRecognizes(int i, String str) throws RemoteException;

    int getHardwareSupportType() throws RemoteException;

    int getPayResult(IBinder iBinder, int[] iArr, byte[] bArr, int[] iArr2, byte[] bArr2) throws RemoteException;

    int getRemainingNum() throws RemoteException;

    long getRemainingTime() throws RemoteException;

    int getTotalAuthFailedTimes() throws RemoteException;

    int hasAlternateAppearance(IBinder iBinder, int i) throws RemoteException;

    int init(IBinder iBinder, String str) throws RemoteException;

    int postEnroll(IBinder iBinder) throws RemoteException;

    long preEnroll(IBinder iBinder) throws RemoteException;

    int preparePayInfo(IBinder iBinder, byte[] bArr, byte[] bArr2, byte[] bArr3) throws RemoteException;

    int registerSecureRegCallBack(SecureRegCallBack secureRegCallBack) throws RemoteException;

    int release(IBinder iBinder, String str) throws RemoteException;

    void remove(IBinder iBinder, int i, int i2, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver) throws RemoteException;

    void rename(int i, int i2, String str) throws RemoteException;

    void resetTimeout(byte[] bArr) throws RemoteException;

    int setEnrollInfo(int[] iArr) throws RemoteException;

    int setSecureFaceMode(IBinder iBinder, int i) throws RemoteException;

    public static class Default implements IFaceRecognizeService {
        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public void authenticate(IBinder token, long sessionId, int flags, int userId, IFaceRecognizeServiceReceiver receiver, String opPackageName) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public void cancelAuthentication(IBinder token, String opPackageName) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public void enroll(IBinder token, byte[] authToken, int flags, int userId, IFaceRecognizeServiceReceiver receiver, String opPackageName) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public void cancelEnrollment(IBinder token) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public void remove(IBinder token, int faceId, int userId, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public void rename(int faceId, int userId, String name) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public List<FaceRecognition> getEnrolledFaceRecognizes(int userId, String opPackageName) throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int getHardwareSupportType() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public long preEnroll(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int postEnroll(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public void resetTimeout(byte[] token) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int getRemainingNum() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int getTotalAuthFailedTimes() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public long getRemainingTime() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int init(IBinder token, String packageName) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int release(IBinder token, String packageName) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int setSecureFaceMode(IBinder token, int mode) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int getAngleDim(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int preparePayInfo(IBinder token, byte[] aaid, byte[] nonce, byte[] reserve) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int getPayResult(IBinder token, int[] faceId, byte[] tokenResult, int[] tokenResultLen, byte[] reserve) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int hasAlternateAppearance(IBinder token, int faceId) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public long getAuthenticatorId(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int setEnrollInfo(int[] enrollInfo) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeService
        public int registerSecureRegCallBack(SecureRegCallBack callback) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFaceRecognizeService {
        private static final String DESCRIPTOR = "huawei.android.security.facerecognition.IFaceRecognizeService";
        static final int TRANSACTION_authenticate = 1;
        static final int TRANSACTION_cancelAuthentication = 2;
        static final int TRANSACTION_cancelEnrollment = 4;
        static final int TRANSACTION_enroll = 3;
        static final int TRANSACTION_getAngleDim = 18;
        static final int TRANSACTION_getAuthenticatorId = 22;
        static final int TRANSACTION_getEnrolledFaceRecognizes = 7;
        static final int TRANSACTION_getHardwareSupportType = 8;
        static final int TRANSACTION_getPayResult = 20;
        static final int TRANSACTION_getRemainingNum = 12;
        static final int TRANSACTION_getRemainingTime = 14;
        static final int TRANSACTION_getTotalAuthFailedTimes = 13;
        static final int TRANSACTION_hasAlternateAppearance = 21;
        static final int TRANSACTION_init = 15;
        static final int TRANSACTION_postEnroll = 10;
        static final int TRANSACTION_preEnroll = 9;
        static final int TRANSACTION_preparePayInfo = 19;
        static final int TRANSACTION_registerSecureRegCallBack = 24;
        static final int TRANSACTION_release = 16;
        static final int TRANSACTION_remove = 5;
        static final int TRANSACTION_rename = 6;
        static final int TRANSACTION_resetTimeout = 11;
        static final int TRANSACTION_setEnrollInfo = 23;
        static final int TRANSACTION_setSecureFaceMode = 17;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFaceRecognizeService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFaceRecognizeService)) {
                return new Proxy(obj);
            }
            return (IFaceRecognizeService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int[] _arg1;
            byte[] _arg2;
            int[] _arg3;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        authenticate(data.readStrongBinder(), data.readLong(), data.readInt(), data.readInt(), IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAuthentication(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        enroll(data.readStrongBinder(), data.createByteArray(), data.readInt(), data.readInt(), IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        cancelEnrollment(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        remove(data.readStrongBinder(), data.readInt(), data.readInt(), IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        rename(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List<FaceRecognition> _result = getEnrolledFaceRecognizes(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getHardwareSupportType();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        long _result3 = preEnroll(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeLong(_result3);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = postEnroll(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        resetTimeout(data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getRemainingNum();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getTotalAuthFailedTimes();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        long _result7 = getRemainingTime();
                        reply.writeNoException();
                        reply.writeLong(_result7);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = init(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = release(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = setSecureFaceMode(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getAngleDim(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg0 = data.readStrongBinder();
                        byte[] _arg12 = data.createByteArray();
                        byte[] _arg22 = data.createByteArray();
                        byte[] _arg32 = data.createByteArray();
                        int _result12 = preparePayInfo(_arg0, _arg12, _arg22, _arg32);
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        reply.writeByteArray(_arg32);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg02 = data.readStrongBinder();
                        int _arg1_length = data.readInt();
                        if (_arg1_length < 0) {
                            _arg1 = null;
                        } else {
                            _arg1 = new int[_arg1_length];
                        }
                        int _arg2_length = data.readInt();
                        if (_arg2_length < 0) {
                            _arg2 = null;
                        } else {
                            _arg2 = new byte[_arg2_length];
                        }
                        int _arg3_length = data.readInt();
                        if (_arg3_length < 0) {
                            _arg3 = null;
                        } else {
                            _arg3 = new int[_arg3_length];
                        }
                        byte[] _arg4 = data.createByteArray();
                        int _result13 = getPayResult(_arg02, _arg1, _arg2, _arg3, _arg4);
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        reply.writeIntArray(_arg1);
                        reply.writeByteArray(_arg2);
                        reply.writeIntArray(_arg3);
                        reply.writeByteArray(_arg4);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = hasAlternateAppearance(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        long _result15 = getAuthenticatorId(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeLong(_result15);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = setEnrollInfo(data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = registerSecureRegCallBack(SecureRegCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result17);
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
        public static class Proxy implements IFaceRecognizeService {
            public static IFaceRecognizeService sDefaultImpl;
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public void authenticate(IBinder token, long sessionId, int flags, int userId, IFaceRecognizeServiceReceiver receiver, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(sessionId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(flags);
                        try {
                            _data.writeInt(userId);
                            _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                            _data.writeString(opPackageName);
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().authenticate(token, sessionId, flags, userId, receiver, opPackageName);
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
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public void cancelAuthentication(IBinder token, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public void enroll(IBinder token, byte[] authToken, int flags, int userId, IFaceRecognizeServiceReceiver receiver, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(token);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(authToken);
                        try {
                            _data.writeInt(flags);
                            try {
                                _data.writeInt(userId);
                                _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
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
                    try {
                        if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().enroll(token, authToken, flags, userId, receiver, opPackageName);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public void cancelEnrollment(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public void remove(IBinder token, int faceId, int userId, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(faceId);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().remove(token, faceId, userId, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public void rename(int faceId, int userId, String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(faceId);
                    _data.writeInt(userId);
                    _data.writeString(name);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().rename(faceId, userId, name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public List<FaceRecognition> getEnrolledFaceRecognizes(int userId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnrolledFaceRecognizes(userId, opPackageName);
                    }
                    _reply.readException();
                    List<FaceRecognition> _result = _reply.createTypedArrayList(FaceRecognition.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int getHardwareSupportType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHardwareSupportType();
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public long preEnroll(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int postEnroll(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public void resetTimeout(byte[] token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(token);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetTimeout(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int getRemainingNum() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int getTotalAuthFailedTimes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTotalAuthFailedTimes();
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public long getRemainingTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int init(IBinder token, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().init(token, packageName);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int release(IBinder token, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().release(token, packageName);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int setSecureFaceMode(IBinder token, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSecureFaceMode(token, mode);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int getAngleDim(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAngleDim(token);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int preparePayInfo(IBinder token, byte[] aaid, byte[] nonce, byte[] reserve) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeByteArray(aaid);
                    _data.writeByteArray(nonce);
                    _data.writeByteArray(reserve);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().preparePayInfo(token, aaid, nonce, reserve);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(reserve);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int getPayResult(IBinder token, int[] faceId, byte[] tokenResult, int[] tokenResultLen, byte[] reserve) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (faceId == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(faceId.length);
                    }
                    if (tokenResult == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(tokenResult.length);
                    }
                    if (tokenResultLen == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(tokenResultLen.length);
                    }
                    _data.writeByteArray(reserve);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPayResult(token, faceId, tokenResult, tokenResultLen, reserve);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readIntArray(faceId);
                    _reply.readByteArray(tokenResult);
                    _reply.readIntArray(tokenResultLen);
                    _reply.readByteArray(reserve);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int hasAlternateAppearance(IBinder token, int faceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(faceId);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasAlternateAppearance(token, faceId);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public long getAuthenticatorId(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAuthenticatorId(token);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int setEnrollInfo(int[] enrollInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(enrollInfo);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setEnrollInfo(enrollInfo);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeService
            public int registerSecureRegCallBack(SecureRegCallBack callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerSecureRegCallBack(callback);
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
        }

        public static boolean setDefaultImpl(IFaceRecognizeService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFaceRecognizeService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
