package com.huawei.security.deviceauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.security.deviceauth.IHichainGroupCallback;
import com.huawei.security.deviceauth.IHichainGroupChangeListener;
import java.util.List;

public interface IGroupManageService extends IInterface {
    int addMemberToGroup(String str, long j, String str2, String str3, int i) throws RemoteException;

    int cancelGroupRequest(String str, long j) throws RemoteException;

    int cancelRequest(long j) throws RemoteException;

    int createGroup(String str, String str2, int i, String str3) throws RemoteException;

    int deleteDevGroup(String str, String str2) throws RemoteException;

    int deleteGroup(String str) throws RemoteException;

    int deleteMemberFromGroup(String str, long j, String str2, String str3) throws RemoteException;

    List<String> getDevGroupInfo(String str, String str2) throws RemoteException;

    List<String> getFriendsList(String str, String str2) throws RemoteException;

    List<String> getGroupInfo(String str) throws RemoteException;

    String getLocalConnectInfo() throws RemoteException;

    boolean isDeviceInDevGroup(String str, String str2, String str3) throws RemoteException;

    boolean isDeviceInGroup(String str, String str2) throws RemoteException;

    List<String> listJoinedDevGroups(String str, int i) throws RemoteException;

    List<String> listJoinedGroups(int i) throws RemoteException;

    List<String> listTrustedDevices(String str) throws RemoteException;

    List<String> listTrustedDevicesInGroup(String str, String str2) throws RemoteException;

    int registerCallback(String str, IHichainGroupCallback iHichainGroupCallback) throws RemoteException;

    int registerDevGroupNotice(String str, String str2, IHichainGroupChangeListener iHichainGroupChangeListener) throws RemoteException;

    int registerGroupNotice(String str, IHichainGroupChangeListener iHichainGroupChangeListener) throws RemoteException;

    int revokeDevGroupNotice(String str, String str2) throws RemoteException;

    int revokeGroupNotice(String str) throws RemoteException;

    int setFriendsList(String str, String str2, List<String> list) throws RemoteException;

    public static abstract class Stub extends Binder implements IGroupManageService {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.IGroupManageService";
        static final int TRANSACTION_addMemberToGroup = 5;
        static final int TRANSACTION_cancelGroupRequest = 15;
        static final int TRANSACTION_cancelRequest = 7;
        static final int TRANSACTION_createGroup = 2;
        static final int TRANSACTION_deleteDevGroup = 14;
        static final int TRANSACTION_deleteGroup = 3;
        static final int TRANSACTION_deleteMemberFromGroup = 6;
        static final int TRANSACTION_getDevGroupInfo = 19;
        static final int TRANSACTION_getFriendsList = 23;
        static final int TRANSACTION_getGroupInfo = 11;
        static final int TRANSACTION_getLocalConnectInfo = 4;
        static final int TRANSACTION_isDeviceInDevGroup = 18;
        static final int TRANSACTION_isDeviceInGroup = 10;
        static final int TRANSACTION_listJoinedDevGroups = 16;
        static final int TRANSACTION_listJoinedGroups = 8;
        static final int TRANSACTION_listTrustedDevices = 9;
        static final int TRANSACTION_listTrustedDevicesInGroup = 17;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_registerDevGroupNotice = 20;
        static final int TRANSACTION_registerGroupNotice = 12;
        static final int TRANSACTION_revokeDevGroupNotice = 21;
        static final int TRANSACTION_revokeGroupNotice = 13;
        static final int TRANSACTION_setFriendsList = 22;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGroupManageService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGroupManageService)) {
                return new Proxy(obj);
            }
            return (IGroupManageService) iin;
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
                        int _result = registerCallback(data.readString(), IHichainGroupCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = createGroup(data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = deleteGroup(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getLocalConnectInfo();
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = addMemberToGroup(data.readString(), data.readLong(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = deleteMemberFromGroup(data.readString(), data.readLong(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = cancelRequest(data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result8 = listJoinedGroups(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result9 = listTrustedDevices(data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDeviceInGroup = isDeviceInGroup(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isDeviceInGroup ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result10 = getGroupInfo(data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result10);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = registerGroupNotice(data.readString(), IHichainGroupChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = revokeGroupNotice(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = deleteDevGroup(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = cancelGroupRequest(data.readString(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result15 = listJoinedDevGroups(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result15);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result16 = listTrustedDevicesInGroup(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result16);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDeviceInDevGroup = isDeviceInDevGroup(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isDeviceInDevGroup ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result17 = getDevGroupInfo(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result17);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = registerDevGroupNotice(data.readString(), data.readString(), IHichainGroupChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = revokeDevGroupNotice(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result20 = setFriendsList(data.readString(), data.readString(), data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result21 = getFriendsList(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result21);
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
        public static class Proxy implements IGroupManageService {
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

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int registerCallback(String appId, IHichainGroupCallback callbackHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appId);
                    _data.writeStrongBinder(callbackHandler != null ? callbackHandler.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int createGroup(String appId, String groupName, int groupType, String groupInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appId);
                    _data.writeString(groupName);
                    _data.writeInt(groupType);
                    _data.writeString(groupInfo);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int deleteGroup(String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public String getLocalConnectInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int addMemberToGroup(String appId, long requestId, String addParams, String connectParams, int groupType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appId);
                    _data.writeLong(requestId);
                    _data.writeString(addParams);
                    _data.writeString(connectParams);
                    _data.writeInt(groupType);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int deleteMemberFromGroup(String appId, long requestId, String deleteParams, String connectParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appId);
                    _data.writeLong(requestId);
                    _data.writeString(deleteParams);
                    _data.writeString(connectParams);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int cancelRequest(long requestId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(requestId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public List<String> listJoinedGroups(int groupType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(groupType);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public List<String> listTrustedDevices(String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public boolean isDeviceInGroup(String groupId, String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeString(deviceId);
                    boolean _result = false;
                    this.mRemote.transact(10, _data, _reply, 0);
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

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public List<String> getGroupInfo(String queryParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(queryParams);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int registerGroupNotice(String groupId, IHichainGroupChangeListener groupChangeListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeStrongBinder(groupChangeListener != null ? groupChangeListener.asBinder() : null);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int revokeGroupNotice(String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int deleteDevGroup(String callerPkgName, String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(groupId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int cancelGroupRequest(String callerPkgName, long requestId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeLong(requestId);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public List<String> listJoinedDevGroups(String callerPkgName, int groupType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeInt(groupType);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public List<String> listTrustedDevicesInGroup(String callerPkgName, String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(groupId);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public boolean isDeviceInDevGroup(String callerPkgName, String groupId, String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(groupId);
                    _data.writeString(deviceId);
                    boolean _result = false;
                    this.mRemote.transact(18, _data, _reply, 0);
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

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public List<String> getDevGroupInfo(String callerPkgName, String queryParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(queryParams);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int registerDevGroupNotice(String callerPkgName, String groupId, IHichainGroupChangeListener groupChangeListener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(groupId);
                    _data.writeStrongBinder(groupChangeListener != null ? groupChangeListener.asBinder() : null);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int revokeDevGroupNotice(String callerPkgName, String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(groupId);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public int setFriendsList(String callerPkgName, String groupId, List<String> friendsList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(groupId);
                    _data.writeStringList(friendsList);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupManageService
            public List<String> getFriendsList(String callerPkgName, String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkgName);
                    _data.writeString(groupId);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
