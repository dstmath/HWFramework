package android.content.pm;

import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.IntentSender;
import android.content.pm.IOnAppsChangedListener;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInstaller;
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
    void addOnAppsChangedListener(String str, IOnAppsChangedListener iOnAppsChangedListener) throws RemoteException;

    ParceledListSlice getAllSessions(String str) throws RemoteException;

    LauncherApps.AppUsageLimit getAppUsageLimit(String str, String str2, UserHandle userHandle) throws RemoteException;

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

    void registerPackageInstallerCallback(String str, IPackageInstallerCallback iPackageInstallerCallback) throws RemoteException;

    void removeOnAppsChangedListener(IOnAppsChangedListener iOnAppsChangedListener) throws RemoteException;

    ActivityInfo resolveActivity(String str, ComponentName componentName, UserHandle userHandle) throws RemoteException;

    boolean shouldHideFromSuggestions(String str, UserHandle userHandle) throws RemoteException;

    void showAppDetailsAsUser(IApplicationThread iApplicationThread, String str, ComponentName componentName, Rect rect, Bundle bundle, UserHandle userHandle) throws RemoteException;

    void startActivityAsUser(IApplicationThread iApplicationThread, String str, ComponentName componentName, Rect rect, Bundle bundle, UserHandle userHandle) throws RemoteException;

    void startSessionDetailsActivityAsUser(IApplicationThread iApplicationThread, String str, PackageInstaller.SessionInfo sessionInfo, Rect rect, Bundle bundle, UserHandle userHandle) throws RemoteException;

    boolean startShortcut(String str, String str2, String str3, Rect rect, Bundle bundle, int i) throws RemoteException;

    public static class Default implements ILauncherApps {
        @Override // android.content.pm.ILauncherApps
        public void addOnAppsChangedListener(String callingPackage, IOnAppsChangedListener listener) throws RemoteException {
        }

        @Override // android.content.pm.ILauncherApps
        public void removeOnAppsChangedListener(IOnAppsChangedListener listener) throws RemoteException {
        }

        @Override // android.content.pm.ILauncherApps
        public ParceledListSlice getLauncherActivities(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.ILauncherApps
        public ActivityInfo resolveActivity(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.ILauncherApps
        public void startSessionDetailsActivityAsUser(IApplicationThread caller, String callingPackage, PackageInstaller.SessionInfo sessionInfo, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
        }

        @Override // android.content.pm.ILauncherApps
        public void startActivityAsUser(IApplicationThread caller, String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
        }

        @Override // android.content.pm.ILauncherApps
        public void showAppDetailsAsUser(IApplicationThread caller, String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
        }

        @Override // android.content.pm.ILauncherApps
        public boolean isPackageEnabled(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.ILauncherApps
        public Bundle getSuspendedPackageLauncherExtras(String packageName, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.ILauncherApps
        public boolean isActivityEnabled(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.ILauncherApps
        public ApplicationInfo getApplicationInfo(String callingPackage, String packageName, int flags, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.ILauncherApps
        public LauncherApps.AppUsageLimit getAppUsageLimit(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.ILauncherApps
        public ParceledListSlice getShortcuts(String callingPackage, long changedSince, String packageName, List shortcutIds, ComponentName componentName, int flags, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.ILauncherApps
        public void pinShortcuts(String callingPackage, String packageName, List<String> list, UserHandle user) throws RemoteException {
        }

        @Override // android.content.pm.ILauncherApps
        public boolean startShortcut(String callingPackage, String packageName, String id, Rect sourceBounds, Bundle startActivityOptions, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.ILauncherApps
        public int getShortcutIconResId(String callingPackage, String packageName, String id, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.ILauncherApps
        public ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.ILauncherApps
        public boolean hasShortcutHostPermission(String callingPackage) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.ILauncherApps
        public boolean shouldHideFromSuggestions(String packageName, UserHandle user) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.ILauncherApps
        public ParceledListSlice getShortcutConfigActivities(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.ILauncherApps
        public IntentSender getShortcutConfigActivityIntent(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.ILauncherApps
        public void registerPackageInstallerCallback(String callingPackage, IPackageInstallerCallback callback) throws RemoteException {
        }

        @Override // android.content.pm.ILauncherApps
        public ParceledListSlice getAllSessions(String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILauncherApps {
        private static final String DESCRIPTOR = "android.content.pm.ILauncherApps";
        static final int TRANSACTION_addOnAppsChangedListener = 1;
        static final int TRANSACTION_getAllSessions = 23;
        static final int TRANSACTION_getAppUsageLimit = 12;
        static final int TRANSACTION_getApplicationInfo = 11;
        static final int TRANSACTION_getLauncherActivities = 3;
        static final int TRANSACTION_getShortcutConfigActivities = 20;
        static final int TRANSACTION_getShortcutConfigActivityIntent = 21;
        static final int TRANSACTION_getShortcutIconFd = 17;
        static final int TRANSACTION_getShortcutIconResId = 16;
        static final int TRANSACTION_getShortcuts = 13;
        static final int TRANSACTION_getSuspendedPackageLauncherExtras = 9;
        static final int TRANSACTION_hasShortcutHostPermission = 18;
        static final int TRANSACTION_isActivityEnabled = 10;
        static final int TRANSACTION_isPackageEnabled = 8;
        static final int TRANSACTION_pinShortcuts = 14;
        static final int TRANSACTION_registerPackageInstallerCallback = 22;
        static final int TRANSACTION_removeOnAppsChangedListener = 2;
        static final int TRANSACTION_resolveActivity = 4;
        static final int TRANSACTION_shouldHideFromSuggestions = 19;
        static final int TRANSACTION_showAppDetailsAsUser = 7;
        static final int TRANSACTION_startActivityAsUser = 6;
        static final int TRANSACTION_startSessionDetailsActivityAsUser = 5;
        static final int TRANSACTION_startShortcut = 15;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "addOnAppsChangedListener";
                case 2:
                    return "removeOnAppsChangedListener";
                case 3:
                    return "getLauncherActivities";
                case 4:
                    return "resolveActivity";
                case 5:
                    return "startSessionDetailsActivityAsUser";
                case 6:
                    return "startActivityAsUser";
                case 7:
                    return "showAppDetailsAsUser";
                case 8:
                    return "isPackageEnabled";
                case 9:
                    return "getSuspendedPackageLauncherExtras";
                case 10:
                    return "isActivityEnabled";
                case 11:
                    return "getApplicationInfo";
                case 12:
                    return "getAppUsageLimit";
                case 13:
                    return "getShortcuts";
                case 14:
                    return "pinShortcuts";
                case 15:
                    return "startShortcut";
                case 16:
                    return "getShortcutIconResId";
                case 17:
                    return "getShortcutIconFd";
                case 18:
                    return "hasShortcutHostPermission";
                case 19:
                    return "shouldHideFromSuggestions";
                case 20:
                    return "getShortcutConfigActivities";
                case 21:
                    return "getShortcutConfigActivityIntent";
                case 22:
                    return "registerPackageInstallerCallback";
                case 23:
                    return "getAllSessions";
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
            UserHandle _arg2;
            ComponentName _arg1;
            UserHandle _arg22;
            PackageInstaller.SessionInfo _arg23;
            Rect _arg3;
            Bundle _arg4;
            UserHandle _arg5;
            ComponentName _arg24;
            Rect _arg32;
            Bundle _arg42;
            UserHandle _arg52;
            ComponentName _arg25;
            Rect _arg33;
            Bundle _arg43;
            UserHandle _arg53;
            UserHandle _arg26;
            UserHandle _arg12;
            ComponentName _arg13;
            UserHandle _arg27;
            UserHandle _arg34;
            UserHandle _arg28;
            ComponentName _arg44;
            UserHandle _arg6;
            UserHandle _arg35;
            Rect _arg36;
            Bundle _arg45;
            UserHandle _arg14;
            UserHandle _arg29;
            ComponentName _arg15;
            UserHandle _arg210;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        addOnAppsChangedListener(data.readString(), IOnAppsChangedListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        removeOnAppsChangedListener(IOnAppsChangedListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        ParceledListSlice _result = getLauncherActivities(_arg0, _arg16, _arg2);
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        ActivityInfo _result2 = resolveActivity(_arg02, _arg1, _arg22);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg03 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg17 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = PackageInstaller.SessionInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg3 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg4 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg5 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        startSessionDetailsActivityAsUser(_arg03, _arg17, _arg23, _arg3, _arg4, _arg5);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg04 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg18 = data.readString();
                        if (data.readInt() != 0) {
                            _arg24 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg42 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg52 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg52 = null;
                        }
                        startActivityAsUser(_arg04, _arg18, _arg24, _arg32, _arg42, _arg52);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IApplicationThread _arg05 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg25 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg25 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg33 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg33 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg43 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg43 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg53 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg53 = null;
                        }
                        showAppDetailsAsUser(_arg05, _arg19, _arg25, _arg33, _arg43, _arg53);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        String _arg110 = data.readString();
                        if (data.readInt() != 0) {
                            _arg26 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg26 = null;
                        }
                        boolean isPackageEnabled = isPackageEnabled(_arg06, _arg110, _arg26);
                        reply.writeNoException();
                        reply.writeInt(isPackageEnabled ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        Bundle _result3 = getSuspendedPackageLauncherExtras(_arg07, _arg12);
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg27 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg27 = null;
                        }
                        boolean isActivityEnabled = isActivityEnabled(_arg08, _arg13, _arg27);
                        reply.writeNoException();
                        reply.writeInt(isActivityEnabled ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        String _arg111 = data.readString();
                        int _arg211 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg34 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg34 = null;
                        }
                        ApplicationInfo _result4 = getApplicationInfo(_arg09, _arg111, _arg211, _arg34);
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        String _arg112 = data.readString();
                        if (data.readInt() != 0) {
                            _arg28 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg28 = null;
                        }
                        LauncherApps.AppUsageLimit _result5 = getAppUsageLimit(_arg010, _arg112, _arg28);
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        long _arg113 = data.readLong();
                        String _arg212 = data.readString();
                        List _arg37 = data.readArrayList(getClass().getClassLoader());
                        if (data.readInt() != 0) {
                            _arg44 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg44 = null;
                        }
                        int _arg54 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg6 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        ParceledListSlice _result6 = getShortcuts(_arg011, _arg113, _arg212, _arg37, _arg44, _arg54, _arg6);
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg012 = data.readString();
                        String _arg114 = data.readString();
                        List<String> _arg213 = data.createStringArrayList();
                        if (data.readInt() != 0) {
                            _arg35 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg35 = null;
                        }
                        pinShortcuts(_arg012, _arg114, _arg213, _arg35);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg013 = data.readString();
                        String _arg115 = data.readString();
                        String _arg214 = data.readString();
                        if (data.readInt() != 0) {
                            _arg36 = Rect.CREATOR.createFromParcel(data);
                        } else {
                            _arg36 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg45 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg45 = null;
                        }
                        boolean startShortcut = startShortcut(_arg013, _arg115, _arg214, _arg36, _arg45, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(startShortcut ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getShortcutIconResId(data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        ParcelFileDescriptor _result8 = getShortcutIconFd(data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasShortcutHostPermission = hasShortcutHostPermission(data.readString());
                        reply.writeNoException();
                        reply.writeInt(hasShortcutHostPermission ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg014 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        boolean shouldHideFromSuggestions = shouldHideFromSuggestions(_arg014, _arg14);
                        reply.writeNoException();
                        reply.writeInt(shouldHideFromSuggestions ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg015 = data.readString();
                        String _arg116 = data.readString();
                        if (data.readInt() != 0) {
                            _arg29 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg29 = null;
                        }
                        ParceledListSlice _result9 = getShortcutConfigActivities(_arg015, _arg116, _arg29);
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg016 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg210 = UserHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg210 = null;
                        }
                        IntentSender _result10 = getShortcutConfigActivityIntent(_arg016, _arg15, _arg210);
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        registerPackageInstallerCallback(data.readString(), IPackageInstallerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result11 = getAllSessions(data.readString());
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
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
        public static class Proxy implements ILauncherApps {
            public static ILauncherApps sDefaultImpl;
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

            @Override // android.content.pm.ILauncherApps
            public void addOnAppsChangedListener(String callingPackage, IOnAppsChangedListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addOnAppsChangedListener(callingPackage, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.ILauncherApps
            public void removeOnAppsChangedListener(IOnAppsChangedListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeOnAppsChangedListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.ILauncherApps
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
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLauncherActivities(callingPackage, packageName, user);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.ILauncherApps
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
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resolveActivity(callingPackage, component, user);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.ILauncherApps
            public void startSessionDetailsActivityAsUser(IApplicationThread caller, String callingPackage, PackageInstaller.SessionInfo sessionInfo, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
                        _data.writeString(callingPackage);
                        if (sessionInfo != null) {
                            _data.writeInt(1);
                            sessionInfo.writeToParcel(_data, 0);
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
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().startSessionDetailsActivityAsUser(caller, callingPackage, sessionInfo, sourceBounds, opts, user);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.pm.ILauncherApps
            public void startActivityAsUser(IApplicationThread caller, String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
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
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().startActivityAsUser(caller, callingPackage, component, sourceBounds, opts, user);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.pm.ILauncherApps
            public void showAppDetailsAsUser(IApplicationThread caller, String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
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
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().showAppDetailsAsUser(caller, callingPackage, component, sourceBounds, opts, user);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.pm.ILauncherApps
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
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPackageEnabled(callingPackage, packageName, user);
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

            @Override // android.content.pm.ILauncherApps
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
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSuspendedPackageLauncherExtras(packageName, user);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.ILauncherApps
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
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isActivityEnabled(callingPackage, component, user);
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

            @Override // android.content.pm.ILauncherApps
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
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApplicationInfo(callingPackage, packageName, flags, user);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ApplicationInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.ILauncherApps
            public LauncherApps.AppUsageLimit getAppUsageLimit(String callingPackage, String packageName, UserHandle user) throws RemoteException {
                LauncherApps.AppUsageLimit _result;
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
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUsageLimit(callingPackage, packageName, user);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = LauncherApps.AppUsageLimit.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.ILauncherApps
            public ParceledListSlice getShortcuts(String callingPackage, long changedSince, String packageName, List shortcutIds, ComponentName componentName, int flags, UserHandle user) throws RemoteException {
                Throwable th;
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callingPackage);
                        _data.writeLong(changedSince);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
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
                        if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                            } else {
                                _result = null;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        ParceledListSlice shortcuts = Stub.getDefaultImpl().getShortcuts(callingPackage, changedSince, packageName, shortcutIds, componentName, flags, user);
                        _reply.recycle();
                        _data.recycle();
                        return shortcuts;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.content.pm.ILauncherApps
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
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pinShortcuts(callingPackage, packageName, shortcutIds, user);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.ILauncherApps
            public boolean startShortcut(String callingPackage, String packageName, String id, Rect sourceBounds, Bundle startActivityOptions, int userId) throws RemoteException {
                Throwable th;
                boolean _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callingPackage);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                        try {
                            _data.writeString(id);
                            _result = true;
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
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(userId);
                            if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean startShortcut = Stub.getDefaultImpl().startShortcut(callingPackage, packageName, id, sourceBounds, startActivityOptions, userId);
                            _reply.recycle();
                            _data.recycle();
                            return startShortcut;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
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
            }

            @Override // android.content.pm.ILauncherApps
            public int getShortcutIconResId(String callingPackage, String packageName, String id, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(packageName);
                    _data.writeString(id);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getShortcutIconResId(callingPackage, packageName, id, userId);
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

            @Override // android.content.pm.ILauncherApps
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
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getShortcutIconFd(callingPackage, packageName, id, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.ILauncherApps
            public boolean hasShortcutHostPermission(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasShortcutHostPermission(callingPackage);
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

            @Override // android.content.pm.ILauncherApps
            public boolean shouldHideFromSuggestions(String packageName, UserHandle user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (user != null) {
                        _data.writeInt(1);
                        user.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldHideFromSuggestions(packageName, user);
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

            @Override // android.content.pm.ILauncherApps
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
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getShortcutConfigActivities(callingPackage, packageName, user);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.ILauncherApps
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
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getShortcutConfigActivityIntent(callingPackage, component, user);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = IntentSender.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.ILauncherApps
            public void registerPackageInstallerCallback(String callingPackage, IPackageInstallerCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerPackageInstallerCallback(callingPackage, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.ILauncherApps
            public ParceledListSlice getAllSessions(String callingPackage) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllSessions(callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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
        }

        public static boolean setDefaultImpl(ILauncherApps impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILauncherApps getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
