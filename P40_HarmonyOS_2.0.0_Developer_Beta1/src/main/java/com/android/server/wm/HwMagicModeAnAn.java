package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import com.huawei.android.util.SlogEx;
import com.huawei.server.utils.Utils;
import java.util.ArrayList;

public class HwMagicModeAnAn extends HwMagicModeBase {
    private static final String TAG = "HWMW_HwMagicModeAnAn";

    public HwMagicModeAnAn(HwMagicWinManager manager, HwMagicWinAmsPolicy policy, Context context) {
        super(manager, policy, context);
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public boolean shouldEnterMagicWinForTah(ActivityRecordEx focus, ActivityRecordEx next) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (this.mPolicy.isDefaultFullscreenActivity(container, next) || this.mPolicy.isSpecTransActivityPreDefined(container, next)) {
            return false;
        }
        boolean isHome = this.mMwManager.getAmsPolicy().isHomeActivity(container, focus);
        boolean isEgnoreHome = this.mPolicy.isEnterDoubleWindowIgnoreHome(container, Utils.getRealPkgName(next));
        if ((!isHome || isEgnoreHome) && (!isEgnoreHome || !isAnyNormalActvityExist(container, focus, next))) {
            return false;
        }
        return true;
    }

    private boolean isAnyNormalActvityExist(HwMagicContainer container, ActivityRecordEx focus, ActivityRecordEx next) {
        for (ActivityRecordEx activity : this.mPolicy.getAllActivities(focus.getActivityStackEx())) {
            if (!(this.mPolicy.isSpecTransActivity(container, focus) || activity.equalsActivityRecord(next) || activity.isFinishing())) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void setLeftTopActivityToPause(ActivityRecordEx focus) {
        ActivityRecordEx masterTop = this.mPolicy.getActivityByPosition(focus, 1, 0);
        if (masterTop != null) {
            this.mPolicy.setMagicWindowToPause(masterTop);
        }
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public boolean isMoveActivityToMaster(ActivityRecordEx focus, ActivityRecordEx next, int targetPosition) {
        boolean lockMaster = isLockMaster(focus);
        if (isSkippingMoveToMaster(focus, next) || !next.isFullscreen() || lockMaster || targetPosition != 2) {
            return false;
        }
        if (this.mMwManager.isMiddle(focus) || this.mMwManager.isSlave(focus)) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void adjustWindowForFinish(ActivityRecordEx activity, String finishReason) {
        if (!isNonFullScreen(activity) || !this.mMwManager.isMaster(activity) || this.mPolicy.getActivityByPosition(activity, 1, 1) == null) {
            adjustWindowForDoubleWindows(activity, finishReason);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.HwMagicModeBase
    public ActivityRecordEx getHomePageActivityRecord(ArrayList<ActivityRecordEx> activities) {
        HwMagicContainer container;
        if (activities.size() < 1 || (container = this.mMwManager.getContainer(activities.get(0))) == null) {
            return null;
        }
        if (!this.mPolicy.isSupportMainRelatedMode(container, activities.get(0))) {
            return super.getHomePageActivityRecord(activities);
        }
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityRecordEx activity = activities.get(i);
            if (!activity.isFinishing() && (this.mPolicy.isMainActivity(container, activity) || !this.mPolicy.isSpecTransActivity(container, activity))) {
                return activity;
            }
        }
        return null;
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void setActivityBoundByMode(ArrayList<ActivityRecordEx> activities, String pkgName, HwMagicContainer toContainer) {
        if (activities.size() < 1) {
            SlogEx.d(TAG, "there is not any activity in the list, return");
            return;
        }
        HwMagicContainer container = toContainer != null ? toContainer : this.mMwManager.getContainer(activities.get(0));
        if (container != null) {
            int index = getLockPageIndex(activities);
            setActivityBoundMainRelatedIfNeed(activities, pkgName, toContainer);
            ActivityRecordEx homeActivity = getHomePageActivityRecord(activities);
            if (homeActivity == null || homeActivity.equalsActivityRecord(activities.get(0)) || this.mMwManager.isDragFullMode(homeActivity)) {
                super.setActivityBoundByMode(activities, pkgName, toContainer);
                return;
            }
            boolean hasFullscreenActivity = false;
            int i = activities.size() - 1;
            while (true) {
                int i2 = 2;
                if (i < 0) {
                    break;
                }
                ActivityRecordEx ar = activities.get(i);
                String pkg = Utils.getRealPkgName(ar);
                boolean isTop = i == 0;
                int pos = isTop ? 2 : 1;
                if (index != -1) {
                    if (i >= index) {
                        i2 = 1;
                    }
                    pos = i2;
                }
                Rect bounds = container.getBounds(pos, pkg);
                if (isTop && this.mPolicy.isKeyguardLockedAndOccluded()) {
                    bounds = container.getBounds(3, pkg);
                }
                ar.setBounds(bounds);
                hasFullscreenActivity = setDefaultFullscreenBounds(ar, hasFullscreenActivity);
                i--;
            }
            for (int n = 0; n <= activities.size() - 1; n++) {
                ActivityRecordEx ar2 = activities.get(n);
                if (n == 0 && !this.mMwManager.isSlave(ar2)) {
                    return;
                }
                if (!this.mMwManager.isMaster(ar2) || (this.mPolicy.isSpecTransActivity(container, ar2) && !homeActivity.equalsActivityRecord(ar2))) {
                    ar2.setBounds(container.getBounds(2, Utils.getRealPkgName(ar2)));
                } else {
                    return;
                }
            }
        }
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public int getTargetWindowPosition(ActivityRecordEx focus, ActivityRecordEx next) {
        if (!isNonFullScreen(next)) {
            return super.getTargetWindowPosition(focus, next);
        }
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (container == null) {
            return 3;
        }
        if (this.mPolicy.isDefaultFullscreenActivity(container, next)) {
            return 5;
        }
        return container.getBoundsPosition(focus.getRequestedOverrideBounds());
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void moveOtherActivities(ActivityRecordEx movedActivity, int currentPosition) {
        if (isNonFullScreen(movedActivity)) {
            if (currentPosition != 1 || this.mPolicy.getActivityByPosition(movedActivity, 1, 1) != null) {
                ActivityRecordEx needMoveActivity = this.mPolicy.getActivityByPosition(movedActivity, currentPosition, 0);
                HwMagicContainer container = this.mMwManager.getContainer(needMoveActivity);
                if (needMoveActivity != null && !this.mPolicy.isRelatedActivity(container, needMoveActivity) && !this.mPolicy.isMainActivity(container, needMoveActivity)) {
                    int targetPosition = currentPosition == 1 ? 2 : 1;
                    if (targetPosition == 1) {
                        needMoveActivity.getTaskRecordEx().moveActivityToFrontLocked(needMoveActivity);
                        needMoveActivity.getTaskRecordEx().moveActivityToFrontLocked(movedActivity);
                    }
                    this.mPolicy.moveWindow(needMoveActivity, targetPosition);
                }
            }
        }
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void finishRightAfterFinishingLeft(ActivityRecordEx finishActivity) {
        if (!isNonFullScreen(finishActivity) || this.mPolicy.getActivityByPosition(finishActivity, 1, 1) == null) {
            super.finishRightAfterFinishingLeft(finishActivity);
        }
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public boolean isMastersFinish(ActivityRecordEx finishActivity, String finishReason) {
        if (((!HwMagicWinAmsPolicy.MAGIC_WINDOW_FINISH_EVENT.equals(finishReason) || !this.mMwManager.isSlave(finishActivity)) && (!this.mMwManager.isMaster(finishActivity) || isNonFullScreen(finishActivity))) || this.mPolicy.getActivityByPosition(finishActivity, 1, 2) == null) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public boolean isExitSliding(ActivityRecordEx finishActivity, ActivityRecordEx secondSlaveActivity, String finishReason) {
        if (secondSlaveActivity == null || HwMagicWinAmsPolicy.MAGIC_WINDOW_FINISH_EVENT.equals(finishReason) || !this.mMwManager.isSlave(finishActivity) || this.mPolicy.getActivityByPosition(finishActivity, 1, 1) != null) {
            return false;
        }
        return true;
    }
}
