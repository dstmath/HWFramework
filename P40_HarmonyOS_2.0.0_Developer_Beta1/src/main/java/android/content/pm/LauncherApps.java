package android.content.pm;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ILauncherApps;
import android.content.pm.IOnAppsChangedListener;
import android.content.pm.IPinItemRequest;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

public class LauncherApps {
    public static final String ACTION_CONFIRM_PIN_APPWIDGET = "android.content.pm.action.CONFIRM_PIN_APPWIDGET";
    public static final String ACTION_CONFIRM_PIN_SHORTCUT = "android.content.pm.action.CONFIRM_PIN_SHORTCUT";
    static final boolean DEBUG = false;
    public static final String EXTRA_PIN_ITEM_REQUEST = "android.content.pm.extra.PIN_ITEM_REQUEST";
    static final String TAG = "LauncherApps";
    private IOnAppsChangedListener.Stub mAppsChangedListener;
    private final List<CallbackMessageHandler> mCallbacks;
    private final Context mContext;
    private final List<PackageInstaller.SessionCallbackDelegate> mDelegates;
    @UnsupportedAppUsage
    private final PackageManager mPm;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final ILauncherApps mService;
    private final UserManager mUserManager;

    public static abstract class Callback {
        public abstract void onPackageAdded(String str, UserHandle userHandle);

        public abstract void onPackageChanged(String str, UserHandle userHandle);

        public abstract void onPackageRemoved(String str, UserHandle userHandle);

        public abstract void onPackagesAvailable(String[] strArr, UserHandle userHandle, boolean z);

        public abstract void onPackagesUnavailable(String[] strArr, UserHandle userHandle, boolean z);

        public void onPackagesSuspended(String[] packageNames, UserHandle user) {
        }

        public void onPackagesSuspended(String[] packageNames, UserHandle user, Bundle launcherExtras) {
            onPackagesSuspended(packageNames, user);
        }

        public void onPackagesUnsuspended(String[] packageNames, UserHandle user) {
        }

        public void onShortcutsChanged(String packageName, List<ShortcutInfo> list, UserHandle user) {
        }
    }

    public static class ShortcutQuery {
        @Deprecated
        public static final int FLAG_GET_ALL_KINDS = 11;
        @Deprecated
        public static final int FLAG_GET_DYNAMIC = 1;
        public static final int FLAG_GET_KEY_FIELDS_ONLY = 4;
        @Deprecated
        public static final int FLAG_GET_MANIFEST = 8;
        @Deprecated
        public static final int FLAG_GET_PINNED = 2;
        public static final int FLAG_MATCH_ALL_KINDS = 11;
        public static final int FLAG_MATCH_ALL_KINDS_WITH_ALL_PINNED = 1035;
        public static final int FLAG_MATCH_DYNAMIC = 1;
        public static final int FLAG_MATCH_MANIFEST = 8;
        public static final int FLAG_MATCH_PINNED = 2;
        public static final int FLAG_MATCH_PINNED_BY_ANY_LAUNCHER = 1024;
        ComponentName mActivity;
        long mChangedSince;
        String mPackage;
        int mQueryFlags;
        List<String> mShortcutIds;

        @Retention(RetentionPolicy.SOURCE)
        public @interface QueryFlags {
        }

        public ShortcutQuery setChangedSince(long changedSince) {
            this.mChangedSince = changedSince;
            return this;
        }

        public ShortcutQuery setPackage(String packageName) {
            this.mPackage = packageName;
            return this;
        }

        public ShortcutQuery setShortcutIds(List<String> shortcutIds) {
            this.mShortcutIds = shortcutIds;
            return this;
        }

        public ShortcutQuery setActivity(ComponentName activity) {
            this.mActivity = activity;
            return this;
        }

        public ShortcutQuery setQueryFlags(int queryFlags) {
            this.mQueryFlags = queryFlags;
            return this;
        }
    }

    public LauncherApps(Context context, ILauncherApps service) {
        this.mCallbacks = new ArrayList();
        this.mDelegates = new ArrayList();
        this.mAppsChangedListener = new IOnAppsChangedListener.Stub() {
            /* class android.content.pm.LauncherApps.AnonymousClass1 */

            @Override // android.content.pm.IOnAppsChangedListener
            public void onPackageRemoved(UserHandle user, String packageName) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackageRemoved(packageName, user);
                    }
                }
            }

            @Override // android.content.pm.IOnAppsChangedListener
            public void onPackageChanged(UserHandle user, String packageName) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackageChanged(packageName, user);
                    }
                }
            }

            @Override // android.content.pm.IOnAppsChangedListener
            public void onPackageAdded(UserHandle user, String packageName) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackageAdded(packageName, user);
                    }
                }
            }

            @Override // android.content.pm.IOnAppsChangedListener
            public void onPackagesAvailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesAvailable(packageNames, user, replacing);
                    }
                }
            }

            @Override // android.content.pm.IOnAppsChangedListener
            public void onPackagesUnavailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesUnavailable(packageNames, user, replacing);
                    }
                }
            }

            @Override // android.content.pm.IOnAppsChangedListener
            public void onPackagesSuspended(UserHandle user, String[] packageNames, Bundle launcherExtras) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesSuspended(packageNames, launcherExtras, user);
                    }
                }
            }

            @Override // android.content.pm.IOnAppsChangedListener
            public void onPackagesUnsuspended(UserHandle user, String[] packageNames) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesUnsuspended(packageNames, user);
                    }
                }
            }

            @Override // android.content.pm.IOnAppsChangedListener
            public void onShortcutChanged(UserHandle user, String packageName, ParceledListSlice shortcuts) {
                List<ShortcutInfo> list = shortcuts.getList();
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnShortcutChanged(packageName, user, list);
                    }
                }
            }
        };
        this.mContext = context;
        this.mService = service;
        this.mPm = context.getPackageManager();
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
    }

    public LauncherApps(Context context) {
        this(context, ILauncherApps.Stub.asInterface(ServiceManager.getService(Context.LAUNCHER_APPS_SERVICE)));
    }

    private void logErrorForInvalidProfileAccess(UserHandle target) {
        if (UserHandle.myUserId() != target.getIdentifier() && this.mUserManager.isManagedProfile()) {
            Log.w(TAG, "Accessing other profiles/users from managed profile is no longer allowed.");
        }
    }

    public List<UserHandle> getProfiles() {
        if (!this.mUserManager.isManagedProfile()) {
            return this.mUserManager.getUserProfiles();
        }
        List result = new ArrayList(1);
        result.add(Process.myUserHandle());
        return result;
    }

    public List<LauncherActivityInfo> getActivityList(String packageName, UserHandle user) {
        logErrorForInvalidProfileAccess(user);
        try {
            return convertToActivityList(this.mService.getLauncherActivities(this.mContext.getPackageName(), packageName, user), user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public LauncherActivityInfo resolveActivity(Intent intent, UserHandle user) {
        logErrorForInvalidProfileAccess(user);
        try {
            ActivityInfo ai = this.mService.resolveActivity(this.mContext.getPackageName(), intent.getComponent(), user);
            if (ai != null) {
                return new LauncherActivityInfo(this.mContext, ai, user);
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void startMainActivity(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        logErrorForInvalidProfileAccess(user);
        try {
            this.mService.startActivityAsUser(this.mContext.getIApplicationThread(), this.mContext.getPackageName(), component, sourceBounds, opts, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void startPackageInstallerSessionDetailsActivity(PackageInstaller.SessionInfo sessionInfo, Rect sourceBounds, Bundle opts) {
        try {
            this.mService.startSessionDetailsActivityAsUser(this.mContext.getIApplicationThread(), this.mContext.getPackageName(), sessionInfo, sourceBounds, opts, sessionInfo.getUser());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void startAppDetailsActivity(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        logErrorForInvalidProfileAccess(user);
        try {
            this.mService.showAppDetailsAsUser(this.mContext.getIApplicationThread(), this.mContext.getPackageName(), component, sourceBounds, opts, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<LauncherActivityInfo> getShortcutConfigActivityList(String packageName, UserHandle user) {
        logErrorForInvalidProfileAccess(user);
        try {
            return convertToActivityList(this.mService.getShortcutConfigActivities(this.mContext.getPackageName(), packageName, user), user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    private List<LauncherActivityInfo> convertToActivityList(ParceledListSlice<ResolveInfo> activities, UserHandle user) {
        if (activities == null) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<LauncherActivityInfo> lais = new ArrayList<>();
        for (ResolveInfo ri : activities.getList()) {
            lais.add(new LauncherActivityInfo(this.mContext, ri.activityInfo, user));
        }
        return lais;
    }

    public IntentSender getShortcutConfigActivityIntent(LauncherActivityInfo info) {
        try {
            return this.mService.getShortcutConfigActivityIntent(this.mContext.getPackageName(), info.getComponentName(), info.getUser());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isPackageEnabled(String packageName, UserHandle user) {
        logErrorForInvalidProfileAccess(user);
        try {
            return this.mService.isPackageEnabled(this.mContext.getPackageName(), packageName, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Bundle getSuspendedPackageLauncherExtras(String packageName, UserHandle user) {
        logErrorForInvalidProfileAccess(user);
        try {
            return this.mService.getSuspendedPackageLauncherExtras(packageName, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean shouldHideFromSuggestions(String packageName, UserHandle user) {
        Preconditions.checkNotNull(packageName, "packageName");
        Preconditions.checkNotNull(user, "user");
        try {
            return this.mService.shouldHideFromSuggestions(packageName, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags, UserHandle user) throws PackageManager.NameNotFoundException {
        Preconditions.checkNotNull(packageName, "packageName");
        Preconditions.checkNotNull(user, "user");
        logErrorForInvalidProfileAccess(user);
        try {
            ApplicationInfo ai = this.mService.getApplicationInfo(this.mContext.getPackageName(), packageName, flags, user);
            if (ai != null) {
                return ai;
            }
            throw new PackageManager.NameNotFoundException("Package " + packageName + " not found for user " + user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public AppUsageLimit getAppUsageLimit(String packageName, UserHandle user) {
        try {
            return this.mService.getAppUsageLimit(this.mContext.getPackageName(), packageName, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isActivityEnabled(ComponentName component, UserHandle user) {
        logErrorForInvalidProfileAccess(user);
        try {
            return this.mService.isActivityEnabled(this.mContext.getPackageName(), component, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean hasShortcutHostPermission() {
        try {
            return this.mService.hasShortcutHostPermission(this.mContext.getPackageName());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    private List<ShortcutInfo> maybeUpdateDisabledMessage(List<ShortcutInfo> shortcuts) {
        if (shortcuts == null) {
            return null;
        }
        for (int i = shortcuts.size() - 1; i >= 0; i--) {
            ShortcutInfo si = shortcuts.get(i);
            String message = ShortcutInfo.getDisabledReasonForRestoreIssue(this.mContext, si.getDisabledReason());
            if (message != null) {
                si.setDisabledMessage(message);
            }
        }
        return shortcuts;
    }

    public List<ShortcutInfo> getShortcuts(ShortcutQuery query, UserHandle user) {
        logErrorForInvalidProfileAccess(user);
        try {
            return maybeUpdateDisabledMessage(this.mService.getShortcuts(this.mContext.getPackageName(), query.mChangedSince, query.mPackage, query.mShortcutIds, query.mActivity, query.mQueryFlags, user).getList());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public List<ShortcutInfo> getShortcutInfo(String packageName, List<String> ids, UserHandle user) {
        ShortcutQuery q = new ShortcutQuery();
        q.setPackage(packageName);
        q.setShortcutIds(ids);
        q.setQueryFlags(11);
        return getShortcuts(q, user);
    }

    public void pinShortcuts(String packageName, List<String> shortcutIds, UserHandle user) {
        logErrorForInvalidProfileAccess(user);
        try {
            this.mService.pinShortcuts(this.mContext.getPackageName(), packageName, shortcutIds, user);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public int getShortcutIconResId(ShortcutInfo shortcut) {
        return shortcut.getIconResourceId();
    }

    @Deprecated
    public int getShortcutIconResId(String packageName, String shortcutId, UserHandle user) {
        ShortcutQuery q = new ShortcutQuery();
        q.setPackage(packageName);
        q.setShortcutIds(Arrays.asList(shortcutId));
        q.setQueryFlags(11);
        List<ShortcutInfo> shortcuts = getShortcuts(q, user);
        if (shortcuts.size() > 0) {
            return shortcuts.get(0).getIconResourceId();
        }
        return 0;
    }

    public ParcelFileDescriptor getShortcutIconFd(ShortcutInfo shortcut) {
        return getShortcutIconFd(shortcut.getPackage(), shortcut.getId(), shortcut.getUserId());
    }

    public ParcelFileDescriptor getShortcutIconFd(String packageName, String shortcutId, UserHandle user) {
        return getShortcutIconFd(packageName, shortcutId, user.getIdentifier());
    }

    private ParcelFileDescriptor getShortcutIconFd(String packageName, String shortcutId, int userId) {
        try {
            return this.mService.getShortcutIconFd(this.mContext.getPackageName(), packageName, shortcutId, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Drawable getShortcutIconDrawable(ShortcutInfo shortcut, int density) {
        if (shortcut.hasIconFile()) {
            ParcelFileDescriptor pfd = getShortcutIconFd(shortcut);
            if (pfd == null) {
                return null;
            }
            try {
                Bitmap bmp = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                if (bmp != null) {
                    BitmapDrawable dr = new BitmapDrawable(this.mContext.getResources(), bmp);
                    if (shortcut.hasAdaptiveBitmap()) {
                        return new AdaptiveIconDrawable((Drawable) null, dr);
                    }
                    try {
                        pfd.close();
                    } catch (IOException e) {
                    }
                    return dr;
                }
                try {
                    pfd.close();
                } catch (IOException e2) {
                }
                return null;
            } finally {
                try {
                    pfd.close();
                } catch (IOException e3) {
                }
            }
        } else if (shortcut.hasIconResource()) {
            return loadDrawableResourceFromPackage(shortcut.getPackage(), shortcut.getIconResourceId(), shortcut.getUserHandle(), density);
        } else {
            if (shortcut.getIcon() == null) {
                return null;
            }
            Icon icon = shortcut.getIcon();
            int type = icon.getType();
            if (type != 1) {
                if (type == 2) {
                    return loadDrawableResourceFromPackage(shortcut.getPackage(), icon.getResId(), shortcut.getUserHandle(), density);
                }
                if (type != 5) {
                    return null;
                }
            }
            return icon.loadDrawable(this.mContext);
        }
    }

    private Drawable loadDrawableResourceFromPackage(String packageName, int resId, UserHandle user, int density) {
        if (resId == 0) {
            return null;
        }
        try {
            return this.mContext.getPackageManager().getResourcesForApplication(getApplicationInfo(packageName, 0, user)).getDrawableForDensity(resId, density);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            return null;
        }
    }

    public Drawable getShortcutBadgedIconDrawable(ShortcutInfo shortcut, int density) {
        Drawable originalIcon = getShortcutIconDrawable(shortcut, density);
        if (originalIcon == null) {
            return null;
        }
        return this.mContext.getPackageManager().getUserBadgedIcon(originalIcon, shortcut.getUserHandle());
    }

    public void startShortcut(String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, UserHandle user) {
        logErrorForInvalidProfileAccess(user);
        startShortcut(packageName, shortcutId, sourceBounds, startActivityOptions, user.getIdentifier());
    }

    public void startShortcut(ShortcutInfo shortcut, Rect sourceBounds, Bundle startActivityOptions) {
        startShortcut(shortcut.getPackage(), shortcut.getId(), sourceBounds, startActivityOptions, shortcut.getUserId());
    }

    @UnsupportedAppUsage
    private void startShortcut(String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, int userId) {
        try {
            if (!this.mService.startShortcut(this.mContext.getPackageName(), packageName, shortcutId, sourceBounds, startActivityOptions, userId)) {
                throw new ActivityNotFoundException("Shortcut could not be started");
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerCallback(Callback callback) {
        registerCallback(callback, null);
    }

    public void registerCallback(Callback callback, Handler handler) {
        synchronized (this) {
            if (callback != null) {
                if (findCallbackLocked(callback) < 0) {
                    boolean addedFirstCallback = this.mCallbacks.size() == 0;
                    addCallbackLocked(callback, handler);
                    if (addedFirstCallback) {
                        try {
                            this.mService.addOnAppsChangedListener(this.mContext.getPackageName(), this.mAppsChangedListener);
                        } catch (RemoteException re) {
                            throw re.rethrowFromSystemServer();
                        }
                    }
                }
            }
        }
    }

    public void unregisterCallback(Callback callback) {
        synchronized (this) {
            removeCallbackLocked(callback);
            if (this.mCallbacks.size() == 0) {
                try {
                    this.mService.removeOnAppsChangedListener(this.mAppsChangedListener);
                } catch (RemoteException re) {
                    throw re.rethrowFromSystemServer();
                }
            }
        }
    }

    private int findCallbackLocked(Callback callback) {
        if (callback != null) {
            int size = this.mCallbacks.size();
            for (int i = 0; i < size; i++) {
                if (this.mCallbacks.get(i).mCallback == callback) {
                    return i;
                }
            }
            return -1;
        }
        throw new IllegalArgumentException("Callback cannot be null");
    }

    private void removeCallbackLocked(Callback callback) {
        int pos = findCallbackLocked(callback);
        if (pos >= 0) {
            this.mCallbacks.remove(pos);
        }
    }

    private void addCallbackLocked(Callback callback, Handler handler) {
        removeCallbackLocked(callback);
        if (handler == null) {
            handler = new Handler();
        }
        this.mCallbacks.add(new CallbackMessageHandler(handler.getLooper(), callback));
    }

    /* access modifiers changed from: private */
    public static class CallbackMessageHandler extends Handler {
        private static final int MSG_ADDED = 1;
        private static final int MSG_AVAILABLE = 4;
        private static final int MSG_CHANGED = 3;
        private static final int MSG_REMOVED = 2;
        private static final int MSG_SHORTCUT_CHANGED = 8;
        private static final int MSG_SUSPENDED = 6;
        private static final int MSG_UNAVAILABLE = 5;
        private static final int MSG_UNSUSPENDED = 7;
        private Callback mCallback;

        /* access modifiers changed from: private */
        public static class CallbackInfo {
            Bundle launcherExtras;
            String packageName;
            String[] packageNames;
            boolean replacing;
            List<ShortcutInfo> shortcuts;
            UserHandle user;

            private CallbackInfo() {
            }
        }

        public CallbackMessageHandler(Looper looper, Callback callback) {
            super(looper, null, true);
            this.mCallback = callback;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (this.mCallback != null && (msg.obj instanceof CallbackInfo)) {
                CallbackInfo info = (CallbackInfo) msg.obj;
                switch (msg.what) {
                    case 1:
                        this.mCallback.onPackageAdded(info.packageName, info.user);
                        return;
                    case 2:
                        this.mCallback.onPackageRemoved(info.packageName, info.user);
                        return;
                    case 3:
                        this.mCallback.onPackageChanged(info.packageName, info.user);
                        return;
                    case 4:
                        this.mCallback.onPackagesAvailable(info.packageNames, info.user, info.replacing);
                        return;
                    case 5:
                        this.mCallback.onPackagesUnavailable(info.packageNames, info.user, info.replacing);
                        return;
                    case 6:
                        this.mCallback.onPackagesSuspended(info.packageNames, info.user, info.launcherExtras);
                        return;
                    case 7:
                        this.mCallback.onPackagesUnsuspended(info.packageNames, info.user);
                        return;
                    case 8:
                        this.mCallback.onShortcutsChanged(info.packageName, info.shortcuts, info.user);
                        return;
                    default:
                        return;
                }
            }
        }

        public void postOnPackageAdded(String packageName, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            obtainMessage(1, info).sendToTarget();
        }

        public void postOnPackageRemoved(String packageName, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            obtainMessage(2, info).sendToTarget();
        }

        public void postOnPackageChanged(String packageName, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            obtainMessage(3, info).sendToTarget();
        }

        public void postOnPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.replacing = replacing;
            info.user = user;
            obtainMessage(4, info).sendToTarget();
        }

        public void postOnPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.replacing = replacing;
            info.user = user;
            obtainMessage(5, info).sendToTarget();
        }

        public void postOnPackagesSuspended(String[] packageNames, Bundle launcherExtras, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.user = user;
            info.launcherExtras = launcherExtras;
            obtainMessage(6, info).sendToTarget();
        }

        public void postOnPackagesUnsuspended(String[] packageNames, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.user = user;
            obtainMessage(7, info).sendToTarget();
        }

        public void postOnShortcutChanged(String packageName, UserHandle user, List<ShortcutInfo> shortcuts) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            info.shortcuts = shortcuts;
            obtainMessage(8, info).sendToTarget();
        }
    }

    public void registerPackageInstallerSessionCallback(Executor executor, PackageInstaller.SessionCallback callback) {
        if (executor != null) {
            synchronized (this.mDelegates) {
                PackageInstaller.SessionCallbackDelegate delegate = new PackageInstaller.SessionCallbackDelegate(callback, executor);
                try {
                    this.mService.registerPackageInstallerCallback(this.mContext.getPackageName(), delegate);
                    this.mDelegates.add(delegate);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return;
        }
        throw new NullPointerException("Executor must not be null");
    }

    public void unregisterPackageInstallerSessionCallback(PackageInstaller.SessionCallback callback) {
        synchronized (this.mDelegates) {
            Iterator<PackageInstaller.SessionCallbackDelegate> i = this.mDelegates.iterator();
            while (i.hasNext()) {
                PackageInstaller.SessionCallbackDelegate delegate = i.next();
                if (delegate.mCallback == callback) {
                    this.mPm.getPackageInstaller().unregisterSessionCallback(delegate.mCallback);
                    i.remove();
                }
            }
        }
    }

    public List<PackageInstaller.SessionInfo> getAllPackageInstallerSessions() {
        try {
            return this.mService.getAllSessions(this.mContext.getPackageName()).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PinItemRequest getPinItemRequest(Intent intent) {
        return (PinItemRequest) intent.getParcelableExtra(EXTRA_PIN_ITEM_REQUEST);
    }

    public static final class PinItemRequest implements Parcelable {
        public static final Parcelable.Creator<PinItemRequest> CREATOR = new Parcelable.Creator<PinItemRequest>() {
            /* class android.content.pm.LauncherApps.PinItemRequest.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public PinItemRequest createFromParcel(Parcel source) {
                return new PinItemRequest(source);
            }

            @Override // android.os.Parcelable.Creator
            public PinItemRequest[] newArray(int size) {
                return new PinItemRequest[size];
            }
        };
        public static final int REQUEST_TYPE_APPWIDGET = 2;
        public static final int REQUEST_TYPE_SHORTCUT = 1;
        private final IPinItemRequest mInner;
        private final int mRequestType;

        @Retention(RetentionPolicy.SOURCE)
        public @interface RequestType {
        }

        public PinItemRequest(IPinItemRequest inner, int type) {
            this.mInner = inner;
            this.mRequestType = type;
        }

        public int getRequestType() {
            return this.mRequestType;
        }

        public ShortcutInfo getShortcutInfo() {
            try {
                return this.mInner.getShortcutInfo();
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }
        }

        public AppWidgetProviderInfo getAppWidgetProviderInfo(Context context) {
            try {
                AppWidgetProviderInfo info = this.mInner.getAppWidgetProviderInfo();
                if (info == null) {
                    return null;
                }
                info.updateDimensions(context.getResources().getDisplayMetrics());
                return info;
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }
        }

        public Bundle getExtras() {
            try {
                return this.mInner.getExtras();
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }
        }

        public boolean isValid() {
            try {
                return this.mInner.isValid();
            } catch (RemoteException e) {
                return false;
            }
        }

        public boolean accept(Bundle options) {
            try {
                return this.mInner.accept(options);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public boolean accept() {
            return accept(null);
        }

        private PinItemRequest(Parcel source) {
            getClass().getClassLoader();
            this.mRequestType = source.readInt();
            this.mInner = IPinItemRequest.Stub.asInterface(source.readStrongBinder());
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mRequestType);
            dest.writeStrongBinder(this.mInner.asBinder());
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }
    }

    @SystemApi
    public static final class AppUsageLimit implements Parcelable {
        public static final Parcelable.Creator<AppUsageLimit> CREATOR = new Parcelable.Creator<AppUsageLimit>() {
            /* class android.content.pm.LauncherApps.AppUsageLimit.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public AppUsageLimit createFromParcel(Parcel source) {
                return new AppUsageLimit(source);
            }

            @Override // android.os.Parcelable.Creator
            public AppUsageLimit[] newArray(int size) {
                return new AppUsageLimit[size];
            }
        };
        private final long mTotalUsageLimit;
        private final long mUsageRemaining;

        public AppUsageLimit(long totalUsageLimit, long usageRemaining) {
            this.mTotalUsageLimit = totalUsageLimit;
            this.mUsageRemaining = usageRemaining;
        }

        public long getTotalUsageLimit() {
            return this.mTotalUsageLimit;
        }

        public long getUsageRemaining() {
            return this.mUsageRemaining;
        }

        private AppUsageLimit(Parcel source) {
            this.mTotalUsageLimit = source.readLong();
            this.mUsageRemaining = source.readLong();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.mTotalUsageLimit);
            dest.writeLong(this.mUsageRemaining);
        }
    }
}
