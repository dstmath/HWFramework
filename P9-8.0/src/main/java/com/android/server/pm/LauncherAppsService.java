package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ILauncherApps.Stub;
import android.content.pm.IOnAppsChangedListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutServiceInternal;
import android.content.pm.ShortcutServiceInternal.ShortcutChangeListener;
import android.content.pm.UserInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.Slog;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import java.util.Collections;
import java.util.List;

public class LauncherAppsService extends SystemService {
    private final LauncherAppsImpl mLauncherAppsImpl;

    static class BroadcastCookie {
        public final String packageName;
        public final UserHandle user;

        BroadcastCookie(UserHandle userHandle, String packageName) {
            this.user = userHandle;
            this.packageName = packageName;
        }
    }

    static class LauncherAppsImpl extends Stub {
        private static final boolean DEBUG = false;
        private static final String TAG = "LauncherAppsService";
        private final ActivityManagerInternal mActivityManagerInternal;
        private final Handler mCallbackHandler;
        private final Context mContext;
        private final PackageCallbackList<IOnAppsChangedListener> mListeners = new PackageCallbackList();
        private final MyPackageMonitor mPackageMonitor = new MyPackageMonitor(this, null);
        private final ShortcutServiceInternal mShortcutServiceInternal;
        private final UserManager mUm;

        private class MyPackageMonitor extends PackageMonitor implements ShortcutChangeListener {
            /* synthetic */ MyPackageMonitor(LauncherAppsImpl this$1, MyPackageMonitor -this1) {
                this();
            }

            private MyPackageMonitor() {
            }

            public void onPackageAdded(String packageName, int uid) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackageAdded")) {
                            listener.onPackageAdded(user, packageName);
                        }
                    } catch (RemoteException re) {
                        Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackageAdded(packageName, uid);
            }

            public void onPackageRemoved(String packageName, int uid) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackageRemoved")) {
                            listener.onPackageRemoved(user, packageName);
                        }
                    } catch (RemoteException re) {
                        Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackageRemoved(packageName, uid);
            }

            public void onPackageModified(String packageName) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackageModified")) {
                            listener.onPackageChanged(user, packageName);
                        }
                    } catch (RemoteException re) {
                        Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackageModified(packageName);
            }

            public void onPackagesAvailable(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackagesAvailable")) {
                            listener.onPackagesAvailable(user, packages, isReplacing());
                        }
                    } catch (RemoteException re) {
                        Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackagesAvailable(packages);
            }

            public void onPackagesUnavailable(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackagesUnavailable")) {
                            listener.onPackagesUnavailable(user, packages, isReplacing());
                        }
                    } catch (RemoteException re) {
                        Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackagesUnavailable(packages);
            }

            public void onPackagesSuspended(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackagesSuspended")) {
                            listener.onPackagesSuspended(user, packages);
                        }
                    } catch (RemoteException re) {
                        Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackagesSuspended(packages);
            }

            public void onPackagesUnsuspended(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackagesUnsuspended")) {
                            listener.onPackagesUnsuspended(user, packages);
                        }
                    } catch (RemoteException re) {
                        Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackagesUnsuspended(packages);
            }

            public void onShortcutChanged(String packageName, int userId) {
                LauncherAppsImpl.this.postToPackageMonitorHandler(new -$Lambda$Ppv_Klr53hhBrL3tX83sMKRnyiw(userId, this, packageName));
            }

            private void onShortcutChangedInner(String packageName, int userId) {
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                try {
                    UserHandle user = UserHandle.of(userId);
                    for (int i = 0; i < n; i++) {
                        IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        BroadcastCookie cookie = (BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(user, cookie.user, "onShortcutChanged")) {
                            int launcherUserId = cookie.user.getIdentifier();
                            if (LauncherAppsImpl.this.mShortcutServiceInternal.hasShortcutHostPermission(launcherUserId, cookie.packageName)) {
                                try {
                                    listener.onShortcutChanged(user, packageName, new ParceledListSlice(LauncherAppsImpl.this.mShortcutServiceInternal.getShortcuts(launcherUserId, cookie.packageName, 0, packageName, null, null, 15, userId)));
                                } catch (Throwable re) {
                                    Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    Log.w(LauncherAppsImpl.TAG, e.getMessage(), e);
                } finally {
                    LauncherAppsImpl.this.mListeners.finishBroadcast();
                }
            }
        }

        class PackageCallbackList<T extends IInterface> extends RemoteCallbackList<T> {
            PackageCallbackList() {
            }

            public void onCallbackDied(T t, Object cookie) {
                LauncherAppsImpl.this.checkCallbackCount();
            }
        }

        public LauncherAppsImpl(Context context) {
            this.mContext = context;
            this.mUm = (UserManager) this.mContext.getSystemService("user");
            this.mActivityManagerInternal = (ActivityManagerInternal) Preconditions.checkNotNull((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class));
            this.mShortcutServiceInternal = (ShortcutServiceInternal) Preconditions.checkNotNull((ShortcutServiceInternal) LocalServices.getService(ShortcutServiceInternal.class));
            this.mShortcutServiceInternal.addListener(this.mPackageMonitor);
            this.mCallbackHandler = BackgroundThread.getHandler();
        }

        int injectBinderCallingUid() {
            return getCallingUid();
        }

        final int injectCallingUserId() {
            return UserHandle.getUserId(injectBinderCallingUid());
        }

        long injectClearCallingIdentity() {
            return Binder.clearCallingIdentity();
        }

        void injectRestoreCallingIdentity(long token) {
            Binder.restoreCallingIdentity(token);
        }

        private int getCallingUserId() {
            return UserHandle.getUserId(injectBinderCallingUid());
        }

        public void addOnAppsChangedListener(String callingPackage, IOnAppsChangedListener listener) throws RemoteException {
            verifyCallingPackage(callingPackage);
            synchronized (this.mListeners) {
                if (this.mListeners.getRegisteredCallbackCount() == 0) {
                    startWatchingPackageBroadcasts();
                }
                this.mListeners.unregister(listener);
                this.mListeners.register(listener, new BroadcastCookie(UserHandle.of(getCallingUserId()), callingPackage));
            }
        }

        public void removeOnAppsChangedListener(IOnAppsChangedListener listener) throws RemoteException {
            synchronized (this.mListeners) {
                this.mListeners.unregister(listener);
                if (this.mListeners.getRegisteredCallbackCount() == 0) {
                    stopWatchingPackageBroadcasts();
                }
            }
        }

        private void startWatchingPackageBroadcasts() {
            this.mPackageMonitor.register(this.mContext, UserHandle.ALL, true, this.mCallbackHandler);
        }

        private void stopWatchingPackageBroadcasts() {
            this.mPackageMonitor.unregister();
        }

        void checkCallbackCount() {
            synchronized (this.mListeners) {
                if (this.mListeners.getRegisteredCallbackCount() == 0) {
                    stopWatchingPackageBroadcasts();
                }
            }
        }

        private boolean canAccessProfile(String callingPackage, UserHandle targetUser, String message) {
            return canAccessProfile(callingPackage, targetUser.getIdentifier(), message);
        }

        private boolean canAccessProfile(String callingPackage, int targetUserId, String message) {
            int callingUserId = injectCallingUserId();
            if (targetUserId == callingUserId) {
                return true;
            }
            long ident = injectClearCallingIdentity();
            try {
                UserInfo callingUserInfo = this.mUm.getUserInfo(callingUserId);
                if (callingUserInfo.isManagedProfile()) {
                    Slog.w(TAG, message + " by " + callingPackage + " for another profile " + targetUserId + " from " + callingUserId);
                    return false;
                }
                UserInfo targetUserInfo = this.mUm.getUserInfo(targetUserId);
                if (!(targetUserInfo == null || targetUserInfo.profileGroupId == -10000)) {
                    if (targetUserInfo.profileGroupId == callingUserInfo.profileGroupId) {
                        injectRestoreCallingIdentity(ident);
                        return true;
                    }
                }
                throw new SecurityException(message + " for unrelated profile " + targetUserId);
            } finally {
                injectRestoreCallingIdentity(ident);
            }
        }

        void verifyCallingPackage(String callingPackage) {
            int packageUid = -1;
            try {
                packageUid = AppGlobals.getPackageManager().getPackageUid(callingPackage, 794624, UserHandle.getUserId(getCallingUid()));
            } catch (RemoteException e) {
            }
            if (packageUid < 0) {
                Log.e(TAG, "Package not found: " + callingPackage);
            }
            if (packageUid != injectBinderCallingUid()) {
                throw new SecurityException("Calling package name mismatch");
            }
        }

        private boolean isUserEnabled(UserHandle user) {
            return isUserEnabled(user.getIdentifier());
        }

        private boolean isUserEnabled(int userId) {
            long ident = injectClearCallingIdentity();
            try {
                UserInfo targetUserInfo = this.mUm.getUserInfo(userId);
                boolean isEnabled = targetUserInfo != null ? targetUserInfo.isEnabled() : false;
                injectRestoreCallingIdentity(ident);
                return isEnabled;
            } catch (Throwable th) {
                injectRestoreCallingIdentity(ident);
            }
        }

        public ParceledListSlice<ResolveInfo> getLauncherActivities(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            return queryActivitiesForUser(callingPackage, new Intent("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER").setPackage(packageName), user);
        }

        public ActivityInfo resolveActivity(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
            if (!canAccessProfile(callingPackage, user, "Cannot resolve activity") || !isUserEnabled(user)) {
                return null;
            }
            int callingUid = injectBinderCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityInfo activityInfo = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getActivityInfo(component, 786432, callingUid, user.getIdentifier());
                return activityInfo;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public ParceledListSlice getShortcutConfigActivities(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            return queryActivitiesForUser(callingPackage, new Intent("android.intent.action.CREATE_SHORTCUT").setPackage(packageName), user);
        }

        private ParceledListSlice<ResolveInfo> queryActivitiesForUser(String callingPackage, Intent intent, UserHandle user) {
            if (!canAccessProfile(callingPackage, user, "Cannot retrieve activities") || !isUserEnabled(user)) {
                return null;
            }
            int callingUid = injectBinderCallingUid();
            long ident = injectClearCallingIdentity();
            try {
                ParceledListSlice<ResolveInfo> parceledListSlice = new ParceledListSlice(((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).queryIntentActivities(intent, 786432, callingUid, user.getIdentifier()));
                return parceledListSlice;
            } finally {
                injectRestoreCallingIdentity(ident);
            }
        }

        public IntentSender getShortcutConfigActivityIntent(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
            ensureShortcutPermission(callingPackage);
            if (!canAccessProfile(callingPackage, user, "Cannot check package")) {
                return null;
            }
            Preconditions.checkNotNull(component);
            Preconditions.checkArgument(isUserEnabled(user), "User not enabled");
            Intent intent = new Intent("android.intent.action.CREATE_SHORTCUT").setComponent(component);
            long identity = Binder.clearCallingIdentity();
            try {
                PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 1409286144, null, user);
                IntentSender intentSender = pi == null ? null : pi.getIntentSender();
                Binder.restoreCallingIdentity(identity);
                return intentSender;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public ResolveInfo resolveActivityByIntent(Intent intent, UserHandle user) throws RemoteException {
            if (!canAccessProfile(null, user, "Cannot resolve activity") || !isUserEnabled(user)) {
                return null;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                ResolveInfo app = this.mContext.getPackageManager().resolveActivityAsUser(intent, 786432, user.getIdentifier());
                return app;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isPackageEnabled(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            boolean z = false;
            if (!canAccessProfile(callingPackage, user, "Cannot check package") || !isUserEnabled(user)) {
                return false;
            }
            int callingUid = injectBinderCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PackageInfo info = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackageInfo(packageName, 786432, callingUid, user.getIdentifier());
                if (info != null) {
                    z = info.applicationInfo.enabled;
                }
                Binder.restoreCallingIdentity(ident);
                return z;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public ApplicationInfo getApplicationInfo(String callingPackage, String packageName, int flags, UserHandle user) throws RemoteException {
            if (!canAccessProfile(callingPackage, user, "Cannot check package") || !isUserEnabled(user)) {
                return null;
            }
            int callingUid = injectBinderCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                ApplicationInfo info = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getApplicationInfo(packageName, flags, callingUid, user.getIdentifier());
                return info;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        private void ensureShortcutPermission(String callingPackage) {
            verifyCallingPackage(callingPackage);
            if (!this.mShortcutServiceInternal.hasShortcutHostPermission(getCallingUserId(), callingPackage)) {
                throw new SecurityException("Caller can't access shortcut information");
            }
        }

        public ParceledListSlice getShortcuts(String callingPackage, long changedSince, String packageName, List shortcutIds, ComponentName componentName, int flags, UserHandle targetUser) {
            ensureShortcutPermission(callingPackage);
            if (!canAccessProfile(callingPackage, targetUser, "Cannot get shortcuts") || (isUserEnabled(targetUser) ^ 1) != 0) {
                return new ParceledListSlice(Collections.EMPTY_LIST);
            }
            if (shortcutIds == null || packageName != null) {
                return new ParceledListSlice(this.mShortcutServiceInternal.getShortcuts(getCallingUserId(), callingPackage, changedSince, packageName, shortcutIds, componentName, flags, targetUser.getIdentifier()));
            }
            throw new IllegalArgumentException("To query by shortcut ID, package name must also be set");
        }

        public void pinShortcuts(String callingPackage, String packageName, List<String> ids, UserHandle targetUser) {
            ensureShortcutPermission(callingPackage);
            if (!canAccessProfile(callingPackage, targetUser, "Cannot pin shortcuts")) {
                return;
            }
            if (isUserEnabled(targetUser)) {
                this.mShortcutServiceInternal.pinShortcuts(getCallingUserId(), callingPackage, packageName, ids, targetUser.getIdentifier());
                return;
            }
            throw new IllegalStateException("Cannot pin shortcuts for disabled profile " + targetUser);
        }

        public int getShortcutIconResId(String callingPackage, String packageName, String id, int targetUserId) {
            ensureShortcutPermission(callingPackage);
            if (canAccessProfile(callingPackage, targetUserId, "Cannot access shortcuts") && isUserEnabled(targetUserId)) {
                return this.mShortcutServiceInternal.getShortcutIconResId(getCallingUserId(), callingPackage, packageName, id, targetUserId);
            }
            return 0;
        }

        public ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id, int targetUserId) {
            ensureShortcutPermission(callingPackage);
            if (canAccessProfile(callingPackage, targetUserId, "Cannot access shortcuts") && isUserEnabled(targetUserId)) {
                return this.mShortcutServiceInternal.getShortcutIconFd(getCallingUserId(), callingPackage, packageName, id, targetUserId);
            }
            return null;
        }

        public boolean hasShortcutHostPermission(String callingPackage) {
            verifyCallingPackage(callingPackage);
            return this.mShortcutServiceInternal.hasShortcutHostPermission(getCallingUserId(), callingPackage);
        }

        public boolean startShortcut(String callingPackage, String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, int targetUserId) {
            verifyCallingPackage(callingPackage);
            if (!canAccessProfile(callingPackage, targetUserId, "Cannot start activity")) {
                return false;
            }
            if (isUserEnabled(targetUserId)) {
                if (!this.mShortcutServiceInternal.isPinnedByCaller(getCallingUserId(), callingPackage, packageName, shortcutId, targetUserId)) {
                    ensureShortcutPermission(callingPackage);
                }
                Intent[] intents = this.mShortcutServiceInternal.createShortcutIntents(getCallingUserId(), callingPackage, packageName, shortcutId, targetUserId);
                if (intents == null || intents.length == 0) {
                    return false;
                }
                intents[0].addFlags(268435456);
                intents[0].setSourceBounds(sourceBounds);
                return startShortcutIntentsAsPublisher(intents, packageName, startActivityOptions, targetUserId);
            }
            throw new IllegalStateException("Cannot start a shortcut for disabled profile " + targetUserId);
        }

        private boolean startShortcutIntentsAsPublisher(Intent[] intents, String publisherPackage, Bundle startActivityOptions, int userId) {
            long ident = injectClearCallingIdentity();
            try {
                int code = this.mActivityManagerInternal.startActivitiesAsPackage(publisherPackage, userId, intents, startActivityOptions);
                boolean z;
                if (ActivityManager.isStartResultSuccessful(code)) {
                    z = true;
                    return z;
                }
                z = TAG;
                Log.e(z, "Couldn't start activity, code=" + code);
                injectRestoreCallingIdentity(ident);
                return false;
            } catch (SecurityException e) {
                return false;
            } finally {
                injectRestoreCallingIdentity(ident);
            }
        }

        public boolean isActivityEnabled(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
            boolean z = false;
            if (!canAccessProfile(callingPackage, user, "Cannot check component") || !isUserEnabled(user)) {
                return false;
            }
            int callingUid = injectBinderCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                if (((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getActivityInfo(component, 786432, callingUid, user.getIdentifier()) != null) {
                    z = true;
                }
                Binder.restoreCallingIdentity(ident);
                return z;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void startActivityAsUser(String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
            if (!canAccessProfile(callingPackage, user, "Cannot start activity")) {
                return;
            }
            if (isUserEnabled(user)) {
                Intent launchIntent = new Intent("android.intent.action.MAIN");
                launchIntent.addCategory("android.intent.category.LAUNCHER");
                launchIntent.setSourceBounds(sourceBounds);
                launchIntent.addFlags(270532608);
                launchIntent.setPackage(component.getPackageName());
                int callingUid = injectBinderCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PackageManagerInternal pmInt = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                    if (pmInt.getActivityInfo(component, 786432, callingUid, user.getIdentifier()).exported) {
                        List<ResolveInfo> apps = pmInt.queryIntentActivities(launchIntent, 786432, callingUid, user.getIdentifier());
                        int size = apps.size();
                        for (int i = 0; i < size; i++) {
                            ActivityInfo activityInfo = ((ResolveInfo) apps.get(i)).activityInfo;
                            if (activityInfo.packageName.equals(component.getPackageName()) && activityInfo.name.equals(component.getClassName())) {
                                launchIntent.setComponent(component);
                                this.mContext.startActivityAsUser(launchIntent, opts, user);
                                return;
                            }
                        }
                        throw new SecurityException("Attempt to launch activity without  category Intent.CATEGORY_LAUNCHER " + component);
                    }
                    throw new SecurityException("Cannot launch non-exported components " + component);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalStateException("Cannot start activity for disabled profile " + user);
            }
        }

        public void showAppDetailsAsUser(String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
            if (!canAccessProfile(callingPackage, user, "Cannot show app details")) {
                return;
            }
            if (isUserEnabled(user)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts(HwBroadcastRadarUtil.KEY_PACKAGE, component.getPackageName(), null));
                    intent.setFlags(268468224);
                    intent.setSourceBounds(sourceBounds);
                    this.mContext.startActivityAsUser(intent, opts, user);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalStateException("Cannot show app details for disabled profile " + user);
            }
        }

        private boolean isEnabledProfileOf(UserHandle user, UserHandle listeningUser, String debugMsg) {
            if (user.getIdentifier() == listeningUser.getIdentifier()) {
                return true;
            }
            if (this.mUm.isManagedProfile(listeningUser.getIdentifier())) {
                return false;
            }
            long ident = injectClearCallingIdentity();
            try {
                UserInfo userInfo = this.mUm.getUserInfo(user.getIdentifier());
                UserInfo listeningUserInfo = this.mUm.getUserInfo(listeningUser.getIdentifier());
                if (!(userInfo == null || listeningUserInfo == null)) {
                    if (userInfo.profileGroupId != -10000 && userInfo.profileGroupId == listeningUserInfo.profileGroupId && (userInfo.isEnabled() ^ 1) == 0) {
                        injectRestoreCallingIdentity(ident);
                        return true;
                    }
                }
                injectRestoreCallingIdentity(ident);
                return false;
            } catch (Throwable th) {
                injectRestoreCallingIdentity(ident);
            }
        }

        void postToPackageMonitorHandler(Runnable r) {
            this.mCallbackHandler.post(r);
        }
    }

    public LauncherAppsService(Context context) {
        super(context);
        this.mLauncherAppsImpl = new LauncherAppsImpl(context);
    }

    public void onStart() {
        publishBinderService("launcherapps", this.mLauncherAppsImpl);
    }
}
