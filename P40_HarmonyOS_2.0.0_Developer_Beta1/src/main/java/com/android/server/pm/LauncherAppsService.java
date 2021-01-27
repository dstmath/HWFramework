package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IApplicationThread;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.common.IHwHarmonyServiceManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ILauncherApps;
import android.content.pm.IOnAppsChangedListener;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutServiceInternal;
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
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.pm.LauncherAppsService;
import com.android.server.wm.ActivityTaskManagerInternal;
import com.huawei.android.app.HwActivityTaskManager;
import huawei.android.security.IHwBehaviorCollectManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntPredicate;

public class LauncherAppsService extends SystemService {
    private final LauncherAppsImpl mLauncherAppsImpl;

    public LauncherAppsService(Context context) {
        super(context);
        this.mLauncherAppsImpl = new LauncherAppsImpl(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.pm.LauncherAppsService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.pm.LauncherAppsService$LauncherAppsImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("launcherapps", this.mLauncherAppsImpl);
    }

    /* access modifiers changed from: package-private */
    public static class BroadcastCookie {
        public final int callingPid;
        public final int callingUid;
        public final String packageName;
        public final UserHandle user;

        BroadcastCookie(UserHandle userHandle, String packageName2, int callingPid2, int callingUid2) {
            this.user = userHandle;
            this.packageName = packageName2;
            this.callingUid = callingUid2;
            this.callingPid = callingPid2;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class LauncherAppsImpl extends ILauncherApps.Stub {
        private static final boolean DEBUG = false;
        private static final String TAG = "LauncherAppsService";
        private final ActivityManagerInternal mActivityManagerInternal;
        private final ActivityTaskManagerInternal mActivityTaskManagerInternal;
        private final Handler mCallbackHandler;
        private final Context mContext;
        private final DevicePolicyManager mDpm;
        private IHwHarmonyServiceManager mHwHarmonyServiceManager = HwFrameworkFactory.getHwHarmonyServiceManager();
        private final PackageCallbackList<IOnAppsChangedListener> mListeners = new PackageCallbackList<>();
        private PackageInstallerService mPackageInstallerService;
        private final MyPackageMonitor mPackageMonitor = new MyPackageMonitor();
        private final ShortcutServiceInternal mShortcutServiceInternal;
        private final UserManager mUm;
        private final UsageStatsManagerInternal mUsageStatsManagerInternal;
        private final UserManagerInternal mUserManagerInternal;

        public LauncherAppsImpl(Context context) {
            this.mContext = context;
            this.mUm = (UserManager) this.mContext.getSystemService("user");
            this.mUserManagerInternal = (UserManagerInternal) Preconditions.checkNotNull((UserManagerInternal) LocalServices.getService(UserManagerInternal.class));
            this.mUsageStatsManagerInternal = (UsageStatsManagerInternal) Preconditions.checkNotNull((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class));
            this.mActivityManagerInternal = (ActivityManagerInternal) Preconditions.checkNotNull((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class));
            this.mActivityTaskManagerInternal = (ActivityTaskManagerInternal) Preconditions.checkNotNull((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class));
            this.mShortcutServiceInternal = (ShortcutServiceInternal) Preconditions.checkNotNull((ShortcutServiceInternal) LocalServices.getService(ShortcutServiceInternal.class));
            this.mShortcutServiceInternal.addListener(this.mPackageMonitor);
            this.mCallbackHandler = BackgroundThread.getHandler();
            this.mDpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
            if (this.mDpm == null) {
                Log.e(TAG, " LauncherAppsImpl get device policy service is null");
            }
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int injectBinderCallingUid() {
            return getCallingUid();
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public int injectBinderCallingPid() {
            return getCallingPid();
        }

        /* access modifiers changed from: package-private */
        public final int injectCallingUserId() {
            return UserHandle.getUserId(injectBinderCallingUid());
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public long injectClearCallingIdentity() {
            return Binder.clearCallingIdentity();
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void injectRestoreCallingIdentity(long token) {
            Binder.restoreCallingIdentity(token);
        }

        private int getCallingUserId() {
            return UserHandle.getUserId(injectBinderCallingUid());
        }

        public void addOnAppsChangedListener(String callingPackage, IOnAppsChangedListener listener) throws RemoteException {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.LAUNCHERAPPS_ADDONAPPSCHANGEDLISTENER);
            verifyCallingPackage(callingPackage);
            synchronized (this.mListeners) {
                if (this.mListeners.getRegisteredCallbackCount() == 0) {
                    startWatchingPackageBroadcasts();
                }
                this.mListeners.unregister(listener);
                this.mListeners.register(listener, new BroadcastCookie(UserHandle.of(getCallingUserId()), callingPackage, injectBinderCallingPid(), injectBinderCallingUid()));
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

        public void registerPackageInstallerCallback(String callingPackage, IPackageInstallerCallback callback) {
            verifyCallingPackage(callingPackage);
            getPackageInstallerService().registerCallback(callback, new IntPredicate(new UserHandle(getCallingUserId())) {
                /* class com.android.server.pm.$$Lambda$LauncherAppsService$LauncherAppsImpl$PR6SMHDNFTsnoL92MFZskMzN8k */
                private final /* synthetic */ UserHandle f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.IntPredicate
                public final boolean test(int i) {
                    return LauncherAppsService.LauncherAppsImpl.this.lambda$registerPackageInstallerCallback$0$LauncherAppsService$LauncherAppsImpl(this.f$1, i);
                }
            });
        }

        public /* synthetic */ boolean lambda$registerPackageInstallerCallback$0$LauncherAppsService$LauncherAppsImpl(UserHandle callingIdUserHandle, int eventUserId) {
            return isEnabledProfileOf(callingIdUserHandle, new UserHandle(eventUserId), "shouldReceiveEvent");
        }

        /* JADX INFO: finally extract failed */
        public ParceledListSlice<PackageInstaller.SessionInfo> getAllSessions(String callingPackage) {
            verifyCallingPackage(callingPackage);
            List<PackageInstaller.SessionInfo> sessionInfos = new ArrayList<>();
            int[] userIds = this.mUm.getEnabledProfileIds(getCallingUserId());
            long token = Binder.clearCallingIdentity();
            try {
                for (int userId : userIds) {
                    sessionInfos.addAll(getPackageInstallerService().getAllSessions(userId).getList());
                }
                Binder.restoreCallingIdentity(token);
                return new ParceledListSlice<>(sessionInfos);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        private PackageInstallerService getPackageInstallerService() {
            if (this.mPackageInstallerService == null) {
                this.mPackageInstallerService = ((PackageManagerService) ServiceManager.getService("package")).getPackageInstaller();
            }
            return this.mPackageInstallerService;
        }

        private void startWatchingPackageBroadcasts() {
            this.mPackageMonitor.register(this.mContext, UserHandle.ALL, true, this.mCallbackHandler);
        }

        private void stopWatchingPackageBroadcasts() {
            this.mPackageMonitor.unregister();
        }

        /* access modifiers changed from: package-private */
        public void checkCallbackCount() {
            synchronized (this.mListeners) {
                if (this.mListeners.getRegisteredCallbackCount() == 0) {
                    stopWatchingPackageBroadcasts();
                }
            }
        }

        private boolean canAccessProfile(int targetUserId, String message) {
            int callingUserId = injectCallingUserId();
            if (targetUserId == callingUserId) {
                return true;
            }
            long ident = injectClearCallingIdentity();
            try {
                UserInfo callingUserInfo = this.mUm.getUserInfo(callingUserId);
                if (callingUserInfo == null || !callingUserInfo.isManagedProfile()) {
                    injectRestoreCallingIdentity(ident);
                    return this.mUserManagerInternal.isProfileAccessible(injectCallingUserId(), targetUserId, message, true);
                }
                Slog.w(TAG, message + " for another profile " + targetUserId + " from " + callingUserId + " not allowed");
                return false;
            } finally {
                injectRestoreCallingIdentity(ident);
            }
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void verifyCallingPackage(String callingPackage) {
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

        private ResolveInfo getHiddenAppActivityInfo(String packageName, int callingUid, UserHandle user) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME));
            List<ResolveInfo> apps = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).queryIntentActivities(intent, 786432, callingUid, user.getIdentifier());
            if (apps.size() > 0) {
                return apps.get(0);
            }
            return null;
        }

        public boolean shouldHideFromSuggestions(String packageName, UserHandle user) {
            if (canAccessProfile(user.getIdentifier(), "cannot get shouldHideFromSuggestions") && (((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getDistractingPackageRestrictions(packageName, user.getIdentifier()) & 1) != 0) {
                return true;
            }
            return false;
        }

        public ParceledListSlice<ResolveInfo> getLauncherActivities(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            ResolveInfo info;
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.LAUNCHERAPPS_GETLAUNCHERACTIVITIES);
            ParceledListSlice<ResolveInfo> launcherActivities = queryActivitiesForUser(callingPackage, new Intent("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER").setPackage(packageName), user);
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "show_hidden_icon_apps_enabled", 1) == 0) {
                return launcherActivities;
            }
            if (launcherActivities == null) {
                return null;
            }
            int callingUid = injectBinderCallingUid();
            long ident = injectClearCallingIdentity();
            try {
                if (this.mUm.getUserInfo(user.getIdentifier()).isManagedProfile()) {
                    return launcherActivities;
                }
                if (!checkDevicePolicyService(this.mDpm) || this.mDpm.getDeviceOwnerComponentOnAnyUser() == null) {
                    ArrayList<ResolveInfo> result = new ArrayList<>(launcherActivities.getList());
                    PackageManagerInternal pmInt = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                    if (packageName == null) {
                        HashSet<String> visiblePackages = new HashSet<>();
                        Iterator<ResolveInfo> it = result.iterator();
                        while (it.hasNext()) {
                            visiblePackages.add(it.next().activityInfo.packageName);
                        }
                        for (ApplicationInfo applicationInfo : pmInt.getInstalledApplications(0, user.getIdentifier(), callingUid)) {
                            if (!visiblePackages.contains(applicationInfo.packageName)) {
                                if (shouldShowSyntheticActivity(user, applicationInfo)) {
                                    ResolveInfo info2 = getHiddenAppActivityInfo(applicationInfo.packageName, callingUid, user);
                                    if (info2 != null) {
                                        if (this.mHwHarmonyServiceManager == null) {
                                            result.add(info2);
                                        }
                                        if (this.mHwHarmonyServiceManager != null && !this.mHwHarmonyServiceManager.isSilentInstalled(applicationInfo.packageName)) {
                                            result.add(info2);
                                        }
                                    }
                                }
                            }
                        }
                        ParceledListSlice<ResolveInfo> parceledListSlice = new ParceledListSlice<>(result);
                        injectRestoreCallingIdentity(ident);
                        return parceledListSlice;
                    } else if (result.size() > 0) {
                        injectRestoreCallingIdentity(ident);
                        return launcherActivities;
                    } else {
                        if (shouldShowSyntheticActivity(user, pmInt.getApplicationInfo(packageName, 0, callingUid, user.getIdentifier())) && (info = getHiddenAppActivityInfo(packageName, callingUid, user)) != null) {
                            if (this.mHwHarmonyServiceManager == null) {
                                result.add(info);
                            }
                            if (this.mHwHarmonyServiceManager != null && !this.mHwHarmonyServiceManager.isSilentInstalled(packageName)) {
                                result.add(info);
                            }
                        }
                        ParceledListSlice<ResolveInfo> parceledListSlice2 = new ParceledListSlice<>(result);
                        injectRestoreCallingIdentity(ident);
                        return parceledListSlice2;
                    }
                } else {
                    injectRestoreCallingIdentity(ident);
                    return launcherActivities;
                }
            } finally {
                injectRestoreCallingIdentity(ident);
            }
        }

        private boolean checkDevicePolicyService(DevicePolicyManager dpm) {
            if (dpm != null) {
                return true;
            }
            Log.e(TAG, " get device policy service is null");
            return false;
        }

        private boolean shouldShowSyntheticActivity(UserHandle user, ApplicationInfo appInfo) {
            if (appInfo == null || appInfo.isSystemApp() || appInfo.isUpdatedSystemApp() || isManagedProfileAdmin(user, appInfo.packageName)) {
                return false;
            }
            return hasComponentsAndRequestsPermissions(appInfo.packageName);
        }

        private boolean hasComponentsAndRequestsPermissions(String packageName) {
            PackageParser.Package pkg = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackage(packageName);
            if (pkg == null || ArrayUtils.isEmpty(pkg.requestedPermissions)) {
                return false;
            }
            if (hasApplicationDeclaredActivities(pkg) || !ArrayUtils.isEmpty(pkg.receivers) || !ArrayUtils.isEmpty(pkg.providers) || !ArrayUtils.isEmpty(pkg.services)) {
                return true;
            }
            return false;
        }

        private boolean hasApplicationDeclaredActivities(PackageParser.Package pkg) {
            if (pkg.activities == null || ArrayUtils.isEmpty(pkg.activities)) {
                return false;
            }
            if (pkg.activities.size() != 1 || !PackageManager.APP_DETAILS_ACTIVITY_CLASS_NAME.equals(((PackageParser.Activity) pkg.activities.get(0)).className)) {
                return true;
            }
            return false;
        }

        private boolean isManagedProfileAdmin(UserHandle user, String packageName) {
            List<UserInfo> userInfoList = this.mUm.getProfiles(user.getIdentifier());
            for (int i = 0; i < userInfoList.size(); i++) {
                UserInfo userInfo = userInfoList.get(i);
                if (userInfo.isManagedProfile()) {
                    if (!checkDevicePolicyService(this.mDpm)) {
                        return false;
                    }
                    ComponentName componentName = this.mDpm.getProfileOwnerAsUser(userInfo.getUserHandle());
                    if (componentName != null && componentName.getPackageName().equals(packageName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public ActivityInfo resolveActivity(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
            if (!canAccessProfile(user.getIdentifier(), "Cannot resolve activity")) {
                return null;
            }
            int callingUid = injectBinderCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                return ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getActivityInfo(component, 786432, callingUid, user.getIdentifier());
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public ParceledListSlice getShortcutConfigActivities(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            return queryActivitiesForUser(callingPackage, new Intent("android.intent.action.CREATE_SHORTCUT").setPackage(packageName), user);
        }

        private ParceledListSlice<ResolveInfo> queryActivitiesForUser(String callingPackage, Intent intent, UserHandle user) {
            if (!canAccessProfile(user.getIdentifier(), "Cannot retrieve activities")) {
                return null;
            }
            int callingUid = injectBinderCallingUid();
            long ident = injectClearCallingIdentity();
            try {
                return new ParceledListSlice<>(((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).queryIntentActivities(intent, 786432, callingUid, user.getIdentifier()));
            } finally {
                injectRestoreCallingIdentity(ident);
            }
        }

        public IntentSender getShortcutConfigActivityIntent(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
            ensureShortcutPermission(callingPackage);
            IntentSender intentSender = null;
            if (!canAccessProfile(user.getIdentifier(), "Cannot check package")) {
                return null;
            }
            Preconditions.checkNotNull(component);
            Intent intent = new Intent("android.intent.action.CREATE_SHORTCUT").setComponent(component);
            long identity = Binder.clearCallingIdentity();
            try {
                PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 1409286144, null, user);
                if (pi != null) {
                    intentSender = pi.getIntentSender();
                }
                return intentSender;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean isPackageEnabled(String callingPackage, String packageName, UserHandle user) throws RemoteException {
            boolean z = false;
            if (!canAccessProfile(user.getIdentifier(), "Cannot check package")) {
                return false;
            }
            int callingUid = injectBinderCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PackageInfo info = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getPackageInfo(packageName, 786432, callingUid, user.getIdentifier());
                if (info != null && info.applicationInfo.enabled) {
                    z = true;
                }
                return z;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public Bundle getSuspendedPackageLauncherExtras(String packageName, UserHandle user) {
            if (!canAccessProfile(user.getIdentifier(), "Cannot get launcher extras")) {
                return null;
            }
            return ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getSuspendedPackageLauncherExtras(packageName, user.getIdentifier());
        }

        public ApplicationInfo getApplicationInfo(String callingPackage, String packageName, int flags, UserHandle user) throws RemoteException {
            if (!canAccessProfile(user.getIdentifier(), "Cannot check package")) {
                return null;
            }
            int callingUid = injectBinderCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                return ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getApplicationInfo(packageName, flags, callingUid, user.getIdentifier());
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public LauncherApps.AppUsageLimit getAppUsageLimit(String callingPackage, String packageName, UserHandle user) {
            verifyCallingPackage(callingPackage);
            if (!canAccessProfile(user.getIdentifier(), "Cannot access usage limit")) {
                return null;
            }
            if (this.mActivityTaskManagerInternal.isCallerRecents(Binder.getCallingUid())) {
                UsageStatsManagerInternal.AppUsageLimitData data = this.mUsageStatsManagerInternal.getAppUsageLimit(packageName, user);
                if (data == null) {
                    return null;
                }
                return new LauncherApps.AppUsageLimit(data.getTotalUsageLimit(), data.getUsageRemaining());
            }
            throw new SecurityException("Caller is not the recents app");
        }

        private void ensureShortcutPermission(String callingPackage) {
            verifyCallingPackage(callingPackage);
            if (!this.mShortcutServiceInternal.hasShortcutHostPermission(getCallingUserId(), callingPackage, injectBinderCallingPid(), injectBinderCallingUid())) {
                throw new SecurityException("Caller can't access shortcut information");
            }
        }

        public ParceledListSlice getShortcuts(String callingPackage, long changedSince, String packageName, List shortcutIds, ComponentName componentName, int flags, UserHandle targetUser) {
            ensureShortcutPermission(callingPackage);
            if (!canAccessProfile(targetUser.getIdentifier(), "Cannot get shortcuts")) {
                return new ParceledListSlice(Collections.EMPTY_LIST);
            }
            if (shortcutIds == null || packageName != null) {
                return new ParceledListSlice(this.mShortcutServiceInternal.getShortcuts(getCallingUserId(), callingPackage, changedSince, packageName, shortcutIds, componentName, flags, targetUser.getIdentifier(), injectBinderCallingPid(), injectBinderCallingUid()));
            }
            throw new IllegalArgumentException("To query by shortcut ID, package name must also be set");
        }

        public void pinShortcuts(String callingPackage, String packageName, List<String> ids, UserHandle targetUser) {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.LAUNCHERAPPS_PINSHORTCUTS);
            ensureShortcutPermission(callingPackage);
            if (canAccessProfile(targetUser.getIdentifier(), "Cannot pin shortcuts")) {
                this.mShortcutServiceInternal.pinShortcuts(getCallingUserId(), callingPackage, packageName, ids, targetUser.getIdentifier());
            }
        }

        public int getShortcutIconResId(String callingPackage, String packageName, String id, int targetUserId) {
            ensureShortcutPermission(callingPackage);
            if (!canAccessProfile(targetUserId, "Cannot access shortcuts")) {
                return 0;
            }
            return this.mShortcutServiceInternal.getShortcutIconResId(getCallingUserId(), callingPackage, packageName, id, targetUserId);
        }

        public ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id, int targetUserId) {
            ensureShortcutPermission(callingPackage);
            if (!canAccessProfile(targetUserId, "Cannot access shortcuts")) {
                return null;
            }
            return this.mShortcutServiceInternal.getShortcutIconFd(getCallingUserId(), callingPackage, packageName, id, targetUserId);
        }

        public boolean hasShortcutHostPermission(String callingPackage) {
            verifyCallingPackage(callingPackage);
            return this.mShortcutServiceInternal.hasShortcutHostPermission(getCallingUserId(), callingPackage, injectBinderCallingPid(), injectBinderCallingUid());
        }

        public boolean startShortcut(String callingPackage, String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, int targetUserId) {
            Bundle startActivityOptions2;
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.LAUNCHERAPPS_STARTSHORTCUT);
            verifyCallingPackage(callingPackage);
            if (!canAccessProfile(targetUserId, "Cannot start activity")) {
                return false;
            }
            if (!this.mShortcutServiceInternal.isPinnedByCaller(getCallingUserId(), callingPackage, packageName, shortcutId, targetUserId)) {
                ensureShortcutPermission(callingPackage);
            }
            Intent[] intents = this.mShortcutServiceInternal.createShortcutIntents(getCallingUserId(), callingPackage, packageName, shortcutId, targetUserId, injectBinderCallingPid(), injectBinderCallingUid());
            if (intents != null) {
                if (intents.length != 0) {
                    intents[0].addFlags(268435456);
                    intents[0].setSourceBounds(sourceBounds);
                    if (HwActivityTaskManager.isPCMultiCastMode()) {
                        ActivityOptions activityOptions = ActivityOptions.fromBundle(startActivityOptions);
                        if (activityOptions == null) {
                            activityOptions = ActivityOptions.makeBasic();
                        }
                        activityOptions.setActivityLaunchEventFrom(3);
                        startActivityOptions2 = activityOptions.toBundle();
                    } else {
                        startActivityOptions2 = startActivityOptions;
                    }
                    return startShortcutIntentsAsPublisher(intents, packageName, startActivityOptions2, targetUserId);
                }
            }
            return false;
        }

        private boolean startShortcutIntentsAsPublisher(Intent[] intents, String publisherPackage, Bundle startActivityOptions, int userId) {
            try {
                int code = this.mActivityTaskManagerInternal.startActivitiesAsPackage(publisherPackage, userId, intents, startActivityOptions);
                if (ActivityManager.isStartResultSuccessful(code)) {
                    return true;
                }
                Log.e(TAG, "Couldn't start activity, code=" + code);
                return false;
            } catch (SecurityException e) {
                return false;
            }
        }

        public boolean isActivityEnabled(String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
            boolean z = false;
            if (!canAccessProfile(user.getIdentifier(), "Cannot check component")) {
                return false;
            }
            int callingUid = injectBinderCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityInfo info = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getActivityInfo(component, 786432, callingUid, user.getIdentifier());
                if (info != null && info.isEnabled()) {
                    z = true;
                }
                return z;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void startSessionDetailsActivityAsUser(IApplicationThread caller, String callingPackage, PackageInstaller.SessionInfo sessionInfo, Rect sourceBounds, Bundle opts, UserHandle userHandle) throws RemoteException {
            int userId = userHandle.getIdentifier();
            if (canAccessProfile(userId, "Cannot start details activity")) {
                Intent i = new Intent("android.intent.action.VIEW").setData(new Uri.Builder().scheme("market").authority("details").appendQueryParameter("id", sessionInfo.appPackageName).build()).putExtra("android.intent.extra.REFERRER", new Uri.Builder().scheme("android-app").authority(callingPackage).build());
                i.setSourceBounds(sourceBounds);
                this.mActivityTaskManagerInternal.startActivityAsUser(caller, callingPackage, i, opts, userId);
            }
        }

        public void startActivityAsUser(IApplicationThread caller, String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
            PackageManagerInternal pmInt;
            boolean canLaunch;
            if (canAccessProfile(user.getIdentifier(), "Cannot start activity")) {
                Intent launchIntent = new Intent("android.intent.action.MAIN");
                launchIntent.addCategory("android.intent.category.LAUNCHER");
                launchIntent.setSourceBounds(sourceBounds);
                launchIntent.addFlags(270532608);
                launchIntent.setPackage(component.getPackageName());
                int callingUid = injectBinderCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    List<ResolveInfo> apps = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).queryIntentActivities(launchIntent, 786432, callingUid, user.getIdentifier());
                    int size = apps.size();
                    int i = 0;
                    while (true) {
                        if (i >= size) {
                            canLaunch = false;
                            break;
                        }
                        ActivityInfo activityInfo = apps.get(i).activityInfo;
                        if (!activityInfo.packageName.equals(component.getPackageName()) || !activityInfo.name.equals(component.getClassName())) {
                            i++;
                        } else if (activityInfo.exported) {
                            launchIntent.setPackage(null);
                            launchIntent.setComponent(component);
                            canLaunch = true;
                        } else {
                            throw new SecurityException("Cannot launch non-exported components " + component);
                        }
                    }
                    if (canLaunch) {
                        Binder.restoreCallingIdentity(ident);
                        this.mActivityTaskManagerInternal.startActivityAsUser(caller, callingPackage, launchIntent, opts, user.getIdentifier());
                        return;
                    }
                    try {
                        throw new SecurityException("Attempt to launch activity without  category Intent.CATEGORY_LAUNCHER " + component);
                    } catch (Throwable th) {
                        pmInt = th;
                        Binder.restoreCallingIdentity(ident);
                        throw pmInt;
                    }
                } catch (Throwable th2) {
                    pmInt = th2;
                    Binder.restoreCallingIdentity(ident);
                    throw pmInt;
                }
            }
        }

        public void showAppDetailsAsUser(IApplicationThread caller, String callingPackage, ComponentName component, Rect sourceBounds, Bundle opts, UserHandle user) throws RemoteException {
            Throwable th;
            if (canAccessProfile(user.getIdentifier(), "Cannot show app details")) {
                long ident = Binder.clearCallingIdentity();
                try {
                    Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", component.getPackageName(), null));
                    intent.setFlags(268468224);
                    try {
                        intent.setSourceBounds(sourceBounds);
                        Binder.restoreCallingIdentity(ident);
                        this.mActivityTaskManagerInternal.startActivityAsUser(caller, callingPackage, intent, opts, user.getIdentifier());
                    } catch (Throwable th2) {
                        th = th2;
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isEnabledProfileOf(UserHandle listeningUser, UserHandle user, String debugMsg) {
            return this.mUserManagerInternal.isProfileAccessible(listeningUser.getIdentifier(), user.getIdentifier(), debugMsg, false);
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void postToPackageMonitorHandler(Runnable r) {
            this.mCallbackHandler.post(r);
        }

        /* access modifiers changed from: private */
        public class MyPackageMonitor extends PackageMonitor implements ShortcutServiceInternal.ShortcutChangeListener {
            private MyPackageMonitor() {
            }

            public void onPackageAdded(String packageName, int uid) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, user, "onPackageAdded")) {
                            try {
                                listener.onPackageAdded(user, packageName);
                            } catch (RemoteException re) {
                                Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                            }
                        }
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                        throw th;
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                LauncherAppsImpl.super.onPackageAdded(packageName, uid);
            }

            public void onPackageRemoved(String packageName, int uid) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, user, "onPackageRemoved")) {
                            try {
                                listener.onPackageRemoved(user, packageName);
                            } catch (RemoteException re) {
                                Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                            }
                        }
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                        throw th;
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                LauncherAppsImpl.super.onPackageRemoved(packageName, uid);
            }

            public void onPackageModified(String packageName) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, user, "onPackageModified")) {
                            try {
                                listener.onPackageChanged(user, packageName);
                            } catch (RemoteException re) {
                                Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                            }
                        }
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                        throw th;
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                LauncherAppsImpl.super.onPackageModified(packageName);
            }

            public void onPackagesAvailable(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, user, "onPackagesAvailable")) {
                            try {
                                listener.onPackagesAvailable(user, packages, isReplacing());
                            } catch (RemoteException re) {
                                Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                            }
                        }
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                        throw th;
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                LauncherAppsImpl.super.onPackagesAvailable(packages);
            }

            public void onPackagesUnavailable(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, user, "onPackagesUnavailable")) {
                            try {
                                listener.onPackagesUnavailable(user, packages, isReplacing());
                            } catch (RemoteException re) {
                                Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                            }
                        }
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                        throw th;
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                LauncherAppsImpl.super.onPackagesUnavailable(packages);
            }

            public void onPackagesSuspended(String[] packages, Bundle launcherExtras) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, user, "onPackagesSuspended")) {
                            try {
                                listener.onPackagesSuspended(user, packages, launcherExtras);
                            } catch (RemoteException re) {
                                Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                            }
                        }
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                        throw th;
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                LauncherAppsImpl.super.onPackagesSuspended(packages, launcherExtras);
            }

            public void onPackagesUnsuspended(String[] packages) {
                UserHandle user = new UserHandle(getChangingUserId());
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        IOnAppsChangedListener listener = LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(((BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i)).user, user, "onPackagesUnsuspended")) {
                            try {
                                listener.onPackagesUnsuspended(user, packages);
                            } catch (RemoteException re) {
                                Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                            }
                        }
                    } catch (Throwable th) {
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                        throw th;
                    }
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
                LauncherAppsImpl.super.onPackagesUnsuspended(packages);
            }

            public void onShortcutChanged(String packageName, int userId) {
                LauncherAppsImpl.this.postToPackageMonitorHandler(new Runnable(packageName, userId) {
                    /* class com.android.server.pm.$$Lambda$LauncherAppsService$LauncherAppsImpl$MyPackageMonitor$eTair5Mvr14v4M0nq9aQEW2cpY */
                    private final /* synthetic */ String f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        LauncherAppsService.LauncherAppsImpl.MyPackageMonitor.this.lambda$onShortcutChanged$0$LauncherAppsService$LauncherAppsImpl$MyPackageMonitor(this.f$1, this.f$2);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: onShortcutChangedInner */
            public void lambda$onShortcutChanged$0$LauncherAppsService$LauncherAppsImpl$MyPackageMonitor(String packageName, int userId) {
                Throwable th;
                RuntimeException e;
                RemoteException re;
                int n = LauncherAppsImpl.this.mListeners.beginBroadcast();
                try {
                    UserHandle user = UserHandle.of(userId);
                    for (int i = 0; i < n; i++) {
                        IOnAppsChangedListener listener = LauncherAppsImpl.this.mListeners.getBroadcastItem(i);
                        BroadcastCookie cookie = (BroadcastCookie) LauncherAppsImpl.this.mListeners.getBroadcastCookie(i);
                        if (LauncherAppsImpl.this.isEnabledProfileOf(cookie.user, user, "onShortcutChanged")) {
                            int launcherUserId = cookie.user.getIdentifier();
                            if (LauncherAppsImpl.this.mShortcutServiceInternal.hasShortcutHostPermission(launcherUserId, cookie.packageName, cookie.callingPid, cookie.callingUid)) {
                                try {
                                    try {
                                        listener.onShortcutChanged(user, packageName, new ParceledListSlice(LauncherAppsImpl.this.mShortcutServiceInternal.getShortcuts(launcherUserId, cookie.packageName, 0, packageName, (List) null, (ComponentName) null, 1039, userId, cookie.callingPid, cookie.callingUid)));
                                    } catch (RemoteException e2) {
                                        re = e2;
                                    }
                                } catch (RemoteException e3) {
                                    re = e3;
                                    try {
                                        Slog.d(LauncherAppsImpl.TAG, "Callback failed ", re);
                                    } catch (RuntimeException e4) {
                                        e = e4;
                                    }
                                }
                            }
                        }
                    }
                } catch (RuntimeException e5) {
                    e = e5;
                    try {
                        Log.w(LauncherAppsImpl.TAG, e.getMessage(), e);
                        LauncherAppsImpl.this.mListeners.finishBroadcast();
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    LauncherAppsImpl.this.mListeners.finishBroadcast();
                    throw th;
                }
                LauncherAppsImpl.this.mListeners.finishBroadcast();
            }
        }

        /* access modifiers changed from: package-private */
        public class PackageCallbackList<T extends IInterface> extends RemoteCallbackList<T> {
            PackageCallbackList() {
            }

            @Override // android.os.RemoteCallbackList
            public void onCallbackDied(T t, Object cookie) {
                LauncherAppsImpl.this.checkCallbackCount();
            }
        }
    }
}
