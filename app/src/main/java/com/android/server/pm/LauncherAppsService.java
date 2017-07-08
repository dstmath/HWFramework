package com.android.server.pm;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ILauncherApps.Stub;
import android.content.pm.IOnAppsChangedListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import java.util.ArrayList;
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
        private final Handler mCallbackHandler;
        private final Context mContext;
        private final PackageCallbackList<IOnAppsChangedListener> mListeners;
        private final MyPackageMonitor mPackageMonitor;
        private final PackageManager mPm;
        private final ShortcutServiceInternal mShortcutServiceInternal;
        private final UserManager mUm;

        private class MyPackageMonitor extends PackageMonitor implements ShortcutChangeListener {

            final /* synthetic */ class -void_onShortcutChanged_java_lang_String_packageName_int_userId_LambdaImpl0 implements Runnable {
                private /* synthetic */ String val$packageName;
                private /* synthetic */ MyPackageMonitor val$this;
                private /* synthetic */ int val$userId;

                public /* synthetic */ -void_onShortcutChanged_java_lang_String_packageName_int_userId_LambdaImpl0(MyPackageMonitor myPackageMonitor, String str, int i) {
                    this.val$this = myPackageMonitor;
                    this.val$packageName = str;
                    this.val$userId = i;
                }

                public void run() {
                    this.val$this.-com_android_server_pm_LauncherAppsService$LauncherAppsImpl$MyPackageMonitor_lambda$1(this.val$packageName, this.val$userId);
                }
            }

            private MyPackageMonitor() {
            }

            public void onPackageAdded(String packageName, int uid) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                    if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackageAdded")) {
                        try {
                            listener.onPackageAdded(user, packageName);
                        } catch (RemoteException re) {
                            Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                        }
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackageAdded(packageName, uid);
            }

            public void onPackageRemoved(String packageName, int uid) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                    if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackageRemoved")) {
                        try {
                            listener.onPackageRemoved(user, packageName);
                        } catch (RemoteException re) {
                            Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                        }
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackageRemoved(packageName, uid);
            }

            public void onPackageModified(String packageName) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                    if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackageModified")) {
                        try {
                            listener.onPackageChanged(user, packageName);
                        } catch (RemoteException re) {
                            Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                        }
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackageModified(packageName);
            }

            public void onPackagesAvailable(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                    if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackagesAvailable")) {
                        try {
                            listener.onPackagesAvailable(user, packages, isReplacing());
                        } catch (RemoteException re) {
                            Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                        }
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackagesAvailable(packages);
            }

            public void onPackagesUnavailable(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                    if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackagesUnavailable")) {
                        try {
                            listener.onPackagesUnavailable(user, packages, isReplacing());
                        } catch (RemoteException re) {
                            Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                        }
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackagesUnavailable(packages);
            }

            public void onPackagesSuspended(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                    if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackagesSuspended")) {
                        try {
                            listener.onPackagesSuspended(user, packages);
                        } catch (RemoteException re) {
                            Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                        }
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackagesSuspended(packages);
            }

            public void onPackagesUnsuspended(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                    if (LauncherAppsImpl.this.isEnabledProfileOf(user, ((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, "onPackagesUnsuspended")) {
                        try {
                            listener.onPackagesUnsuspended(user, packages);
                        } catch (RemoteException re) {
                            Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                        }
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                super.onPackagesUnsuspended(packages);
            }

            public void onShortcutChanged(String packageName, int userId) {
            }

            /* synthetic */ void -com_android_server_pm_LauncherAppsService$LauncherAppsImpl$MyPackageMonitor_lambda$1(String packageName, int userId) {
                onShortcutChangedInner(packageName, userId);
            }

            private void onShortcutChangedInner(String packageName, int userId) {
                UserHandle user = UserHandle.of(userId);
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    IOnAppsChangedListener listener = (IOnAppsChangedListener) LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                    BroadcastCookie cookie = (BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i);
                    if (LauncherAppsImpl.this.isEnabledProfileOf(user, cookie.user, "onShortcutChanged")) {
                        int launcherUserId = cookie.user.getIdentifier();
                        if (LauncherAppsImpl.this.mShortcutServiceInternal.hasShortcutHostPermission(launcherUserId, cookie.packageName)) {
                            try {
                                listener.onShortcutChanged(user, packageName, new ParceledListSlice(LauncherAppsImpl.this.mShortcutServiceInternal.getShortcuts(launcherUserId, cookie.packageName, 0, packageName, null, null, 7, userId)));
                            } catch (Throwable re) {
                                Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                            }
                        }
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
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
            this.mListeners = new PackageCallbackList();
            this.mPackageMonitor = new MyPackageMonitor();
            this.mContext = context;
            this.mPm = this.mContext.getPackageManager();
            this.mUm = (UserManager) this.mContext.getSystemService("user");
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

        private void ensureInUserProfiles(UserHandle userToCheck, String message) {
            ensureInUserProfiles(userToCheck.getIdentifier(), message);
        }

        private void ensureInUserProfiles(int targetUserId, String message) {
            int callingUserId = injectCallingUserId();
            if (targetUserId != callingUserId) {
                long ident = injectClearCallingIdentity();
                try {
                    UserInfo callingUserInfo = this.mUm.getUserInfo(callingUserId);
                    UserInfo targetUserInfo = this.mUm.getUserInfo(targetUserId);
                    if (!(targetUserInfo == null || targetUserInfo.profileGroupId == -10000)) {
                        if (targetUserInfo.profileGroupId == callingUserInfo.profileGroupId) {
                            return;
                        }
                    }
                    throw new SecurityException(message);
                } finally {
                    injectRestoreCallingIdentity(ident);
                }
            }
        }

        void verifyCallingPackage(String callingPackage) {
            int packageUid = -1;
            try {
                packageUid = this.mPm.getPackageUidAsUser(callingPackage, 794624, UserHandle.getUserId(getCallingUid()));
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Package not found: " + callingPackage);
            }
            if (packageUid != Binder.getCallingUid()) {
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
                boolean isEnabled = targetUserInfo != null ? targetUserInfo.isEnabled() : DEBUG;
                injectRestoreCallingIdentity(ident);
                return isEnabled;
            } catch (Throwable th) {
                injectRestoreCallingIdentity(ident);
            }
        }

        public ParceledListSlice<ResolveInfo> getLauncherActivities(String packageName, UserHandle user) throws RemoteException {
            ensureInUserProfiles(user, "Cannot retrieve activities for unrelated profile " + user);
            if (!isUserEnabled(user)) {
                return null;
            }
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            mainIntent.setPackage(packageName);
            long ident = Binder.clearCallingIdentity();
            try {
                ParceledListSlice<ResolveInfo> parceledListSlice = new ParceledListSlice(this.mPm.queryIntentActivitiesAsUser(mainIntent, 786432, user.getIdentifier()));
                return parceledListSlice;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public ActivityInfo resolveActivity(ComponentName component, UserHandle user) throws RemoteException {
            ensureInUserProfiles(user, "Cannot resolve activity for unrelated profile " + user);
            if (!isUserEnabled(user)) {
                return null;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityInfo activityInfo = AppGlobals.getPackageManager().getActivityInfo(component, 786432, user.getIdentifier());
                return activityInfo;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public ResolveInfo resolveActivityByIntent(Intent intent, UserHandle user) throws RemoteException {
            ensureInUserProfiles(user, "Cannot resolve activity for unrelated profile " + user);
            if (!isUserEnabled(user)) {
                return null;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                ResolveInfo app = this.mPm.resolveActivityAsUser(intent, 786432, user.getIdentifier());
                return app;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isPackageEnabled(String packageName, UserHandle user) throws RemoteException {
            boolean z = DEBUG;
            ensureInUserProfiles(user, "Cannot check package for unrelated profile " + user);
            if (!isUserEnabled(user)) {
                return DEBUG;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                PackageInfo info = AppGlobals.getPackageManager().getPackageInfo(packageName, 786432, user.getIdentifier());
                if (info != null) {
                    z = info.applicationInfo.enabled;
                }
                Binder.restoreCallingIdentity(ident);
                return z;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public ApplicationInfo getApplicationInfo(String packageName, int flags, UserHandle user) throws RemoteException {
            ensureInUserProfiles(user, "Cannot check package for unrelated profile " + user);
            if (!isUserEnabled(user)) {
                return null;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                ApplicationInfo info = AppGlobals.getPackageManager().getApplicationInfo(packageName, flags, user.getIdentifier());
                return info;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        private void ensureShortcutPermission(String callingPackage, UserHandle user) {
            ensureShortcutPermission(callingPackage, user.getIdentifier());
        }

        private void ensureShortcutPermission(String callingPackage, int userId) {
            verifyCallingPackage(callingPackage);
            ensureInUserProfiles(userId, "Cannot start activity for unrelated profile " + userId);
            if (!this.mShortcutServiceInternal.hasShortcutHostPermission(getCallingUserId(), callingPackage)) {
                throw new SecurityException("Caller can't access shortcut information");
            }
        }

        public ParceledListSlice getShortcuts(String callingPackage, long changedSince, String packageName, List shortcutIds, ComponentName componentName, int flags, UserHandle user) {
            ensureShortcutPermission(callingPackage, user);
            if (!isUserEnabled(user)) {
                return new ParceledListSlice(new ArrayList(0));
            }
            if (shortcutIds == null || packageName != null) {
                return new ParceledListSlice(this.mShortcutServiceInternal.getShortcuts(getCallingUserId(), callingPackage, changedSince, packageName, shortcutIds, componentName, flags, user.getIdentifier()));
            }
            throw new IllegalArgumentException("To query by shortcut ID, package name must also be set");
        }

        public void pinShortcuts(String callingPackage, String packageName, List<String> ids, UserHandle user) {
            ensureShortcutPermission(callingPackage, user);
            if (isUserEnabled(user)) {
                this.mShortcutServiceInternal.pinShortcuts(getCallingUserId(), callingPackage, packageName, ids, user.getIdentifier());
                return;
            }
            throw new IllegalStateException("Cannot pin shortcuts for disabled profile " + user);
        }

        public int getShortcutIconResId(String callingPackage, String packageName, String id, int userId) {
            ensureShortcutPermission(callingPackage, userId);
            if (isUserEnabled(userId)) {
                return this.mShortcutServiceInternal.getShortcutIconResId(getCallingUserId(), callingPackage, packageName, id, userId);
            }
            return 0;
        }

        public ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id, int userId) {
            ensureShortcutPermission(callingPackage, userId);
            if (isUserEnabled(userId)) {
                return this.mShortcutServiceInternal.getShortcutIconFd(getCallingUserId(), callingPackage, packageName, id, userId);
            }
            return null;
        }

        public boolean hasShortcutHostPermission(String callingPackage) {
            verifyCallingPackage(callingPackage);
            return this.mShortcutServiceInternal.hasShortcutHostPermission(getCallingUserId(), callingPackage);
        }

        public boolean startShortcut(String callingPackage, String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, int userId) {
            verifyCallingPackage(callingPackage);
            ensureInUserProfiles(userId, "Cannot start activity for unrelated profile " + userId);
            if (isUserEnabled(userId)) {
                if (!this.mShortcutServiceInternal.isPinnedByCaller(getCallingUserId(), callingPackage, packageName, shortcutId, userId)) {
                    ensureShortcutPermission(callingPackage, userId);
                }
                Intent intent = this.mShortcutServiceInternal.createShortcutIntent(getCallingUserId(), callingPackage, packageName, shortcutId, userId);
                if (intent == null) {
                    return DEBUG;
                }
                intent.setSourceBounds(sourceBounds);
                prepareIntentForLaunch(intent, sourceBounds);
                long ident = Binder.clearCallingIdentity();
                try {
                    this.mContext.startActivityAsUser(intent, startActivityOptions, UserHandle.of(userId));
                    return true;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalStateException("Cannot start a shortcut for disabled profile " + userId);
            }
        }

        public boolean isActivityEnabled(ComponentName component, UserHandle user) throws RemoteException {
            boolean z = DEBUG;
            ensureInUserProfiles(user, "Cannot check component for unrelated profile " + user);
            if (!isUserEnabled(user)) {
                return DEBUG;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                if (AppGlobals.getPackageManager().getActivityInfo(component, 786432, user.getIdentifier()) != null) {
                    z = true;
                }
                Binder.restoreCallingIdentity(ident);
                return z;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void startActivityAsUser(ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
            ensureInUserProfiles(user, "Cannot start activity for unrelated profile " + user);
            if (isUserEnabled(user)) {
                Intent launchIntent = new Intent("android.intent.action.MAIN");
                launchIntent.addCategory("android.intent.category.LAUNCHER");
                prepareIntentForLaunch(launchIntent, sourceBounds);
                launchIntent.setPackage(component.getPackageName());
                long ident = Binder.clearCallingIdentity();
                try {
                    if (AppGlobals.getPackageManager().getActivityInfo(component, 786432, user.getIdentifier()).exported) {
                        List<ResolveInfo> apps = this.mPm.queryIntentActivitiesAsUser(launchIntent, 786432, user.getIdentifier());
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

        private void prepareIntentForLaunch(Intent launchIntent, Rect sourceBounds) {
            launchIntent.setSourceBounds(sourceBounds);
            launchIntent.addFlags(270532608);
        }

        public void showAppDetailsAsUser(ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
            ensureInUserProfiles(user, "Cannot show app details for unrelated profile " + user);
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
            long ident = injectClearCallingIdentity();
            try {
                UserInfo userInfo = this.mUm.getUserInfo(user.getIdentifier());
                UserInfo listeningUserInfo = this.mUm.getUserInfo(listeningUser.getIdentifier());
                if (!(userInfo == null || listeningUserInfo == null)) {
                    if (userInfo.profileGroupId != -10000 && userInfo.profileGroupId == listeningUserInfo.profileGroupId && userInfo.isEnabled()) {
                        injectRestoreCallingIdentity(ident);
                        return true;
                    }
                }
                injectRestoreCallingIdentity(ident);
                return DEBUG;
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
