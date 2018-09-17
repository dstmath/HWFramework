package android.content.pm;

import android.os.Binder;
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
                        _data.writeInt(Stub.TRANSACTION_onPackageRemoved);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_onPackageRemoved, _data, null, Stub.TRANSACTION_onPackageRemoved);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackageAdded(UserHandle user, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_onPackageRemoved);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_onPackageAdded, _data, null, Stub.TRANSACTION_onPackageRemoved);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackageChanged(UserHandle user, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_onPackageRemoved);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_onPackageChanged, _data, null, Stub.TRANSACTION_onPackageRemoved);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackagesAvailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                int i = Stub.TRANSACTION_onPackageRemoved;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_onPackageRemoved);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(packageNames);
                    if (!replacing) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onPackagesAvailable, _data, null, Stub.TRANSACTION_onPackageRemoved);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackagesUnavailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                int i = Stub.TRANSACTION_onPackageRemoved;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_onPackageRemoved);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(packageNames);
                    if (!replacing) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onPackagesUnavailable, _data, null, Stub.TRANSACTION_onPackageRemoved);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackagesSuspended(UserHandle user, String[] packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_onPackageRemoved);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(packageNames);
                    this.mRemote.transact(Stub.TRANSACTION_onPackagesSuspended, _data, null, Stub.TRANSACTION_onPackageRemoved);
                } finally {
                    _data.recycle();
                }
            }

            public void onPackagesUnsuspended(UserHandle user, String[] packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_onPackageRemoved);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(packageNames);
                    this.mRemote.transact(Stub.TRANSACTION_onPackagesUnsuspended, _data, null, Stub.TRANSACTION_onPackageRemoved);
                } finally {
                    _data.recycle();
                }
            }

            public void onShortcutChanged(UserHandle user, String packageName, ParceledListSlice shortcuts) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_onPackageRemoved);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (shortcuts != null) {
                        _data.writeInt(Stub.TRANSACTION_onPackageRemoved);
                        shortcuts.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onShortcutChanged, _data, null, Stub.TRANSACTION_onPackageRemoved);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg2 = false;
            UserHandle userHandle;
            String[] _arg1;
            switch (code) {
                case TRANSACTION_onPackageRemoved /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    onPackageRemoved(userHandle, data.readString());
                    return true;
                case TRANSACTION_onPackageAdded /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    onPackageAdded(userHandle, data.readString());
                    return true;
                case TRANSACTION_onPackageChanged /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    onPackageChanged(userHandle, data.readString());
                    return true;
                case TRANSACTION_onPackagesAvailable /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    _arg1 = data.createStringArray();
                    if (data.readInt() != 0) {
                        _arg2 = true;
                    }
                    onPackagesAvailable(userHandle, _arg1, _arg2);
                    return true;
                case TRANSACTION_onPackagesUnavailable /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    _arg1 = data.createStringArray();
                    if (data.readInt() != 0) {
                        _arg2 = true;
                    }
                    onPackagesUnavailable(userHandle, _arg1, _arg2);
                    return true;
                case TRANSACTION_onPackagesSuspended /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    onPackagesSuspended(userHandle, data.createStringArray());
                    return true;
                case TRANSACTION_onPackagesUnsuspended /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    onPackagesUnsuspended(userHandle, data.createStringArray());
                    return true;
                case TRANSACTION_onShortcutChanged /*8*/:
                    ParceledListSlice parceledListSlice;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    String _arg12 = data.readString();
                    if (data.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        parceledListSlice = null;
                    }
                    onShortcutChanged(userHandle, _arg12, parceledListSlice);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onPackageAdded(UserHandle userHandle, String str) throws RemoteException;

    void onPackageChanged(UserHandle userHandle, String str) throws RemoteException;

    void onPackageRemoved(UserHandle userHandle, String str) throws RemoteException;

    void onPackagesAvailable(UserHandle userHandle, String[] strArr, boolean z) throws RemoteException;

    void onPackagesSuspended(UserHandle userHandle, String[] strArr) throws RemoteException;

    void onPackagesUnavailable(UserHandle userHandle, String[] strArr, boolean z) throws RemoteException;

    void onPackagesUnsuspended(UserHandle userHandle, String[] strArr) throws RemoteException;

    void onShortcutChanged(UserHandle userHandle, String str, ParceledListSlice parceledListSlice) throws RemoteException;
}
