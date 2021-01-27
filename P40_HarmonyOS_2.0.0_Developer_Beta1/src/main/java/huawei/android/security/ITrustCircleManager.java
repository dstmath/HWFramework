package huawei.android.security;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.IAuthCallback;
import huawei.android.security.IAuthSessionListener;
import huawei.android.security.IKaCallback;
import huawei.android.security.ILifeCycleCallback;
import huawei.android.security.ISignCallback;

public interface ITrustCircleManager extends IInterface {
    void cancelAuthentication(long j) throws RemoteException;

    void cancelRegOrLogin(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;

    void finalLogin(ILifeCycleCallback iLifeCycleCallback, int i, String str, String str2) throws RemoteException;

    void finalRegister(ILifeCycleCallback iLifeCycleCallback, String str, String str2, String str3, String str4) throws RemoteException;

    int getCurrentState() throws RemoteException;

    Bundle getTcisInfo() throws RemoteException;

    long initAuthenticate(IAuthCallback iAuthCallback, int i, int i2, int i3, long j, byte[] bArr) throws RemoteException;

    int initAuthenticateSession(IAuthSessionListener iAuthSessionListener, long j, String str) throws RemoteException;

    long initKeyAgreement(IKaCallback iKaCallback, int i, long j, byte[] bArr, String str) throws RemoteException;

    void loginServerRequest(ILifeCycleCallback iLifeCycleCallback, long j, int i, String str) throws RemoteException;

    void logout(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;

    boolean receiveAck(long j, byte[] bArr) throws RemoteException;

    boolean receiveAuthSessionSyncAck(long j, String str) throws RemoteException;

    long receiveAuthSync(IAuthCallback iAuthCallback, int i, int i2, int i3, int i4, long j, byte[] bArr, byte[] bArr2, int i5, long j2, int i6, byte[] bArr3, byte[] bArr4) throws RemoteException;

    boolean receiveAuthSyncAck(long j, byte[] bArr, int i, long j2, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4) throws RemoteException;

    boolean receivePK(long j, int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean receiveRequest(IAuthSessionListener iAuthSessionListener, long j, String str) throws RemoteException;

    boolean receiveSessionAck(long j, int i, String str) throws RemoteException;

    boolean receiveSessionFinish(long j, int i, String str) throws RemoteException;

    boolean requestPK(long j, long j2) throws RemoteException;

    int requestSignature(ISignCallback iSignCallback, long j, byte[] bArr) throws RemoteException;

    void unregister(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;

    void updateServerRequest(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;

    public static class Default implements ITrustCircleManager {
        @Override // huawei.android.security.ITrustCircleManager
        public Bundle getTcisInfo() throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public long initKeyAgreement(IKaCallback callBack, int kaVersion, long userId, byte[] aesTmpKey, String kaInfo) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public int getCurrentState() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public void loginServerRequest(ILifeCycleCallback callback, long userID, int serverRegisterStatus, String sessionID) throws RemoteException {
        }

        @Override // huawei.android.security.ITrustCircleManager
        public void updateServerRequest(ILifeCycleCallback callback, long userID) throws RemoteException {
        }

        @Override // huawei.android.security.ITrustCircleManager
        public void finalRegister(ILifeCycleCallback callback, String authPKData, String authPKDataSign, String updateIndexInfo, String updateIndexSignature) throws RemoteException {
        }

        @Override // huawei.android.security.ITrustCircleManager
        public void finalLogin(ILifeCycleCallback callback, int updateResult, String updateIndexInfo, String updateIndexSignature) throws RemoteException {
        }

        @Override // huawei.android.security.ITrustCircleManager
        public void cancelRegOrLogin(ILifeCycleCallback callback, long userID) throws RemoteException {
        }

        @Override // huawei.android.security.ITrustCircleManager
        public void logout(ILifeCycleCallback callback, long userID) throws RemoteException {
        }

        @Override // huawei.android.security.ITrustCircleManager
        public void unregister(ILifeCycleCallback callback, long userID) throws RemoteException {
        }

        @Override // huawei.android.security.ITrustCircleManager
        public long initAuthenticate(IAuthCallback callback, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public long receiveAuthSync(IAuthCallback callback, int authType, int authVersion, int taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public boolean receiveAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoTypeSlave, byte[] authKeyInfoSlave, byte[] authKeyInfoSignSlave) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public boolean receiveAck(long authID, byte[] mac) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public boolean requestPK(long authID, long userID) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public boolean receivePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public void cancelAuthentication(long authId) throws RemoteException {
        }

        @Override // huawei.android.security.ITrustCircleManager
        public int initAuthenticateSession(IAuthSessionListener callback, long authId, String authParams) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public boolean receiveRequest(IAuthSessionListener callback, long authId, String data) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public boolean receiveAuthSessionSyncAck(long authId, String data) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public boolean receiveSessionAck(long authId, int userType, String data) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public boolean receiveSessionFinish(long authId, int userType, String data) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.ITrustCircleManager
        public int requestSignature(ISignCallback callback, long signReqId, byte[] signReqParams) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITrustCircleManager {
        private static final String DESCRIPTOR = "huawei.android.security.ITrustCircleManager";
        static final int TRANSACTION_cancelAuthentication = 17;
        static final int TRANSACTION_cancelRegOrLogin = 8;
        static final int TRANSACTION_finalLogin = 7;
        static final int TRANSACTION_finalRegister = 6;
        static final int TRANSACTION_getCurrentState = 3;
        static final int TRANSACTION_getTcisInfo = 1;
        static final int TRANSACTION_initAuthenticate = 11;
        static final int TRANSACTION_initAuthenticateSession = 18;
        static final int TRANSACTION_initKeyAgreement = 2;
        static final int TRANSACTION_loginServerRequest = 4;
        static final int TRANSACTION_logout = 9;
        static final int TRANSACTION_receiveAck = 14;
        static final int TRANSACTION_receiveAuthSessionSyncAck = 20;
        static final int TRANSACTION_receiveAuthSync = 12;
        static final int TRANSACTION_receiveAuthSyncAck = 13;
        static final int TRANSACTION_receivePK = 16;
        static final int TRANSACTION_receiveRequest = 19;
        static final int TRANSACTION_receiveSessionAck = 21;
        static final int TRANSACTION_receiveSessionFinish = 22;
        static final int TRANSACTION_requestPK = 15;
        static final int TRANSACTION_requestSignature = 23;
        static final int TRANSACTION_unregister = 10;
        static final int TRANSACTION_updateServerRequest = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustCircleManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustCircleManager)) {
                return new Proxy(obj);
            }
            return (ITrustCircleManager) iin;
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
                        Bundle _result = getTcisInfo();
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        long _result2 = initKeyAgreement(IKaCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readLong(), data.createByteArray(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getCurrentState();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        loginServerRequest(ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readLong(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        updateServerRequest(ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        finalRegister(ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        finalLogin(ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        cancelRegOrLogin(ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        logout(ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        unregister(ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        long _result4 = initAuthenticate(IAuthCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt(), data.readLong(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        long _result5 = receiveAuthSync(IAuthCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readLong(), data.createByteArray(), data.createByteArray(), data.readInt(), data.readLong(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeLong(_result5);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean receiveAuthSyncAck = receiveAuthSyncAck(data.readLong(), data.createByteArray(), data.readInt(), data.readLong(), data.createByteArray(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(receiveAuthSyncAck ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean receiveAck = receiveAck(data.readLong(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(receiveAck ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean requestPK = requestPK(data.readLong(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(requestPK ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean receivePK = receivePK(data.readLong(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(receivePK ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAuthentication(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = initAuthenticateSession(IAuthSessionListener.Stub.asInterface(data.readStrongBinder()), data.readLong(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean receiveRequest = receiveRequest(IAuthSessionListener.Stub.asInterface(data.readStrongBinder()), data.readLong(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(receiveRequest ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean receiveAuthSessionSyncAck = receiveAuthSessionSyncAck(data.readLong(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(receiveAuthSessionSyncAck ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean receiveSessionAck = receiveSessionAck(data.readLong(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(receiveSessionAck ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean receiveSessionFinish = receiveSessionFinish(data.readLong(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(receiveSessionFinish ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = requestSignature(ISignCallback.Stub.asInterface(data.readStrongBinder()), data.readLong(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result7);
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
        public static class Proxy implements ITrustCircleManager {
            public static ITrustCircleManager sDefaultImpl;
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

            @Override // huawei.android.security.ITrustCircleManager
            public Bundle getTcisInfo() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTcisInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // huawei.android.security.ITrustCircleManager
            public long initKeyAgreement(IKaCallback callBack, int kaVersion, long userId, byte[] aesTmpKey, String kaInfo) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    try {
                        _data.writeInt(kaVersion);
                        try {
                            _data.writeLong(userId);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeByteArray(aesTmpKey);
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
                    try {
                        _data.writeString(kaInfo);
                        try {
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                long _result = _reply.readLong();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            long initKeyAgreement = Stub.getDefaultImpl().initKeyAgreement(callBack, kaVersion, userId, aesTmpKey, kaInfo);
                            _reply.recycle();
                            _data.recycle();
                            return initKeyAgreement;
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
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public int getCurrentState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentState();
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

            @Override // huawei.android.security.ITrustCircleManager
            public void loginServerRequest(ILifeCycleCallback callback, long userID, int serverRegisterStatus, String sessionID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    _data.writeInt(serverRegisterStatus);
                    _data.writeString(sessionID);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().loginServerRequest(callback, userID, serverRegisterStatus, sessionID);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public void updateServerRequest(ILifeCycleCallback callback, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateServerRequest(callback, userID);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public void finalRegister(ILifeCycleCallback callback, String authPKData, String authPKDataSign, String updateIndexInfo, String updateIndexSignature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(authPKData);
                    _data.writeString(authPKDataSign);
                    _data.writeString(updateIndexInfo);
                    _data.writeString(updateIndexSignature);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finalRegister(callback, authPKData, authPKDataSign, updateIndexInfo, updateIndexSignature);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public void finalLogin(ILifeCycleCallback callback, int updateResult, String updateIndexInfo, String updateIndexSignature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(updateResult);
                    _data.writeString(updateIndexInfo);
                    _data.writeString(updateIndexSignature);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finalLogin(callback, updateResult, updateIndexInfo, updateIndexSignature);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public void cancelRegOrLogin(ILifeCycleCallback callback, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelRegOrLogin(callback, userID);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public void logout(ILifeCycleCallback callback, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().logout(callback, userID);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public void unregister(ILifeCycleCallback callback, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(userID);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregister(callback, userID);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public long initAuthenticate(IAuthCallback callback, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    try {
                        _data.writeInt(authType);
                        try {
                            _data.writeInt(authVersion);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(policy);
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
                    try {
                        _data.writeLong(userID);
                        _data.writeByteArray(AESTmpKey);
                        if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            long _result = _reply.readLong();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        long initAuthenticate = Stub.getDefaultImpl().initAuthenticate(callback, authType, authVersion, policy, userID, AESTmpKey);
                        _reply.recycle();
                        _data.recycle();
                        return initAuthenticate;
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

            @Override // huawei.android.security.ITrustCircleManager
            public long receiveAuthSync(IAuthCallback callback, int authType, int authVersion, int taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(authType);
                    _data.writeInt(authVersion);
                    _data.writeInt(taVersion);
                    _data.writeInt(policy);
                    _data.writeLong(userID);
                    _data.writeByteArray(AESTmpKey);
                    _data.writeByteArray(tcisId);
                    _data.writeInt(pkVersion);
                    _data.writeLong(nonce);
                    _data.writeInt(authKeyAlgoType);
                    _data.writeByteArray(authKeyInfo);
                    _data.writeByteArray(authKeyInfoSign);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().receiveAuthSync(callback, authType, authVersion, taVersion, policy, userID, AESTmpKey, tcisId, pkVersion, nonce, authKeyAlgoType, authKeyInfo, authKeyInfoSign);
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

            @Override // huawei.android.security.ITrustCircleManager
            public boolean receiveAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoTypeSlave, byte[] authKeyInfoSlave, byte[] authKeyInfoSignSlave) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(authID);
                        _data.writeByteArray(tcisIdSlave);
                        _data.writeInt(pkVersionSlave);
                        _data.writeLong(nonceSlave);
                        _data.writeByteArray(mac);
                        _data.writeInt(authKeyAlgoTypeSlave);
                        _data.writeByteArray(authKeyInfoSlave);
                        _data.writeByteArray(authKeyInfoSignSlave);
                        boolean _result = false;
                        if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = true;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        boolean receiveAuthSyncAck = Stub.getDefaultImpl().receiveAuthSyncAck(authID, tcisIdSlave, pkVersionSlave, nonceSlave, mac, authKeyAlgoTypeSlave, authKeyInfoSlave, authKeyInfoSignSlave);
                        _reply.recycle();
                        _data.recycle();
                        return receiveAuthSyncAck;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public boolean receiveAck(long authID, byte[] mac) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeByteArray(mac);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().receiveAck(authID, mac);
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

            @Override // huawei.android.security.ITrustCircleManager
            public boolean requestPK(long authID, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeLong(userID);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestPK(authID, userID);
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

            @Override // huawei.android.security.ITrustCircleManager
            public boolean receivePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(authID);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(authKeyAlgoType);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(authKeyData);
                        try {
                            _data.writeByteArray(authKeyDataSign);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            boolean _result = false;
                            if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean receivePK = Stub.getDefaultImpl().receivePK(authID, authKeyAlgoType, authKeyData, authKeyDataSign);
                            _reply.recycle();
                            _data.recycle();
                            return receivePK;
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
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public void cancelAuthentication(long authId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authId);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelAuthentication(authId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ITrustCircleManager
            public int initAuthenticateSession(IAuthSessionListener callback, long authId, String authParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(authId);
                    _data.writeString(authParams);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().initAuthenticateSession(callback, authId, authParams);
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

            @Override // huawei.android.security.ITrustCircleManager
            public boolean receiveRequest(IAuthSessionListener callback, long authId, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(authId);
                    _data.writeString(data);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().receiveRequest(callback, authId, data);
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

            @Override // huawei.android.security.ITrustCircleManager
            public boolean receiveAuthSessionSyncAck(long authId, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authId);
                    _data.writeString(data);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().receiveAuthSessionSyncAck(authId, data);
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

            @Override // huawei.android.security.ITrustCircleManager
            public boolean receiveSessionAck(long authId, int userType, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authId);
                    _data.writeInt(userType);
                    _data.writeString(data);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().receiveSessionAck(authId, userType, data);
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

            @Override // huawei.android.security.ITrustCircleManager
            public boolean receiveSessionFinish(long authId, int userType, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authId);
                    _data.writeInt(userType);
                    _data.writeString(data);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().receiveSessionFinish(authId, userType, data);
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

            @Override // huawei.android.security.ITrustCircleManager
            public int requestSignature(ISignCallback callback, long signReqId, byte[] signReqParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(signReqId);
                    _data.writeByteArray(signReqParams);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestSignature(callback, signReqId, signReqParams);
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

        public static boolean setDefaultImpl(ITrustCircleManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITrustCircleManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
