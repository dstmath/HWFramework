package huawei.android.security.voicerecognition;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.voicerecognition.IHeadsetStatusCallback;
import huawei.android.security.voicerecognition.IVoiceAuthCallback;
import huawei.android.security.voicerecognition.IVoiceRecognizeServiceReceiver;
import java.util.List;

public interface IVoiceRecognizeService extends IInterface {
    void cancelEnroll(IBinder iBinder) throws RemoteException;

    void continueEnroll() throws RemoteException;

    void enroll(IBinder iBinder, byte[] bArr, int i, int i2, IVoiceRecognizeServiceReceiver iVoiceRecognizeServiceReceiver, String str) throws RemoteException;

    int[] getEnrolledVoiceIdList(int i, String str) throws RemoteException;

    int getHeadsetStatus() throws RemoteException;

    int getHeadsetStatusByMac(String str) throws RemoteException;

    int getRemainingNum() throws RemoteException;

    long getRemainingTime() throws RemoteException;

    int getTotalAuthFailedTimes() throws RemoteException;

    VoiceCommandList getVoiceCommandList() throws RemoteException;

    List<String> getVoiceEnrollStringList() throws RemoteException;

    int[] getVoiceIdSupportCommandList() throws RemoteException;

    int getVoiceIdUserUpdateStatus(int i) throws RemoteException;

    int postEnroll() throws RemoteException;

    long preEnroll() throws RemoteException;

    void remove(IBinder iBinder, int i, int i2, IVoiceRecognizeServiceReceiver iVoiceRecognizeServiceReceiver) throws RemoteException;

    void removeAll(IBinder iBinder, int i, IVoiceRecognizeServiceReceiver iVoiceRecognizeServiceReceiver) throws RemoteException;

    void resetTimeout() throws RemoteException;

    void setAuthCallback(IVoiceAuthCallback iVoiceAuthCallback) throws RemoteException;

    void setHeadsetStatusCallback(IHeadsetStatusCallback iHeadsetStatusCallback) throws RemoteException;

    void startVoiceActivity(int i) throws RemoteException;

    public static class Default implements IVoiceRecognizeService {
        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public void enroll(IBinder token, byte[] authToken, int flags, int userId, IVoiceRecognizeServiceReceiver receiver, String opPackageName) throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public void cancelEnroll(IBinder token) throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public void remove(IBinder token, int voiceId, int userId, IVoiceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public void removeAll(IBinder token, int userId, IVoiceRecognizeServiceReceiver receiver) throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public void setAuthCallback(IVoiceAuthCallback callback) throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public void setHeadsetStatusCallback(IHeadsetStatusCallback callback) throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public int[] getEnrolledVoiceIdList(int userId, String opPackageName) throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public long preEnroll() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public int postEnroll() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public void resetTimeout() throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public int getRemainingNum() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public int getTotalAuthFailedTimes() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public long getRemainingTime() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public int getHeadsetStatus() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public List<String> getVoiceEnrollStringList() throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public void continueEnroll() throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public void startVoiceActivity(int voiceType) throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public int getHeadsetStatusByMac(String mac) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public VoiceCommandList getVoiceCommandList() throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public int getVoiceIdUserUpdateStatus(int userId) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
        public int[] getVoiceIdSupportCommandList() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceRecognizeService {
        private static final String DESCRIPTOR = "huawei.android.security.voicerecognition.IVoiceRecognizeService";
        static final int TRANSACTION_cancelEnroll = 2;
        static final int TRANSACTION_continueEnroll = 16;
        static final int TRANSACTION_enroll = 1;
        static final int TRANSACTION_getEnrolledVoiceIdList = 7;
        static final int TRANSACTION_getHeadsetStatus = 14;
        static final int TRANSACTION_getHeadsetStatusByMac = 18;
        static final int TRANSACTION_getRemainingNum = 11;
        static final int TRANSACTION_getRemainingTime = 13;
        static final int TRANSACTION_getTotalAuthFailedTimes = 12;
        static final int TRANSACTION_getVoiceCommandList = 19;
        static final int TRANSACTION_getVoiceEnrollStringList = 15;
        static final int TRANSACTION_getVoiceIdSupportCommandList = 21;
        static final int TRANSACTION_getVoiceIdUserUpdateStatus = 20;
        static final int TRANSACTION_postEnroll = 9;
        static final int TRANSACTION_preEnroll = 8;
        static final int TRANSACTION_remove = 3;
        static final int TRANSACTION_removeAll = 4;
        static final int TRANSACTION_resetTimeout = 10;
        static final int TRANSACTION_setAuthCallback = 5;
        static final int TRANSACTION_setHeadsetStatusCallback = 6;
        static final int TRANSACTION_startVoiceActivity = 17;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceRecognizeService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceRecognizeService)) {
                return new Proxy(obj);
            }
            return (IVoiceRecognizeService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        enroll(data.readStrongBinder(), data.createByteArray(), data.readInt(), data.readInt(), IVoiceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        cancelEnroll(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        remove(data.readStrongBinder(), data.readInt(), data.readInt(), IVoiceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        removeAll(data.readStrongBinder(), data.readInt(), IVoiceRecognizeServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setAuthCallback(IVoiceAuthCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setHeadsetStatusCallback(IHeadsetStatusCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result = getEnrolledVoiceIdList(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeIntArray(_result);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        long _result2 = preEnroll();
                        reply.writeNoException();
                        reply.writeLong(_result2);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = postEnroll();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        resetTimeout();
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getRemainingNum();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getTotalAuthFailedTimes();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        long _result6 = getRemainingTime();
                        reply.writeNoException();
                        reply.writeLong(_result6);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getHeadsetStatus();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result8 = getVoiceEnrollStringList();
                        reply.writeNoException();
                        reply.writeStringList(_result8);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        continueEnroll();
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        startVoiceActivity(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getHeadsetStatusByMac(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        VoiceCommandList _result10 = getVoiceCommandList();
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getVoiceIdUserUpdateStatus(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result12 = getVoiceIdSupportCommandList();
                        reply.writeNoException();
                        reply.writeIntArray(_result12);
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
        public static class Proxy implements IVoiceRecognizeService {
            public static IVoiceRecognizeService sDefaultImpl;
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public void enroll(IBinder token, byte[] authToken, int flags, int userId, IVoiceRecognizeServiceReceiver receiver, String opPackageName) throws RemoteException {
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
                        _data.writeByteArray(authToken);
                        try {
                            _data.writeInt(flags);
                            try {
                                _data.writeInt(userId);
                                _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                            try {
                                _data.writeString(opPackageName);
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
                        if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().enroll(token, authToken, flags, userId, receiver, opPackageName);
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public void cancelEnroll(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelEnroll(token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public void remove(IBinder token, int voiceId, int userId, IVoiceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(voiceId);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().remove(token, voiceId, userId, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public void removeAll(IBinder token, int userId, IVoiceRecognizeServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(userId);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAll(token, userId, receiver);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public void setAuthCallback(IVoiceAuthCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAuthCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public void setHeadsetStatusCallback(IHeadsetStatusCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHeadsetStatusCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public int[] getEnrolledVoiceIdList(int userId, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(opPackageName);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEnrolledVoiceIdList(userId, opPackageName);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public long preEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public int postEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().postEnroll();
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public void resetTimeout() throws RemoteException {
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
                    Stub.getDefaultImpl().resetTimeout();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public int getRemainingNum() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public int getTotalAuthFailedTimes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public long getRemainingTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public int getHeadsetStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHeadsetStatus();
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public List<String> getVoiceEnrollStringList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceEnrollStringList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public void continueEnroll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().continueEnroll();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public void startVoiceActivity(int voiceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(voiceType);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startVoiceActivity(voiceType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public int getHeadsetStatusByMac(String mac) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mac);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHeadsetStatusByMac(mac);
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public VoiceCommandList getVoiceCommandList() throws RemoteException {
                VoiceCommandList _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceCommandList();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (VoiceCommandList) VoiceCommandList.CREATOR.createFromParcel(_reply);
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public int getVoiceIdUserUpdateStatus(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceIdUserUpdateStatus(userId);
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

            @Override // huawei.android.security.voicerecognition.IVoiceRecognizeService
            public int[] getVoiceIdSupportCommandList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVoiceIdSupportCommandList();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceRecognizeService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceRecognizeService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
