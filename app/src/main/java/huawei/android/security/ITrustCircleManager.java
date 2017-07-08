package huawei.android.security;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITrustCircleManager extends IInterface {

    public static abstract class Stub extends Binder implements ITrustCircleManager {
        private static final String DESCRIPTOR = "huawei.android.security.ITrustCircleManager";
        static final int TRANSACTION_cancelAuthentication = 15;
        static final int TRANSACTION_cancelRegOrLogin = 6;
        static final int TRANSACTION_finalLogin = 5;
        static final int TRANSACTION_finalRegister = 4;
        static final int TRANSACTION_getCurrentState = 2;
        static final int TRANSACTION_getTcisInfo = 1;
        static final int TRANSACTION_initAuthenticate = 9;
        static final int TRANSACTION_loginServerRequest = 3;
        static final int TRANSACTION_logout = 7;
        static final int TRANSACTION_receiveAck = 12;
        static final int TRANSACTION_receiveAuthSync = 10;
        static final int TRANSACTION_receiveAuthSyncAck = 11;
        static final int TRANSACTION_receivePK = 14;
        static final int TRANSACTION_requestPK = 13;
        static final int TRANSACTION_unregister = 8;

        private static class Proxy implements ITrustCircleManager {
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

            public Bundle getTcisInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTcisInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCurrentState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void loginServerRequest(ILifeCycleCallback callback, long userID, int serverRegisterStatus, String sessionID) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(userID);
                    _data.writeInt(serverRegisterStatus);
                    _data.writeString(sessionID);
                    this.mRemote.transact(Stub.TRANSACTION_loginServerRequest, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finalRegister(ILifeCycleCallback callback, String authPKData, String authPKDataSign, String updateIndexInfo, String updateIndexSignature) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(authPKData);
                    _data.writeString(authPKDataSign);
                    _data.writeString(updateIndexInfo);
                    _data.writeString(updateIndexSignature);
                    this.mRemote.transact(Stub.TRANSACTION_finalRegister, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finalLogin(ILifeCycleCallback callback, int updateResult, String updateIndexInfo, String updateIndexSignature) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(updateResult);
                    _data.writeString(updateIndexInfo);
                    _data.writeString(updateIndexSignature);
                    this.mRemote.transact(Stub.TRANSACTION_finalLogin, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelRegOrLogin(ILifeCycleCallback callback, long userID) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(userID);
                    this.mRemote.transact(Stub.TRANSACTION_cancelRegOrLogin, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void logout(ILifeCycleCallback callback, long userID) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(userID);
                    this.mRemote.transact(Stub.TRANSACTION_logout, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregister(ILifeCycleCallback callback, long userID) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(userID);
                    this.mRemote.transact(Stub.TRANSACTION_unregister, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long initAuthenticate(IAuthCallback callback, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(authType);
                    _data.writeInt(authVersion);
                    _data.writeInt(policy);
                    _data.writeLong(userID);
                    _data.writeByteArray(AESTmpKey);
                    this.mRemote.transact(Stub.TRANSACTION_initAuthenticate, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(Stub.TRANSACTION_receiveAuthSync, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean receiveAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoTypeSlave, byte[] authKeyInfoSlave, byte[] authKeyInfoSignSlave) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeByteArray(tcisIdSlave);
                    _data.writeInt(pkVersionSlave);
                    _data.writeLong(nonceSlave);
                    _data.writeByteArray(mac);
                    _data.writeInt(authKeyAlgoTypeSlave);
                    _data.writeByteArray(authKeyInfoSlave);
                    _data.writeByteArray(authKeyInfoSignSlave);
                    this.mRemote.transact(Stub.TRANSACTION_receiveAuthSyncAck, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean receiveAck(long authID, byte[] mac) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeByteArray(mac);
                    this.mRemote.transact(Stub.TRANSACTION_receiveAck, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean requestPK(long authID, long userID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeLong(userID);
                    this.mRemote.transact(Stub.TRANSACTION_requestPK, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean receivePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authID);
                    _data.writeInt(authKeyAlgoType);
                    _data.writeByteArray(authKeyData);
                    _data.writeByteArray(authKeyDataSign);
                    this.mRemote.transact(Stub.TRANSACTION_receivePK, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelAuthentication(long authId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(authId);
                    this.mRemote.transact(Stub.TRANSACTION_cancelAuthentication, _data, _reply, 0);
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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            long _result;
            boolean _result2;
            switch (code) {
                case TRANSACTION_getTcisInfo /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _result3 = getTcisInfo();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getTcisInfo);
                        _result3.writeToParcel(reply, TRANSACTION_getTcisInfo);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getCurrentState /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result4 = getCurrentState();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_loginServerRequest /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    loginServerRequest(huawei.android.security.ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readLong(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_finalRegister /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    finalRegister(huawei.android.security.ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_finalLogin /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    finalLogin(huawei.android.security.ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelRegOrLogin /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelRegOrLogin(huawei.android.security.ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_logout /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    logout(huawei.android.security.ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregister /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregister(huawei.android.security.ILifeCycleCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_initAuthenticate /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = initAuthenticate(huawei.android.security.IAuthCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt(), data.readLong(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_receiveAuthSync /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = receiveAuthSync(huawei.android.security.IAuthCallback.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readLong(), data.createByteArray(), data.createByteArray(), data.readInt(), data.readLong(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_receiveAuthSyncAck /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = receiveAuthSyncAck(data.readLong(), data.createByteArray(), data.readInt(), data.readLong(), data.createByteArray(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getTcisInfo : 0);
                    return true;
                case TRANSACTION_receiveAck /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = receiveAck(data.readLong(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getTcisInfo : 0);
                    return true;
                case TRANSACTION_requestPK /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = requestPK(data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getTcisInfo : 0);
                    return true;
                case TRANSACTION_receivePK /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = receivePK(data.readLong(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getTcisInfo : 0);
                    return true;
                case TRANSACTION_cancelAuthentication /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelAuthentication(data.readLong());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void cancelAuthentication(long j) throws RemoteException;

    void cancelRegOrLogin(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;

    void finalLogin(ILifeCycleCallback iLifeCycleCallback, int i, String str, String str2) throws RemoteException;

    void finalRegister(ILifeCycleCallback iLifeCycleCallback, String str, String str2, String str3, String str4) throws RemoteException;

    int getCurrentState() throws RemoteException;

    Bundle getTcisInfo() throws RemoteException;

    long initAuthenticate(IAuthCallback iAuthCallback, int i, int i2, int i3, long j, byte[] bArr) throws RemoteException;

    void loginServerRequest(ILifeCycleCallback iLifeCycleCallback, long j, int i, String str) throws RemoteException;

    void logout(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;

    boolean receiveAck(long j, byte[] bArr) throws RemoteException;

    long receiveAuthSync(IAuthCallback iAuthCallback, int i, int i2, int i3, int i4, long j, byte[] bArr, byte[] bArr2, int i5, long j2, int i6, byte[] bArr3, byte[] bArr4) throws RemoteException;

    boolean receiveAuthSyncAck(long j, byte[] bArr, int i, long j2, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4) throws RemoteException;

    boolean receivePK(long j, int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean requestPK(long j, long j2) throws RemoteException;

    void unregister(ILifeCycleCallback iLifeCycleCallback, long j) throws RemoteException;
}
