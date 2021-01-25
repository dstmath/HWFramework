package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import com.huawei.android.util.SlogEx;
import com.huawei.server.utils.Utils;
import java.util.ArrayList;

public class HwMagicModeA1An extends HwMagicModeBase {
    private static final String TAG = "HWMW_HwMagicModeA1An";

    public HwMagicModeA1An(HwMagicWinManager manager, HwMagicWinAmsPolicy policy, Context context) {
        super(manager, policy, context);
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public void setActivityBoundByMode(ArrayList<ActivityRecordEx> activities, String pkgName, HwMagicContainer toContainer) {
        if (activities.size() < 1) {
            SlogEx.d(TAG, "there is not any activity in the list, return");
            return;
        }
        HwMagicContainer container = toContainer != null ? toContainer : this.mMwManager.getContainer(activities.get(0));
        if (container != null) {
            ActivityRecordEx homeActivity = getHomePageActivityRecord(activities);
            if (homeActivity == null || homeActivity.equalsActivityRecord(activities.get(0)) || this.mMwManager.isDragFullMode(homeActivity)) {
                super.setActivityBoundByMode(activities, pkgName, container);
                return;
            }
            boolean hasFullscreenActivity = false;
            for (int i = activities.size() - 1; i >= 0; i--) {
                ActivityRecordEx ar = activities.get(i);
                String pkg = Utils.getRealPkgName(ar);
                Rect bounds = container.getBounds(ar.equalsActivityRecord(homeActivity) ? 1 : 2, pkg);
                if (ar.isTopRunningActivity() && this.mPolicy.isKeyguardLockedAndOccluded()) {
                    bounds = container.getBounds(3, pkg);
                }
                ar.setBounds(bounds);
                hasFullscreenActivity = setDefaultFullscreenBounds(ar, hasFullscreenActivity);
            }
        }
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public boolean isMoveActivityToMaster(ActivityRecordEx focus, ActivityRecordEx next, int targetPosition) {
        if (!isSkippingMoveToMaster(focus, next) && targetPosition == 2 && this.mMwManager.isMiddle(focus)) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.wm.HwMagicModeBase
    public boolean isExitSliding(ActivityRecordEx finishActivity, ActivityRecordEx secondSlaveActivity, String finishReason) {
        if (secondSlaveActivity == null) {
            return false;
        }
        return this.mMwManager.isSlave(finishActivity);
    }
}
