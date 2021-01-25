package com.huawei.security.deviceauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.security.deviceauth.IGroupAuthCallbacks;
import com.huawei.security.deviceauth.ISignReqCallback;
import com.huawei.security.deviceauth.ITrustedDeviceChangeListener;
import java.util.List;

public interface IGroupAuthService extends IInterface {
    int addDeviceChangeListener(String str, ITrustedDeviceChangeListener iTrustedDeviceChangeListener, IBinder iBinder) throws RemoteException;

    int authDevice(String str, IGroupAuthCallbacks iGroupAuthCallbacks, long j, String str2, IBinder iBinder) throws RemoteException;

    int cancelAuthRequest(String str, long j, IBinder iBinder) throws RemoteException;

    int checkAccessToDevice(String str, String str2) throws RemoteException;

    int checkAccessToGroup(String str, String str2) throws RemoteException;

    String getAuthState(String str, long j, String str2, String str3, IBinder iBinder) throws RemoteException;

    String getDeviceIdHash(String str, int i) throws RemoteException;

    List<String> getGroupInfo(String str) throws RemoteException;

    List<String> getRelatedGroupInfo(String str) throws RemoteException;

    void informDeviceDisconnection(String str, String str2) throws RemoteException;

    boolean isIdenticalAccountDevice(String str) throws RemoteException;

    boolean isPotentialTrustedDevice(int i, String str, boolean z) throws RemoteException;

    boolean processAuthData(String str, IGroupAuthCallbacks iGroupAuthCallbacks, long j, byte[] bArr, IBinder iBinder) throws RemoteException;

    int queryTrustedDeviceNum() throws RemoteException;

    int requestSignature(ISignReqCallback iSignReqCallback, long j, byte[] bArr) throws RemoteException;

    int revokeDeviceChangeListener(String str, IBinder iBinder) throws RemoteException;

    int startGroupManageService() throws RemoteException;

    public static abstract class Stub extends Binder implements IGroupAuthService {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.IGroupAuthService";
        static final int TRANSACTION_addDeviceChangeListener = 4;
        static final int TRANSACTION_authDevice = 1;
        static final int TRANSACTION_cancelAuthRequest = 3;
        static final int TRANSACTION_checkAccessToDevice = 16;
        static final int TRANSACTION_checkAccessToGroup = 15;
        static final int TRANSACTION_getAuthState = 8;
        static final int TRANSACTION_getDeviceIdHash = 6;
        static final int TRANSACTION_getGroupInfo = 13;
        static final int TRANSACTION_getRelatedGroupInfo = 14;
        static final int TRANSACTION_informDeviceDisconnection = 9;
        static final int TRANSACTION_isIdenticalAccountDevice = 17;
        static final int TRANSACTION_isPotentialTrustedDevice = 7;
        static final int TRANSACTION_processAuthData = 2;
        static final int TRANSACTION_queryTrustedDeviceNum = 11;
        static final int TRANSACTION_requestSignature = 10;
        static final int TRANSACTION_revokeDeviceChangeListener = 5;
        static final int TRANSACTION_startGroupManageService = 12;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGroupAuthService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGroupAuthService)) {
                return new Proxy(obj);
            }
            return (IGroupAuthService) iin;
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
                        int _result = authDevice(data.readString(), IGroupAuthCallbacks.Stub.asInterface(data.readStrongBinder()), data.readLong(), data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean processAuthData = processAuthData(data.readString(), IGroupAuthCallbacks.Stub.asInterface(data.readStrongBinder()), data.readLong(), data.createByteArray(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(processAuthData ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = cancelAuthRequest(data.readString(), data.readLong(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = addDeviceChangeListener(data.readString(), ITrustedDeviceChangeListener.Stub.asInterface(data.readStrongBinder()), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = revokeDeviceChangeListener(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getDeviceIdHash(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPotentialTrustedDevice = isPotentialTrustedDevice(data.readInt(), data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(isPotentialTrustedDevice ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getAuthState(data.readString(), data.readLong(), data.readString(), data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        informDeviceDisconnection(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = requestSignature(ISignReqCallback.Stub.asInterface(data.readStrongBinder()), data.readLong(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = queryTrustedDeviceNum();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = startGroupManageService();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result10 = getGroupInfo(data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result10);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result11 = getRelatedGroupInfo(data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result11);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = checkAccessToGroup(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = checkAccessToDevice(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isIdenticalAccountDevice = isIdenticalAccountDevice(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isIdenticalAccountDevice ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IGroupAuthService {
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int authDevice(String callerPkgName, IGroupAuthCallbacks callbacks, long reqId, String authParams, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    _data.writeLong(reqId);
                    _data.writeString(authParams);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public boolean processAuthData(String callerPkgName, IGroupAuthCallbacks callbacks, long reqId, byte[] data, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    _data.writeLong(reqId);
                    _data.writeByteArray(data);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int cancelAuthRequest(String callerPkgName, long reqId, IBinder binderToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeLong(reqId);
                    _data.writeStrongBinder(binderToken);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int addDeviceChangeListener(String callerPkgName, ITrustedDeviceChangeListener trustedDeviceChangeListener, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeStrongBinder(trustedDeviceChangeListener != null ? trustedDeviceChangeListener.asBinder() : null);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int revokeDeviceChangeListener(String callerPkgName, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public String getDeviceIdHash(String deviceId, int hashLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(hashLen);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public boolean isPotentialTrustedDevice(int idType, String deviceId, boolean isPrecise) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(idType);
                    _data.writeString(deviceId);
                    boolean _result = true;
                    _data.writeInt(isPrecise ? 1 : 0);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public String getAuthState(String callerPkgName, long reqId, String groupId, String peerConnDeviceId, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeLong(reqId);
                    _data.writeString(groupId);
                    _data.writeString(peerConnDeviceId);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public void informDeviceDisconnection(String callerPkgName, String connDeviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(connDeviceId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int requestSignature(ISignReqCallback callback, long signReqId, byte[] signReqParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeLong(signReqId);
                    _data.writeByteArray(signReqParams);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int queryTrustedDeviceNum() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int startGroupManageService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public List<String> getGroupInfo(String queryParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(queryParams);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public List<String> getRelatedGroupInfo(String connDeviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int checkAccessToGroup(String groupId, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeString(pkgName);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int checkAccessToDevice(String connDeviceId, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
                    _data.writeString(pkgName);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public boolean isIdenticalAccountDevice(String connDeviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
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
        }
    }
}
