package com.android.server.wm;

import com.huawei.server.magicwin.DefaultHwMagicWinCombineManager;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HwMagicWinCombineManagerImpl extends DefaultHwMagicWinCombineManager {
    private static final int DEFAULT_CAPACITY = 2;
    private static final String INVALID_KEY = "INVALID_KEY";
    private static final String TAG = "HWMW_HwMagicWinCombineManager";
    private static volatile HwMagicWinCombineManagerImpl sInstance = new HwMagicWinCombineManagerImpl();
    private final ConcurrentHashMap<String, CombineInfo> mCombineMap = new ConcurrentHashMap<>(2);
    private final Object mLock = new Object();

    private HwMagicWinCombineManagerImpl() {
    }

    public static HwMagicWinCombineManagerImpl getInstance() {
        return sInstance;
    }

    private TaskRecordEx getRecentsTaskWithStack(ActivityStackEx stack) {
        RecentTasksEx recentTasks = stack.getActivityTaskManagerServiceEx().getRecentTasksEx();
        int recentsCount = recentTasks.getRawTasksSize();
        for (int i = 0; i < recentsCount; i++) {
            TaskRecordEx tr = recentTasks.getRawTaskOfIndex(i);
            if (tr != null && tr.equalsStackId(stack)) {
                return tr;
            }
        }
        return null;
    }

    private TaskRecordEx getRecentsRemoveTask(TaskRecordEx primaryTask, TaskRecordEx secondaryTask) {
        RecentTasksEx recentTasks = primaryTask.getActivityTaskManagerServiceEx().getRecentTasksEx();
        int recentsCount = recentTasks.getRawTasksSize();
        for (int i = 0; i < recentsCount; i++) {
            TaskRecordEx tr = recentTasks.getRawTaskOfIndex(i);
            if (!primaryTask.equalsTaskRecord(tr) && !secondaryTask.equalsTaskRecord(tr) && primaryTask.getUserId() == tr.getUserId() && secondaryTask.getUserId() == tr.getUserId() && ((primaryTask.getAffinity() != null && primaryTask.getAffinity().equals(tr.getAffinity())) || (secondaryTask.getAffinity() != null && secondaryTask.getAffinity().equals(tr.getAffinity())))) {
                return tr;
            }
        }
        return null;
    }

    private void removeTaskFromRecentsIfNeeded(ActivityStackEx primaryStack, ActivityStackEx secondaryStack) {
        if (primaryStack != null && secondaryStack != null && primaryStack.topTask() != null && secondaryStack.topTask() != null) {
            TaskRecordEx primaryTask = primaryStack.topTask();
            TaskRecordEx secondaryTask = secondaryStack.topTask();
            if (primaryTask.getAffinity() == null || !primaryTask.getAffinity().equals(secondaryTask.getAffinity())) {
                TaskRecordEx removingTask = getRecentsRemoveTask(primaryTask, secondaryTask);
                if (removingTask != null) {
                    primaryStack.getActivityTaskManagerServiceEx().getRecentTasksEx().removeTaskRecord(removingTask);
                    return;
                }
                return;
            }
            secondaryTask.getActivityTaskManagerServiceEx().getRecentTasksEx().removeTaskRecord(secondaryTask);
            secondaryTask.getActivityTaskManagerServiceEx().getRecentTasksEx().addTaskRecord(secondaryTask);
        }
    }

    private void addTaskToRecentsIfNeeded(ActivityStackEx primaryStack, ActivityStackEx secondaryStack) {
        if (primaryStack != null && secondaryStack != null && primaryStack.topTask() != null && secondaryStack.topTask() != null) {
            TaskRecordEx primaryTask = primaryStack.topTask();
            TaskRecordEx secondaryTask = secondaryStack.topTask();
            if (!primaryTask.inRecents()) {
                primaryTask.getActivityTaskManagerServiceEx().getRecentTasksEx().addTaskRecord(primaryTask);
            }
            if (!secondaryTask.inRecents()) {
                secondaryTask.getActivityTaskManagerServiceEx().getRecentTasksEx().addTaskRecord(secondaryTask);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0010  */
    private String getCombinePkgName(ActivityStackEx stack) {
        for (String key : this.mCombineMap.keySet()) {
            CombineInfo info = this.mCombineMap.get(key);
            if (stack.equalsStack(info.mPrimaryStack) || stack.equalsStack(info.mSecondaryStack)) {
                return key;
            }
            while (r1.hasNext()) {
            }
        }
        return null;
    }

    private void removeStack(final ActivityTaskManagerServiceEx service, final Set<Integer> stackIds) {
        service.getMH().post(new Runnable() {
            /* class com.android.server.wm.HwMagicWinCombineManagerImpl.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                for (Integer num : stackIds) {
                    service.removeStack(num.intValue());
                }
            }
        });
    }

    private String getUserPkgName(String pkgName, int userId) {
        if (pkgName == null) {
            return INVALID_KEY;
        }
        return pkgName + String.valueOf(userId);
    }

    public void removeStackReferenceIfNeeded(ActivityStackEx stack) {
        CombineInfo combineInfo;
        String userPkgName = getCombinePkgName(stack);
        if (userPkgName != null && (combineInfo = this.mCombineMap.get(userPkgName)) != null && !combineInfo.mSplitScreenStackIds.isEmpty()) {
            if (combineInfo.mPrimaryStack.equalsStack(stack)) {
                combineInfo.mPrimaryStack = null;
            } else if (combineInfo.mSecondaryStack.equalsStack(stack)) {
                combineInfo.mSecondaryStack = null;
            } else {
                return;
            }
            synchronized (this.mLock) {
                combineInfo.mSplitScreenStackIds.remove(Integer.valueOf(stack.getStackId()));
            }
            if (combineInfo.mPrimaryStack == null && combineInfo.mSecondaryStack == null) {
                Set<Integer> stackIds = new HashSet<>(combineInfo.mSplitScreenStackIds);
                this.mCombineMap.remove(userPkgName);
                removeStack(stack.getActivityTaskManagerServiceEx(), stackIds);
                return;
            }
            TaskRecordEx taskRecordEx = getRecentsTaskWithStack(stack);
            if (taskRecordEx != null) {
                stack.getActivityTaskManagerServiceEx().getRecentTasksEx().removeTaskRecord(taskRecordEx);
            }
            if (combineInfo.mSplitScreenStackIds.size() < 2) {
                this.mCombineMap.remove(userPkgName);
            }
        }
    }

    public String getTaskPackageName(TaskRecordEx taskRecord) {
        if (taskRecord == null) {
            return null;
        }
        if (taskRecord.getOrigActivity() != null) {
            return taskRecord.getOrigActivity().getPackageName();
        }
        if (taskRecord.getRealActivity() != null) {
            return taskRecord.getRealActivity().getPackageName();
        }
        if (taskRecord.getTopActivity() != null) {
            return taskRecord.getTopActivity().getPackageName();
        }
        return null;
    }

    public boolean isForegroundTaskIds(int[] foregroundTaskIds, int taskId) {
        if (foregroundTaskIds == null) {
            return false;
        }
        if (foregroundTaskIds[0] == taskId || foregroundTaskIds[1] == taskId) {
            return true;
        }
        return false;
    }

    public int[] getForegroundTaskIds(String packageName, int userId) {
        CombineInfo combineInfo = this.mCombineMap.get(getUserPkgName(packageName, userId));
        if (combineInfo == null || combineInfo.mPrimaryStack == null || combineInfo.mSecondaryStack == null || combineInfo.mPrimaryStack.topTask() == null || combineInfo.mSecondaryStack.topTask() == null) {
            return null;
        }
        return new int[]{combineInfo.mPrimaryStack.topTask().getTaskId(), combineInfo.mSecondaryStack.topTask().getTaskId()};
    }

    public Set<Integer> getSplitScreenStackIds(String packageName, int userId) {
        CombineInfo combineInfo = this.mCombineMap.get(getUserPkgName(packageName, userId));
        if (combineInfo == null || combineInfo.mSplitScreenStackIds.isEmpty() || getForegroundTaskIds(packageName, userId) == null) {
            return null;
        }
        return combineInfo.mSplitScreenStackIds;
    }

    public void updateForegroundTaskIds(String pkgName, int userId, HwMagicWinSplitManager splitManager, int displayId) {
        CombineInfo combineInfo = this.mCombineMap.get(getUserPkgName(pkgName, userId));
        if (combineInfo != null) {
            ActivityStackEx primaryStack = splitManager.getTopMwStackByPosition(1, pkgName, userId, displayId);
            ActivityStackEx secondaryStack = splitManager.getTopMwStackByPosition(2, pkgName, userId, displayId);
            if (primaryStack != null && secondaryStack != null) {
                combineInfo.mPrimaryStack = primaryStack;
                combineInfo.mSecondaryStack = secondaryStack;
                addTaskToRecentsIfNeeded(primaryStack, secondaryStack);
            }
        }
    }

    public void addStackToSplitScreenList(ActivityStackEx stack, int position, String pkgName, int userId) {
        String userPkgName = getUserPkgName(pkgName, userId);
        CombineInfo combineInfo = this.mCombineMap.get(userPkgName);
        if (combineInfo == null) {
            combineInfo = new CombineInfo();
            this.mCombineMap.put(userPkgName, combineInfo);
        }
        synchronized (this.mLock) {
            combineInfo.mSplitScreenStackIds.add(Integer.valueOf(stack.getStackId()));
        }
        if (position == 1) {
            combineInfo.mPrimaryStack = stack;
        } else {
            combineInfo.mSecondaryStack = stack;
        }
        addTaskToRecentsIfNeeded(combineInfo.mPrimaryStack, combineInfo.mSecondaryStack);
    }

    public void clearSplitScreenList(String pkgName, int userId) {
        String userPkgName = getUserPkgName(pkgName, userId);
        CombineInfo combineInfo = this.mCombineMap.get(userPkgName);
        if (combineInfo != null) {
            this.mCombineMap.remove(userPkgName);
            removeTaskFromRecentsIfNeeded(combineInfo.mPrimaryStack, combineInfo.mSecondaryStack);
        }
    }

    public void removeStackFromSplitScreenList(ActivityStackEx stack, String pkgName, HwMagicWinSplitManager splitManager, int userId) {
        CombineInfo combineInfo = this.mCombineMap.get(getUserPkgName(pkgName, userId));
        if (combineInfo != null && !combineInfo.mSplitScreenStackIds.isEmpty()) {
            synchronized (this.mLock) {
                combineInfo.mSplitScreenStackIds.remove(Integer.valueOf(stack.getStackId()));
            }
            updateForegroundTaskIds(pkgName, userId, splitManager, stack.getDisplayId());
        }
    }

    /* access modifiers changed from: package-private */
    public class CombineInfo {
        private ActivityStackEx mPrimaryStack;
        private ActivityStackEx mSecondaryStack;
        private final Set<Integer> mSplitScreenStackIds = new HashSet();

        CombineInfo() {
        }
    }
}
