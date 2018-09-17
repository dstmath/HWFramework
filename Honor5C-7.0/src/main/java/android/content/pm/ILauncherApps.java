package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import java.util.List;

public interface ILauncherApps extends IInterface {

    public static abstract class Stub extends Binder implements ILauncherApps {
        private static final String DESCRIPTOR = "android.content.pm.ILauncherApps";
        static final int TRANSACTION_addOnAppsChangedListener = 1;
        static final int TRANSACTION_getApplicationInfo = 10;
        static final int TRANSACTION_getLauncherActivities = 3;
        static final int TRANSACTION_getShortcutIconFd = 15;
        static final int TRANSACTION_getShortcutIconResId = 14;
        static final int TRANSACTION_getShortcuts = 11;
        static final int TRANSACTION_hasShortcutHostPermission = 16;
        static final int TRANSACTION_isActivityEnabled = 9;
        static final int TRANSACTION_isPackageEnabled = 8;
        static final int TRANSACTION_pinShortcuts = 12;
        static final int TRANSACTION_removeOnAppsChangedListener = 2;
        static final int TRANSACTION_resolveActivity = 4;
        static final int TRANSACTION_resolveActivityByIntent = 5;
        static final int TRANSACTION_showAppDetailsAsUser = 7;
        static final int TRANSACTION_startActivityAsUser = 6;
        static final int TRANSACTION_startShortcut = 13;

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
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addOnAppsChangedListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeOnAppsChangedListener(IOnAppsChangedListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeOnAppsChangedListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getLauncherActivities(String packageName, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getLauncherActivities, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityInfo resolveActivity(ComponentName component, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ActivityInfo activityInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (component != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_resolveActivity, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        activityInfo = (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        activityInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return activityInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ResolveInfo resolveActivityByIntent(Intent intent, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ResolveInfo resolveInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_resolveActivityByIntent, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        resolveInfo = (ResolveInfo) ResolveInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        resolveInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return resolveInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startActivityAsUser(ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (component != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sourceBounds != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        sourceBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (opts != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        opts.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startActivityAsUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void showAppDetailsAsUser(ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (component != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sourceBounds != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        sourceBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (opts != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        opts.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_showAppDetailsAsUser, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isPackageEnabled(String packageName, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isPackageEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isActivityEnabled(ComponentName component, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (component != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        component.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_isActivityEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ApplicationInfo getApplicationInfo(String packageName, int flags, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ApplicationInfo applicationInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getApplicationInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        applicationInfo = (ApplicationInfo) ApplicationInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        applicationInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return applicationInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getShortcuts(String callingPackage, long changedSince, String packageName, List shortcutIds, ComponentName componentName, int flags, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeLong(changedSince);
                    _data.writeString(packageName);
                    _data.writeList(shortcutIds);
                    if (componentName != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    if (user != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getShortcuts, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
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
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_pinShortcuts, _data, _reply, 0);
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
                    if (sourceBounds != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        sourceBounds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (startActivityOptions != null) {
                        _data.writeInt(Stub.TRANSACTION_addOnAppsChangedListener);
                        startActivityOptions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_startShortcut, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_getShortcutIconResId, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParcelFileDescriptor parcelFileDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    _data.writeString(id);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getShortcutIconFd, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parcelFileDescriptor;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_hasShortcutHostPermission, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            UserHandle userHandle;
            ParceledListSlice _result;
            ComponentName componentName;
            Rect rect;
            Bundle bundle;
            UserHandle userHandle2;
            boolean _result2;
            String _arg2;
            String _arg1;
            switch (code) {
                case TRANSACTION_addOnAppsChangedListener /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    addOnAppsChangedListener(data.readString(), android.content.pm.IOnAppsChangedListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeOnAppsChangedListener /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeOnAppsChangedListener(android.content.pm.IOnAppsChangedListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getLauncherActivities /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    _result = getLauncherActivities(_arg0, userHandle);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_addOnAppsChangedListener);
                        _result.writeToParcel(reply, TRANSACTION_addOnAppsChangedListener);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_resolveActivity /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    ActivityInfo _result3 = resolveActivity(componentName, userHandle);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_addOnAppsChangedListener);
                        _result3.writeToParcel(reply, TRANSACTION_addOnAppsChangedListener);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_resolveActivityByIntent /*5*/:
                    Intent intent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    ResolveInfo _result4 = resolveActivityByIntent(intent, userHandle);
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_addOnAppsChangedListener);
                        _result4.writeToParcel(reply, TRANSACTION_addOnAppsChangedListener);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_startActivityAsUser /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    if (data.readInt() != 0) {
                        userHandle2 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle2 = null;
                    }
                    startActivityAsUser(componentName, rect, bundle, userHandle2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_showAppDetailsAsUser /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    if (data.readInt() != 0) {
                        userHandle2 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle2 = null;
                    }
                    showAppDetailsAsUser(componentName, rect, bundle, userHandle2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isPackageEnabled /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    _result2 = isPackageEnabled(_arg0, userHandle);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_addOnAppsChangedListener : 0);
                    return true;
                case TRANSACTION_isActivityEnabled /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    if (data.readInt() != 0) {
                        userHandle = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle = null;
                    }
                    _result2 = isActivityEnabled(componentName, userHandle);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_addOnAppsChangedListener : 0);
                    return true;
                case TRANSACTION_getApplicationInfo /*10*/:
                    UserHandle userHandle3;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    int _arg12 = data.readInt();
                    if (data.readInt() != 0) {
                        userHandle3 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle3 = null;
                    }
                    ApplicationInfo _result5 = getApplicationInfo(_arg0, _arg12, userHandle3);
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_addOnAppsChangedListener);
                        _result5.writeToParcel(reply, TRANSACTION_addOnAppsChangedListener);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getShortcuts /*11*/:
                    ComponentName componentName2;
                    UserHandle userHandle4;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    long _arg13 = data.readLong();
                    _arg2 = data.readString();
                    List _arg3 = data.readArrayList(getClass().getClassLoader());
                    if (data.readInt() != 0) {
                        componentName2 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName2 = null;
                    }
                    int _arg5 = data.readInt();
                    if (data.readInt() != 0) {
                        userHandle4 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle4 = null;
                    }
                    _result = getShortcuts(_arg0, _arg13, _arg2, _arg3, componentName2, _arg5, userHandle4);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_addOnAppsChangedListener);
                        _result.writeToParcel(reply, TRANSACTION_addOnAppsChangedListener);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_pinShortcuts /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readString();
                    List<String> _arg22 = data.createStringArrayList();
                    if (data.readInt() != 0) {
                        userHandle2 = (UserHandle) UserHandle.CREATOR.createFromParcel(data);
                    } else {
                        userHandle2 = null;
                    }
                    pinShortcuts(_arg0, _arg1, _arg22, userHandle2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startShortcut /*13*/:
                    Rect rect2;
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        rect2 = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect2 = null;
                    }
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    _result2 = startShortcut(_arg0, _arg1, _arg2, rect2, bundle2, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_addOnAppsChangedListener : 0);
                    return true;
                case TRANSACTION_getShortcutIconResId /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result6 = getShortcutIconResId(data.readString(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result6);
                    return true;
                case TRANSACTION_getShortcutIconFd /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    ParcelFileDescriptor _result7 = getShortcutIconFd(data.readString(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(TRANSACTION_addOnAppsChangedListener);
                        _result7.writeToParcel(reply, TRANSACTION_addOnAppsChangedListener);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_hasShortcutHostPermission /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = hasShortcutHostPermission(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_addOnAppsChangedListener : 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addOnAppsChangedListener(String str, IOnAppsChangedListener iOnAppsChangedListener) throws RemoteException;

    ApplicationInfo getApplicationInfo(String str, int i, UserHandle userHandle) throws RemoteException;

    ParceledListSlice getLauncherActivities(String str, UserHandle userHandle) throws RemoteException;

    ParcelFileDescriptor getShortcutIconFd(String str, String str2, String str3, int i) throws RemoteException;

    int getShortcutIconResId(String str, String str2, String str3, int i) throws RemoteException;

    ParceledListSlice getShortcuts(String str, long j, String str2, List list, ComponentName componentName, int i, UserHandle userHandle) throws RemoteException;

    boolean hasShortcutHostPermission(String str) throws RemoteException;

    boolean isActivityEnabled(ComponentName componentName, UserHandle userHandle) throws RemoteException;

    boolean isPackageEnabled(String str, UserHandle userHandle) throws RemoteException;

    void pinShortcuts(String str, String str2, List<String> list, UserHandle userHandle) throws RemoteException;

    void removeOnAppsChangedListener(IOnAppsChangedListener iOnAppsChangedListener) throws RemoteException;

    ActivityInfo resolveActivity(ComponentName componentName, UserHandle userHandle) throws RemoteException;

    ResolveInfo resolveActivityByIntent(Intent intent, UserHandle userHandle) throws RemoteException;

    void showAppDetailsAsUser(ComponentName componentName, Rect rect, Bundle bundle, UserHandle userHandle) throws RemoteException;

    void startActivityAsUser(ComponentName componentName, Rect rect, Bundle bundle, UserHandle userHandle) throws RemoteException;

    boolean startShortcut(String str, String str2, String str3, Rect rect, Bundle bundle, int i) throws RemoteException;
}
