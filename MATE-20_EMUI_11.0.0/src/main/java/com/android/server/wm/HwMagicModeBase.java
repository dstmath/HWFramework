package com.android.server.wm;

import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.HwMwUtils;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.utils.Utils;
import java.util.ArrayList;
import java.util.Iterator;

public class HwMagicModeBase {
    private static final String ADOSETTINGS_ACTIVITY = "com.android.settings.Settings$AppDrawOverlaySettingsActivity";
    private static final String INCALL_ACTIVITY = "com.huawei.hicallmanager.InCallActivity";
    protected static final int INDEX_OF_SECOND_ACTIVITY = 1;
    private static final String MAGIC_WINDOW_LOCK_MASTER_ACTION = "huawei.intent.action.MAGIC_WINDOW_LOCK_MASTER";
    private static final String MAGIC_WINDOW_LOCK_PRIMARY_ACTION = "huawei.intent.action.MAGIC_WINDOW_LOCK_PRIMARY";
    protected static final int NUM_ACTIVITY_SIZE = 2;
    private static final String TAG = "HWMW_HwMagicModeBase";
    protected Context mContext;
    protected HwMagicWinManager mMwManager;
    protected IBinder mOrigActivityToken = null;
    protected HwMagicWinAmsPolicy mPolicy;

    public HwMagicModeBase(HwMagicWinManager manager, HwMagicWinAmsPolicy policy, Context context) {
        this.mMwManager = manager;
        this.mContext = context;
        this.mPolicy = policy;
    }

    /* access modifiers changed from: protected */
    public void setOrigActivityToken(HwMagicContainer container, ActivityRecordEx activityRecordEx) {
        if (activityRecordEx != null && !this.mPolicy.isSpecTransActivity(container, activityRecordEx)) {
            this.mOrigActivityToken = activityRecordEx.getAppToken();
        }
    }

    public void overrideIntent(ActivityRecordEx focus, ActivityRecordEx next, boolean isNewTask) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (container != null) {
            int targetPosition = 3;
            int focusSourcePosition = container.getBoundsPosition(focus.getRequestedOverrideBounds());
            if (focus.inHwMagicWindowingMode() && !isNewTask) {
                targetPosition = getTargetWindowPosition(focus, next);
                if (targetPosition == 1) {
                    this.mPolicy.mMagicWinSplitMng.setAboveStackToDefault(focus.getTaskRecordEx().getStack(), targetPosition);
                }
                boolean isFocusSpecTrans = this.mPolicy.isSpecTransActivity(container, focus) && focus.isFinishAllRightBottom();
                if ((this.mMwManager.isMaster(focus) || isFocusSpecTrans) && targetPosition == 2) {
                    next.setIsFinishAllRightBottom(true);
                    ActivityRecordEx slaveTop = this.mPolicy.getActivityByPosition(focus, 2, 0);
                    if (this.mPolicy.isRelatedActivity(container, slaveTop)) {
                        this.mPolicy.setMagicWindowToPause(slaveTop);
                    }
                }
                adjustActivityToMaster(focus, next, targetPosition);
                if (isMoveActivityToMaster(focus, next, targetPosition)) {
                    setLeftTopActivityToPause(focus);
                    next.setIsAniRunningBelow(true);
                    this.mPolicy.moveWindow(focus, 1);
                    moveOtherActivities(focus, 2);
                }
            }
            int targetPosition2 = updateTargetPosition(container, focus, next, isNewTask, targetPosition);
            setNextDragFullMode(focus, next, isNewTask, targetPosition2);
            setActivityBoundByPosition(next, targetPosition2);
            HwMagicWinAnimationScene.StartAnimationScene animationScene = new HwMagicWinAnimationScene.StartAnimationScene();
            animationScene.setFocusSourcePosition(focusSourcePosition);
            animationScene.setTargetPosition(container.getBoundsPosition(next.getRequestedOverrideBounds()));
            animationScene.setTransition(this.mPolicy.isSpecTransActivity(container, focus));
            animationScene.setMagicMode(container.getConfig().getWindowMode(focus.getPackageName()));
            animationScene.setFocusTargetPosition(container.getBoundsPosition(focus.getRequestedOverrideBounds()));
            animationScene.setAllDrawn(this.mMwManager.getWmsPolicy().getAllDrawnByActivity(focus.getAppToken()));
            container.getAnimation().overrideStartActivityAnimation(animationScene.calculatedAnimationScene());
            SlogEx.i(TAG, "overrideIntent focus=" + focus + " next=" + next + " targetPosition=" + targetPosition2);
        }
    }

    private void adjustActivityToMaster(ActivityRecordEx focus, ActivityRecordEx next, int targetPosition) {
        if (next.getIntent() == null) {
            SlogEx.i(TAG, "the intent is null,not need adjust activity to master");
        } else if ((next.getIntent().getFlags() & 67108864) == 0) {
            SlogEx.i(TAG, "clear top does not exist in the FLAG,not need adjust activity to master");
        } else if (!focus.isFinishing() || targetPosition != 2) {
            SlogEx.i(TAG, "focus is not finishing or target activity is not slave,not need adjust activity to master");
        } else {
            ActivityRecordEx middleTop = this.mPolicy.getActivityByPosition(focus, 3, 0);
            if (middleTop != null) {
                this.mPolicy.moveWindow(middleTop, 1);
                SlogEx.i(TAG, "adjust activity to master");
            }
        }
    }

    public void setPairForFinish(ActivityRecordEx finishActivity) {
    }

    public void finishRightAfterFinishingLeft(ActivityRecordEx finishActivity) {
        this.mPolicy.finishMagicWindow(finishActivity, true);
    }

    /* access modifiers changed from: protected */
    public boolean checkStatus(ActivityRecordEx focus, ActivityRecordEx next) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (container == null || focus == null || focus.getShadow() == null || next == null || next.getShadow() == null) {
            SlogEx.w(TAG, "focus or next is a null, next = " + next);
            return false;
        } else if (!container.getHwMagicWinEnabled(Utils.getPackageName(next))) {
            return false;
        } else {
            setConfigChanges(container, next);
            if (!container.isInMagicWinOrientation()) {
                SlogEx.w(TAG, "device is not in magicwindow orientation, orientation=" + container.getOrientation());
                return false;
            }
            if (!container.isFoldableDevice()) {
                if (focus.getWindowingMode() == 1 && focus.isFullScreenVideoInLandscape() && (next.isFullScreenVideoInLandscape() || next.getInfo().screenOrientation == 3) && focus.getActivityStackEx().equalsStack(next.getActivityStackEx())) {
                    SlogEx.w(TAG, "do nothing when app is in fullscreen status and fullscreen mode");
                    return false;
                } else if (!focus.inHwMagicWindowingMode() && focus.getActivityStackEx().equalsStack(next.getActivityStackEx())) {
                    return false;
                }
            }
            if (HwMwUtils.isInSuitableScene(true) && !focus.getActivityStackEx().inSplitScreenWindowingMode()) {
                return true;
            }
            SlogEx.w(TAG, "the current status does not need to enter magic widnow");
            return false;
        }
    }

    public void addNewTaskFlag(ActivityRecordEx focus, ActivityRecordEx next) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (container != null) {
            boolean isStartWithTrans = true;
            if (container.getHwMagicWinEnabled(Utils.getPackageName(next))) {
                int targetPosition = this.mPolicy.getMode(focus).getTargetWindowPosition(focus, next);
                ActivityRecordEx origActivity = ActivityRecordEx.forToken(this.mOrigActivityToken);
                if (!this.mPolicy.isSpecTransActivity(container, focus) || (!this.mMwManager.isMaster(origActivity) && !this.mMwManager.isMiddle(origActivity))) {
                    isStartWithTrans = false;
                }
                if ((this.mMwManager.isMiddle(focus) || this.mMwManager.isMaster(focus) || isStartWithTrans) && targetPosition == 2 && !this.mPolicy.isMainActivity(container, next)) {
                    next.getIntent().removeFlags(603979776);
                }
            } else if (container.isFoldableDevice() && next.getResultTo() != null) {
                SlogEx.d(TAG, "need to return result to another app, do not add the new task flag");
                if (HwMagicWinAmsPolicy.PERMISSION_ACTIVITY.equals(Utils.getClassName(next))) {
                    this.mMwManager.getUIController().updateSplitBarVisibility(false, container.getDisplayId());
                }
            } else if (focus.getAppInfo() != null && next.getLaunchedFromUid() != focus.getAppInfo().uid) {
                SlogEx.d(TAG, "need to return for not start from current app");
            } else if (isNeedNewTaskForSystemApplication(focus, next)) {
                next.getIntent().addFlags(268435456);
                next.setIsMwNewTask(true);
                SlogEx.i(TAG, "add new task flag for next = " + next);
            } else {
                this.mMwManager.getUIController().updateSplitBarVisibility(false, container.getDisplayId());
            }
        }
    }

    public ActivityRecordEx getOrigActivityRecordEx() {
        IBinder iBinder = this.mOrigActivityToken;
        if (iBinder == null) {
            return null;
        }
        return ActivityRecordEx.forToken(iBinder);
    }

    private boolean isNeedNewTaskForSystemApplication(ActivityRecordEx focus, ActivityRecordEx next) {
        return !HwMagicWinAmsPolicy.PERMISSION_ACTIVITY.equals(Utils.getClassName(next)) && !HwMagicWinAmsPolicy.DEVICE_ADMIN_ACTIVITY.equals(Utils.getClassName(next)) && !HwMagicWinAmsPolicy.DOCUMENTUI_PACKAGENAME.equals(Utils.getPackageName(next)) && (!INCALL_ACTIVITY.equals(Utils.getClassName(focus)) || !ADOSETTINGS_ACTIVITY.equals(Utils.getClassName(next)));
    }

    private void setNextDragFullMode(ActivityRecordEx focus, ActivityRecordEx next, boolean isNewTask, int targetPos) {
        if (this.mMwManager.isDragFullMode(focus) && isNewTask && Utils.getPackageName(focus).equals(Utils.getPackageName(next)) && targetPos == 5) {
            HwMagicWinManager hwMagicWinManager = this.mMwManager;
            hwMagicWinManager.setDragFullMode(next, hwMagicWinManager.getDragFullMode(focus));
        }
    }

    private int updateTargetPosition(HwMagicContainer container, ActivityRecordEx focus, ActivityRecordEx next, boolean isNewTask, int targetPos) {
        int focusPosition;
        boolean isDragFull = false;
        boolean isDialogWin = !next.isFullscreen() && this.mMwManager.isFull(focus);
        if (focus.inHwMagicWindowingMode() && (next.isFullScreenVideoInLandscape() || isDialogWin)) {
            return 5;
        }
        if (this.mPolicy.isPkgInLogoffStatus(next)) {
            return 3;
        }
        if (focus.inHwMagicWindowingMode() && isNewTask && this.mPolicy.mMagicWinSplitMng.isPkgSpliteScreenMode(next, true) && ((focusPosition = container.getBoundsPosition(focus.getRequestedOverrideBounds())) == 1 || focusPosition == 2)) {
            return focusPosition;
        }
        if (this.mMwManager.isDragFullMode(next) || (this.mMwManager.isDragFullMode(focus) && isNewTask && Utils.getPackageName(focus).equals(Utils.getPackageName(next)))) {
            isDragFull = true;
        }
        if ((container.isFoldableDevice() || !isNewTask || !this.mPolicy.isDefaultFullscreenActivity(container, next)) && !isDragFull) {
            return targetPos;
        }
        return 5;
    }

    public int getTargetWindowPosition(ActivityRecordEx focus, ActivityRecordEx next) {
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        if (container == null) {
            return 3;
        }
        String nextPkg = Utils.getPackageName(next);
        if ((this.mMwManager.getAmsPolicy().isHomeActivity(container, next) && !this.mPolicy.isSupportMainRelatedMode(container, next)) || container.getConfig().getWindowMode(nextPkg) == 0) {
            return 3;
        }
        if (this.mPolicy.isMainActivity(container, next)) {
            return 1;
        }
        int focusPosition = container.getBoundsPosition(focus.getRequestedOverrideBounds());
        SlogEx.i(TAG, "getTargetWindowPosition focusPosition=" + focusPosition);
        if (this.mPolicy.isSpecTransActivityPreDefined(container, next)) {
            return focusPosition;
        }
        if (this.mPolicy.isRelatedActivity(container, next)) {
            return 2;
        }
        if (container.getConfig().isSupportAppTaskSplitScreen(nextPkg)) {
            boolean isPkgSpliteMode = this.mPolicy.mMagicWinSplitMng.isPkgSpliteScreenMode(next, true);
            if (nextPkg.equals(Utils.getPackageName(focus)) && isPkgSpliteMode) {
                return focusPosition;
            }
            if (next.getLaunchMode() == 2 && !nextPkg.equals(next.getTaskAffinity())) {
                return 3;
            }
        }
        if (this.mPolicy.isDefaultFullscreenActivity(container, next)) {
            return 5;
        }
        return getTargetPositionInner(focus, next, container);
    }

    private int getTargetPositionInner(ActivityRecordEx focus, ActivityRecordEx next, HwMagicContainer container) {
        int focusPosition = container.getBoundsPosition(focus.getRequestedOverrideBounds());
        boolean isEnterDoubleWindow = this.mMwManager.getAmsPolicy().isHomeActivity(container, focus) || this.mPolicy.isEnterDoubleWindowIgnoreHome(container, Utils.getRealPkgName(next));
        if (focusPosition == 1 || focusPosition == 2) {
            return 2;
        }
        if (focusPosition != 3) {
            return (focusPosition == 5 && !HwMagicWinAmsPolicy.PERMISSION_ACTIVITY.equals(Utils.getClassName(focus)) && focus.getTaskRecordEx().equalsTaskRecord(next.getTaskRecordEx())) ? 5 : 3;
        }
        if (!this.mPolicy.isSpecTransActivity(container, focus) && isEnterDoubleWindow) {
            return 2;
        }
        return 3;
    }

    public void adjustWindowForFinish(ActivityRecordEx activity, String finishReason) {
    }

    public void setActivityBoundByMode(ArrayList<ActivityRecordEx> activities, String pkgName, HwMagicContainer toContainer) {
        if (activities.size() < 1) {
            SlogEx.d(TAG, "there is not any activity in the list, return");
            return;
        }
        HwMagicContainer container = toContainer != null ? toContainer : this.mMwManager.getContainer(activities.get(0));
        if (container != null) {
            setActivityBoundMainRelatedIfNeed(activities, pkgName, toContainer);
            boolean hasFullscreenActivity = false;
            Iterator<ActivityRecordEx> it = activities.iterator();
            while (it.hasNext()) {
                ActivityRecordEx activityRecord = it.next();
                activityRecord.setBounds(container.getBounds(this.mMwManager.isDragFullMode(activityRecord) ? 5 : 3, Utils.getRealPkgName(activityRecord)));
                hasFullscreenActivity = setDefaultFullscreenBounds(activityRecord, hasFullscreenActivity);
            }
        }
    }

    public boolean isLockMaster(ActivityRecordEx focus) {
        ActivityRecordEx masterTop = this.mPolicy.getActivityByPosition(focus, 1, 0);
        ActivityRecordEx slaveTop = this.mPolicy.getActivityByPosition(focus, 2, 0);
        return (masterTop != null && isLockMasterActivity(masterTop)) && !(slaveTop != null && isLockMasterActivity(slaveTop));
    }

    public boolean isLockMasterActivity(ActivityRecordEx ar) {
        HwMagicContainer container = this.mMwManager.getContainer(ar);
        return container != null && (container.getConfig().isLockMasterActivity(Utils.getPackageName(ar), Utils.getClassName(ar)) || (ar.getIntent() != null && (MAGIC_WINDOW_LOCK_MASTER_ACTION.equals(ar.getIntent().getAction()) || MAGIC_WINDOW_LOCK_PRIMARY_ACTION.equals(ar.getIntent().getAction()))));
    }

    /* access modifiers changed from: protected */
    public int getLockPageIndex(ArrayList<ActivityRecordEx> activities) {
        int tempIndex = -1;
        int i = activities.size() - 1;
        while (true) {
            if (i < 0) {
                break;
            }
            ActivityRecordEx ar = activities.get(i);
            if (this.mPolicy.isDefaultFullscreenActivity(this.mMwManager.getContainer(ar), ar)) {
                for (int index = i + 2; index < activities.size() - 1; index++) {
                    if (isLockMasterActivity(ar)) {
                        return index;
                    }
                }
            } else {
                if (isLockMasterActivity(ar) && i != 0) {
                    tempIndex = i;
                }
                i--;
            }
        }
        return tempIndex;
    }

    /* access modifiers changed from: protected */
    public boolean setDefaultFullscreenBounds(ActivityRecordEx activityRecord, boolean isFullscreen) {
        HwMagicContainer container = this.mMwManager.getContainer(activityRecord);
        if (container == null) {
            return false;
        }
        if (!isFullscreen && !this.mPolicy.isDefaultFullscreenActivity(container, activityRecord)) {
            return false;
        }
        activityRecord.setBounds(container.getBounds(5, Utils.getRealPkgName(activityRecord)));
        return true;
    }

    /* access modifiers changed from: protected */
    public void setActivityBoundMainRelatedIfNeed(ArrayList<ActivityRecordEx> activities, String pkgName, HwMagicContainer toContainer) {
        HwMagicContainer container = toContainer != null ? toContainer : this.mMwManager.getContainer(activities.get(0));
        if (container == null || container.isInFoldedStatus() || !this.mPolicy.isSupportMainRelatedMode(container, activities.get(0)) || !container.getHwMagicWinEnabled(pkgName)) {
            SlogEx.d(TAG, "activity is not A1A0 or folded, return");
        } else if (activities.size() < 1) {
            SlogEx.d(TAG, "there is not any activity in the list, return");
        } else {
            ActivityRecordEx mainActivity = activities.get(0);
            if (this.mPolicy.isMainActivity(container, mainActivity)) {
                ActivityStackEx stack = mainActivity.getActivityStackEx();
                activities.remove(0);
                stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, false, false, false, true, false);
                stack.resize((Rect) null, (Rect) null, (Rect) null);
                mainActivity.setBounds(container.getBounds(1, pkgName));
                if (stack.equalsStack(this.mPolicy.getFocusedTopStack(container))) {
                    SlogEx.d(TAG, "set activity bound startRelateActivityIfNeed " + mainActivity);
                    this.mPolicy.startRelateActivityIfNeed(mainActivity, false);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public ActivityRecordEx getHomePageActivityRecord(ArrayList<ActivityRecordEx> activities) {
        Iterator<ActivityRecordEx> it = activities.iterator();
        while (it.hasNext()) {
            ActivityRecordEx activityRecord = it.next();
            if (this.mMwManager.getAmsPolicy().isHomeActivity(this.mMwManager.getContainer(activityRecord), activityRecord)) {
                return activityRecord;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void adjustWindowForDoubleWindows(ActivityRecordEx activity, String finishReason) {
        int windowIndex;
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container != null) {
            if (!this.mMwManager.isMaster(activity)) {
                ActivityRecordEx slaveNext = this.mPolicy.getActivityByPosition(activity, 2, 1);
                ActivityRecordEx masterNext = this.mPolicy.getActivityByPosition(activity, 1, 1);
                String pkgName = Utils.getPackageName(activity);
                boolean isLeftTopMove = (this.mPolicy.isRelatedActivity(container, slaveNext) || this.mPolicy.isSpecTransActivity(container, slaveNext)) && (container.isSupportOpenMode(pkgName) || container.isSupportAnAnMode(pkgName)) && masterNext != null && !isNonFullScreen(activity);
                if (slaveNext == null || isLeftTopMove) {
                    windowIndex = 0;
                } else {
                    SlogEx.i(TAG, "adjustWindowForDoubleWindows abort because right index 1 have activity");
                    this.mPolicy.moveToFrontInner(slaveNext);
                    return;
                }
            } else if (!activity.isTopRunningActivity()) {
                moveRightActivityToMiddleIfNeeded(activity);
                return;
            } else {
                windowIndex = 1;
            }
            if (!shouldStartRelatedActivityForFinish(activity, finishReason)) {
                ActivityRecordEx activityRecord = this.mPolicy.getActivityByPosition(activity, 1, windowIndex);
                if (activityRecord != null) {
                    if (this.mPolicy.getActivityByPosition(activity, 1, windowIndex + 1) == null || this.mPolicy.isHomeActivity(container, activityRecord)) {
                        activity.setIsAniRunningBelow(true);
                        this.mPolicy.moveWindow(activityRecord, 3);
                    } else if (!HwMagicWinAmsPolicy.MAGIC_WINDOW_FINISH_EVENT.equals(finishReason)) {
                        activity.setIsAniRunningBelow(!isMastersFinish(activity, finishReason));
                        this.mPolicy.moveWindow(activityRecord, 2);
                        moveOtherActivities(activityRecord, 1);
                    }
                } else if (container.isFoldableDevice() && !this.mMwManager.isDragFullMode(activity)) {
                    this.mPolicy.mModeSwitcher.updateActivityToFullScreenConfiguration(this.mPolicy.getActivityByPosition(activity, 0, 1));
                }
            }
        }
    }

    private void moveRightActivityToMiddleIfNeeded(ActivityRecordEx activity) {
        ActivityRecordEx leftNext = this.mPolicy.getActivityByPosition(activity, 1, 1);
        ActivityRecordEx rightTop = this.mPolicy.getActivityByPosition(activity, 2, 0);
        if (leftNext == null && rightTop != null) {
            activity.setIsAniRunningBelow(true);
            this.mPolicy.moveWindow(rightTop, 3);
        }
    }

    private boolean shouldStartRelatedActivityForFinish(ActivityRecordEx activity, String finishReason) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        boolean z = false;
        if (!this.mPolicy.isSupportMainRelatedMode(container, activity) || HwMagicWinAmsPolicy.MAGIC_WINDOW_FINISH_EVENT.equals(finishReason)) {
            return false;
        }
        ArrayList<ActivityRecordEx> tempActivityList = this.mPolicy.getAllActivities(activity.getActivityStackEx());
        ActivityRecordEx.remove(tempActivityList, activity);
        if (tempActivityList.size() != 0 && !this.mPolicy.isRelatedActivity(container, activity)) {
            ActivityRecordEx topActivity = tempActivityList.get(0);
            if (this.mPolicy.isMainActivity(container, topActivity) && topActivity.inHwMagicWindowingMode()) {
                HwMagicWinAmsPolicy hwMagicWinAmsPolicy = this.mPolicy;
                if (tempActivityList.size() == 1) {
                    z = true;
                }
                hwMagicWinAmsPolicy.startRelateActivityIfNeed(topActivity, z);
                return true;
            }
        }
        return false;
    }

    public boolean isMoveActivityToMaster(ActivityRecordEx focus, ActivityRecordEx next, int targetPosition) {
        return false;
    }

    public boolean shouldEnterMagicWinForTah(ActivityRecordEx focus, ActivityRecordEx next) {
        return false;
    }

    public boolean isSkippingMoveToMaster(ActivityRecordEx focus, ActivityRecordEx next) {
        boolean isSingleTaskOrSingleTop = next.getInfo().launchMode == 2 || next.getInfo().launchMode == 1;
        boolean isSameClassAndPackName = Utils.getClassName(focus).equals(Utils.getClassName(next)) && Utils.getPackageName(focus).equals(Utils.getPackageName(next));
        HwMagicContainer container = this.mMwManager.getContainer(focus);
        return this.mPolicy.isSpecTransActivity(container, focus) || this.mPolicy.isSpecTransActivityPreDefined(container, next) || (isSingleTaskOrSingleTop && isSameClassAndPackName) || this.mPolicy.isRelatedActivity(container, focus) || this.mPolicy.isHomeStackHotStart(focus, next);
    }

    public void setActivityBoundByPosition(ActivityRecordEx next, int position) {
        HwMagicContainer container = this.mMwManager.getContainer(next);
        if (next != null && container != null) {
            if (next.getActivityStackEx() != null && !next.getActivityStackEx().inHwMagicWindowingMode()) {
                next.getActivityStackEx().setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE, true, false, false, true, false);
            }
            Rect bound = new Rect(container.getBounds(position, Utils.getRealPkgName(next)));
            if (this.mPolicy.mMagicWinSplitMng.isSpliteModeStack(next.getActivityStackEx())) {
                container.getConfig().adjustSplitBound(position, bound);
            }
            if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
                SlogEx.i(TAG, "setActivityBoundByPosition next = " + next + " position = " + position + " bound = " + bound);
            }
            next.setBounds(bound);
        }
    }

    public void setLeftTopActivityToPause(ActivityRecordEx focus) {
    }

    public boolean isNonFullScreen(ActivityRecordEx activityRecord) {
        HwMagicContainer container = this.mMwManager.getContainer(activityRecord);
        if (container == null || !container.isFoldableDevice()) {
            return false;
        }
        boolean isSpecTrans = this.mPolicy.isSpecTransActivityPreDefined(container, activityRecord);
        if (activityRecord.isFullscreen() || isSpecTrans) {
            return false;
        }
        return true;
    }

    public void moveOtherActivities(ActivityRecordEx focus, int currentPosition) {
    }

    public void moveNextActivityToFrontIfNeeded(ActivityRecordEx curActivity) {
        HwMagicContainer container = this.mMwManager.getContainer(curActivity);
        if (container != null) {
            int curPosition = container.getBoundsPosition(curActivity.getRequestedOverrideBounds());
            if (curPosition == 1 || curPosition == 2) {
                ArrayList<ActivityRecordEx> tempActivityList = this.mPolicy.getAllActivities(curActivity.getActivityStackEx());
                ArrayList<ActivityRecordEx> resultActivityList = new ArrayList<>();
                ActivityRecordEx preActivity = curActivity;
                boolean isBeginToMove = false;
                Iterator<ActivityRecordEx> it = tempActivityList.iterator();
                while (it.hasNext()) {
                    ActivityRecordEx activity = it.next();
                    if (isBeginToMove) {
                        if (!(container.getBoundsPosition(activity.getRequestedOverrideBounds()) == curPosition && activity.equalsActivityRecord(preActivity.getResultTo()))) {
                            break;
                        }
                        resultActivityList.add(activity);
                        preActivity = activity;
                    } else if (curActivity.equalsActivityRecord(activity)) {
                        isBeginToMove = true;
                    }
                }
                for (int index = resultActivityList.size() - 1; index >= 0; index--) {
                    curActivity.getTaskRecordEx().moveActivityToFrontLocked(resultActivityList.get(index));
                }
            }
        }
    }

    public boolean isMastersFinish(ActivityRecordEx finishActivity, String finishReason) {
        return false;
    }

    private void setConfigChanges(HwMagicContainer container, ActivityRecordEx next) {
        if (!container.isFoldableDevice() && !container.isVirtualContainer()) {
            if (!container.getConfig().needRelaunch(Utils.getPackageName(next))) {
                next.getInfo().configChanges |= 3328;
                return;
            }
            next.getInfo().configChanges &= -3329;
        }
    }

    public boolean isExitSliding(ActivityRecordEx finishActivity, ActivityRecordEx secondSlaveActivity, String finishReason) {
        return false;
    }
}
