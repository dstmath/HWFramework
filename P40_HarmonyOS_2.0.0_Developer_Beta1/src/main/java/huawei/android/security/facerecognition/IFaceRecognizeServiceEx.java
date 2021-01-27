package huawei.android.security.facerecognition;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver;
import java.util.List;

public interface IFaceRecognizeServiceEx extends IInterface {
    void authenticate(IBinder iBinder, AuthParam authParam, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver) throws RemoteException;

    void cancelAuthentication(IBinder iBinder, AuthParam authParam, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver) throws RemoteException;

    void cancelEnrollment(IBinder iBinder, EnrollParam enrollParam, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver) throws RemoteException;

    void checkNeedUpgradeFeature(IBinder iBinder, EnrollParam enrollParam, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver) throws RemoteException;

    void enroll(IBinder iBinder, EnrollParam enrollParam, Surface surface, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver) throws RemoteException;

    int getAngleDim() throws RemoteException;

    long getAuthenticatorId() throws RemoteException;

    List<FaceRecognition> getEnrolledFaceRecognizes(int i, String str) throws RemoteException;

    int getHardwareSupportType() throws RemoteException;

    int getPayResult(int[] iArr, byte[] bArr, int[] iArr2, byte[] bArr2) throws RemoteException;

    int getRemainingNum() throws RemoteException;

    long getRemainingTime() throws RemoteException;

    int getTotalAuthFailedTimes() throws RemoteException;

    int hasAlternateAppearance(int i) throws RemoteException;

    int init(String str) throws RemoteException;

    void postEnroll() throws RemoteException;

    long preEnroll() throws RemoteException;

    int preparePayInfo(byte[] bArr, byte[] bArr2, byte[] bArr3) throws RemoteException;

    int registerSecureRegistryCallback(IBinder iBinder) throws RemoteException;

    int release(String str) throws RemoteException;

    void remove(IBinder iBinder, RemoveParam removeParam, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver) throws RemoteException;

    void rename(int i, int i2, String str) throws RemoteException;

    void resetTimeout() throws RemoteException;

    int setEnrollInfo(int[] iArr) throws RemoteException;

    void upgradeFeature(IBinder iBinder, EnrollParam enrollParam, IFaceRecognizeServiceReceiver iFaceRecognizeServiceReceiver) throws RemoteException;

    public static class Default implements IFaceRecognizeServiceEx {
        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void authenticate(IBinder token, AuthParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void cancelAuthentication(IBinder token, AuthParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void enroll(IBinder token, EnrollParam param, Surface preview, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void cancelEnrollment(IBinder token, EnrollParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void remove(IBinder token, RemoveParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void rename(int faceId, int userId, String name) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public List<FaceRecognition> getEnrolledFaceRecognizes(int userId, String opPackageName) throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int getHardwareSupportType() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public long preEnroll() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void postEnroll() throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void resetTimeout() throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int getRemainingNum() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int getTotalAuthFailedTimes() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public long getRemainingTime() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int init(String opPackageName) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int release(String opPackageName) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int getAngleDim() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int preparePayInfo(byte[] aaid, byte[] nonce, byte[] extra) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int getPayResult(int[] faceId, byte[] tokenResult, int[] tokenResultLen, byte[] reserve) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int hasAlternateAppearance(int faceId) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public long getAuthenticatorId() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int setEnrollInfo(int[] enrollInfo) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public int registerSecureRegistryCallback(IBinder callback) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void checkNeedUpgradeFeature(IBinder token, EnrollParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
        public void upgradeFeature(IBinder token, EnrollParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFaceRecognizeServiceEx {
        private static final String DESCRIPTOR = "huawei.android.security.facerecognition.IFaceRecognizeServiceEx";
        static final int TRANSACTION_authenticate = 1;
        static final int TRANSACTION_cancelAuthentication = 2;
        static final int TRANSACTION_cancelEnrollment = 4;
        static final int TRANSACTION_checkNeedUpgradeFeature = 24;
        static final int TRANSACTION_enroll = 3;
        static final int TRANSACTION_getAngleDim = 17;
        static final int TRANSACTION_getAuthenticatorId = 21;
        static final int TRANSACTION_getEnrolledFaceRecognizes = 7;
        static final int TRANSACTION_getHardwareSupportType = 8;
        static final int TRANSACTION_getPayResult = 19;
        static final int TRANSACTION_getRemainingNum = 12;
        static final int TRANSACTION_getRemainingTime = 14;
        static final int TRANSACTION_getTotalAuthFailedTimes = 13;
        static final int TRANSACTION_hasAlternateAppearance = 20;
        static final int TRANSACTION_init = 15;
        static final int TRANSACTION_postEnroll = 10;
        static final int TRANSACTION_preEnroll = 9;
        static final int TRANSACTION_preparePayInfo = 18;
        static final int TRANSACTION_registerSecureRegistryCallback = 23;
        static final int TRANSACTION_release = 16;
        static final int TRANSACTION_remove = 5;
        static final int TRANSACTION_rename = 6;
        static final int TRANSACTION_resetTimeout = 11;
        static final int TRANSACTION_setEnrollInfo = 22;
        static final int TRANSACTION_upgradeFeature = 25;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFaceRecognizeServiceEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFaceRecognizeServiceEx)) {
                return new Proxy(obj);
            }
            return (IFaceRecognizeServiceEx) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AuthParam _arg1;
            AuthParam _arg12;
            EnrollParam _arg13;
            Surface _arg2;
            EnrollParam _arg14;
            RemoveParam _arg15;
            int[] _arg0;
            byte[] _arg16;
            int[] _arg22;
            EnrollParam _arg17;
            EnrollParam _arg18;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg02 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = (AuthParam) AuthParam.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        authenticate(_arg02, _arg1, IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg03 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg12 = (AuthParam) AuthParam.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        cancelAuthentication(_arg03, _arg12, IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg04 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg13 = (EnrollParam) EnrollParam.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Surface) Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        enroll(_arg04, _arg13, _arg2, IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg05 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg14 = (EnrollParam) EnrollParam.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        cancelEnrollment(_arg05, _arg14, IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg06 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg15 = (RemoveParam) RemoveParam.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        remove(_arg06, _arg15, IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
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
                        long _result3 = preEnroll();
                        reply.writeNoException();
                        reply.writeLong(_result3);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        postEnroll();
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        resetTimeout();
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getRemainingNum();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getTotalAuthFailedTimes();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        long _result6 = getRemainingTime();
                        reply.writeNoException();
                        reply.writeLong(_result6);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = init(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = release(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getAngleDim();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _arg07 = data.createByteArray();
                        byte[] _arg19 = data.createByteArray();
                        byte[] _arg23 = data.createByteArray();
                        int _result10 = preparePayInfo(_arg07, _arg19, _arg23);
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        reply.writeByteArray(_arg23);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0_length = data.readInt();
                        if (_arg0_length < 0) {
                            _arg0 = null;
                        } else {
                            _arg0 = new int[_arg0_length];
                        }
                        int _arg1_length = data.readInt();
                        if (_arg1_length < 0) {
                            _arg16 = null;
                        } else {
                            _arg16 = new byte[_arg1_length];
                        }
                        int _arg2_length = data.readInt();
                        if (_arg2_length < 0) {
                            _arg22 = null;
                        } else {
                            _arg22 = new int[_arg2_length];
                        }
                        byte[] _arg3 = data.createByteArray();
                        int _result11 = getPayResult(_arg0, _arg16, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        reply.writeIntArray(_arg0);
                        reply.writeByteArray(_arg16);
                        reply.writeIntArray(_arg22);
                        reply.writeByteArray(_arg3);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = hasAlternateAppearance(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        long _result13 = getAuthenticatorId();
                        reply.writeNoException();
                        reply.writeLong(_result13);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = setEnrollInfo(data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = registerSecureRegistryCallback(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg08 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg17 = (EnrollParam) EnrollParam.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        checkNeedUpgradeFeature(_arg08, _arg17, IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg09 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg18 = (EnrollParam) EnrollParam.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        upgradeFeature(_arg09, _arg18, IFaceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
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
        public static class Proxy implements IFaceRecognizeServiceEx {
            public static IFaceRecognizeServiceEx sDefaultImpl;
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public void authenticate(IBinder token, AuthParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (param != null) {
                        _data.writeInt(1);
                        param.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().authenticate(token, param, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public void cancelAuthentication(IBinder token, AuthParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (param != null) {
                        _data.writeInt(1);
                        param.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelAuthentication(token, param, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public void enroll(IBinder token, EnrollParam param, Surface preview, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (param != null) {
                        _data.writeInt(1);
                        param.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (preview != null) {
                        _data.writeInt(1);
                        preview.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enroll(token, param, preview, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public void cancelEnrollment(IBinder token, EnrollParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (param != null) {
                        _data.writeInt(1);
                        param.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelEnrollment(token, param, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public void remove(IBinder token, RemoveParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (param != null) {
                        _data.writeInt(1);
                        param.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().remove(token, param, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public long preEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().preEnroll();
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public void postEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().postEnroll();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public void resetTimeout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetTimeout();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public int init(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().init(opPackageName);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public int release(String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().release(opPackageName);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public int getAngleDim() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAngleDim();
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public int preparePayInfo(byte[] aaid, byte[] nonce, byte[] extra) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(aaid);
                    _data.writeByteArray(nonce);
                    _data.writeByteArray(extra);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().preparePayInfo(aaid, nonce, extra);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(extra);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public int getPayResult(int[] faceId, byte[] tokenResult, int[] tokenResultLen, byte[] reserve) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPayResult(faceId, tokenResult, tokenResultLen, reserve);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public int hasAlternateAppearance(int faceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(faceId);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasAlternateAppearance(faceId);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public long getAuthenticatorId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAuthenticatorId();
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public int setEnrollInfo(int[] enrollInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(enrollInfo);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public int registerSecureRegistryCallback(IBinder callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerSecureRegistryCallback(callback);
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public void checkNeedUpgradeFeature(IBinder token, EnrollParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (param != null) {
                        _data.writeInt(1);
                        param.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().checkNeedUpgradeFeature(token, param, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceEx
            public void upgradeFeature(IBinder token, EnrollParam param, IFaceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (param != null) {
                        _data.writeInt(1);
                        param.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().upgradeFeature(token, param, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IFaceRecognizeServiceEx impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFaceRecognizeServiceEx getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
