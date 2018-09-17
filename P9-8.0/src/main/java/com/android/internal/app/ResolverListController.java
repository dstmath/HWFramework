package com.android.internal.app;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.app.ResolverActivity.DisplayResolveInfo;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import com.android.internal.app.ResolverComparator.AfterCompute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ResolverListController {
    private static final boolean DEBUG = false;
    private static final String TAG = "ResolverListController";
    private final Context mContext;
    private boolean mIsClonedProfile;
    private final int mLaunchedFromUid;
    private final String mReferrerPackage;
    private ResolverComparator mResolverComparator;
    private final Intent mTargetIntent;
    private final PackageManager mpm;

    private class ComputeCallback implements AfterCompute {
        private CountDownLatch mFinishComputeSignal;

        public ComputeCallback(CountDownLatch finishComputeSignal) {
            this.mFinishComputeSignal = finishComputeSignal;
        }

        public void afterCompute() {
            this.mFinishComputeSignal.countDown();
        }
    }

    public ResolverListController(Context context, PackageManager pm, Intent targetIntent, String referrerPackage, int launchedFromUid) {
        this.mContext = context;
        this.mpm = pm;
        this.mLaunchedFromUid = launchedFromUid;
        this.mTargetIntent = targetIntent;
        this.mReferrerPackage = referrerPackage;
        if (this.mContext.getUserId() != 0) {
            this.mIsClonedProfile = ((UserManager) this.mContext.getSystemService("user")).getUserInfo(this.mContext.getUserId()).isClonedProfile();
        }
    }

    public ResolveInfo getLastChosen() throws RemoteException {
        return AppGlobals.getPackageManager().getLastChosenActivity(this.mTargetIntent, this.mTargetIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 65536);
    }

    public void setLastChosen(Intent intent, IntentFilter filter, int match) throws RemoteException {
        AppGlobals.getPackageManager().setLastChosenActivity(intent, intent.resolveType(this.mContext.getContentResolver()), 65536, filter, match, intent.getComponent());
    }

    public List<ResolvedComponentInfo> getResolversForIntent(boolean shouldGetResolvedFilter, boolean shouldGetActivityMetadata, List<Intent> intents) {
        List<ResolvedComponentInfo> resolvedComponents = null;
        int N = intents.size();
        for (int i = 0; i < N; i++) {
            int i2;
            Intent intent = (Intent) intents.get(i);
            PackageManager packageManager = this.mpm;
            if (shouldGetResolvedFilter) {
                i2 = 64;
            } else {
                i2 = 0;
            }
            int i3 = 65536 | i2;
            if (shouldGetActivityMetadata) {
                i2 = 128;
            } else {
                i2 = 0;
            }
            i3 = 8388608 | (i2 | i3);
            if (this.mIsClonedProfile) {
                i2 = 4202496;
            } else {
                i2 = 0;
            }
            List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, i2 | i3);
            for (int j = infos.size() - 1; j >= 0; j--) {
                ResolveInfo info = (ResolveInfo) infos.get(j);
                if (!(info.activityInfo == null || (info.activityInfo.exported ^ 1) == 0)) {
                    infos.remove(j);
                }
            }
            if (infos != null) {
                if (resolvedComponents == null) {
                    resolvedComponents = new ArrayList();
                }
                addResolveListDedupe(resolvedComponents, intent, infos);
            }
        }
        return resolvedComponents;
    }

    public void addResolveListDedupe(List<ResolvedComponentInfo> into, Intent intent, List<ResolveInfo> from) {
        int fromCount = from.size();
        int intoCount = into.size();
        for (int i = 0; i < fromCount; i++) {
            ResolvedComponentInfo rci;
            ResolveInfo newInfo = (ResolveInfo) from.get(i);
            boolean found = false;
            for (int j = 0; j < intoCount; j++) {
                rci = (ResolvedComponentInfo) into.get(j);
                if (isSameResolvedComponent(newInfo, rci)) {
                    found = true;
                    rci.add(intent, newInfo);
                    break;
                }
            }
            if (!found) {
                ComponentName name = new ComponentName(newInfo.activityInfo.packageName, newInfo.activityInfo.name);
                rci = new ResolvedComponentInfo(name, intent, newInfo);
                rci.setPinned(isComponentPinned(name));
                into.add(rci);
            }
        }
    }

    public ArrayList<ResolvedComponentInfo> filterIneligibleActivities(List<ResolvedComponentInfo> inputList, boolean returnCopyOfOriginalListIfModified) {
        ArrayList<ResolvedComponentInfo> listToReturn = null;
        for (int i = inputList.size() - 1; i >= 0; i--) {
            ActivityInfo ai = ((ResolvedComponentInfo) inputList.get(i)).getResolveInfoAt(0).activityInfo;
            int granted = ActivityManager.checkComponentPermission(ai.permission, this.mLaunchedFromUid, ai.applicationInfo.uid, ai.exported);
            boolean suspended = (ai.applicationInfo.flags & 1073741824) != 0;
            if (granted != 0 || suspended || isComponentFiltered(ai.getComponentName())) {
                if (returnCopyOfOriginalListIfModified && listToReturn == null) {
                    listToReturn = new ArrayList(inputList);
                }
                inputList.remove(i);
            }
        }
        return listToReturn;
    }

    public ArrayList<ResolvedComponentInfo> filterLowPriority(List<ResolvedComponentInfo> inputList, boolean returnCopyOfOriginalListIfModified) {
        ArrayList listToReturn = null;
        ResolveInfo r0 = ((ResolvedComponentInfo) inputList.get(0)).getResolveInfoAt(0);
        int N = inputList.size();
        for (int i = 1; i < N; i++) {
            ResolveInfo ri = ((ResolvedComponentInfo) inputList.get(i)).getResolveInfoAt(0);
            if (r0.priority != ri.priority || r0.isDefault != ri.isDefault) {
                while (i < N) {
                    if (returnCopyOfOriginalListIfModified && listToReturn == null) {
                        listToReturn = new ArrayList(inputList);
                    }
                    inputList.remove(i);
                    N--;
                }
            }
        }
        return listToReturn;
    }

    public void sort(List<ResolvedComponentInfo> inputList) {
        CountDownLatch finishComputeSignal = new CountDownLatch(1);
        ComputeCallback callback = new ComputeCallback(finishComputeSignal);
        if (this.mResolverComparator == null) {
            this.mResolverComparator = new ResolverComparator(this.mContext, this.mTargetIntent, this.mReferrerPackage, callback);
        } else {
            this.mResolverComparator.setCallBack(callback);
        }
        try {
            long beforeRank = System.currentTimeMillis();
            this.mResolverComparator.compute(inputList);
            finishComputeSignal.await();
            Collections.sort(inputList, this.mResolverComparator);
            long currentTimeMillis = System.currentTimeMillis();
        } catch (InterruptedException e) {
            Log.e(TAG, "Compute & Sort was interrupted: " + e);
        }
    }

    private static boolean isSameResolvedComponent(ResolveInfo a, ResolvedComponentInfo b) {
        ActivityInfo ai = a.activityInfo;
        if (ai.packageName.equals(b.name.getPackageName())) {
            return ai.name.equals(b.name.getClassName());
        }
        return false;
    }

    boolean isComponentPinned(ComponentName name) {
        return false;
    }

    boolean isComponentFiltered(ComponentName componentName) {
        return false;
    }

    public float getScore(DisplayResolveInfo target) {
        if (this.mResolverComparator == null) {
            return 0.0f;
        }
        return this.mResolverComparator.getScore(target.getResolvedComponentName());
    }

    public void updateModel(ComponentName componentName) {
        if (this.mResolverComparator != null) {
            this.mResolverComparator.updateModel(componentName);
        }
    }

    public void updateChooserCounts(String packageName, int userId, String action) {
        if (this.mResolverComparator != null) {
            this.mResolverComparator.updateChooserCounts(packageName, userId, action);
        }
    }

    public void destroy() {
        if (this.mResolverComparator != null) {
            this.mResolverComparator.destroy();
        }
    }
}
