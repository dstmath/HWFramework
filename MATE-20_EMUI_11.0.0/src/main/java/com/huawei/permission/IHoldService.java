package com.huawei.permission;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHoldService extends IInterface {
    void addRecord(int i, int i2, int i3, boolean z) throws RemoteException;

    void authenticateSmsSend(IBinder iBinder, int i, int i2, String str, String str2) throws RemoteException;

    Bundle callHsmService(String str, Bundle bundle) throws RemoteException;

    int checkBeforeShowDialog(int i, int i2, String str) throws RemoteException;

    int checkBeforeShowDialogWithPid(int i, int i2, int i3, String str) throws RemoteException;

    boolean checkPreBlock(int i, int i2, boolean z) throws RemoteException;

    boolean checkSystemAppInternal(int i, boolean z) throws RemoteException;

    PendingIntent getPendingIntent(int i, Intent intent, int i2) throws RemoteException;

    int holdServiceByRequestPermission(int i, int i2, int i3, String str) throws RemoteException;

    void notifyBackgroundMgr(String str, int i, int i2, int i3) throws RemoteException;

    int releaseHoldService(int i, int i2, int i3) throws RemoteException;

    void removeRuntimePermissions(String str) throws RemoteException;

    public static class Default implements IHoldService {
        @Override // com.huawei.permission.IHoldService
        public int holdServiceByRequestPermission(int uid, int pid, int permissionType, String desAddr) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.permission.IHoldService
        public boolean checkSystemAppInternal(int callUid, boolean excludeCust) throws RemoteException {
            return false;
        }

        @Override // com.huawei.permission.IHoldService
        public PendingIntent getPendingIntent(int requestCode, Intent intent, int flags) throws RemoteException {
            return null;
        }

        @Override // com.huawei.permission.IHoldService
        public void addRecord(int uid, int permissionType, int state, boolean click) throws RemoteException {
        }

        @Override // com.huawei.permission.IHoldService
        public int releaseHoldService(int uid, int userSelection, int permissionType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.permission.IHoldService
        public int checkBeforeShowDialog(int uid, int permissionType, String desAddr) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.permission.IHoldService
        public int checkBeforeShowDialogWithPid(int uid, int pid, int permissionType, String desAddr) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.permission.IHoldService
        public void removeRuntimePermissions(String packageName) throws RemoteException {
        }

        @Override // com.huawei.permission.IHoldService
        public Bundle callHsmService(String method, Bundle params) throws RemoteException {
            return null;
        }

        @Override // com.huawei.permission.IHoldService
        public boolean checkPreBlock(int callUid, int permissionType, boolean showToast) throws RemoteException {
            return false;
        }

        @Override // com.huawei.permission.IHoldService
        public void authenticateSmsSend(IBinder notifyResult, int uidOf3RdApk, int smsId, String smsBody, String smsAddress) throws RemoteException {
        }

        @Override // com.huawei.permission.IHoldService
        public void notifyBackgroundMgr(String pkgName, int uidOf3RdApk, int permType, int permCfg) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHoldService {
        private static final String DESCRIPTOR = "com.huawei.permission.IHoldService";
        static final int TRANSACTION_addRecord = 4;
        static final int TRANSACTION_authenticateSmsSend = 11;
        static final int TRANSACTION_callHsmService = 9;
        static final int TRANSACTION_checkBeforeShowDialog = 6;
        static final int TRANSACTION_checkBeforeShowDialogWithPid = 7;
        static final int TRANSACTION_checkPreBlock = 10;
        static final int TRANSACTION_checkSystemAppInternal = 2;
        static final int TRANSACTION_getPendingIntent = 3;
        static final int TRANSACTION_holdServiceByRequestPermission = 1;
        static final int TRANSACTION_notifyBackgroundMgr = 12;
        static final int TRANSACTION_releaseHoldService = 5;
        static final int TRANSACTION_removeRuntimePermissions = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHoldService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHoldService)) {
                return new Proxy(obj);
            }
            return (IHoldService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg1;
            Bundle _arg12;
            if (code != 1598968902) {
                boolean _arg2 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = holdServiceByRequestPermission(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        boolean checkSystemAppInternal = checkSystemAppInternal(_arg0, _arg2);
                        reply.writeNoException();
                        reply.writeInt(checkSystemAppInternal ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Intent) Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        PendingIntent _result2 = getPendingIntent(_arg02, _arg1, data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg13 = data.readInt();
                        int _arg22 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        addRecord(_arg03, _arg13, _arg22, _arg2);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = releaseHoldService(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = checkBeforeShowDialog(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = checkBeforeShowDialogWithPid(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        removeRuntimePermissions(data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        Bundle _result6 = callHsmService(_arg04, _arg12);
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        boolean checkPreBlock = checkPreBlock(_arg05, _arg14, _arg2);
                        reply.writeNoException();
                        reply.writeInt(checkPreBlock ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        authenticateSmsSend(data.readStrongBinder(), data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        notifyBackgroundMgr(data.readString(), data.readInt(), data.readInt(), data.readInt());
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
        public static class Proxy implements IHoldService {
            public static IHoldService sDefaultImpl;
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

            @Override // com.huawei.permission.IHoldService
            public int holdServiceByRequestPermission(int uid, int pid, int permissionType, String desAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(permissionType);
                    _data.writeString(desAddr);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().holdServiceByRequestPermission(uid, pid, permissionType, desAddr);
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

            @Override // com.huawei.permission.IHoldService
            public boolean checkSystemAppInternal(int callUid, boolean excludeCust) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callUid);
                    boolean _result = true;
                    _data.writeInt(excludeCust ? 1 : 0);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkSystemAppInternal(callUid, excludeCust);
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

            @Override // com.huawei.permission.IHoldService
            public PendingIntent getPendingIntent(int requestCode, Intent intent, int flags) throws RemoteException {
                PendingIntent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestCode);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPendingIntent(requestCode, intent, flags);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PendingIntent) PendingIntent.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.permission.IHoldService
            public void addRecord(int uid, int permissionType, int state, boolean click) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(permissionType);
                    _data.writeInt(state);
                    _data.writeInt(click ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addRecord(uid, permissionType, state, click);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.permission.IHoldService
            public int releaseHoldService(int uid, int userSelection, int permissionType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(userSelection);
                    _data.writeInt(permissionType);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().releaseHoldService(uid, userSelection, permissionType);
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

            @Override // com.huawei.permission.IHoldService
            public int checkBeforeShowDialog(int uid, int permissionType, String desAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(permissionType);
                    _data.writeString(desAddr);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkBeforeShowDialog(uid, permissionType, desAddr);
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

            @Override // com.huawei.permission.IHoldService
            public int checkBeforeShowDialogWithPid(int uid, int pid, int permissionType, String desAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(permissionType);
                    _data.writeString(desAddr);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkBeforeShowDialogWithPid(uid, pid, permissionType, desAddr);
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

            @Override // com.huawei.permission.IHoldService
            public void removeRuntimePermissions(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeRuntimePermissions(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.permission.IHoldService
            public Bundle callHsmService(String method, Bundle params) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(method);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().callHsmService(method, params);
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

            @Override // com.huawei.permission.IHoldService
            public boolean checkPreBlock(int callUid, int permissionType, boolean showToast) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callUid);
                    _data.writeInt(permissionType);
                    boolean _result = true;
                    _data.writeInt(showToast ? 1 : 0);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPreBlock(callUid, permissionType, showToast);
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

            @Override // com.huawei.permission.IHoldService
            public void authenticateSmsSend(IBinder notifyResult, int uidOf3RdApk, int smsId, String smsBody, String smsAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(notifyResult);
                    _data.writeInt(uidOf3RdApk);
                    _data.writeInt(smsId);
                    _data.writeString(smsBody);
                    _data.writeString(smsAddress);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().authenticateSmsSend(notifyResult, uidOf3RdApk, smsId, smsBody, smsAddress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.permission.IHoldService
            public void notifyBackgroundMgr(String pkgName, int uidOf3RdApk, int permType, int permCfg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(uidOf3RdApk);
                    _data.writeInt(permType);
                    _data.writeInt(permCfg);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyBackgroundMgr(pkgName, uidOf3RdApk, permType, permCfg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHoldService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHoldService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
