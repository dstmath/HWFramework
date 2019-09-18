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
import android.content.pm.UserInfo;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.ResolverActivity;
import com.android.internal.app.ResolverComparator;
import com.android.internal.util.Protocol;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ResolverListController {
    private static final boolean DEBUG = false;
    private static final String TAG = "ResolverListController";
    private boolean isComputed = false;
    private final Context mContext;
    private boolean mIsClonedProfile;
    private final int mLaunchedFromUid;
    private final String mReferrerPackage;
    private ResolverComparator mResolverComparator;
    private final Intent mTargetIntent;
    private final PackageManager mpm;

    private class ComputeCallback implements ResolverComparator.AfterCompute {
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
        this.mResolverComparator = new ResolverComparator(this.mContext, this.mTargetIntent, this.mReferrerPackage, null);
        if (this.mContext.getUserId() != 0) {
            UserManager userManager = (UserManager) this.mContext.getSystemService("user");
            if (userManager != null) {
                UserInfo userInfo = userManager.getUserInfo(this.mContext.getUserId());
                if (userInfo != null) {
                    this.mIsClonedProfile = userInfo.isClonedProfile();
                }
            }
        }
    }

    @VisibleForTesting
    public ResolveInfo getLastChosen() throws RemoteException {
        return AppGlobals.getPackageManager().getLastChosenActivity(this.mTargetIntent, this.mTargetIntent.resolveTypeIfNeeded(this.mContext.getContentResolver()), Protocol.BASE_SYSTEM_RESERVED);
    }

    @VisibleForTesting
    public void setLastChosen(Intent intent, IntentFilter filter, int match) throws RemoteException {
        AppGlobals.getPackageManager().setLastChosenActivity(intent, intent.resolveType(this.mContext.getContentResolver()), Protocol.BASE_SYSTEM_RESERVED, filter, match, intent.getComponent());
    }

    @VisibleForTesting
    public List<ResolverActivity.ResolvedComponentInfo> getResolversForIntent(boolean shouldGetResolvedFilter, boolean shouldGetActivityMetadata, List<Intent> intents) {
        List<ResolverActivity.ResolvedComponentInfo> resolvedComponents = null;
        int N = intents.size();
        for (int i = 0; i < N; i++) {
            Intent intent = intents.get(i);
            int i2 = 0;
            int i3 = 65536 | (shouldGetResolvedFilter ? 64 : 0);
            if (shouldGetActivityMetadata) {
                i2 = 128;
            }
            int flags = i3 | i2;
            if (intent.isWebIntent() || (intent.getFlags() & 2048) != 0) {
                flags |= 8388608;
            }
            if (this.mIsClonedProfile) {
                flags |= 4202496;
            }
            List<ResolveInfo> infos = this.mpm.queryIntentActivities(intent, flags);
            for (int j = infos.size() - 1; j >= 0; j--) {
                ResolveInfo info = infos.get(j);
                if (info.activityInfo != null && !info.activityInfo.exported) {
                    infos.remove(j);
                }
            }
            if (infos != null) {
                if (resolvedComponents == null) {
                    resolvedComponents = new ArrayList<>();
                }
                addResolveListDedupe(resolvedComponents, intent, infos);
            }
        }
        return resolvedComponents;
    }

    @VisibleForTesting
    public void addResolveListDedupe(List<ResolverActivity.ResolvedComponentInfo> into, Intent intent, List<ResolveInfo> from) {
        int fromCount = from.size();
        int intoCount = into.size();
        for (int i = 0; i < fromCount; i++) {
            ResolveInfo newInfo = from.get(i);
            boolean found = false;
            int j = 0;
            while (true) {
                if (j >= intoCount) {
                    break;
                }
                ResolverActivity.ResolvedComponentInfo rci = into.get(j);
                if (isSameResolvedComponent(newInfo, rci)) {
                    found = true;
                    rci.add(intent, newInfo);
                    break;
                }
                j++;
            }
            if (!found) {
                ComponentName name = new ComponentName(newInfo.activityInfo.packageName, newInfo.activityInfo.name);
                ResolverActivity.ResolvedComponentInfo rci2 = new ResolverActivity.ResolvedComponentInfo(name, intent, newInfo);
                rci2.setPinned(isComponentPinned(name));
                into.add(rci2);
            }
        }
    }

    @VisibleForTesting
    public ArrayList<ResolverActivity.ResolvedComponentInfo> filterIneligibleActivities(List<ResolverActivity.ResolvedComponentInfo> inputList, boolean returnCopyOfOriginalListIfModified) {
        ArrayList<ResolverActivity.ResolvedComponentInfo> listToReturn = null;
        for (int i = inputList.size() - 1; i >= 0; i--) {
            boolean suspended = false;
            ActivityInfo ai = inputList.get(i).getResolveInfoAt(0).activityInfo;
            int granted = ActivityManager.checkComponentPermission(ai.permission, this.mLaunchedFromUid, ai.applicationInfo.uid, ai.exported);
            if ((ai.applicationInfo.flags & 1073741824) != 0) {
                suspended = true;
            }
            if (granted != 0 || suspended || isComponentFiltered(ai.getComponentName())) {
                if (returnCopyOfOriginalListIfModified && listToReturn == null) {
                    listToReturn = new ArrayList<>(inputList);
                }
                inputList.remove(i);
            }
        }
        return listToReturn;
    }

    @VisibleForTesting
    public ArrayList<ResolverActivity.ResolvedComponentInfo> filterLowPriority(List<ResolverActivity.ResolvedComponentInfo> inputList, boolean returnCopyOfOriginalListIfModified) {
        ArrayList<ResolverActivity.ResolvedComponentInfo> listToReturn = null;
        ResolveInfo r0 = inputList.get(0).getResolveInfoAt(0);
        int N = inputList.size();
        for (int i = 1; i < N; i++) {
            ResolveInfo ri = inputList.get(i).getResolveInfoAt(0);
            if (r0.priority != ri.priority || r0.isDefault != ri.isDefault) {
                while (i < N) {
                    if (returnCopyOfOriginalListIfModified && listToReturn == null) {
                        listToReturn = new ArrayList<>(inputList);
                    }
                    inputList.remove(i);
                    N--;
                }
            }
        }
        return listToReturn;
    }

    @VisibleForTesting
    public void sort(List<ResolverActivity.ResolvedComponentInfo> inputList) {
        if (this.mResolverComparator == null) {
            Log.d(TAG, "Comparator has already been destroyed; skipped.");
            return;
        }
        try {
            long currentTimeMillis = System.currentTimeMillis();
            if (!this.isComputed) {
                CountDownLatch finishComputeSignal = new CountDownLatch(1);
                this.mResolverComparator.setCallBack(new ComputeCallback(finishComputeSignal));
                this.mResolverComparator.compute(inputList);
                finishComputeSignal.await();
                this.isComputed = true;
            }
            Collections.sort(inputList, this.mResolverComparator);
            System.currentTimeMillis();
        } catch (InterruptedException e) {
            Log.e(TAG, "Compute & Sort was interrupted: " + e);
        }
    }

    private static boolean isSameResolvedComponent(ResolveInfo a, ResolverActivity.ResolvedComponentInfo b) {
        ActivityInfo ai = a.activityInfo;
        return ai.packageName.equals(b.name.getPackageName()) && ai.name.equals(b.name.getClassName());
    }

    /* access modifiers changed from: package-private */
    public boolean isComponentPinned(ComponentName name) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isComponentFiltered(ComponentName componentName) {
        return false;
    }

    @VisibleForTesting
    public float getScore(ResolverActivity.DisplayResolveInfo target) {
        return this.mResolverComparator.getScore(target.getResolvedComponentName());
    }

    public void updateModel(ComponentName componentName) {
        this.mResolverComparator.updateModel(componentName);
    }

    public void updateChooserCounts(String packageName, int userId, String action) {
        this.mResolverComparator.updateChooserCounts(packageName, userId, action);
    }

    public void destroy() {
        this.mResolverComparator.destroy();
    }
}
