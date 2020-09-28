package android.permission;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.UserHandle;
import java.util.List;

public interface IPermissionController extends IInterface {
    void countPermissionApps(List<String> list, int i, RemoteCallback remoteCallback) throws RemoteException;

    void getAppPermissions(String str, RemoteCallback remoteCallback) throws RemoteException;

    void getPermissionUsages(boolean z, long j, RemoteCallback remoteCallback) throws RemoteException;

    void getRuntimePermissionBackup(UserHandle userHandle, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void grantOrUpgradeDefaultRuntimePermissions(RemoteCallback remoteCallback) throws RemoteException;

    void restoreDelayedRuntimePermissionBackup(String str, UserHandle userHandle, RemoteCallback remoteCallback) throws RemoteException;

    void restoreRuntimePermissionBackup(UserHandle userHandle, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void revokeRuntimePermission(String str, String str2) throws RemoteException;

    void revokeRuntimePermissions(Bundle bundle, boolean z, int i, String str, RemoteCallback remoteCallback) throws RemoteException;

    void setRuntimePermissionGrantStateByDeviceAdmin(String str, String str2, String str3, int i, RemoteCallback remoteCallback) throws RemoteException;

    public static class Default implements IPermissionController {
        @Override // android.permission.IPermissionController
        public void revokeRuntimePermissions(Bundle request, boolean doDryRun, int reason, String callerPackageName, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.permission.IPermissionController
        public void getRuntimePermissionBackup(UserHandle user, ParcelFileDescriptor pipe) throws RemoteException {
        }

        @Override // android.permission.IPermissionController
        public void restoreRuntimePermissionBackup(UserHandle user, ParcelFileDescriptor pipe) throws RemoteException {
        }

        @Override // android.permission.IPermissionController
        public void restoreDelayedRuntimePermissionBackup(String packageName, UserHandle user, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.permission.IPermissionController
        public void getAppPermissions(String packageName, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.permission.IPermissionController
        public void revokeRuntimePermission(String packageName, String permissionName) throws RemoteException {
        }

        @Override // android.permission.IPermissionController
        public void countPermissionApps(List<String> list, int flags, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.permission.IPermissionController
        public void getPermissionUsages(boolean countSystem, long numMillis, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.permission.IPermissionController
        public void setRuntimePermissionGrantStateByDeviceAdmin(String callerPackageName, String packageName, String permission, int grantState, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.permission.IPermissionController
        public void grantOrUpgradeDefaultRuntimePermissions(RemoteCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPermissionController {
        private static final String DESCRIPTOR = "android.permission.IPermissionController";
        static final int TRANSACTION_countPermissionApps = 7;
        static final int TRANSACTION_getAppPermissions = 5;
        static final int TRANSACTION_getPermissionUsages = 8;
        static final int TRANSACTION_getRuntimePermissionBackup = 2;
        static final int TRANSACTION_grantOrUpgradeDefaultRuntimePermissions = 10;
        static final int TRANSACTION_restoreDelayedRuntimePermissionBackup = 4;
        static final int TRANSACTION_restoreRuntimePermissionBackup = 3;
        static final int TRANSACTION_revokeRuntimePermission = 6;
        static final int TRANSACTION_revokeRuntimePermissions = 1;
        static final int TRANSACTION_setRuntimePermissionGrantStateByDeviceAdmin = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPermissionController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPermissionController)) {
                return new Proxy(obj);
            }
            return (IPermissionController) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "revokeRuntimePermissions";
                case 2:
                    return "getRuntimePermissionBackup";
                case 3:
                    return "restoreRuntimePermissionBackup";
                case 4:
                    return "restoreDelayedRuntimePermissionBackup";
                case 5:
                    return "getAppPermissions";
                case 6:
                    return "revokeRuntimePermission";
                case 7:
                    return "countPermissionApps";
                case 8:
                    return "getPermissionUsages";
                case 9:
                    return "setRuntimePermissionGrantStateByDeviceAdmin";
                case 10:
                    return "grantOrUpgradeDefaultRuntimePermissions";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg0;
            RemoteCallback _arg4;
            UserHandle _arg02;
            ParcelFileDescriptor _arg1;
            UserHandle _arg03;
            ParcelFileDescriptor _arg12;
            UserHandle _arg13;
            RemoteCallback _arg2;
            RemoteCallback _arg14;
            RemoteCallback _arg22;
            RemoteCallback _arg23;
            RemoteCallback _arg42;
            RemoteCallback _arg04;
            if (code != 1598968902) {
                boolean _arg05 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean _arg15 = data.readInt() != 0;
                        int _arg24 = data.readInt();
                        String _arg3 = data.readString();
                        if (data.readInt() != 0) {
                            _arg4 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        revokeRuntimePermissions(_arg0, _arg15, _arg24, _arg3, _arg4);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        getRuntimePermissionBackup(_arg02, _arg1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        restoreRuntimePermissionBackup(_arg03, _arg12);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        restoreDelayedRuntimePermissionBackup(_arg06, _arg13, _arg2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        getAppPermissions(_arg07, _arg14);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        revokeRuntimePermission(data.readString(), data.readString());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _arg08 = data.createStringArrayList();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        countPermissionApps(_arg08, _arg16, _arg22);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        long _arg17 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg23 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        getPermissionUsages(_arg05, _arg17, _arg23);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        String _arg18 = data.readString();
                        String _arg25 = data.readString();
                        int _arg32 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg42 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        setRuntimePermissionGrantStateByDeviceAdmin(_arg09, _arg18, _arg25, _arg32, _arg42);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        grantOrUpgradeDefaultRuntimePermissions(_arg04);
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
        public static class Proxy implements IPermissionController {
            public static IPermissionController sDefaultImpl;
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

            @Override // android.permission.IPermissionController
            public void revokeRuntimePermissions(Bundle request, boolean doDryRun, int reason, String callerPackageName, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(doDryRun ? 1 : 0);
                    _data.writeInt(reason);
                    _data.writeString(callerPackageName);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().revokeRuntimePermissions(request, doDryRun, reason, callerPackageName, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.permission.IPermissionController
            public void getRuntimePermissionBackup(UserHandle user, ParcelFileDescriptor pipe) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pipe != null) {
                        _data.writeInt(1);
                        pipe.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getRuntimePermissionBackup(user, pipe);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.permission.IPermissionController
            public void restoreRuntimePermissionBackup(UserHandle user, ParcelFileDescriptor pipe) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (pipe != null) {
                        _data.writeInt(1);
                        pipe.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().restoreRuntimePermissionBackup(user, pipe);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.permission.IPermissionController
            public void restoreDelayedRuntimePermissionBackup(String packageName, UserHandle user, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().restoreDelayedRuntimePermissionBackup(packageName, user, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.permission.IPermissionController
            public void getAppPermissions(String packageName, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getAppPermissions(packageName, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.permission.IPermissionController
            public void revokeRuntimePermission(String packageName, String permissionName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(permissionName);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().revokeRuntimePermission(packageName, permissionName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.permission.IPermissionController
            public void countPermissionApps(List<String> permissionNames, int flags, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(permissionNames);
                    _data.writeInt(flags);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().countPermissionApps(permissionNames, flags, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.permission.IPermissionController
            public void getPermissionUsages(boolean countSystem, long numMillis, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(countSystem ? 1 : 0);
                    _data.writeLong(numMillis);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getPermissionUsages(countSystem, numMillis, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.permission.IPermissionController
            public void setRuntimePermissionGrantStateByDeviceAdmin(String callerPackageName, String packageName, String permission, int grantState, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPackageName);
                    _data.writeString(packageName);
                    _data.writeString(permission);
                    _data.writeInt(grantState);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setRuntimePermissionGrantStateByDeviceAdmin(callerPackageName, packageName, permission, grantState, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.permission.IPermissionController
            public void grantOrUpgradeDefaultRuntimePermissions(RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().grantOrUpgradeDefaultRuntimePermissions(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPermissionController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPermissionController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
