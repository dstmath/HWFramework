package com.huawei.server.magicwin;

import com.android.server.wm.ActivityStackEx;
import com.android.server.wm.TaskRecordEx;
import java.util.Set;

public class DefaultHwMagicWinCombineManager {
    private static DefaultHwMagicWinCombineManager sInstance = new DefaultHwMagicWinCombineManager();

    public static DefaultHwMagicWinCombineManager getInstance() {
        return sInstance;
    }

    public void removeStackReferenceIfNeeded(ActivityStackEx stack) {
    }

    public String getTaskPackageName(TaskRecordEx taskRecord) {
        return null;
    }

    public int[] getForegroundTaskIds(String packageName, int userId) {
        return new int[0];
    }

    public Set<Integer> getSplitScreenStackIds(String packageName, int userId) {
        return null;
    }

    public boolean isForegroundTaskIds(int[] foregroundTaskIds, int taskId) {
        return false;
    }

    public void clearSplitScreenList(String pkgName, int userId) {
    }
}
