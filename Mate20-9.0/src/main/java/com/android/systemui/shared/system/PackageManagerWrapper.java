package com.android.systemui.shared.system;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class PackageManagerWrapper {
    public static final String ACTION_PREFERRED_ACTIVITY_CHANGED = "android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED";
    private static final String TAG = "PackageManagerWrapper";
    private static final IPackageManager mIPackageManager = AppGlobals.getPackageManager();
    private static final PackageManagerWrapper sInstance = new PackageManagerWrapper();

    public static PackageManagerWrapper getInstance() {
        return sInstance;
    }

    public ActivityInfo getActivityInfo(ComponentName componentName, int userId) {
        try {
            return mIPackageManager.getActivityInfo(componentName, 128, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ComponentName getHomeActivities(List<ResolveInfo> allHomeCandidates) {
        try {
            return mIPackageManager.getHomeActivities(allHomeCandidates);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isDefaultHomeActivity(String packageName) {
        List<ResolveInfo> allHomeCandidates = new ArrayList<>();
        boolean z = false;
        try {
            ComponentName home = mIPackageManager.getHomeActivities(allHomeCandidates);
            if (home != null && packageName.equals(home.getPackageName())) {
                return true;
            }
            int size = allHomeCandidates.size();
            ComponentName lastComponent = null;
            int lastPriority = Integer.MIN_VALUE;
            for (int i = 0; i < size; i++) {
                ResolveInfo ri = allHomeCandidates.get(i);
                if (ri.priority > lastPriority) {
                    lastComponent = ri.activityInfo.getComponentName();
                    lastPriority = ri.priority;
                } else if (ri.priority == lastPriority) {
                    lastComponent = null;
                }
            }
            if (lastComponent != null && packageName.equals(lastComponent.getPackageName())) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
