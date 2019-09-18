package com.android.server.pm;

import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.app.AppOpsManager;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ICrossProfileApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import java.util.ArrayList;
import java.util.List;

public class CrossProfileAppsServiceImpl extends ICrossProfileApps.Stub {
    private static final String TAG = "CrossProfileAppsService";
    private Context mContext;
    private Injector mInjector;

    @VisibleForTesting
    public interface Injector {
        long clearCallingIdentity();

        ActivityManagerInternal getActivityManagerInternal();

        AppOpsManager getAppOpsManager();

        int getCallingUid();

        UserHandle getCallingUserHandle();

        int getCallingUserId();

        PackageManager getPackageManager();

        PackageManagerInternal getPackageManagerInternal();

        UserManager getUserManager();

        void restoreCallingIdentity(long j);
    }

    private static class InjectorImpl implements Injector {
        private Context mContext;

        public InjectorImpl(Context context) {
            this.mContext = context;
        }

        public int getCallingUid() {
            return Binder.getCallingUid();
        }

        public int getCallingUserId() {
            return UserHandle.getCallingUserId();
        }

        public UserHandle getCallingUserHandle() {
            return Binder.getCallingUserHandle();
        }

        public long clearCallingIdentity() {
            return Binder.clearCallingIdentity();
        }

        public void restoreCallingIdentity(long token) {
            Binder.restoreCallingIdentity(token);
        }

        public UserManager getUserManager() {
            return (UserManager) this.mContext.getSystemService(UserManager.class);
        }

        public PackageManagerInternal getPackageManagerInternal() {
            return (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }

        public PackageManager getPackageManager() {
            return this.mContext.getPackageManager();
        }

        public AppOpsManager getAppOpsManager() {
            return (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        }

        public ActivityManagerInternal getActivityManagerInternal() {
            return (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        }
    }

    public CrossProfileAppsServiceImpl(Context context) {
        this(context, new InjectorImpl(context));
    }

    @VisibleForTesting
    CrossProfileAppsServiceImpl(Context context, Injector injector) {
        this.mContext = context;
        this.mInjector = injector;
    }

    public List<UserHandle> getTargetUserProfiles(String callingPackage) {
        Preconditions.checkNotNull(callingPackage);
        verifyCallingPackage(callingPackage);
        return getTargetUserProfilesUnchecked(callingPackage, this.mInjector.getCallingUserId());
    }

    public void startActivityAsUser(IApplicationThread caller, String callingPackage, ComponentName component, UserHandle user) throws RemoteException {
        Preconditions.checkNotNull(callingPackage);
        Preconditions.checkNotNull(component);
        Preconditions.checkNotNull(user);
        verifyCallingPackage(callingPackage);
        if (!getTargetUserProfilesUnchecked(callingPackage, this.mInjector.getCallingUserId()).contains(user)) {
            throw new SecurityException(callingPackage + " cannot access unrelated user " + user.getIdentifier());
        } else if (callingPackage.equals(component.getPackageName())) {
            int callingUid = this.mInjector.getCallingUid();
            Intent launchIntent = new Intent("android.intent.action.MAIN");
            launchIntent.addCategory("android.intent.category.LAUNCHER");
            launchIntent.addFlags(270532608);
            launchIntent.setPackage(component.getPackageName());
            verifyActivityCanHandleIntentAndExported(launchIntent, component, callingUid, user);
            launchIntent.setPackage(null);
            launchIntent.setComponent(component);
            this.mInjector.getActivityManagerInternal().startActivityAsUser(caller, callingPackage, launchIntent, ActivityOptions.makeOpenCrossProfileAppsAnimation().toBundle(), user.getIdentifier());
        } else {
            throw new SecurityException(callingPackage + " attempts to start an activity in other package - " + component.getPackageName());
        }
    }

    private List<UserHandle> getTargetUserProfilesUnchecked(String callingPackage, int callingUserId) {
        long ident = this.mInjector.clearCallingIdentity();
        try {
            int[] enabledProfileIds = this.mInjector.getUserManager().getEnabledProfileIds(callingUserId);
            List<UserHandle> targetProfiles = new ArrayList<>();
            for (int userId : enabledProfileIds) {
                if (userId != callingUserId) {
                    if (isPackageEnabled(callingPackage, userId)) {
                        targetProfiles.add(UserHandle.of(userId));
                    }
                }
            }
            return targetProfiles;
        } finally {
            this.mInjector.restoreCallingIdentity(ident);
        }
    }

    private boolean isPackageEnabled(String packageName, int userId) {
        int callingUid = this.mInjector.getCallingUid();
        long ident = this.mInjector.clearCallingIdentity();
        try {
            PackageInfo info = this.mInjector.getPackageManagerInternal().getPackageInfo(packageName, 786432, callingUid, userId);
            return info != null && info.applicationInfo.enabled;
        } finally {
            this.mInjector.restoreCallingIdentity(ident);
        }
    }

    private void verifyActivityCanHandleIntentAndExported(Intent launchIntent, ComponentName component, int callingUid, UserHandle user) {
        long ident = this.mInjector.clearCallingIdentity();
        try {
            List<ResolveInfo> apps = this.mInjector.getPackageManagerInternal().queryIntentActivities(launchIntent, 786432, callingUid, user.getIdentifier());
            int size = apps.size();
            int i = 0;
            while (i < size) {
                ActivityInfo activityInfo = apps.get(i).activityInfo;
                if (!TextUtils.equals(activityInfo.packageName, component.getPackageName()) || !TextUtils.equals(activityInfo.name, component.getClassName()) || !activityInfo.exported) {
                    i++;
                } else {
                    return;
                }
            }
            throw new SecurityException("Attempt to launch activity without  category Intent.CATEGORY_LAUNCHER or activity is not exported" + component);
        } finally {
            this.mInjector.restoreCallingIdentity(ident);
        }
    }

    private void verifyCallingPackage(String callingPackage) {
        this.mInjector.getAppOpsManager().checkPackage(this.mInjector.getCallingUid(), callingPackage);
    }
}
