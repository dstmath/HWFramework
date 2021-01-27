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

    String getDeviceExtInfo(String str, String str2, String str3) throws RemoteException;

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

    int setDeviceExtInfo(String str, String str2, String str3, String str4) throws RemoteException;

    int startGroupManageService() throws RemoteException;

    public static class Default implements IGroupAuthService {
        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int authDevice(String callerPkgName, IGroupAuthCallbacks callbacks, long reqId, String authParams, IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public boolean processAuthData(String callerPkgName, IGroupAuthCallbacks callbacks, long reqId, byte[] data, IBinder token) throws RemoteException {
            return false;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int cancelAuthRequest(String callerPkgName, long reqId, IBinder binderToken) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int addDeviceChangeListener(String callerPkgName, ITrustedDeviceChangeListener trustedDeviceChangeListener, IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int revokeDeviceChangeListener(String callerPkgName, IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public String getDeviceIdHash(String deviceId, int hashLen) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public boolean isPotentialTrustedDevice(int idType, String deviceId, boolean isPrecise) throws RemoteException {
            return false;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public String getAuthState(String callerPkgName, long reqId, String groupId, String peerConnDeviceId, IBinder token) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public void informDeviceDisconnection(String callerPkgName, String connDeviceId) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int requestSignature(ISignReqCallback callback, long signReqId, byte[] signReqParams) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int queryTrustedDeviceNum() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int startGroupManageService() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public List<String> getGroupInfo(String queryParams) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public List<String> getRelatedGroupInfo(String connDeviceId) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int checkAccessToGroup(String groupId, String pkgName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int checkAccessToDevice(String connDeviceId, String pkgName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public boolean isIdenticalAccountDevice(String connDeviceId) throws RemoteException {
            return false;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public int setDeviceExtInfo(String callerPkgName, String connDeviceId, String groupId, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthService
        public String getDeviceExtInfo(String callerPkgName, String connDeviceId, String groupId) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGroupAuthService {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.IGroupAuthService";
        static final int TRANSACTION_addDeviceChangeListener = 4;
        static final int TRANSACTION_authDevice = 1;
        static final int TRANSACTION_cancelAuthRequest = 3;
        static final int TRANSACTION_checkAccessToDevice = 16;
        static final int TRANSACTION_checkAccessToGroup = 15;
        static final int TRANSACTION_getAuthState = 8;
        static final int TRANSACTION_getDeviceExtInfo = 19;
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
        static final int TRANSACTION_setDeviceExtInfo = 18;
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
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = setDeviceExtInfo(data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _result15 = getDeviceExtInfo(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result15);
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
        public static class Proxy implements IGroupAuthService {
            public static IGroupAuthService sDefaultImpl;
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
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callerPkgName);
                        _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(reqId);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(authParams);
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStrongBinder(token);
                        try {
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int authDevice = Stub.getDefaultImpl().authDevice(callerPkgName, callbacks, reqId, authParams, token);
                            _reply.recycle();
                            _data.recycle();
                            return authDevice;
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public boolean processAuthData(String callerPkgName, IGroupAuthCallbacks callbacks, long reqId, byte[] data, IBinder token) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callerPkgName);
                        _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(reqId);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(data);
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStrongBinder(token);
                        try {
                            boolean _result = false;
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean processAuthData = Stub.getDefaultImpl().processAuthData(callerPkgName, callbacks, reqId, data, token);
                            _reply.recycle();
                            _data.recycle();
                            return processAuthData;
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int cancelAuthRequest(String callerPkgName, long reqId, IBinder binderToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeLong(reqId);
                    _data.writeStrongBinder(binderToken);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cancelAuthRequest(callerPkgName, reqId, binderToken);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int addDeviceChangeListener(String callerPkgName, ITrustedDeviceChangeListener trustedDeviceChangeListener, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeStrongBinder(trustedDeviceChangeListener != null ? trustedDeviceChangeListener.asBinder() : null);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addDeviceChangeListener(callerPkgName, trustedDeviceChangeListener, token);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int revokeDeviceChangeListener(String callerPkgName, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().revokeDeviceChangeListener(callerPkgName, token);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public String getDeviceIdHash(String deviceId, int hashLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(hashLen);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceIdHash(deviceId, hashLen);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
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
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPotentialTrustedDevice(idType, deviceId, isPrecise);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public String getAuthState(String callerPkgName, long reqId, String groupId, String peerConnDeviceId, IBinder token) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callerPkgName);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(reqId);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(groupId);
                        try {
                            _data.writeString(peerConnDeviceId);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeStrongBinder(token);
                            if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                String _result = _reply.readString();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            String authState = Stub.getDefaultImpl().getAuthState(callerPkgName, reqId, groupId, peerConnDeviceId, token);
                            _reply.recycle();
                            _data.recycle();
                            return authState;
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public void informDeviceDisconnection(String callerPkgName, String connDeviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(connDeviceId);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().informDeviceDisconnection(callerPkgName, connDeviceId);
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
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int queryTrustedDeviceNum() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryTrustedDeviceNum();
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int startGroupManageService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startGroupManageService();
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public List<String> getGroupInfo(String queryParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(queryParams);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGroupInfo(queryParams);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public List<String> getRelatedGroupInfo(String connDeviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRelatedGroupInfo(connDeviceId);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int checkAccessToGroup(String groupId, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkAccessToGroup(groupId, pkgName);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int checkAccessToDevice(String connDeviceId, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkAccessToDevice(connDeviceId, pkgName);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public boolean isIdenticalAccountDevice(String connDeviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isIdenticalAccountDevice(connDeviceId);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public int setDeviceExtInfo(String callerPkgName, String connDeviceId, String groupId, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(connDeviceId);
                    _data.writeString(groupId);
                    _data.writeString(extInfo);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceExtInfo(callerPkgName, connDeviceId, groupId, extInfo);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthService
            public String getDeviceExtInfo(String callerPkgName, String connDeviceId, String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(connDeviceId);
                    _data.writeString(groupId);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceExtInfo(callerPkgName, connDeviceId, groupId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGroupAuthService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGroupAuthService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
