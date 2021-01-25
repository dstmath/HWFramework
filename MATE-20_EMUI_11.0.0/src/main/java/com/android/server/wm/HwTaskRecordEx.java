package com.android.server.wm;

import android.app.TaskInfo;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.HwMwUtils;
import com.huawei.server.HwPartMagicWindowServiceFactory;
import java.util.ArrayList;
import java.util.Locale;

public class HwTaskRecordEx extends HwTaskRecordExBridgeEx {
    private static final String TAG = "HwActivityRecordEx";

    public void forceNewConfigWhenReuseActivity(ArrayList<ActivityRecordEx> activityRecordExes) {
        for (int activityNdx = activityRecordExes.size() - 1; activityNdx >= 0; activityNdx--) {
            activityRecordExes.get(activityNdx).setForceNewConfig(true);
        }
    }

    public void updateMagicWindowTaskInfo(TaskRecordEx taskRecord, TaskInfo info) {
        int[] splitScreenTaskIds;
        char c;
        int i;
        if (taskRecord != null && taskRecord.getActivityStack() != null && info != null && HwMwUtils.ENABLED && taskRecord.getActivityStack().inHwMagicWindowingMode() && (splitScreenTaskIds = HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinCombineManager().getForegroundTaskIds(HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinCombineManager().getTaskPackageName(taskRecord), taskRecord.getUserId())) != null) {
            char c2 = 0;
            boolean isCurrentRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
            if (isCurrentRtl) {
                c = 1;
            } else {
                c = 0;
            }
            int primaryTaskId = splitScreenTaskIds[c];
            if (!isCurrentRtl) {
                c2 = 1;
            }
            int seconrdaryTaskId = splitScreenTaskIds[c2];
            if (taskRecord.getTaskId() == primaryTaskId || taskRecord.getTaskId() == seconrdaryTaskId) {
                info.combinedTaskIds = splitScreenTaskIds;
                if (taskRecord.getTaskId() == primaryTaskId) {
                    i = 100;
                } else {
                    i = 101;
                }
                info.windowMode = i;
                info.bounds = new Rect(taskRecord.getActivityStack().getBounds());
                info.supportsSplitScreenMultiWindow = true;
                info.configuration.windowConfiguration.setWindowingMode(info.windowMode);
            }
        }
    }
}
