package com.android.systemui.shared.system;

import android.app.AppGlobals;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import java.util.List;

public class PackageManagerWrapper {
    public static final String ACTION_PREFERRED_ACTIVITY_CHANGED = "android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED";
    private static final String TAG = "PackageManagerWrapper";
    private static final IPackageManager mIPackageManager = AppGlobals.getPackageManager();
    private static final PackageManagerWrapper sInstance = new PackageManagerWrapper();

    public static PackageManagerWrapper getInstance() {
        return sInstance;
    }

    private PackageManagerWrapper() {
    }

    public ActivityInfo getActivityInfo(ComponentName componentName, int userId) {
        try {
            return mIPackageManager.getActivityInfo(componentName, 128, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get activity info!");
            return null;
        }
    }

    public ComponentName getHomeActivities(List<ResolveInfo> allHomeCandidates) {
        try {
            return mIPackageManager.getHomeActivities(allHomeCandidates);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get home activitys!");
            return null;
        }
    }

    public ResolveInfo resolveActivity(Intent intent, int flags) {
        Application application = AppGlobals.getInitialApplication();
        if (application == null || application.getContentResolver() == null) {
            return null;
        }
        try {
            return mIPackageManager.resolveIntent(intent, intent.resolveTypeIfNeeded(application.getContentResolver()), flags, UserHandle.getCallingUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to resolve activity.");
            return null;
        }
    }
}
