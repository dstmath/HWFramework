package android.content.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IOnAppsChangedListener.Stub;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LauncherApps {
    static final boolean DEBUG = false;
    static final String TAG = "LauncherApps";
    private Stub mAppsChangedListener;
    private List<CallbackMessageHandler> mCallbacks;
    private Context mContext;
    private PackageManager mPm;
    private ILauncherApps mService;

    public static abstract class Callback {
        public abstract void onPackageAdded(String str, UserHandle userHandle);

        public abstract void onPackageChanged(String str, UserHandle userHandle);

        public abstract void onPackageRemoved(String str, UserHandle userHandle);

        public abstract void onPackagesAvailable(String[] strArr, UserHandle userHandle, boolean z);

        public abstract void onPackagesUnavailable(String[] strArr, UserHandle userHandle, boolean z);

        public void onPackagesSuspended(String[] packageNames, UserHandle user) {
        }

        public void onPackagesUnsuspended(String[] packageNames, UserHandle user) {
        }

        public void onShortcutsChanged(String packageName, List<ShortcutInfo> list, UserHandle user) {
        }
    }

    private static class CallbackMessageHandler extends Handler {
        private static final int MSG_ADDED = 1;
        private static final int MSG_AVAILABLE = 4;
        private static final int MSG_CHANGED = 3;
        private static final int MSG_REMOVED = 2;
        private static final int MSG_SHORTCUT_CHANGED = 8;
        private static final int MSG_SUSPENDED = 6;
        private static final int MSG_UNAVAILABLE = 5;
        private static final int MSG_UNSUSPENDED = 7;
        private Callback mCallback;

        private static class CallbackInfo {
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

        public void handleMessage(Message msg) {
            if (this.mCallback != null && (msg.obj instanceof CallbackInfo)) {
                CallbackInfo info = msg.obj;
                switch (msg.what) {
                    case MSG_ADDED /*1*/:
                        this.mCallback.onPackageAdded(info.packageName, info.user);
                        break;
                    case MSG_REMOVED /*2*/:
                        this.mCallback.onPackageRemoved(info.packageName, info.user);
                        break;
                    case MSG_CHANGED /*3*/:
                        this.mCallback.onPackageChanged(info.packageName, info.user);
                        break;
                    case MSG_AVAILABLE /*4*/:
                        this.mCallback.onPackagesAvailable(info.packageNames, info.user, info.replacing);
                        break;
                    case MSG_UNAVAILABLE /*5*/:
                        this.mCallback.onPackagesUnavailable(info.packageNames, info.user, info.replacing);
                        break;
                    case MSG_SUSPENDED /*6*/:
                        this.mCallback.onPackagesSuspended(info.packageNames, info.user);
                        break;
                    case MSG_UNSUSPENDED /*7*/:
                        this.mCallback.onPackagesUnsuspended(info.packageNames, info.user);
                        break;
                    case MSG_SHORTCUT_CHANGED /*8*/:
                        this.mCallback.onShortcutsChanged(info.packageName, info.shortcuts, info.user);
                        break;
                }
            }
        }

        public void postOnPackageAdded(String packageName, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            obtainMessage(MSG_ADDED, info).sendToTarget();
        }

        public void postOnPackageRemoved(String packageName, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            obtainMessage(MSG_REMOVED, info).sendToTarget();
        }

        public void postOnPackageChanged(String packageName, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            obtainMessage(MSG_CHANGED, info).sendToTarget();
        }

        public void postOnPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.replacing = replacing;
            info.user = user;
            obtainMessage(MSG_AVAILABLE, info).sendToTarget();
        }

        public void postOnPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.replacing = replacing;
            info.user = user;
            obtainMessage(MSG_UNAVAILABLE, info).sendToTarget();
        }

        public void postOnPackagesSuspended(String[] packageNames, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.user = user;
            obtainMessage(MSG_SUSPENDED, info).sendToTarget();
        }

        public void postOnPackagesUnsuspended(String[] packageNames, UserHandle user) {
            CallbackInfo info = new CallbackInfo();
            info.packageNames = packageNames;
            info.user = user;
            obtainMessage(MSG_UNSUSPENDED, info).sendToTarget();
        }

        public void postOnShortcutChanged(String packageName, UserHandle user, List<ShortcutInfo> shortcuts) {
            CallbackInfo info = new CallbackInfo();
            info.packageName = packageName;
            info.user = user;
            info.shortcuts = shortcuts;
            obtainMessage(MSG_SHORTCUT_CHANGED, info).sendToTarget();
        }
    }

    public static class ShortcutQuery {
        public static final int FLAG_GET_DYNAMIC = 1;
        public static final int FLAG_GET_KEY_FIELDS_ONLY = 4;
        public static final int FLAG_GET_PINNED = 2;
        ComponentName mActivity;
        long mChangedSince;
        String mPackage;
        int mQueryFlags;
        List<String> mShortcutIds;

        public void setChangedSince(long changedSince) {
            this.mChangedSince = changedSince;
        }

        public void setPackage(String packageName) {
            this.mPackage = packageName;
        }

        public void setShortcutIds(List<String> shortcutIds) {
            this.mShortcutIds = shortcutIds;
        }

        public void setActivity(ComponentName activity) {
            this.mActivity = activity;
        }

        public void setQueryFlags(int queryFlags) {
            this.mQueryFlags = queryFlags;
        }
    }

    public LauncherApps(Context context, ILauncherApps service) {
        this.mCallbacks = new ArrayList();
        this.mAppsChangedListener = new Stub() {
            public void onPackageRemoved(UserHandle user, String packageName) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackageRemoved(packageName, user);
                    }
                }
            }

            public void onPackageChanged(UserHandle user, String packageName) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackageChanged(packageName, user);
                    }
                }
            }

            public void onPackageAdded(UserHandle user, String packageName) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackageAdded(packageName, user);
                    }
                }
            }

            public void onPackagesAvailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesAvailable(packageNames, user, replacing);
                    }
                }
            }

            public void onPackagesUnavailable(UserHandle user, String[] packageNames, boolean replacing) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesUnavailable(packageNames, user, replacing);
                    }
                }
            }

            public void onPackagesSuspended(UserHandle user, String[] packageNames) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesSuspended(packageNames, user);
                    }
                }
            }

            public void onPackagesUnsuspended(UserHandle user, String[] packageNames) throws RemoteException {
                synchronized (LauncherApps.this) {
                    for (CallbackMessageHandler callback : LauncherApps.this.mCallbacks) {
                        callback.postOnPackagesUnsuspended(packageNames, user);
                    }
                }
            }

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
    }

    public LauncherApps(Context context) {
        this(context, ILauncherApps.Stub.asInterface(ServiceManager.getService(Context.LAUNCHER_APPS_SERVICE)));
    }

    public List<LauncherActivityInfo> getActivityList(String packageName, UserHandle user) {
        try {
            ParceledListSlice<ResolveInfo> activities = this.mService.getLauncherActivities(packageName, user);
            if (activities == null) {
                return Collections.EMPTY_LIST;
            }
            ArrayList<LauncherActivityInfo> lais = new ArrayList();
            for (ResolveInfo ri : activities.getList()) {
                lais.add(new LauncherActivityInfo(this.mContext, ri.activityInfo, user));
            }
            return lais;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public LauncherActivityInfo resolveActivity(Intent intent, UserHandle user) {
        try {
            if (intent.getComponent() == null) {
                Log.i(TAG, " resolveActivity intent.getComponent() is null!");
                ResolveInfo ri = this.mService.resolveActivityByIntent(intent, user);
                if (ri != null) {
                    return new LauncherActivityInfo(this.mContext, ri.activityInfo, user);
                }
            }
            ActivityInfo ai = this.mService.resolveActivity(intent.getComponent(), user);
            if (ai != null) {
                return new LauncherActivityInfo(this.mContext, ai, user);
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void startMainActivity(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        try {
            this.mService.startActivityAsUser(component, sourceBounds, opts, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void startAppDetailsActivity(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        try {
            this.mService.showAppDetailsAsUser(component, sourceBounds, opts, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isPackageEnabled(String packageName, UserHandle user) {
        try {
            return this.mService.isPackageEnabled(packageName, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags, UserHandle user) {
        try {
            return this.mService.getApplicationInfo(packageName, flags, user);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean isActivityEnabled(ComponentName component, UserHandle user) {
        try {
            return this.mService.isActivityEnabled(component, user);
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

    public List<ShortcutInfo> getShortcuts(ShortcutQuery query, UserHandle user) {
        try {
            return this.mService.getShortcuts(this.mContext.getPackageName(), query.mChangedSince, query.mPackage, query.mShortcutIds, query.mActivity, query.mQueryFlags, user).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ShortcutInfo> getShortcutInfo(String packageName, List<String> ids, UserHandle user) {
        ShortcutQuery q = new ShortcutQuery();
        q.setPackage(packageName);
        q.setShortcutIds(ids);
        q.setQueryFlags(3);
        return getShortcuts(q, user);
    }

    public void pinShortcuts(String packageName, List<String> shortcutIds, UserHandle user) {
        try {
            this.mService.pinShortcuts(this.mContext.getPackageName(), packageName, shortcutIds, user);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getShortcutIconResId(ShortcutInfo shortcut) {
        return shortcut.getIconResourceId();
    }

    public int getShortcutIconResId(String packageName, String shortcutId, UserHandle user) {
        ShortcutQuery q = new ShortcutQuery();
        q.setPackage(packageName);
        q.setShortcutIds(Arrays.asList(new String[]{shortcutId}));
        q.setQueryFlags(3);
        List<ShortcutInfo> shortcuts = getShortcuts(q, user);
        if (shortcuts.size() > 0) {
            return ((ShortcutInfo) shortcuts.get(0)).getIconResourceId();
        }
        return 0;
    }

    public ParcelFileDescriptor getShortcutIconFd(ShortcutInfo shortcut) {
        return getShortcutIconFd(shortcut.getPackageName(), shortcut.getId(), shortcut.getUserId());
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

    public boolean startShortcut(String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, UserHandle user) {
        return startShortcut(packageName, shortcutId, sourceBounds, startActivityOptions, user.getIdentifier());
    }

    public boolean startShortcut(ShortcutInfo shortcut, Rect sourceBounds, Bundle startActivityOptions) {
        return startShortcut(shortcut.getPackageName(), shortcut.getId(), sourceBounds, startActivityOptions, shortcut.getUserId());
    }

    private boolean startShortcut(String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, int userId) {
        try {
            return this.mService.startShortcut(this.mContext.getPackageName(), packageName, shortcutId, sourceBounds, startActivityOptions, userId);
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
                    boolean addedFirstCallback = this.mCallbacks.size() == 0 ? true : DEBUG;
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
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            if (((CallbackMessageHandler) this.mCallbacks.get(i)).mCallback == callback) {
                return i;
            }
        }
        return -1;
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
}
