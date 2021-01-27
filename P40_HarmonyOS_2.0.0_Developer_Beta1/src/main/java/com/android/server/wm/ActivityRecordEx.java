package com.android.server.wm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityStackEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ActivityRecordEx {
    private static final int COMPARATER_RETRUN_VALUE_NEGATIVE = -1;
    public static final int STARTING_WINDOW_NOT_SHOWN = 0;
    private static Comparator<ActivityRecord> sComparator = $$Lambda$ActivityRecordEx$FtoHvbx_5Vj3NsqqwfM0lZtwuR8.INSTANCE;
    private ActivityRecord mActivityRecord;

    static /* synthetic */ int lambda$static$0(ActivityRecord activity1, ActivityRecord activity2) {
        if (((ActivityRecordBridge) activity1).getCreateTime() > ((ActivityRecordBridge) activity2).getCreateTime()) {
            return 1;
        }
        return -1;
    }

    public ActivityRecordEx() {
    }

    public ActivityRecordEx(ActivityRecord activityRecord) {
        this.mActivityRecord = activityRecord;
    }

    public static boolean isActivityRecordInstance(Object obj) {
        return obj instanceof ActivityRecord;
    }

    public static ActivityRecordEx createNewInstance(Object obj) {
        if (!(obj instanceof ActivityRecord)) {
            return null;
        }
        ActivityRecordEx are = new ActivityRecordEx();
        are.setActivityRecord((ActivityRecord) obj);
        return are;
    }

    public static ActivityRecordEx forToken(IBinder token) {
        if (token == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(ActivityRecord.forToken(token));
        return activityRecordEx;
    }

    public static void remove(List<ActivityRecordEx> list, ActivityRecordEx arEx) {
        if (list != null && arEx != null) {
            ActivityRecordEx activityRecordEx = null;
            Iterator<ActivityRecordEx> it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ActivityRecordEx ar = it.next();
                if (ar.getActivityRecord() == arEx.getActivityRecord()) {
                    activityRecordEx = ar;
                    break;
                }
            }
            if (activityRecordEx != null) {
                list.remove(activityRecordEx);
            }
        }
    }

    public static int indexOf(List<ActivityRecordEx> list, ActivityRecordEx arEx) {
        int index = -1;
        if (list != null && arEx != null) {
            for (ActivityRecordEx ar : list) {
                index++;
                if (ar.getActivityRecord() == arEx.getActivityRecord()) {
                    break;
                }
            }
        }
        return index;
    }

    public static void sortActivitiesInStack(ActivityStackEx stackEx) {
        if (!(stackEx == null || stackEx.getActivityStack() == null)) {
            ActivityStack stack = stackEx.getActivityStack();
            for (int taskNdx = stack.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
                Collections.sort(((TaskRecord) stack.mTaskHistory.get(taskNdx)).mActivities, sComparator);
            }
        }
    }

    public static ArrayList<ActivityRecordEx> getActivityRecordExs(ArrayList<ActivityRecord> activityRecords) {
        if (activityRecords == null) {
            return null;
        }
        ArrayList<ActivityRecordEx> activityRecordExs = new ArrayList<>();
        Iterator<ActivityRecord> it = activityRecords.iterator();
        while (it.hasNext()) {
            ActivityRecordEx activityRecordEx = new ActivityRecordEx();
            activityRecordEx.setActivityRecord(it.next());
            activityRecordExs.add(activityRecordEx);
        }
        return activityRecordExs;
    }

    public ActivityRecord getActivityRecord() {
        return this.mActivityRecord;
    }

    public void setActivityRecord(ActivityRecord activityRecord) {
        this.mActivityRecord = activityRecord;
    }

    public void resetActivityRecord(Object activityRecord) {
        if (activityRecord != null && (activityRecord instanceof ActivityRecord)) {
            this.mActivityRecord = (ActivityRecord) activityRecord;
        }
    }

    public boolean isResumedState() {
        return this.mActivityRecord.isState(ActivityStack.ActivityState.RESUMED);
    }

    public ComponentName getActivityComponent() {
        return this.mActivityRecord.mActivityComponent;
    }

    public boolean inMultiWindowMode() {
        return this.mActivityRecord.inMultiWindowMode();
    }

    public boolean isVisibleIgnoringKeyguard() {
        return this.mActivityRecord.visibleIgnoringKeyguard;
    }

    public boolean canShowWhenLocked() {
        return this.mActivityRecord.canShowWhenLocked();
    }

    public Object getShadow() {
        return this.mActivityRecord;
    }

    public void makeFinishingLocked() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.makeFinishingLocked();
        }
    }

    public void setHaveState(boolean isHaveState) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.haveState = isHaveState;
        }
    }

    public String getProcessName() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.processName;
        }
        return null;
    }

    public String getShortComponentName() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.shortComponentName;
        }
        return null;
    }

    public TaskRecordEx getTaskRecordEx() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord == null || activityRecord.getTaskRecord() == null) {
            return null;
        }
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        taskRecordEx.setTaskRecord(this.mActivityRecord.getTaskRecord());
        return taskRecordEx;
    }

    public void setWindowingMode(int windowingMode) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.setWindowingMode(windowingMode);
        }
    }

    public boolean isVisible() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.visible;
        }
        return false;
    }

    public boolean instanceOfHwActivityRecord() {
        ActivityRecord activityRecord = this.mActivityRecord;
        return activityRecord != null && (activityRecord instanceof ActivityRecordBridge);
    }

    public Rect getRequestedOverrideBounds() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.getRequestedOverrideBounds();
        }
        return new Rect();
    }

    public boolean isFinishing() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.finishing;
        }
        return false;
    }

    public boolean isNowVisible() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.nowVisible;
        }
        return false;
    }

    public boolean isFinishAllRightBottom() {
        if (instanceOfHwActivityRecord()) {
            return this.mActivityRecord.isFinishAllRightBottom();
        }
        return false;
    }

    public void setIsFromFullscreenToMagicWin(boolean isFromFullscreenToMagicWin) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.setFromFullscreenToMagicWin(isFromFullscreenToMagicWin);
        }
    }

    public boolean isFromFullscreenToMagicWin() {
        if (instanceOfHwActivityRecord()) {
            return this.mActivityRecord.isFromFullscreenToMagicWin();
        }
        return false;
    }

    public void setIsFinishAllRightBottom(boolean isFinishAllRightBottom) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.setFinishAllRightBottom(isFinishAllRightBottom);
        }
    }

    public int getMagicWindowPageType() {
        if (instanceOfHwActivityRecord()) {
            return this.mActivityRecord.getMagicWindowPageType();
        }
        return 0;
    }

    public void setMagicWindowPageType(int type) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.setMagicWindowPageType(type);
        }
    }

    public void resumeKeyDispatchingLocked() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.resumeKeyDispatchingLocked();
        }
    }

    public static ActivityRecordEx isInStackLocked(IBinder token) {
        if (token == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(ActivityRecord.isInStackLocked(token));
        return activityRecordEx;
    }

    public int getWindowingMode() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.getWindowingMode();
        }
        return 0;
    }

    public void setIsAniRunningBelow(boolean isAniRunningBelow) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.setIsAniRunningBelow(isAniRunningBelow);
        }
    }

    public boolean isFullscreen() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.fullscreen;
        }
        return false;
    }

    public void setIsStartFromLauncher(boolean isStart) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.setIsStartFromLauncher(isStart);
        }
    }

    public boolean isActivityTypeHome() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.isActivityTypeHome();
        }
        return false;
    }

    public ActivityInfo getInfo() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.info;
        }
        return null;
    }

    public void setIsFullScreenVideoInLandscape(boolean isFull) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.setFullScreenVideoInLandscape(isFull);
        }
    }

    public boolean inHwMultiStackWindowingMode() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.inHwMultiStackWindowingMode();
        }
        return false;
    }

    public boolean inFreeformWindowingMode() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.inFreeformWindowingMode();
        }
        return false;
    }

    public boolean isStartFromLauncher() {
        if (instanceOfHwActivityRecord()) {
            return this.mActivityRecord.isStartFromLauncher();
        }
        return false;
    }

    public void setStartingWindowState(int state) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.mStartingWindowState = state;
        }
    }

    public int getLaunchedFromUid() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.launchedFromUid;
        }
        return 0;
    }

    public ApplicationInfo getAppInfo() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.appInfo;
        }
        return null;
    }

    public ActivityDisplayEx getDisplayEx() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord == null || activityRecord.getDisplay() == null) {
            return null;
        }
        ActivityDisplayEx activityDisplayEx = new ActivityDisplayEx();
        activityDisplayEx.setActivityDisplay(this.mActivityRecord.getDisplay());
        return activityDisplayEx;
    }

    public int getLaunchMode() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.launchMode;
        }
        return 0;
    }

    public Rect getLastBound() {
        if (instanceOfHwActivityRecord()) {
            return this.mActivityRecord.getLastBound();
        }
        return null;
    }

    public void setLastBound(Rect rect) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.setLastBound(new Rect(rect));
        }
    }

    public void resize() {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.resize();
        }
    }

    public void setRequestedOrientation(int requestedOrientation) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.setRequestedOrientation(requestedOrientation);
        }
    }

    public boolean attachedToProcess() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.attachedToProcess();
        }
        return false;
    }

    public WindowProcessControllerEx getAppEx() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord == null || activityRecord.app == null) {
            return null;
        }
        WindowProcessControllerEx windowProcessControllerEx = new WindowProcessControllerEx();
        windowProcessControllerEx.setWindowProcessController(this.mActivityRecord.app);
        return windowProcessControllerEx;
    }

    public long getCreateTime() {
        if (instanceOfHwActivityRecord()) {
            return this.mActivityRecord.getCreateTime();
        }
        return System.currentTimeMillis();
    }

    public boolean isState(ActivityStackEx.ActivityState state) {
        if (this.mActivityRecord == null || state == null) {
            return false;
        }
        ActivityStack.ActivityState result = ActivityStack.ActivityState.INITIALIZING;
        ActivityStack.ActivityState[] values = ActivityStack.ActivityState.values();
        for (ActivityStack.ActivityState activityState : values) {
            if (state.ordinal() == activityState.ordinal()) {
                result = activityState;
            }
        }
        return this.mActivityRecord.isState(result);
    }

    public boolean isState(ActivityStackEx.ActivityState state1, ActivityStackEx.ActivityState state2, ActivityStackEx.ActivityState state3) {
        if (this.mActivityRecord == null || state1 == null || state2 == null || state3 == null) {
            return false;
        }
        ActivityStack.ActivityState result1 = ActivityStack.ActivityState.INITIALIZING;
        ActivityStack.ActivityState result2 = ActivityStack.ActivityState.INITIALIZING;
        ActivityStack.ActivityState result3 = ActivityStack.ActivityState.INITIALIZING;
        ActivityStack.ActivityState[] values = ActivityStack.ActivityState.values();
        for (ActivityStack.ActivityState activityState : values) {
            if (state1.ordinal() == activityState.ordinal()) {
                result1 = activityState;
            }
            if (state2.ordinal() == activityState.ordinal()) {
                result2 = activityState;
            }
            if (state3.ordinal() == activityState.ordinal()) {
                result3 = activityState;
            }
        }
        return this.mActivityRecord.isState(result1, result2, result3);
    }

    public Configuration getMergedOverrideConfiguration() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.getMergedOverrideConfiguration();
        }
        return new Configuration();
    }

    public boolean ensureActivityConfiguration(int globalChanges, boolean isPreserveWindow) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.ensureActivityConfiguration(globalChanges, isPreserveWindow);
        }
        return true;
    }

    public void setState(ActivityStackEx.ActivityState state, String reason) {
        if (!(this.mActivityRecord == null || state == null)) {
            ActivityStack.ActivityState result = ActivityStack.ActivityState.INITIALIZING;
            ActivityStack.ActivityState[] values = ActivityStack.ActivityState.values();
            for (ActivityStack.ActivityState activityState : values) {
                if (state.ordinal() == activityState.ordinal()) {
                    result = activityState;
                }
            }
            this.mActivityRecord.setState(result, reason);
        }
    }

    public String getTaskAffinity() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.taskAffinity;
        }
        return null;
    }

    public void setLaunchMode(int launchMode) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.launchMode = launchMode;
        }
    }

    public ActivityRecordEx getResultTo() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord == null || activityRecord.resultTo == null) {
            return null;
        }
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        activityRecordEx.setActivityRecord(this.mActivityRecord.resultTo);
        return activityRecordEx;
    }

    public void setIsMwNewTask(boolean isMwNewTask) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.mIsMwNewTask = isMwNewTask;
        }
    }

    public int getLastActivityHash() {
        if (instanceOfHwActivityRecord()) {
            return this.mActivityRecord.getLastActivityHash();
        }
        return -1;
    }

    public void setLastActivityHash(int hash) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.setLastActivityHash(hash);
        }
    }

    public void setForceNewConfig(boolean isForceNewConfig) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.forceNewConfig = isForceNewConfig;
        }
    }

    public Rect getBounds() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.getBounds();
        }
        return new Rect();
    }

    public void onRequestedOverrideConfigurationChanged(Configuration overrideConfiguration) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.onRequestedOverrideConfigurationChanged(overrideConfiguration);
        }
    }

    public int getConfigChangeFlags() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.configChangeFlags;
        }
        return 0;
    }

    public AppWindowTokenExt getAppWindowTokenEx() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord == null || activityRecord.mAppWindowToken == null) {
            return null;
        }
        AppWindowTokenExt appWindowTokenEx = new AppWindowTokenExt();
        appWindowTokenEx.setAppWindowToken(this.mActivityRecord.mAppWindowToken);
        return appWindowTokenEx;
    }

    public IBinder getAppToken() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.appToken;
        }
        return null;
    }

    public boolean isHwActivityRecord() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord instanceof ActivityRecordBridge;
        }
        return false;
    }

    public boolean isFullScreenVideoInLandscape() {
        if (isHwActivityRecord()) {
            return this.mActivityRecord.isFullScreenVideoInLandscape();
        }
        return false;
    }

    public void setFullScreenVideoInLandscape(boolean isFullScreenVideoInLandscape) {
        if (isHwActivityRecord()) {
            this.mActivityRecord.setFullScreenVideoInLandscape(isFullScreenVideoInLandscape);
        }
    }

    public boolean inHwMagicWindowingMode() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.inHwMagicWindowingMode();
        }
        return false;
    }

    public String getPackageName() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.packageName;
        }
        return null;
    }

    public int getStackId() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.getStackId();
        }
        return -1;
    }

    public void setBounds(Rect bounds) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.setBounds(bounds);
        }
    }

    public void reparent(TaskRecordEx newTask, int position, String reason) {
        if (this.mActivityRecord != null) {
            TaskRecord taskRecord = null;
            if (newTask != null) {
                taskRecord = newTask.getTaskRecord();
            }
            this.mActivityRecord.reparent(taskRecord, position, reason);
        }
    }

    public ActivityStackEx getActivityStackEx() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord == null || activityRecord.getActivityStack() == null) {
            return null;
        }
        ActivityStackEx activityStackEx = new ActivityStackEx();
        activityStackEx.setActivityStack(this.mActivityRecord.getActivityStack());
        return activityStackEx;
    }

    public int getUserId() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.mUserId;
        }
        return 0;
    }

    public ActivityInfo getActivityInfo() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.info;
        }
        return null;
    }

    public Intent getIntent() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.intent;
        }
        return null;
    }

    public boolean isTopRunningActivity() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            return activityRecord.isTopRunningActivity();
        }
        return false;
    }

    public boolean equalsActivityRecord(ActivityRecordEx arEx) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord == null || arEx == null || activityRecord != arEx.getActivityRecord()) {
            return false;
        }
        return true;
    }

    public void removeFromLRUList() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.getActivityStack().mLRUActivities.remove(this.mActivityRecord);
        }
    }

    public void addToLRUList(int index) {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord != null) {
            activityRecord.getActivityStack().mLRUActivities.add(index, this.mActivityRecord);
        }
    }

    public void setDelayFinished(boolean isDelayFinished) {
        if (instanceOfHwActivityRecord()) {
            this.mActivityRecord.setIsDelayFinished(isDelayFinished);
        }
    }

    public boolean isDelayFinished() {
        if (instanceOfHwActivityRecord()) {
            return this.mActivityRecord.isDelayFinished();
        }
        return false;
    }

    public String toString() {
        ActivityRecord activity = getActivityRecord();
        return activity != null ? activity.toString() : "null";
    }

    public int getActivityType() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord == null) {
            return 0;
        }
        return activityRecord.getActivityType();
    }

    public boolean isDestroyState() {
        return this.mActivityRecord.getState() == ActivityStack.ActivityState.DESTROYED;
    }

    public boolean isAppNull() {
        ActivityRecord activityRecord = this.mActivityRecord;
        return activityRecord == null || activityRecord.app == null;
    }

    public String getAppName() {
        ActivityRecord activityRecord = this.mActivityRecord;
        if (activityRecord == null) {
            return null;
        }
        return activityRecord.app.mName;
    }

    public boolean isWindowProcessControllerNotNull() {
        return (this.mActivityRecord.app == null || this.mActivityRecord.app.mThread == null) ? false : true;
    }

    public void schedulePCWindowStateChangedEx() {
        ActivityRecordBridge activityRecordBridge = this.mActivityRecord;
        if (activityRecordBridge instanceof ActivityRecordBridge) {
            activityRecordBridge.schedulePCWindowStateChanged();
        }
    }

    public boolean isEmpty() {
        return this.mActivityRecord == null;
    }

    public Bundle getMagicWindowExtras() {
        ActivityRecordBridge activityRecordBridge = this.mActivityRecord;
        if (activityRecordBridge == null || !(activityRecordBridge instanceof ActivityRecordBridge)) {
            return null;
        }
        return activityRecordBridge.getMagicWindowExtras();
    }

    public void setMagicWindowExtras(Bundle bundle) {
        ActivityRecordBridge activityRecordBridge = this.mActivityRecord;
        if (activityRecordBridge != null && (activityRecordBridge instanceof ActivityRecordBridge)) {
            activityRecordBridge.setMagicWindowExtras(bundle);
        }
    }
}
