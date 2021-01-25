package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.WindowConfiguration;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.BadParcelableException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.server.am.ProcessListEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HwFreeFormAssistant {
    private static final String DUMP_APP_BOUNDS_RECORDS = "hwfreeform_app_records";
    private static final String DUMP_HW_FREE_FORM_PREFIX = "hwfreeform";
    private static final String DUMP_SET_RECORDS_LIMITS = "hwfreeform_set_records_limits";
    private static final String DUMP_TASK_BOUNDS_RECORDS = "hwfreeform_task_records";
    private static final int INVALID_VALUE = -1;
    private static final float INVALID_VALUE_FLOAT = -1.0f;
    private static final Object M_LOCK = new Object();
    private static final String TAG = "HwFreeFormAssistant";
    private static volatile HwFreeFormAssistant sSingleInstance = null;
    private final LinkedHashMap<String, HwFreeFormBoundsRecord> mAppBoundsRecords = new LinkedHashMap<>();
    private final BroadcastReceiver mPackageRemovedReceiver = new BroadcastReceiver() {
        /* class com.android.server.wm.HwFreeFormAssistant.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && intent.getAction() != null && "android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                String packageName = null;
                int userId = ProcessListEx.INVALID_ADJ;
                try {
                    if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                        packageName = intent.getData().getEncodedSchemeSpecificPart();
                        userId = intent.getIntExtra("android.intent.extra.user_handle", ProcessListEx.INVALID_ADJ);
                        if (!TextUtils.isEmpty(packageName) && userId != -10000) {
                            HwFreeFormAssistant.this.mAppBoundsRecords.remove(HwFreeFormAssistant.this.getPackageKey(packageName, userId));
                        }
                    }
                } catch (BadParcelableException e) {
                    Slog.i(HwFreeFormAssistant.TAG, "mPackageRemovedReceiver error.");
                }
            }
        }
    };
    private int mRecordsLimits = 300;
    private final ActivityTaskManagerService mService;
    private final LinkedHashMap<Integer, HwFreeFormBoundsRecord> mTaskBoundsRecords = new LinkedHashMap<>();
    private final BroadcastReceiver mUserRemovedReceiver = new BroadcastReceiver() {
        /* class com.android.server.wm.HwFreeFormAssistant.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && intent.getAction() != null && "android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                int userId = ProcessListEx.INVALID_ADJ;
                try {
                    userId = intent.getIntExtra("android.intent.extra.user_handle", ProcessListEx.INVALID_ADJ);
                } catch (BadParcelableException e) {
                    Slog.i(HwFreeFormAssistant.TAG, "mUserRemovedReceiver error.");
                }
                if (userId > 0) {
                    HwFreeFormAssistant hwFreeFormAssistant = HwFreeFormAssistant.this;
                    hwFreeFormAssistant.onUserRemoved(hwFreeFormAssistant.mAppBoundsRecords, userId);
                    HwFreeFormAssistant hwFreeFormAssistant2 = HwFreeFormAssistant.this;
                    hwFreeFormAssistant2.onUserRemoved(hwFreeFormAssistant2.mTaskBoundsRecords, userId);
                }
            }
        }
    };

    /* access modifiers changed from: private */
    public class HwFreeFormBoundsRecord {
        Rect bounds;
        int displayHeight;
        int displayId;
        int displayWidth;
        final String packageName;
        float stackScale;
        final int taskId;
        final int userId;

        HwFreeFormBoundsRecord(String packageName2, int userId2, int taskId2) {
            this.packageName = packageName2;
            this.userId = userId2;
            this.taskId = taskId2;
        }

        /* access modifiers changed from: package-private */
        public boolean isValidRecord() {
            String str = this.packageName;
            return str != null && !str.isEmpty() && this.userId >= 0 && this.taskId >= 0;
        }

        /* access modifiers changed from: package-private */
        public void setBounds(Rect bounds2, float stackScale2) {
            this.bounds = bounds2;
            this.stackScale = stackScale2;
        }

        /* access modifiers changed from: package-private */
        public void setDisplayInfo(int displayId2, int displayWidth2, int displayHeight2) {
            this.displayId = displayId2;
            this.displayWidth = displayWidth2;
            this.displayHeight = displayHeight2;
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.bounds = new Rect();
            this.stackScale = HwFreeFormAssistant.INVALID_VALUE_FLOAT;
            this.displayId = -1;
            this.displayWidth = -1;
            this.displayHeight = -1;
        }
    }

    public static HwFreeFormAssistant getInstance(ActivityTaskManagerService service) {
        if (sSingleInstance == null) {
            synchronized (M_LOCK) {
                if (sSingleInstance == null) {
                    sSingleInstance = new HwFreeFormAssistant(service);
                }
            }
        }
        return sSingleInstance;
    }

    private HwFreeFormAssistant(ActivityTaskManagerService service) {
        this.mService = service;
    }

    public void onSystemReady() {
        IntentFilter packageActionFilter = new IntentFilter();
        packageActionFilter.addDataScheme("package");
        packageActionFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        this.mService.mContext.registerReceiverAsUser(this.mPackageRemovedReceiver, UserHandle.ALL, packageActionFilter, null, null);
        IntentFilter userActionFilter = new IntentFilter();
        userActionFilter.addAction("android.intent.action.USER_REMOVED");
        this.mService.mContext.registerReceiverAsUser(this.mUserRemovedReceiver, UserHandle.ALL, userActionFilter, null, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T> void onUserRemoved(LinkedHashMap<T, HwFreeFormBoundsRecord> records, int userId) {
        Iterator<Map.Entry<T, HwFreeFormBoundsRecord>> it = records.entrySet().iterator();
        while (it.hasNext()) {
            HwFreeFormBoundsRecord item = it.next().getValue();
            if (item != null && item.userId == userId) {
                it.remove();
            }
        }
    }

    public void recordHwFreeFormBounds(TaskRecord task, boolean isRecordByTaskId) {
        HwFreeFormBoundsRecord record = createHwFreeFormBoundsRecord(task);
        if (record == null || !record.isValidRecord()) {
            Slog.w(TAG, "recordHwFreeFormBounds error, record =" + record);
            return;
        }
        addRecord(this.mAppBoundsRecords, getPackageKey(record.packageName, record.userId), record);
        if (isRecordByTaskId) {
            addRecord(this.mTaskBoundsRecords, Integer.valueOf(record.taskId), record);
        }
    }

    private <T> void addRecord(LinkedHashMap<T, HwFreeFormBoundsRecord> recordsMap, T key, HwFreeFormBoundsRecord record) {
        recordsMap.remove(key);
        recordsMap.put(key, record);
        trimBoundsRecords(recordsMap);
    }

    private <T> void trimBoundsRecords(LinkedHashMap<T, HwFreeFormBoundsRecord> records) {
        if (records.size() > this.mRecordsLimits) {
            int trimNumbers = records.size() - this.mRecordsLimits;
            Iterator<Map.Entry<T, HwFreeFormBoundsRecord>> it = records.entrySet().iterator();
            while (it.hasNext() && trimNumbers > 0) {
                it.next();
                it.remove();
                trimNumbers--;
            }
        }
    }

    private HwFreeFormBoundsRecord createHwFreeFormBoundsRecord(TaskRecord task) {
        if (task == null || task.realActivity == null || task.getWindowingMode() != 102 || task.getWindowConfiguration() == null) {
            return null;
        }
        Rect lastBounds = new Rect(task.getWindowConfiguration().getBounds());
        float hwStackScale = INVALID_VALUE_FLOAT;
        if (!(task.getStack() == null || task.getStack().getTaskStack() == null)) {
            hwStackScale = task.getStack().getTaskStack().mHwStackScale;
        }
        String appPackage = task.realActivity.getPackageName();
        if (ActivityStartInterceptorBridge.isAppLockActivity(task.realActivity.flattenToShortString()) && task.intent != null) {
            try {
                appPackage = new Intent(task.intent).getStringExtra("android.intent.extra.PACKAGE_NAME");
            } catch (BadParcelableException e) {
                Slog.w(TAG, "createHwFreeFormBoundsRecord get extra data error.");
            }
        }
        HwFreeFormBoundsRecord record = new HwFreeFormBoundsRecord(appPackage, task.userId, task.taskId);
        record.setBounds(lastBounds, hwStackScale);
        int displayId = task.getStack() != null ? task.getStack().mDisplayId : 0;
        ActivityDisplay display = this.mService.mRootActivityContainer.getActivityDisplay(displayId);
        if (display != null) {
            record.setDisplayInfo(displayId, display.mDisplayContent.getDisplayInfo().logicalWidth, display.mDisplayContent.getDisplayInfo().logicalHeight);
        }
        return record;
    }

    public void removeBoundsRecordById(int taskId) {
        this.mTaskBoundsRecords.remove(Integer.valueOf(taskId));
    }

    public void removeBoundsRecord(String packageName, int userId) {
        this.mAppBoundsRecords.remove(getPackageKey(packageName, userId));
    }

    private int getReusableHwFreeFormTaskId(String packageName, int userId) {
        String key = getPackageKey(packageName, userId);
        if (this.mAppBoundsRecords.containsKey(key)) {
            return this.mAppBoundsRecords.get(key).taskId;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getPackageKey(String packageName, int userId) {
        if (packageName == null) {
            return "";
        }
        return packageName + AwarenessInnerConstants.DASH_KEY + userId;
    }

    public boolean hasHwFreeFormTaskRecordById(int taskId) {
        return this.mTaskBoundsRecords.containsKey(Integer.valueOf(taskId));
    }

    public float getReusableBoundsById(int taskId, Rect outBounds) {
        HwFreeFormBoundsRecord record;
        if (outBounds == null || !this.mTaskBoundsRecords.containsKey(Integer.valueOf(taskId)) || (record = this.mTaskBoundsRecords.get(Integer.valueOf(taskId))) == null || record.bounds == null || record.bounds.isEmpty()) {
            return INVALID_VALUE_FLOAT;
        }
        outBounds.set(record.bounds);
        return record.stackScale;
    }

    public void resetHwFreeFormBoundsRecords(int displayId) {
        ActivityDisplay display = this.mService.mRootActivityContainer.getActivityDisplay(displayId);
        if (display != null) {
            int displayWidth = display.mDisplayContent.getDisplayInfo().logicalWidth;
            int displayHeight = display.mDisplayContent.getDisplayInfo().logicalHeight;
            resetHwFreeFormBoundsRecords(this.mAppBoundsRecords, displayId, displayWidth, displayHeight);
            resetHwFreeFormBoundsRecords(this.mTaskBoundsRecords, displayId, displayWidth, displayHeight);
        }
    }

    private void resetHwFreeFormBoundsRecords(LinkedHashMap<?, HwFreeFormBoundsRecord> records, int displayId, int displayWidth, int displayHeight) {
        for (Map.Entry<?, HwFreeFormBoundsRecord> recordEntry : records.entrySet()) {
            HwFreeFormBoundsRecord record = recordEntry.getValue();
            if (record.displayId == displayId && !(record.displayHeight == displayHeight && record.displayWidth == displayWidth)) {
                record.reset();
            }
        }
    }

    public float getReusableBounds(String packageName, int userId, Rect outBounds) {
        if (packageName == null || packageName.isEmpty() || outBounds == null) {
            return INVALID_VALUE_FLOAT;
        }
        String key = getPackageKey(packageName, userId);
        if (!this.mAppBoundsRecords.containsKey(key) || isSkipReuseBounds(packageName, userId)) {
            return INVALID_VALUE_FLOAT;
        }
        HwFreeFormBoundsRecord record = this.mAppBoundsRecords.get(key);
        if (record.bounds == null || record.bounds.isEmpty()) {
            return INVALID_VALUE_FLOAT;
        }
        outBounds.set(record.bounds);
        if (record.stackScale > 0.0f) {
            return record.stackScale;
        }
        return INVALID_VALUE_FLOAT;
    }

    private boolean isSkipReuseBounds(String packageName, int userId) {
        List<TaskRecord> visibleTasks;
        if (packageName == null || (visibleTasks = getNowVisibleTasks()) == null) {
            return false;
        }
        for (TaskRecord task : visibleTasks) {
            if (task.realActivity != null && packageName.equals(task.realActivity.getPackageName()) && userId == task.userId && task.getWindowingMode() == 102) {
                return true;
            }
        }
        return false;
    }

    private List<TaskRecord> getNowVisibleTasks() {
        List<TaskRecord> list = new ArrayList<>();
        synchronized (this.mService.getGlobalLock()) {
            RootActivityContainer rootActivityContainer = this.mService.mRootActivityContainer;
            for (int i = rootActivityContainer.getChildCount() - 1; i >= 0; i--) {
                ActivityDisplay display = rootActivityContainer.getChildAt(i);
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStack stack = display.getChildAt(stackNdx);
                    List<TaskRecord> taskHistory = stack.getTaskHistory();
                    int taskNdx = taskHistory.size() - 1;
                    while (true) {
                        if (taskNdx < 0) {
                            break;
                        }
                        TaskRecord task = taskHistory.get(taskNdx);
                        ActivityRecord topActivity = task.getTopActivity();
                        if (topActivity == null || !topActivity.nowVisible) {
                            if (!stack.inMultiWindowMode() && topActivity != null) {
                                break;
                            }
                        } else {
                            list.add(task);
                        }
                        taskNdx--;
                    }
                }
            }
        }
        return list;
    }

    public ActivityOptions updateToHwFreeFormIfNeeded(Intent intent, ActivityInfo aInfo, TaskRecord inTask, int launchFlags, ActivityRecord resultTo, ActivityOptions options) {
        if (options != null && isNeedClearHwMultiWindowMode(intent, options)) {
            options.setLaunchWindowingMode(0);
            return options;
        } else if (!isNeedUpdateToHwFreeForm(intent, aInfo, inTask, launchFlags, resultTo, options)) {
            return options;
        } else {
            return getHwFreeFormOptions(options);
        }
    }

    private boolean isNeedClearHwMultiWindowMode(Intent intent, ActivityOptions options) {
        if (intent == null || options == null || !intent.hasCategory("android.intent.category.HOME") || !WindowConfiguration.isHwMultiStackWindowingMode(options.getLaunchWindowingMode())) {
            return false;
        }
        return true;
    }

    private boolean isNeedUpdateToHwFreeForm(Intent intent, ActivityInfo aInfo, TaskRecord inTask, int launchFlags, ActivityRecord resultTo, ActivityOptions options) {
        if (aInfo == null || aInfo.applicationInfo == null || intent == null || intent.hasCategory("android.intent.category.HOME")) {
            return false;
        }
        if (!(((268435456 & launchFlags) != 0 && (134217728 & launchFlags) == 0) || aInfo.launchMode == 3 || aInfo.launchMode == 2) || !(inTask == null && resultTo == null)) {
            return false;
        }
        if (options != null && (options.getLaunchTaskId() != -1 || (options.getLaunchWindowingMode() != 0 && options.getLaunchWindowingMode() != 102))) {
            return false;
        }
        int userId = UserHandle.getUserId(aInfo.applicationInfo.uid);
        boolean isInAppLockList = this.mService.mRootActivityContainer.getHwRootActivityContainerEx().isAppInLockList(aInfo.packageName, userId);
        int hwFreeFormTaskId = getReusableHwFreeFormTaskId(aInfo.packageName, userId);
        TaskRecord hwFreeFormTaskInRecents = null;
        if (hwFreeFormTaskId != -1) {
            TaskRecord hwFreeFormTaskInStacks = this.mService.mRootActivityContainer.anyTaskForId(hwFreeFormTaskId, 0);
            if (hwFreeFormTaskInStacks != null && hwFreeFormTaskInStacks.getRootActivity() != null && !isInAppLockList) {
                return false;
            }
            hwFreeFormTaskInRecents = this.mService.mStackSupervisor.mRecentTasks.getTask(hwFreeFormTaskId);
        }
        if (hwFreeFormTaskInRecents == null || !hwFreeFormTaskInRecents.inHwFreeFormWindowingMode()) {
            return false;
        }
        if (aInfo.taskAffinity != null && !aInfo.taskAffinity.equals(hwFreeFormTaskInRecents.rootAffinity)) {
            return false;
        }
        if (isInAppLockList) {
            removeTaskFromFloatingBall(aInfo.packageName, userId, hwFreeFormTaskInRecents);
        }
        return true;
    }

    private void removeTaskFromFloatingBall(String packageName, int userId, TaskRecord task) {
        boolean isOnlyOneTaskInBall = false;
        List<ActivityManager.RecentTaskInfo> freeFormTasksInBall = this.mService.mHwATMSEx.getFilteredTasks(userId, -1, packageName, new int[]{102}, true, -1);
        if (freeFormTasksInBall != null && freeFormTasksInBall.size() == 1) {
            isOnlyOneTaskInBall = true;
        }
        if (isOnlyOneTaskInBall) {
            List<TaskRecord> removeTasks = new ArrayList<>(1);
            removeTasks.add(task);
            this.mService.mHwATMSEx.dispatchFreeformBallLifeState(removeTasks, "remove");
        }
    }

    private ActivityOptions getHwFreeFormOptions(ActivityOptions options) {
        ActivityOptions newActivityOptions = ActivityOptions.makeBasic();
        if (options != null) {
            newActivityOptions.update(options);
        }
        newActivityOptions.update(ActivityOptions.makeCustomAnimation(this.mService.mContext, 34209874, 0));
        newActivityOptions.setLaunchWindowingMode(102);
        if (options != null) {
            newActivityOptions.setPendingShow(options.isPendingShow());
            newActivityOptions.setLaunchBounds(options.getLaunchBounds());
            newActivityOptions.setStackScale(options.getStackScale());
        }
        return newActivityOptions;
    }

    public void doDump(PrintWriter pw, String cmd, String[] args) {
        if (DUMP_APP_BOUNDS_RECORDS.equals(cmd)) {
            dumpBoundsRecords(pw, this.mAppBoundsRecords, "HwFreeForm Application Bounds Records");
        } else if (DUMP_TASK_BOUNDS_RECORDS.equals(cmd)) {
            dumpBoundsRecords(pw, this.mTaskBoundsRecords, "HwFreeForm Task Bounds Records");
        } else if (DUMP_SET_RECORDS_LIMITS.equals(cmd)) {
            setRecordsLimits(pw, args);
        } else {
            dumpBoundsRecords(pw, this.mAppBoundsRecords, "HwFreeForm Application Bounds Records");
            dumpBoundsRecords(pw, this.mTaskBoundsRecords, "HwFreeForm Task Bounds Records");
        }
    }

    private void dumpBoundsRecords(PrintWriter pw, Map<?, HwFreeFormBoundsRecord> records, String header) {
        if (pw == null) {
            Slog.w(TAG, "dumpBoundsRecords error, pw is null!");
            return;
        }
        pw.print(header);
        pw.println(" (records from earliest to latest):");
        int index = 0;
        for (Map.Entry<?, HwFreeFormBoundsRecord> recordEntry : records.entrySet()) {
            HwFreeFormBoundsRecord record = recordEntry.getValue();
            pw.println("  * Records #" + index + ": userId=" + record.userId + " package=" + record.packageName + " taskId=" + record.taskId + " bounds=" + record.bounds + " scale=" + record.stackScale);
            index++;
        }
        pw.println();
    }

    private void setRecordsLimits(PrintWriter pw, String[] args) {
        if (pw == null || args == null || args.length < 2) {
            Slog.w(TAG, "setRecordsLimits error, pw =" + pw + " args=" + args);
            return;
        }
        try {
            int limits = Integer.parseInt(args[1]);
            if (limits > 0) {
                this.mRecordsLimits = limits;
                this.mAppBoundsRecords.clear();
                this.mTaskBoundsRecords.clear();
                pw.println("Set HwFreeForm records limits to " + this.mRecordsLimits + ", records have been cleared.");
                return;
            }
            pw.println("Records limits must be greater than 0, records limits not set.");
        } catch (NumberFormatException e) {
            Slog.w(TAG, "setRecordsLimits error, invalid args!");
            pw.println("Please enter a correct number, records limits not set.");
        } catch (Exception e2) {
            Slog.wtf(TAG, "setRecordsLimits error!");
            pw.println("System error, records limits not set.");
        }
    }
}
