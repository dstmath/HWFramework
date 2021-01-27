package com.android.server.wm;

import android.app.TaskInfoEx;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.HwMwUtils;
import com.huawei.android.content.res.ConfigurationAdapter;
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

    public void updateMagicWindowTaskInfo(TaskRecordEx taskRecord, TaskInfoEx info) {
        int[] splitScreenTaskIds;
        if (taskRecord != null && taskRecord.getActivityStack() != null && info != null && !info.isEmpty() && HwMwUtils.ENABLED && taskRecord.getActivityStack().inHwMagicWindowingMode() && (splitScreenTaskIds = HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinCombineManager().getForegroundTaskIds(HwPartMagicWindowServiceFactory.getInstance().getHwPartMagicWindowServiceFactoryImpl().getHwMagicWinCombineManager().getTaskPackageName(taskRecord), taskRecord.getUserId())) != null) {
            char c = 0;
            boolean isCurrentRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
            int primaryTaskId = splitScreenTaskIds[isCurrentRtl ? (char) 1 : 0];
            if (!isCurrentRtl) {
                c = 1;
            }
            int seconrdaryTaskId = splitScreenTaskIds[c];
            if (taskRecord.getTaskId() == primaryTaskId || taskRecord.getTaskId() == seconrdaryTaskId) {
                info.setCombinedTaskIds(splitScreenTaskIds);
                info.setWindowMode(taskRecord.getTaskId() == primaryTaskId ? 100 : 101);
                info.setBounds(new Rect(taskRecord.getActivityStack().getBounds()));
                info.setSupportsSplitScreenMultiWindow(true);
                if (info.getConfiguration() != null) {
                    ConfigurationAdapter.setWindowingMode(info.getConfiguration(), info.getWindowMode());
                }
            }
        }
    }
}
