package android.content.pm;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;

public interface IOnAppsChangedListener extends IInterface {

    public static abstract class Stub extends Binder implements IOnAppsChangedListener {
        private static final String DESCRIPTOR = "android.content.pm.IOnAppsChangedListener";
        static final int TRANSACTION_onPackageAdded = 2;
        static final int TRANSACTION_onPackageChanged = 3;
        static final int TRANSACTION_onPackageRemoved = 1;
        static final int TRANSACTION_onPackagesAvailable = 4;
        static final int TRANSACTION_onPackagesSuspended = 6;
        static final int TRANSACTION_onPackagesUnavailable = 5;
        static final int TRANSACTION_onPackagesUnsuspended = 7;
        static final int TRANSACTION_onShortcutChanged = 8;

        private static class Proxy implements IOnAppsChangedListener {
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

            public void onPackageRemoved(UserHandle user, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackageAdded(UserHandle user, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackageChanged(UserHandle user, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackagesAvailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(packageNames);
                    _data.writeInt(replacing);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackagesUnavailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(packageNames);
                    _data.writeInt(replacing);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackagesSuspended(UserHandle user, String[] packageNames, Bundle launcherExtras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(packageNames);
                    if (launcherExtras != null) {
                        _data.writeInt(1);
                        launcherExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackagesUnsuspended(UserHandle user, String[] packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(packageNames);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onShortcutChanged(UserHandle user, String packageName, ParceledListSlice shortcuts) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (shortcuts != null) {
                        _data.writeInt(1);
                        shortcuts.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnAppsChangedListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnAppsChangedListener)) {
                return new Proxy(obj);
            }
            return (IOnAppsChangedListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v13, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v17, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v21, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v29, resolved type: android.content.pm.ParceledListSlice} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v1 */
        /* JADX WARNING: type inference failed for: r3v5 */
        /* JADX WARNING: type inference failed for: r3v9 */
        /* JADX WARNING: type inference failed for: r3v25 */
        /* JADX WARNING: type inference failed for: r3v33 */
        /* JADX WARNING: type inference failed for: r3v34 */
        /* JADX WARNING: type inference failed for: r3v35 */
        /* JADX WARNING: type inference failed for: r3v36 */
        /* JADX WARNING: type inference failed for: r3v37 */
        /* JADX WARNING: type inference failed for: r3v38 */
        /* JADX WARNING: type inference failed for: r3v39 */
        /* JADX WARNING: type inference failed for: r3v40 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            UserHandle _arg0;
            UserHandle _arg02;
            if (code != 1598968902) {
                boolean _arg2 = false;
                ? _arg22 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                        }
                        onPackageRemoved(_arg22, data.readString());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                        }
                        onPackageAdded(_arg22, data.readString());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                        }
                        onPackageChanged(_arg22, data.readString());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                        }
                        String[] _arg1 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        onPackagesAvailable(_arg22, _arg1, _arg2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                        }
                        String[] _arg12 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        onPackagesUnavailable(_arg22, _arg12, _arg2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        String[] _arg13 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onPackagesSuspended(_arg0, _arg13, _arg22);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg22 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                        }
                        onPackagesUnsuspended(_arg22, data.createStringArray());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                        }
                        onShortcutChanged(_arg02, _arg14, _arg22);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onPackageAdded(UserHandle userHandle, String str) throws RemoteException;

    void onPackageChanged(UserHandle userHandle, String str) throws RemoteException;

    void onPackageRemoved(UserHandle userHandle, String str) throws RemoteException;

    void onPackagesAvailable(UserHandle userHandle, String[] strArr, boolean z) throws RemoteException;

    void onPackagesSuspended(UserHandle userHandle, String[] strArr, Bundle bundle) throws RemoteException;

    void onPackagesUnavailable(UserHandle userHandle, String[] strArr, boolean z) throws RemoteException;

    void onPackagesUnsuspended(UserHandle userHandle, String[] strArr) throws RemoteException;

    void onShortcutChanged(UserHandle userHandle, String str, ParceledListSlice parceledListSlice) throws RemoteException;
}
