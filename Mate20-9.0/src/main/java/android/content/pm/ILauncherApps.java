package android.content.pm;

import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IOnAppsChangedListener;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import java.util.ArrayList;
import java.util.List;

public interface ILauncherApps extends IInterface {

    public static abstract class Stub extends Binder implements ILauncherApps {
        private static final String DESCRIPTOR = "android.content.pm.ILauncherApps";
        static final int TRANSACTION_addOnAppsChangedListener = 1;
        static final int TRANSACTION_getApplicationInfo = 11;
        static final int TRANSACTION_getLauncherActivities = 3;
        static final int TRANSACTION_getShortcutConfigActivities = 18;
        static final int TRANSACTION_getShortcutConfigActivityIntent = 19;
        static final int TRANSACTION_getShortcutIconFd = 16;
        static final int TRANSACTION_getShortcutIconResId = 15;
        static final int TRANSACTION_getShortcuts = 12;
        static final int TRANSACTION_getSuspendedPackageLauncherExtras = 9;
        static final int TRANSACTION_hasShortcutHostPermission = 17;
        static final int TRANSACTION_isActivityEnabled = 10;
        static final int TRANSACTION_isPackageEnabled = 8;
        static final int TRANSACTION_pinShortcuts = 13;
        static final int TRANSACTION_removeOnAppsChangedListener = 2;
        static final int TRANSACTION_resolveActivity = 4;
        static final int TRANSACTION_resolveActivityByIntent = 5;
        static final int TRANSACTION_showAppDetailsAsUser = 7;
        static final int TRANSACTION_startActivityAsUser = 6;
        static final int TRANSACTION_startShortcut = 14;

        private static class Proxy implements ILauncherApps {
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

            public void addOnAppsChangedListener(String callingPackage, IOnAppsChangedListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeOnAppsChangedListener(IOnAppsChangedListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getLauncherActivities(String callingPackage, String packageName, UserHandle user) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityInfo resolveActivity(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
                ActivityInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResolveInfo resolveActivityByIntent(Intent intent, UserHandle user) throws RemoteException {
                ResolveInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ResolveInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startActivityAsUser(IApplicationThread caller, String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sourceBounds != null) {
                        _data.writeInt(1);
                        sourceBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (opts != null) {
                        _data.writeInt(1);
                        opts.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showAppDetailsAsUser(IApplicationThread caller, String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    _data.writeString(callingPackage);
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sourceBounds != null) {
                        _data.writeInt(1);
                        sourceBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (opts != null) {
                        _data.writeInt(1);
                        opts.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPackageEnabled(String callingPackage, String packageName, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
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

            public Bundle getSuspendedPackageLauncherExtras(String packageName, UserHandle user) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isActivityEnabled(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = true;
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
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

            public ApplicationInfo getApplicationInfo(String callingPackage, String packageName, int flags, UserHandle user) throws RemoteException {
                ApplicationInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ApplicationInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getShortcuts(String callingPackage, long changedSince, String packageName, List shortcutIds, ComponentName componentName, int flags, UserHandle user) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeLong(changedSince);
                    _data.writeString(packageName);
                    _data.writeList(shortcutIds);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pinShortcuts(String callingPackage, String packageName, List<String> shortcutIds, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    _data.writeStringList(shortcutIds);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startShortcut(String callingPackage, String packageName, String id, Rect sourceBounds, Bundle startActivityOptions, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    _data.writeString(id);
                    boolean _result = true;
                    if (sourceBounds != null) {
                        _data.writeInt(1);
                        sourceBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (startActivityOptions != null) {
                        _data.writeInt(1);
                        startActivityOptions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public int getShortcutIconResId(String callingPackage, String packageName, String id, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    _data.writeString(id);
                    _data.writeInt(userId);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id, int userId) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    _data.writeString(id);
                    _data.writeInt(userId);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasShortcutHostPermission(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
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

            public ParceledListSlice getShortcutConfigActivities(String callingPackage, String packageName, UserHandle user) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IntentSender getShortcutConfigActivityIntent(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
                IntentSender _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (component != null) {
                        _data.writeInt(1);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = IntentSender.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILauncherApps asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILauncherApps)) {
                return new Proxy(obj);
            }
            return (ILauncherApps) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v9, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v13, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v17, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v22, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v27, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v31, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v35, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v39, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v43, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v49, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v53, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v7, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v62, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v66, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v71, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v72, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v73, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v74, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v75, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v76, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v77, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v78, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v79, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v80, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v81, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v82, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v83, resolved type: android.os.UserHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v84, resolved type: android.os.UserHandle} */
        /* JADX WARNING: type inference failed for: r0v58, types: [android.os.Bundle] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg1;
            Intent _arg0;
            ComponentName _arg2;
            Rect _arg3;
            Bundle _arg4;
            ComponentName _arg22;
            Rect _arg32;
            Bundle _arg42;
            ComponentName _arg12;
            ComponentName _arg43;
            Rect _arg33;
            ComponentName _arg13;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                UserHandle _arg23 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        addOnAppsChangedListener(data.readString(), IOnAppsChangedListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeOnAppsChangedListener(IOnAppsChangedListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result = getLauncherActivities(_arg02, _arg14, _arg23);
                        reply.writeNoException();
                        if (_result != null) {
                            parcel2.writeInt(1);
                            _result.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        ActivityInfo _result2 = resolveActivity(_arg03, _arg1, _arg23);
                        reply.writeNoException();
                        if (_result2 != null) {
                            parcel2.writeInt(1);
                            _result2.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Intent.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        ResolveInfo _result3 = resolveActivityByIntent(_arg0, _arg23);
                        reply.writeNoException();
                        if (_result3 != null) {
                            parcel2.writeInt(1);
                            _result3.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg04 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg15 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = Rect.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg4 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        startActivityAsUser(_arg04, _arg15, _arg2, _arg3, _arg4, _arg23);
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg05 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg22 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = Rect.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg32 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg42 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg42 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        showAppDetailsAsUser(_arg05, _arg16, _arg22, _arg32, _arg42, _arg23);
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        String _arg17 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result4 = isPackageEnabled(_arg06, _arg17, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        Bundle _result5 = getSuspendedPackageLauncherExtras(_arg07, _arg23);
                        reply.writeNoException();
                        if (_result5 != null) {
                            parcel2.writeInt(1);
                            _result5.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result6 = isActivityEnabled(_arg08, _arg12, _arg23);
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        String _arg18 = data.readString();
                        int _arg24 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        ApplicationInfo _result7 = getApplicationInfo(_arg09, _arg18, _arg24, _arg23);
                        reply.writeNoException();
                        if (_result7 != null) {
                            parcel2.writeInt(1);
                            _result7.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        long _arg19 = data.readLong();
                        String _arg25 = data.readString();
                        ClassLoader cl = getClass().getClassLoader();
                        ArrayList readArrayList = parcel.readArrayList(cl);
                        if (data.readInt() != 0) {
                            _arg43 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg43 = null;
                        }
                        int _arg5 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        ClassLoader classLoader = cl;
                        ParceledListSlice _result8 = getShortcuts(_arg010, _arg19, _arg25, readArrayList, _arg43, _arg5, _arg23);
                        reply.writeNoException();
                        if (_result8 != null) {
                            parcel2.writeInt(1);
                            _result8.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        String _arg110 = data.readString();
                        List<String> _arg26 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        pinShortcuts(_arg011, _arg110, _arg26, _arg23);
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        String _arg111 = data.readString();
                        String _arg27 = data.readString();
                        if (data.readInt() != 0) {
                            _arg33 = Rect.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg33 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result9 = startShortcut(_arg012, _arg111, _arg27, _arg33, _arg23, data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result10 = getShortcutIconResId(data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result11 = getShortcutIconFd(data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result11 != null) {
                            parcel2.writeInt(1);
                            _result11.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result12 = hasShortcutHostPermission(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg013 = data.readString();
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        ParceledListSlice _result13 = getShortcutConfigActivities(_arg013, _arg112, _arg23);
                        reply.writeNoException();
                        if (_result13 != null) {
                            parcel2.writeInt(1);
                            _result13.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg014 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = ComponentName.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg23 = (UserHandle) UserHandle.CREATOR.createFromParcel(parcel);
                        }
                        IntentSender _result14 = getShortcutConfigActivityIntent(_arg014, _arg13, _arg23);
                        reply.writeNoException();
                        if (_result14 != null) {
                            parcel2.writeInt(1);
                            _result14.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void addOnAppsChangedListener(String str, IOnAppsChangedListener iOnAppsChangedListener) throws RemoteException;

    ApplicationInfo getApplicationInfo(String str, String str2, int i, UserHandle userHandle) throws RemoteException;

    ParceledListSlice getLauncherActivities(String str, String str2, UserHandle userHandle) throws RemoteException;

    ParceledListSlice getShortcutConfigActivities(String str, String str2, UserHandle userHandle) throws RemoteException;

    IntentSender getShortcutConfigActivityIntent(String str, ComponentName componentName, UserHandle userHandle) throws RemoteException;

    ParcelFileDescriptor getShortcutIconFd(String str, String str2, String str3, int i) throws RemoteException;

    int getShortcutIconResId(String str, String str2, String str3, int i) throws RemoteException;

    ParceledListSlice getShortcuts(String str, long j, String str2, List list, ComponentName componentName, int i, UserHandle userHandle) throws RemoteException;

    Bundle getSuspendedPackageLauncherExtras(String str, UserHandle userHandle) throws RemoteException;

    boolean hasShortcutHostPermission(String str) throws RemoteException;

    boolean isActivityEnabled(String str, ComponentName componentName, UserHandle userHandle) throws RemoteException;

    boolean isPackageEnabled(String str, String str2, UserHandle userHandle) throws RemoteException;

    void pinShortcuts(String str, String str2, List<String> list, UserHandle userHandle) throws RemoteException;

    void removeOnAppsChangedListener(IOnAppsChangedListener iOnAppsChangedListener) throws RemoteException;

    ActivityInfo resolveActivity(String str, ComponentName componentName, UserHandle userHandle) throws RemoteException;

    ResolveInfo resolveActivityByIntent(Intent intent, UserHandle userHandle) throws RemoteException;

    void showAppDetailsAsUser(IApplicationThread iApplicationThread, String str, ComponentName componentName, Rect rect, Bundle bundle, UserHandle userHandle) throws RemoteException;

    void startActivityAsUser(IApplicationThread iApplicationThread, String str, ComponentName componentName, Rect rect, Bundle bundle, UserHandle userHandle) throws RemoteException;

    boolean startShortcut(String str, String str2, String str3, Rect rect, Bundle bundle, int i) throws RemoteException;
}
