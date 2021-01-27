package com.huawei.security.deviceauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.security.deviceauth.ICallbackMethods;
import java.util.List;

public interface IHichainService extends IInterface {
    int addAuthInfo(String str, SessionInfo sessionInfo, ICallbackMethods iCallbackMethods, int i, UserInfo userInfo) throws RemoteException;

    int authKeyAgree(String str, SessionInfo sessionInfo, ICallbackMethods iCallbackMethods, String str2, int i) throws RemoteException;

    int authenticate(String str, SessionInfo sessionInfo, ICallbackMethods iCallbackMethods, int i) throws RemoteException;

    int authenticateAcrossProcess(String str, String str2, SessionInfo sessionInfo, ICallbackMethods iCallbackMethods, int i) throws RemoteException;

    int bind(String str, SessionInfo sessionInfo, ICallbackMethods iCallbackMethods, String str2, int i) throws RemoteException;

    int cancel(String str, String str2, IBinder iBinder) throws RemoteException;

    int cloneHomeId(String str, SessionInfo sessionInfo, ICallbackMethods iCallbackMethods, int i) throws RemoteException;

    int deleteLocalAuthInfo(String str, UserInfo userInfo) throws RemoteException;

    byte[] enableExport(String str, UserInfo userInfo) throws RemoteException;

    ExportResult exportAuthInfo(String str, UserInfo userInfo, byte[] bArr, int i) throws RemoteException;

    ExportResult exportHomeId(String str, UserInfo userInfo, int i, String str2) throws RemoteException;

    int importAuthInfo(String str, String str2, byte[] bArr, int i, byte[] bArr2) throws RemoteException;

    int importHomeId(String str, String str2, int i, String str3, byte[] bArr) throws RemoteException;

    boolean isRegistered(String str, UserInfo userInfo) throws RemoteException;

    boolean isTrustPeer(String str, UserInfo userInfo, boolean z) throws RemoteException;

    boolean isTrustPeerAcrossProcess(String str, String str2, UserInfo userInfo, boolean z) throws RemoteException;

    List<String> listTrustPeers(String str, String str2, int i, byte[] bArr, boolean z) throws RemoteException;

    List<String> listTrustPeersAcrossProcess(String str, String str2, UserInfo userInfo, boolean z) throws RemoteException;

    void processReceivedData(String str, SessionInfo sessionInfo, ICallbackMethods iCallbackMethods, byte[] bArr) throws RemoteException;

    int register(String str, UserInfo userInfo) throws RemoteException;

    int registerWithCloud(String str, UserInfo userInfo, int i, String str2, ICallbackMethods iCallbackMethods) throws RemoteException;

    int removeAuthInfo(String str, SessionInfo sessionInfo, ICallbackMethods iCallbackMethods, int i, UserInfo userInfo) throws RemoteException;

    int unBind(String str, SessionInfo sessionInfo, ICallbackMethods iCallbackMethods) throws RemoteException;

    int unregister(String str, UserInfo userInfo) throws RemoteException;

    public static class Default implements IHichainService {
        @Override // com.huawei.security.deviceauth.IHichainService
        public int register(String callerPackageName, UserInfo userInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int unregister(String callerPackageName, UserInfo userInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int deleteLocalAuthInfo(String callerPackageName, UserInfo userInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int importAuthInfo(String callerPackageName, String serviceType, byte[] selfAuthId, int authInfoType, byte[] authInfoBlob) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public ExportResult exportAuthInfo(String callerPackageName, UserInfo exportUserInfo, byte[] selfAuthId, int authInfoType) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public boolean isRegistered(String callerPackageName, UserInfo userInfo) throws RemoteException {
            return false;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public boolean isTrustPeer(String callerPackageName, UserInfo userInfo, boolean localOnly) throws RemoteException {
            return false;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public boolean isTrustPeerAcrossProcess(String callerPackageName, String tartgetPackageName, UserInfo userInfo, boolean localOnly) throws RemoteException {
            return false;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public List<String> listTrustPeers(String callerPackageName, String serviceType, int trustUserType, byte[] ownerId, boolean localOnly) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public List<String> listTrustPeersAcrossProcess(String callerPackageName, String tartgetPackageName, UserInfo trustUserInfo, boolean localOnly) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int cancel(String callerPackageName, String sessionId, IBinder binderToken) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int registerWithCloud(String callerPackageName, UserInfo userInfo, int syncType, String accountId, ICallbackMethods managerCallback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int authKeyAgree(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, String pin, int keyLength) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int bind(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, String pin, int keyLength) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int authenticate(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int authenticateAcrossProcess(String callerPackageName, String tartgetPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int addAuthInfo(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength, UserInfo addUserInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int removeAuthInfo(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength, UserInfo rmvUserInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int unBind(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public void processReceivedData(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, byte[] receivedData) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int cloneHomeId(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public byte[] enableExport(String callerPackageName, UserInfo userInfo) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public ExportResult exportHomeId(String callerPackageName, UserInfo userInfo, int exportType, String encryptKeyBase) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.IHichainService
        public int importHomeId(String callerPackageName, String serviceType, int importType, String encryptKeyBase, byte[] homeIdBlob) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHichainService {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.IHichainService";
        static final int TRANSACTION_addAuthInfo = 17;
        static final int TRANSACTION_authKeyAgree = 13;
        static final int TRANSACTION_authenticate = 15;
        static final int TRANSACTION_authenticateAcrossProcess = 16;
        static final int TRANSACTION_bind = 14;
        static final int TRANSACTION_cancel = 11;
        static final int TRANSACTION_cloneHomeId = 21;
        static final int TRANSACTION_deleteLocalAuthInfo = 3;
        static final int TRANSACTION_enableExport = 22;
        static final int TRANSACTION_exportAuthInfo = 5;
        static final int TRANSACTION_exportHomeId = 23;
        static final int TRANSACTION_importAuthInfo = 4;
        static final int TRANSACTION_importHomeId = 24;
        static final int TRANSACTION_isRegistered = 6;
        static final int TRANSACTION_isTrustPeer = 7;
        static final int TRANSACTION_isTrustPeerAcrossProcess = 8;
        static final int TRANSACTION_listTrustPeers = 9;
        static final int TRANSACTION_listTrustPeersAcrossProcess = 10;
        static final int TRANSACTION_processReceivedData = 20;
        static final int TRANSACTION_register = 1;
        static final int TRANSACTION_registerWithCloud = 12;
        static final int TRANSACTION_removeAuthInfo = 18;
        static final int TRANSACTION_unBind = 19;
        static final int TRANSACTION_unregister = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHichainService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHichainService)) {
                return new Proxy(obj);
            }
            return (IHichainService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            UserInfo _arg1;
            UserInfo _arg12;
            UserInfo _arg13;
            UserInfo _arg14;
            UserInfo _arg15;
            UserInfo _arg16;
            UserInfo _arg2;
            UserInfo _arg22;
            UserInfo _arg17;
            SessionInfo _arg18;
            SessionInfo _arg19;
            SessionInfo _arg110;
            SessionInfo _arg23;
            SessionInfo _arg111;
            UserInfo _arg4;
            SessionInfo _arg112;
            UserInfo _arg42;
            SessionInfo _arg113;
            SessionInfo _arg114;
            SessionInfo _arg115;
            UserInfo _arg116;
            UserInfo _arg117;
            if (code != 1598968902) {
                boolean _arg3 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result = register(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        int _result2 = unregister(_arg02, _arg12);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        int _result3 = deleteLocalAuthInfo(_arg03, _arg13);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = importAuthInfo(data.readString(), data.readString(), data.createByteArray(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        ExportResult _result5 = exportAuthInfo(_arg04, _arg14, data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        boolean isRegistered = isRegistered(_arg05, _arg15);
                        reply.writeNoException();
                        reply.writeInt(isRegistered ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        boolean isTrustPeer = isTrustPeer(_arg06, _arg16, _arg3);
                        reply.writeNoException();
                        reply.writeInt(isTrustPeer ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        String _arg118 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        boolean isTrustPeerAcrossProcess = isTrustPeerAcrossProcess(_arg07, _arg118, _arg2, _arg3);
                        reply.writeNoException();
                        reply.writeInt(isTrustPeerAcrossProcess ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result6 = listTrustPeers(data.readString(), data.readString(), data.readInt(), data.createByteArray(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeStringList(_result6);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        String _arg119 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = true;
                        }
                        List<String> _result7 = listTrustPeersAcrossProcess(_arg08, _arg119, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeStringList(_result7);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = cancel(data.readString(), data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        if (data.readInt() != 0) {
                            _arg17 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg17 = null;
                        }
                        int _result9 = registerWithCloud(_arg09, _arg17, data.readInt(), data.readString(), ICallbackMethods.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        if (data.readInt() != 0) {
                            _arg18 = (SessionInfo) SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg18 = null;
                        }
                        int _result10 = authKeyAgree(_arg010, _arg18, ICallbackMethods.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        if (data.readInt() != 0) {
                            _arg19 = (SessionInfo) SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg19 = null;
                        }
                        int _result11 = bind(_arg011, _arg19, ICallbackMethods.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        if (data.readInt() != 0) {
                            _arg110 = (SessionInfo) SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg110 = null;
                        }
                        int _result12 = authenticate(_arg012, _arg110, ICallbackMethods.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg013 = data.readString();
                        String _arg120 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (SessionInfo) SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        int _result13 = authenticateAcrossProcess(_arg013, _arg120, _arg23, ICallbackMethods.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg014 = data.readString();
                        if (data.readInt() != 0) {
                            _arg111 = (SessionInfo) SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg111 = null;
                        }
                        ICallbackMethods _arg24 = ICallbackMethods.Stub.asInterface(data.readStrongBinder());
                        int _arg32 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg4 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        int _result14 = addAuthInfo(_arg014, _arg111, _arg24, _arg32, _arg4);
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        if (data.readInt() != 0) {
                            _arg112 = (SessionInfo) SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg112 = null;
                        }
                        ICallbackMethods _arg25 = ICallbackMethods.Stub.asInterface(data.readStrongBinder());
                        int _arg33 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg42 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        int _result15 = removeAuthInfo(_arg015, _arg112, _arg25, _arg33, _arg42);
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        if (data.readInt() != 0) {
                            _arg113 = (SessionInfo) SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg113 = null;
                        }
                        int _result16 = unBind(_arg016, _arg113, ICallbackMethods.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg017 = data.readString();
                        if (data.readInt() != 0) {
                            _arg114 = (SessionInfo) SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg114 = null;
                        }
                        processReceivedData(_arg017, _arg114, ICallbackMethods.Stub.asInterface(data.readStrongBinder()), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg018 = data.readString();
                        if (data.readInt() != 0) {
                            _arg115 = (SessionInfo) SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg115 = null;
                        }
                        int _result17 = cloneHomeId(_arg018, _arg115, ICallbackMethods.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg019 = data.readString();
                        if (data.readInt() != 0) {
                            _arg116 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg116 = null;
                        }
                        byte[] _result18 = enableExport(_arg019, _arg116);
                        reply.writeNoException();
                        reply.writeByteArray(_result18);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg020 = data.readString();
                        if (data.readInt() != 0) {
                            _arg117 = (UserInfo) UserInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg117 = null;
                        }
                        ExportResult _result19 = exportHomeId(_arg020, _arg117, data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result19 != null) {
                            reply.writeInt(1);
                            _result19.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = importHomeId(data.readString(), data.readString(), data.readInt(), data.readString(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result20);
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
        public static class Proxy implements IHichainService {
            public static IHichainService sDefaultImpl;
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int register(String callerPackageName, UserInfo userInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().register(callerPackageName, userInfo);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int unregister(String callerPackageName, UserInfo userInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregister(callerPackageName, userInfo);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int deleteLocalAuthInfo(String callerPackageName, UserInfo userInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteLocalAuthInfo(callerPackageName, userInfo);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int importAuthInfo(String callerPackageName, String serviceType, byte[] selfAuthId, int authInfoType, byte[] authInfoBlob) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    _data.writeString(serviceType);
                    _data.writeByteArray(selfAuthId);
                    _data.writeInt(authInfoType);
                    _data.writeByteArray(authInfoBlob);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().importAuthInfo(callerPackageName, serviceType, selfAuthId, authInfoType, authInfoBlob);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public ExportResult exportAuthInfo(String callerPackageName, UserInfo exportUserInfo, byte[] selfAuthId, int authInfoType) throws RemoteException {
                ExportResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (exportUserInfo != null) {
                        _data.writeInt(1);
                        exportUserInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(selfAuthId);
                    _data.writeInt(authInfoType);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().exportAuthInfo(callerPackageName, exportUserInfo, selfAuthId, authInfoType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ExportResult) ExportResult.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public boolean isRegistered(String callerPackageName, UserInfo userInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    boolean _result = true;
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRegistered(callerPackageName, userInfo);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public boolean isTrustPeer(String callerPackageName, UserInfo userInfo, boolean localOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    boolean _result = true;
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(localOnly ? 1 : 0);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTrustPeer(callerPackageName, userInfo, localOnly);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public boolean isTrustPeerAcrossProcess(String callerPackageName, String tartgetPackageName, UserInfo userInfo, boolean localOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    _data.writeString(tartgetPackageName);
                    boolean _result = true;
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(localOnly ? 1 : 0);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isTrustPeerAcrossProcess(callerPackageName, tartgetPackageName, userInfo, localOnly);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public List<String> listTrustPeers(String callerPackageName, String serviceType, int trustUserType, byte[] ownerId, boolean localOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    _data.writeString(serviceType);
                    _data.writeInt(trustUserType);
                    _data.writeByteArray(ownerId);
                    _data.writeInt(localOnly ? 1 : 0);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().listTrustPeers(callerPackageName, serviceType, trustUserType, ownerId, localOnly);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public List<String> listTrustPeersAcrossProcess(String callerPackageName, String tartgetPackageName, UserInfo trustUserInfo, boolean localOnly) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    _data.writeString(tartgetPackageName);
                    int i = 1;
                    if (trustUserInfo != null) {
                        _data.writeInt(1);
                        trustUserInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!localOnly) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().listTrustPeersAcrossProcess(callerPackageName, tartgetPackageName, trustUserInfo, localOnly);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int cancel(String callerPackageName, String sessionId, IBinder binderToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    _data.writeString(sessionId);
                    _data.writeStrongBinder(binderToken);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cancel(callerPackageName, sessionId, binderToken);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int registerWithCloud(String callerPackageName, UserInfo userInfo, int syncType, String accountId, ICallbackMethods managerCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(syncType);
                    _data.writeString(accountId);
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerWithCloud(callerPackageName, userInfo, syncType, accountId, managerCallback);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int authKeyAgree(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, String pin, int keyLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    _data.writeString(pin);
                    _data.writeInt(keyLength);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().authKeyAgree(callerPackageName, sessionInfo, managerCallback, pin, keyLength);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int bind(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, String pin, int keyLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    _data.writeString(pin);
                    _data.writeInt(keyLength);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().bind(callerPackageName, sessionInfo, managerCallback, pin, keyLength);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int authenticate(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    _data.writeInt(keyLength);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().authenticate(callerPackageName, sessionInfo, managerCallback, keyLength);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int authenticateAcrossProcess(String callerPackageName, String tartgetPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    _data.writeString(tartgetPackageName);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    _data.writeInt(keyLength);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().authenticateAcrossProcess(callerPackageName, tartgetPackageName, sessionInfo, managerCallback, keyLength);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int addAuthInfo(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength, UserInfo addUserInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    _data.writeInt(keyLength);
                    if (addUserInfo != null) {
                        _data.writeInt(1);
                        addUserInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addAuthInfo(callerPackageName, sessionInfo, managerCallback, keyLength, addUserInfo);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int removeAuthInfo(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength, UserInfo rmvUserInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    _data.writeInt(keyLength);
                    if (rmvUserInfo != null) {
                        _data.writeInt(1);
                        rmvUserInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeAuthInfo(callerPackageName, sessionInfo, managerCallback, keyLength, rmvUserInfo);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int unBind(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unBind(callerPackageName, sessionInfo, managerCallback);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public void processReceivedData(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, byte[] receivedData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    _data.writeByteArray(receivedData);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().processReceivedData(callerPackageName, sessionInfo, managerCallback, receivedData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IHichainService
            public int cloneHomeId(String callerPackageName, SessionInfo sessionInfo, ICallbackMethods managerCallback, int keyLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (sessionInfo != null) {
                        _data.writeInt(1);
                        sessionInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(managerCallback != null ? managerCallback.asBinder() : null);
                    _data.writeInt(keyLength);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cloneHomeId(callerPackageName, sessionInfo, managerCallback, keyLength);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public byte[] enableExport(String callerPackageName, UserInfo userInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableExport(callerPackageName, userInfo);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IHichainService
            public ExportResult exportHomeId(String callerPackageName, UserInfo userInfo, int exportType, String encryptKeyBase) throws RemoteException {
                ExportResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    if (userInfo != null) {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(exportType);
                    _data.writeString(encryptKeyBase);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().exportHomeId(callerPackageName, userInfo, exportType, encryptKeyBase);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ExportResult) ExportResult.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.security.deviceauth.IHichainService
            public int importHomeId(String callerPackageName, String serviceType, int importType, String encryptKeyBase, byte[] homeIdBlob) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    _data.writeString(serviceType);
                    _data.writeInt(importType);
                    _data.writeString(encryptKeyBase);
                    _data.writeByteArray(homeIdBlob);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().importHomeId(callerPackageName, serviceType, importType, encryptKeyBase, homeIdBlob);
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

        public static boolean setDefaultImpl(IHichainService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHichainService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
