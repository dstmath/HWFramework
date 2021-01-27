package com.huawei.security.dpermission;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.security.dpermission.IPermissionRecordQueryCallback;
import com.huawei.security.dpermission.IRequestPermissionsResult;
import com.huawei.security.dpermission.permissionusingremind.IOnPermissionUsingReminder;

public interface IDistributedPermissionManager extends IInterface {
    void addPermissionRecord(String str, String str2, int i, int i2, int i3) throws RemoteException;

    void addTargetDevice(String str, int i) throws RemoteException;

    int allocateDuid(String str, int i) throws RemoteException;

    boolean canRequestPermissionFromRemote(String str, String str2) throws RemoteException;

    int checkDPermission(int i, String str) throws RemoteException;

    int checkDPermissionAndStartUsing(String str, String str2) throws RemoteException;

    int checkDPermissionAndUse(String str, String str2) throws RemoteException;

    int checkPermission(String str, String str2, int i, int i2) throws RemoteException;

    String getBundleLabelInfo(int i) throws RemoteException;

    String getPermissionRecord(String str) throws RemoteException;

    void getPermissionRecordAsync(String str, IPermissionRecordQueryCallback iPermissionRecordQueryCallback) throws RemoteException;

    String getPermissionUsagesInfo(String str, String[] strArr) throws RemoteException;

    void grantSensitivePermissionToRemoteApp(String str, String str2, int i) throws RemoteException;

    boolean isTargetDevice(String str, int i) throws RemoteException;

    int notifyDeviceStatusChanged(String str, int i) throws RemoteException;

    int notifyPermissionChanged(int i, String str, int i2) throws RemoteException;

    int notifySyncPermission(String str, int i, String str2) throws RemoteException;

    int notifyUidPermissionChanged(int i) throws RemoteException;

    int postPermissionEvent(String str) throws RemoteException;

    String processZ2aMessage(String str, String str2) throws RemoteException;

    int queryDuid(String str, int i) throws RemoteException;

    int registerOnPermissionUsingReminder(IOnPermissionUsingReminder iOnPermissionUsingReminder) throws RemoteException;

    void requestPermissionsFromRemote(String[] strArr, IRequestPermissionsResult iRequestPermissionsResult, String str, String str2, int i) throws RemoteException;

    void startUsingPermission(String str, String str2) throws RemoteException;

    void stopUsingPermission(String str, String str2) throws RemoteException;

    int unregisterOnPermissionUsingReminder(IOnPermissionUsingReminder iOnPermissionUsingReminder) throws RemoteException;

    int verifyPermissionAndState(String str, String str2) throws RemoteException;

    int verifyPermissionFromRemote(String str, String str2, String str3) throws RemoteException;

    int verifySelfPermissionFromRemote(String str, String str2) throws RemoteException;

    int waitDuidReady(String str, int i, int i2) throws RemoteException;

    public static abstract class Stub extends Binder implements IDistributedPermissionManager {
        private static final String DESCRIPTOR = "com.huawei.security.dpermission.IDistributedPermissionManager";
        static final int TRANSACTION_addPermissionRecord = 17;
        static final int TRANSACTION_addTargetDevice = 8;
        static final int TRANSACTION_allocateDuid = 1;
        static final int TRANSACTION_canRequestPermissionFromRemote = 13;
        static final int TRANSACTION_checkDPermission = 3;
        static final int TRANSACTION_checkDPermissionAndStartUsing = 24;
        static final int TRANSACTION_checkDPermissionAndUse = 25;
        static final int TRANSACTION_checkPermission = 26;
        static final int TRANSACTION_getBundleLabelInfo = 30;
        static final int TRANSACTION_getPermissionRecord = 18;
        static final int TRANSACTION_getPermissionRecordAsync = 19;
        static final int TRANSACTION_getPermissionUsagesInfo = 29;
        static final int TRANSACTION_grantSensitivePermissionToRemoteApp = 15;
        static final int TRANSACTION_isTargetDevice = 7;
        static final int TRANSACTION_notifyDeviceStatusChanged = 6;
        static final int TRANSACTION_notifyPermissionChanged = 5;
        static final int TRANSACTION_notifySyncPermission = 9;
        static final int TRANSACTION_notifyUidPermissionChanged = 4;
        static final int TRANSACTION_postPermissionEvent = 27;
        static final int TRANSACTION_processZ2aMessage = 16;
        static final int TRANSACTION_queryDuid = 2;
        static final int TRANSACTION_registerOnPermissionUsingReminder = 20;
        static final int TRANSACTION_requestPermissionsFromRemote = 14;
        static final int TRANSACTION_startUsingPermission = 22;
        static final int TRANSACTION_stopUsingPermission = 23;
        static final int TRANSACTION_unregisterOnPermissionUsingReminder = 21;
        static final int TRANSACTION_verifyPermissionAndState = 28;
        static final int TRANSACTION_verifyPermissionFromRemote = 11;
        static final int TRANSACTION_verifySelfPermissionFromRemote = 12;
        static final int TRANSACTION_waitDuidReady = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDistributedPermissionManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDistributedPermissionManager)) {
                return new Proxy(obj);
            }
            return (IDistributedPermissionManager) iin;
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
                        int _result = allocateDuid(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = queryDuid(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = checkDPermission(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = notifyUidPermissionChanged(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = notifyPermissionChanged(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = notifyDeviceStatusChanged(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isTargetDevice = isTargetDevice(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isTargetDevice ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        addTargetDevice(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = notifySyncPermission(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = waitDuidReady(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = verifyPermissionFromRemote(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = verifySelfPermissionFromRemote(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean canRequestPermissionFromRemote = canRequestPermissionFromRemote(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(canRequestPermissionFromRemote ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        requestPermissionsFromRemote(data.createStringArray(), IRequestPermissionsResult.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        grantSensitivePermissionToRemoteApp(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        String _result11 = processZ2aMessage(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result11);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        addPermissionRecord(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = getPermissionRecord(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        getPermissionRecordAsync(data.readString(), IPermissionRecordQueryCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = registerOnPermissionUsingReminder(IOnPermissionUsingReminder.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = unregisterOnPermissionUsingReminder(IOnPermissionUsingReminder.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        startUsingPermission(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        stopUsingPermission(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_checkDPermissionAndStartUsing /* 24 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = checkDPermissionAndStartUsing(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case TRANSACTION_checkDPermissionAndUse /* 25 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = checkDPermissionAndUse(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result16);
                        return true;
                    case TRANSACTION_checkPermission /* 26 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = checkPermission(data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = postPermissionEvent(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result18);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        int _result19 = verifyPermissionAndState(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result19);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        String _result20 = getPermissionUsagesInfo(data.readString(), data.createStringArray());
                        reply.writeNoException();
                        reply.writeString(_result20);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        String _result21 = getBundleLabelInfo(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result21);
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
        public static class Proxy implements IDistributedPermissionManager {
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

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int allocateDuid(String nodeId, int rUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeInt(rUid);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int queryDuid(String nodeId, int rUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeInt(rUid);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int checkDPermission(int dUid, String permissionName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dUid);
                    _data.writeString(permissionName);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int notifyUidPermissionChanged(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int notifyPermissionChanged(int uid, String permissionName, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(permissionName);
                    _data.writeInt(status);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int notifyDeviceStatusChanged(String nodeId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeInt(status);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public boolean isTargetDevice(String nodeId, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeInt(uid);
                    boolean _result = false;
                    this.mRemote.transact(7, _data, _reply, 0);
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

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public void addTargetDevice(String nodeId, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeInt(uid);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int notifySyncPermission(String nodeId, int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int waitDuidReady(String nodeId, int rUid, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeInt(rUid);
                    _data.writeInt(timeout);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int verifyPermissionFromRemote(String permission, String nodeId, String appIdInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeString(nodeId);
                    _data.writeString(appIdInfo);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int verifySelfPermissionFromRemote(String permission, String nodeId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeString(nodeId);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public boolean canRequestPermissionFromRemote(String permission, String nodeId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeString(nodeId);
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
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

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public void requestPermissionsFromRemote(String[] permissions, IRequestPermissionsResult callback, String nodeId, String bundleName, int reasonResId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(permissions);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(nodeId);
                    _data.writeString(bundleName);
                    _data.writeInt(reasonResId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public void grantSensitivePermissionToRemoteApp(String permission, String nodeId, int ruid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    _data.writeString(nodeId);
                    _data.writeInt(ruid);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public String processZ2aMessage(String command, String payload) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    _data.writeString(payload);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public void addPermissionRecord(String permissionName, String deviceId, int uid, int successCount, int failCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(deviceId);
                    _data.writeInt(uid);
                    _data.writeInt(successCount);
                    _data.writeInt(failCount);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public String getPermissionRecord(String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(data);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public void getPermissionRecordAsync(String data, IPermissionRecordQueryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(data);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int registerOnPermissionUsingReminder(IOnPermissionUsingReminder reminder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(reminder != null ? reminder.asBinder() : null);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int unregisterOnPermissionUsingReminder(IOnPermissionUsingReminder reminder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(reminder != null ? reminder.asBinder() : null);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public void startUsingPermission(String permissionName, String appIdInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(appIdInfo);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public void stopUsingPermission(String permissionName, String appIdInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(appIdInfo);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int checkDPermissionAndStartUsing(String permissionName, String appIdInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(appIdInfo);
                    this.mRemote.transact(Stub.TRANSACTION_checkDPermissionAndStartUsing, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int checkDPermissionAndUse(String permissionName, String appIdInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(appIdInfo);
                    this.mRemote.transact(Stub.TRANSACTION_checkDPermissionAndUse, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int checkPermission(String permissionName, String nodeId, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(nodeId);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    this.mRemote.transact(Stub.TRANSACTION_checkPermission, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int postPermissionEvent(String event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(event);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public int verifyPermissionAndState(String permissionName, String appIdInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permissionName);
                    _data.writeString(appIdInfo);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public String getPermissionUsagesInfo(String packageName, String[] permissions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStringArray(permissions);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IDistributedPermissionManager
            public String getBundleLabelInfo(int dUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dUid);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
