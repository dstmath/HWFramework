package com.android.server.wm;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.server.am.ActivityManagerServiceEx;
import com.huawei.android.app.ActivityOptionsEx;
import com.huawei.android.util.SlogEx;
import com.huawei.android.view.HwTaskSnapshotWrapper;
import com.huawei.server.magicwin.HwMagicWinAnimation;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.utils.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class HwMagicWinSplitManager {
    private static final int INVALID_STACK_ID = -1;
    private static final String MAGIC_WINDOW_FINISH_EVENT = "activity finish for magicwindow";
    private static final int STATUS_LOGIN = 1;
    private static final int STATUS_LOGOFF = 2;
    private static final String TAG = "HWMW_HwMagicWinSplitManager";
    private static final int VALUE_LAST_ACTIVITY_COUTN = 1;
    private static final int VALUE_TOP_ACTIVITY_INDEX = 0;
    private static ConcurrentHashMap<String, Integer> sPkgLoginConfigs = new ConcurrentHashMap<>();
    private ActivityManagerServiceEx mAms;
    private HwMagicWinAmsPolicy mAmsPolicy;
    private ConcurrentHashMap<String, Integer> mMainActivityStackList = new ConcurrentHashMap<>();
    private HwMagicWinManager mMwManager = null;

    public HwMagicWinSplitManager(ActivityManagerServiceEx ams, HwMagicWinManager manager, HwMagicWinAmsPolicy policy) {
        this.mMwManager = manager;
        this.mAms = ams;
        this.mAmsPolicy = policy;
    }

    private String getKeyStr(String pkgName, ActivityStackEx stack) {
        HwMagicWinAmsPolicy hwMagicWinAmsPolicy = this.mAmsPolicy;
        return hwMagicWinAmsPolicy.getJoinStr(pkgName, hwMagicWinAmsPolicy.getStackUserId(stack));
    }

    private int getFocusedUserId(HwMagicContainer container) {
        HwMagicWinAmsPolicy hwMagicWinAmsPolicy = this.mAmsPolicy;
        return hwMagicWinAmsPolicy.getStackUserId(hwMagicWinAmsPolicy.getFocusedTopStack(container));
    }

    private void addStackToSplitScreenList(ActivityStackEx stack, int position, String pkgName) {
        HwMagicWinCombineManagerImpl.getInstance().addStackToSplitScreenList(stack, position, pkgName, this.mAmsPolicy.getStackUserId(stack));
    }

    private void clearSplitScreenList(String pkgName, int userId) {
        HwMagicWinCombineManagerImpl.getInstance().clearSplitScreenList(pkgName, userId);
    }

    private void updateSplitScreenForegroundList(String pkgName, int userId, int displayId) {
        HwMagicWinCombineManagerImpl.getInstance().updateForegroundTaskIds(pkgName, userId, this, displayId);
    }

    private void removeStackFromSplitScreenList(ActivityStackEx stack, String pkgName) {
        HwMagicWinCombineManagerImpl.getInstance().removeStackFromSplitScreenList(stack, pkgName, this, this.mAmsPolicy.getStackUserId(stack));
    }

    private void takeTaskSnapshot(ActivityRecordEx topActivity) {
        if (topActivity != null) {
            this.mAms.takeTaskSnapshot(topActivity.getAppToken(), false);
        }
    }

    public void setLoginStatus(String pkg, int status) {
        SlogEx.i(TAG, "setLoginStatus : status " + status + " pkg " + pkg);
        if ((status == 1 || status == 2) && pkg != null && !pkg.isEmpty()) {
            sPkgLoginConfigs.put(pkg, Integer.valueOf(status));
        }
    }

    public void removeReportLoginStatus(String pkg) {
        if (pkg != null && !pkg.isEmpty() && sPkgLoginConfigs.containsKey(pkg)) {
            sPkgLoginConfigs.remove(pkg);
            SlogEx.i(TAG, "remove status pkg " + pkg);
        }
    }

    public boolean isInLoginStatus(String pkg) {
        Integer config;
        if (pkg == null || pkg.isEmpty() || !sPkgLoginConfigs.containsKey(pkg) || (config = sPkgLoginConfigs.get(pkg)) == null || config.intValue() != 1) {
            return false;
        }
        return true;
    }

    public void updateStackVisibility(ActivityStackEx stack, Bundle result) {
        HwMagicContainer container;
        if (stack != null && result != null && (container = this.mMwManager.getContainer(stack.getTopActivity())) != null) {
            String stackPkg = Utils.getRealPkgName(stack.getTopActivity());
            String focusPkg = this.mAmsPolicy.getFocusedStackPackageName(container);
            int stackUserId = this.mAmsPolicy.getStackUserId(stack);
            if (getFocusedUserId(container) == stackUserId) {
                int stackPos = container.getBoundsPosition(stack.getRequestedOverrideBounds());
                if (stackPos == 1 || stackPos == 2) {
                    int otherPos = stackPos == 1 ? 2 : 1;
                    ActivityStackEx currentStack = getTopMwStackByPosition(stackPos, stackPkg, true, stackUserId, container);
                    ActivityStackEx otherStack = getTopMwStackByPosition(otherPos, stackPkg, false, stackUserId, container);
                    if (otherStack == null) {
                        return;
                    }
                    if ((otherStack.isTopActivityVisible() || (stackPkg.equals(focusPkg) && isSpliteModeStack(this.mAmsPolicy.getFocusedTopStack(container)))) && currentStack != null && currentStack.equalsStack(stack)) {
                        result.putBoolean("RESULT_STACK_VISIBILITY", true);
                    }
                }
            }
        }
    }

    public ActivityStackEx getTopMwStackByPosition(int windowPosition, String pkg, int userId, int displayId) {
        return getTopMwStackByPosition(windowPosition, pkg, true, userId, this.mMwManager.getContainerByDisplayId(displayId));
    }

    private ActivityStackEx getTopMwStackByPosition(int windowPosition, String pkg, boolean isCheckUnderHomeStack, int userId, HwMagicContainer container) {
        if (container == null) {
            return null;
        }
        Iterator<ActivityStackEx> it = getWindowModeOrAllStack(pkg, false, isCheckUnderHomeStack, userId, container).iterator();
        while (it.hasNext()) {
            ActivityStackEx stack = it.next();
            if (windowPosition == container.getBoundsPosition(stack.getRequestedOverrideBounds())) {
                return stack;
            }
        }
        return null;
    }

    private ActivityStackEx getIndexMwStackByPosition(int windowPosition, int windowIndex, String pkg, int userId, HwMagicContainer container) {
        int offsetIndex = 0;
        Iterator<ActivityStackEx> it = getWindowModeOrAllStack(pkg, false, false, userId, container).iterator();
        while (it.hasNext()) {
            ActivityStackEx stack = it.next();
            if (windowPosition == container.getBoundsPosition(stack.getRequestedOverrideBounds())) {
                if (offsetIndex == windowIndex) {
                    return stack;
                }
                offsetIndex++;
            }
        }
        return null;
    }

    public void addOrUpdateMainActivityStat(HwMagicContainer container, ActivityRecordEx ar) {
        if (this.mAmsPolicy.isMainActivity(container, ar) || this.mAmsPolicy.isRelatedActivity(container, ar)) {
            this.mMainActivityStackList.put(this.mAmsPolicy.getJoinStr(Utils.getPackageName(ar), ar.getUserId()), Integer.valueOf(ar.getStackId()));
        }
    }

    public void quitMagicSplitScreenMode(String pkgName, int skipStackId, boolean isClearStack, int userId, HwMagicContainer container) {
        if (container != null) {
            clearSplitScreenList(pkgName, userId);
            ArrayList<ActivityStackEx> stacksInSamePkg = getWindowModeOrAllStack(pkgName, false, true, userId, container);
            synchronized (this.mAms.getActivityTaskManagerEx().getGlobalLock()) {
                this.mAms.getWindowManagerServiceEx().deferSurfaceLayout();
                ActivityDisplayEx display = container.getActivityDisplay();
                ActivityStackEx mainStack = getMainActivityStack(pkgName, userId);
                if (!(mainStack == null || display == null || !ActivityStackEx.containsStack(stacksInSamePkg, mainStack) || mainStack.getStackId() == skipStackId)) {
                    if (isClearStack) {
                        display.moveStackBehindStack(mainStack, display.getHomeStackEx());
                    }
                    this.mAmsPolicy.getMode(mainStack.getTopActivity()).setActivityBoundByMode(this.mAmsPolicy.getAllActivities(mainStack), pkgName, null);
                    this.mAms.resizeStack(mainStack.getStackId(), container.getBounds(3, pkgName), false, false, false, 0);
                }
                Iterator<ActivityStackEx> it = stacksInSamePkg.iterator();
                while (it.hasNext()) {
                    ActivityStackEx stack = it.next();
                    int position = container.getBoundsPosition(stack.getRequestedOverrideBounds());
                    if (position == 1 || position == 2) {
                        if (stack.getStackId() != skipStackId) {
                            if (!stack.equalsStack(mainStack)) {
                                if (!isClearStack || !isNeedNewTaskStack(stack)) {
                                    if (isClearStack) {
                                        display.moveStackBehindStack(stack, display.getHomeStackEx());
                                    }
                                    moveStackToPosition(stack, 3, pkgName);
                                    combineStackToMainStack(stack, mainStack);
                                } else {
                                    this.mAms.getActivityTaskManagerEx().removeStackFromStackSupervisor(stack);
                                }
                            }
                        }
                    }
                }
                if (!isClearStack && pkgName != null && pkgName.equals(this.mAmsPolicy.getFocusedStackPackageName(container))) {
                    updateSystemUiVisibility(this.mAmsPolicy.getFocusedTopStack(container));
                }
                this.mAms.getWindowManagerServiceEx().continueSurfaceLayout();
            }
            this.mMwManager.getUIController().updateBgColor(container.getDisplayId());
        }
    }

    public boolean isNeedProcessCombineStack(ActivityStackEx stack, boolean isNeedChangeBound) {
        ActivityStackEx mainStack;
        if (stack == null || !isNeedNewTaskStack(stack) || (mainStack = getMainActivityStack(stack.getTopActivity())) == null || mainStack.equalsStack(stack)) {
            return false;
        }
        if (isNeedChangeBound) {
            moveStackToPosition(stack, 3, Utils.getRealPkgName(stack.getTopActivity()));
        }
        combineStackToMainStack(stack, mainStack);
        return true;
    }

    private void combineStackToMainStack(ActivityStackEx stack, ActivityStackEx toStack) {
        if (!(toStack == null || !isNeedNewTaskStack(stack) || toStack.equalsStack(stack))) {
            TaskRecordEx topTask = toStack.topTask();
            ArrayList<TaskRecordEx> tasks = stack.getAllTaskRecordExs();
            for (int i = 0; i < tasks.size(); i++) {
                TaskRecordEx task = tasks.get(i);
                boolean isTopTask = true;
                if (i != tasks.size() - 1) {
                    isTopTask = false;
                }
                task.reparent(toStack, true, TaskRecordEx.REPARENT_KEEP_STACK_AT_FRONT, isTopTask, true, true, "reparent for quit magic window split mode");
                if (isTopTask) {
                    int adjustPostion = 0;
                    if (isVoipActivity(topTask.getTopActivity())) {
                        adjustPostion = 1;
                    }
                    Iterator<ActivityRecordEx> it = new ArrayList<>(task.getActivityRecordExs()).iterator();
                    while (it.hasNext()) {
                        it.next().reparent(topTask, topTask.getChildCount() - adjustPostion, "reparent for quit magic window split mode");
                    }
                    task.getActivityTaskManagerServiceEx().getRecentTasksEx().removeTaskRecord(task);
                    task.getActivityTaskManagerServiceEx().getRecentTasksEx().addTaskRecord(topTask);
                    toStack.removeTask(task, "Remove for quit magic window split mode", 0);
                    this.mAms.getActivityTaskManagerEx().removeTaskByIdLockedFromStackSupervisor(task.getTaskId(), true, true, true, "Remove for quit magic window split mode");
                }
            }
            this.mAms.getActivityTaskManagerEx().removeStackFromStackSupervisor(stack);
        }
    }

    public void showMoveAnimation(ActivityRecordEx activityRecord, int rightCheckPosition) {
        String pkgName = Utils.getRealPkgName(activityRecord);
        HwMagicContainer container = this.mMwManager.getContainer(activityRecord);
        if (container != null && container.getConfig().isSupportAppTaskSplitScreen(pkgName) && !this.mAmsPolicy.isRelatedActivity(container, activityRecord)) {
            int position = container.getBoundsPosition(activityRecord.getActivityStackEx().getRequestedOverrideBounds());
            ActivityStackEx leftTopStack = getTopMwStackByPosition(1, pkgName, false, activityRecord.getUserId(), container);
            ActivityStackEx rightUnderStack = getIndexMwStackByPosition(2, rightCheckPosition, pkgName, activityRecord.getUserId(), container);
            if (position == 2 && rightUnderStack == null && !activityRecord.getActivityStackEx().equalsStack(leftTopStack) && leftTopStack != null && !this.mAmsPolicy.isMainActivity(container, leftTopStack.getTopActivity())) {
                if (!isMainStack(pkgName, leftTopStack) || hasDefaultFullscreenActivity(pkgName, leftTopStack)) {
                    this.mMwManager.getWmsPolicy().startMoveAnimationFullScreen(leftTopStack.getTopActivity().getAppToken(), activityRecord.getAppToken());
                } else {
                    this.mMwManager.getWmsPolicy().startMoveAnimation(leftTopStack.getTopActivity().getAppToken(), activityRecord.getAppToken(), pkgName, false);
                }
            }
        }
    }

    private boolean hasDefaultFullscreenActivity(String packageName, ActivityStackEx stack) {
        HwMagicContainer container = this.mMwManager.getContainer(stack.getTopActivity());
        if (container == null) {
            return false;
        }
        Iterator<ActivityRecordEx> it = this.mAmsPolicy.getAllActivities(stack).iterator();
        while (it.hasNext()) {
            if (container.getConfig().isDefaultFullscreenActivity(packageName, Utils.getClassName(it.next()))) {
                return true;
            }
        }
        return false;
    }

    private ActivityStackEx createStackForSplit(ActivityRecordEx activity, ActivityStackEx stack) {
        ActivityStackEx newStack;
        synchronized (this.mAms.getActivityTaskManagerEx().getGlobalLock()) {
            try {
                TaskRecordEx newTask = stack.createTaskRecordEx(this.mAms.getActivityTaskManagerEx().getNextTaskIdForUserLockedFromStackSupervisor(activity.getUserId()), activity.getActivityInfo(), activity.getIntent(), true);
                if (newTask == null) {
                    return null;
                }
                activity.reparent(newTask, 0, "magicwin move activity to new task for split mode");
                ActivityOptionsEx tmpOptions = ActivityOptionsEx.makeBasic();
                tmpOptions.setLaunchWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
                ActivityDisplayEx display = getActivityDisplayEx(activity);
                if (display == null) {
                    return null;
                }
                ActivityStackEx newStack2 = display.getOrCreateStackEx((ActivityRecordEx) null, tmpOptions.getActivityOptions(), newTask, newTask.getActivityType(), true);
                if (newStack2 != null) {
                    if (!newStack2.equalsStack(stack)) {
                        newTask.reparent(newStack2, true, TaskRecordEx.REPARENT_MOVE_STACK_TO_FRONT, true, true, true, "magicwin move task to new stack for split mode");
                        if (newStack2.isHwActivityStack()) {
                            newStack = newStack2;
                            newStack.setMwNewTaskSplitStack(true);
                        } else {
                            newStack = newStack2;
                        }
                        return newStack;
                    }
                }
                return null;
            } catch (Throwable th) {
                newTask = th;
                throw newTask;
            }
        }
    }

    private boolean isTaskIdInContainer(String pkg, int taskId, HwMagicContainer container) {
        ActivityDisplayEx display;
        if (container == null || (display = container.getActivityDisplay()) == null) {
            return false;
        }
        synchronized (this.mAms.getActivityTaskManagerEx().getGlobalLock()) {
            for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStackEx stack = display.getChildAt(stackNdx);
                String packageName = Utils.getRealPkgName(stack.getTopActivity());
                TaskRecordEx taskRecord = stack.taskForIdLocked(taskId);
                if (packageName != null && pkg.equals(packageName)) {
                    if (taskRecord != null) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public HwMagicContainer getMagicContainer(String pkg, int taskId) {
        HwMagicContainer virtual = this.mMwManager.getVirtualContainer();
        if (isTaskIdInContainer(pkg, taskId, virtual)) {
            return virtual;
        }
        HwMagicContainer local = this.mMwManager.getLocalContainer();
        if (isTaskIdInContainer(pkg, taskId, local)) {
            return local;
        }
        return null;
    }

    public void setTaskPosition(String pkg, int taskId, int targetPosition) {
        int newTaskId;
        ActivityStackEx stack;
        ActivityStackEx stack2;
        HwMagicContainer container = getMagicContainer(pkg, taskId);
        if (container != null) {
            ActivityStackEx stack3 = this.mAmsPolicy.getFocusedTopStack(container);
            SlogEx.i(TAG, "setTaskPosition : stack = " + stack3 + ", pkg = " + pkg);
            if (stack3 != null && pkg != null && container.getConfig().isSupportAppTaskSplitScreen(pkg)) {
                int position = container.getBoundsPosition(stack3.getRequestedOverrideBounds());
                ActivityStackEx mainStack = getMainActivityStack(stack3.getTopActivity());
                if (mainStack != null && mainStack.inHwMagicWindowingMode() && position != targetPosition) {
                    if (mainStack.equalsStack(stack3)) {
                        ActivityRecordEx top = stack3.getTopActivity();
                        if (container.getConfig().isNeedStartByNewTaskActivity(pkg, Utils.getClassName(top)) && (stack2 = createStackForSplit(top, stack3)) != null) {
                            stack = stack2;
                            newTaskId = stack2.topTask().getTaskId();
                        } else {
                            return;
                        }
                    } else {
                        stack = stack3;
                        newTaskId = taskId;
                    }
                    showTaskMoveAnimation(pkg, stack, targetPosition, position);
                    if (pkg.equals(this.mAmsPolicy.getFocusedStackPackageName(container)) && stack.isInStackLocked(stack.taskForIdLocked(newTaskId))) {
                        if (targetPosition == 5 || targetPosition == 0 || targetPosition == 3) {
                            SlogEx.i(TAG, "setTaskPosition quit split mode");
                            quitMagicSplitScreenMode(pkg, -1, false, this.mAmsPolicy.getStackUserId(stack), container);
                            return;
                        }
                        SlogEx.i(TAG, "setTaskPosition function: call resizeStack");
                        adjustBackgroundStackPosition(pkg, stack, this.mAmsPolicy.getStackUserId(stack), container);
                        this.mMwManager.getUIController().updateBgColor(container.getDisplayId());
                    }
                }
            }
        }
    }

    private void updateSystemUiVisibility(ActivityStackEx stack) {
        if (stack != null && stack.getDisplayPolicyEx() != null) {
            stack.getDisplayPolicyEx().resetSystemUiVisibilityLw();
        }
    }

    private void showTaskMoveAnimation(String pkg, ActivityStackEx stack, int targetPosition, int position) {
        ActivityStackEx exitStack;
        if ((position == 5 || position == 3) && targetPosition == 2) {
            if (stack.getTopActivity() != null) {
                this.mMwManager.getWmsPolicy().startSplitAnimation(stack.getTopActivity().getAppToken(), pkg);
            }
        } else if ((targetPosition == 5 || targetPosition == 0 || targetPosition == 3) && position == 2) {
            if (stack.getTopActivity() != null) {
                this.mMwManager.getWmsPolicy().startExitSplitAnimation(stack.getTopActivity().getAppToken(), HwMagicWinAnimation.INVALID_THRESHOLD);
            }
        } else if (targetPosition == 2 && position == 1) {
            ActivityRecordEx enterRecord = stack.getTopActivity();
            if (enterRecord != null && (exitStack = getTopMwStackByPosition(2, pkg, false, enterRecord.getUserId(), this.mMwManager.getContainer(enterRecord))) != null && exitStack.getTopActivity() != null) {
                this.mMwManager.getWmsPolicy().startMoveAnimation(enterRecord.getAppToken(), exitStack.getTopActivity().getAppToken(), pkg, true);
            }
        } else {
            SlogEx.d(TAG, "not need show animation");
        }
    }

    private ArrayList<ActivityStackEx> getWindowModeOrAllStack(String pkg, boolean isAll, boolean getUnderHomeStacks, int userId, HwMagicContainer container) {
        String stackPkg;
        ArrayList<ActivityStackEx> stacks = new ArrayList<>();
        if (pkg == null) {
            return stacks;
        }
        ActivityDisplayEx display = container == null ? this.mAms.getActivityTaskManagerEx().getDefaultDisplayEx() : container.getActivityDisplay();
        if (display == null) {
            return stacks;
        }
        boolean isVirtual = container != null && container.isVirtualContainer();
        ActivityStackEx mainStack = getMainActivityStack(pkg, userId);
        boolean isMainStack = false;
        for (int i = display.getStackExs().size() - 1; i >= 0; i--) {
            ActivityStackEx otherStack = display.getChildAt(i);
            ActivityStackEx homeStack = display.getHomeStackEx();
            if (!(getUnderHomeStacks || homeStack == null || otherStack.getStackId() != homeStack.getStackId())) {
                break;
            }
            if (isVirtual && mainStack != null && otherStack.getStackId() == mainStack.getStackId()) {
                isMainStack = true;
            }
            if (isMainStack && !getUnderHomeStacks) {
                int stackPos = container.getBoundsPosition(mainStack.getRequestedOverrideBounds());
                SlogEx.d(TAG, "getWindowModeOrAllStack virtual stacks stackPos " + stackPos);
                if (stackPos == 5) {
                    break;
                }
            }
            if ((otherStack.getWindowingMode() == 103 || isAll) && (stackPkg = Utils.getRealPkgName(otherStack.getTopActivity())) != null && stackPkg.equals(pkg) && userId == this.mAmsPolicy.getStackUserId(otherStack)) {
                stacks.add(otherStack);
            }
        }
        return stacks;
    }

    public void multWindowModeProcess(ActivityRecordEx focus, int windowMode) {
        int targetMode;
        int posFront;
        ActivityStackEx stack;
        if (focus != null) {
            String focusPkg = Utils.getRealPkgName(focus);
            if (isSpliteModeStack(focus.getActivityStackEx())) {
                int posFront2 = 0;
                int targetMode2 = windowMode;
                int posFront3 = 1;
                boolean isCurrentRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
                if (windowMode == 100) {
                    posFront2 = isCurrentRtl ? 1 : 2;
                    targetMode2 = HwMagicWinAnimationScene.SCENE_MIDDLE;
                }
                if (windowMode == 101) {
                    if (isCurrentRtl) {
                        posFront3 = 2;
                    }
                    posFront = posFront3;
                    targetMode = 100;
                } else {
                    posFront = posFront2;
                    targetMode = targetMode2;
                }
                if (posFront != 0 && (stack = getTopMwStackByPosition(posFront, focusPkg, false, focus.getUserId(), this.mMwManager.getContainer(focus))) != null) {
                    quitMagicSplitScreenMode(focusPkg, stack.getStackId(), true, focus.getUserId(), this.mMwManager.getContainer(focus));
                    synchronized (this.mAms.getActivityTaskManagerEx().getGlobalLock()) {
                        stack.moveToFront("move split pos " + posFront + " to top");
                        stack.setWindowingMode(targetMode);
                    }
                }
            }
        }
    }

    private boolean isMainOrAboveHome(ActivityDisplayEx display, ActivityStackEx stack, ActivityStackEx mainStack) {
        if (mainStack != null && stack.getStackId() == mainStack.getStackId()) {
            return true;
        }
        if (display.getIndexOf(stack) > display.getIndexOf(display.getHomeStackEx() == null ? mainStack : display.getHomeStackEx())) {
            return true;
        }
        return false;
    }

    private void adjustBackgroundStackPosition(String pkg, ActivityStackEx currentStack, int userId, HwMagicContainer container) {
        ActivityDisplayEx display = container.getActivityDisplay();
        if (display != null) {
            ActivityStackEx lastLeftStack = currentStack;
            ActivityStackEx mainStack = getMainActivityStack(currentStack.getTopActivity());
            ActivityStackEx topLeftStack = null;
            ArrayList<ActivityStackEx> rightStackList = new ArrayList<>();
            ArrayList<ActivityStackEx> stacks = getWindowModeOrAllStack(pkg, true, true, userId, container);
            synchronized (this.mAms.getActivityTaskManagerEx().getGlobalLock()) {
                this.mAms.getWindowManagerServiceEx().deferSurfaceLayout();
                int i = 1;
                int i2 = stacks.size() - 1;
                while (i2 >= 0) {
                    ActivityStackEx stack = stacks.get(i2);
                    int stackPos = container.getBoundsPosition(stack.getRequestedOverrideBounds());
                    if (!stack.equalsStack(currentStack)) {
                        if ((stackPos == 5 || stackPos == 3) && isMainOrAboveHome(display, stack, mainStack)) {
                            moveStackToPosition(stack, i, pkg);
                            addStackToSplitScreenList(stack, i, pkg);
                            if (mainStack == null || mainStack.getStackId() != stack.getStackId()) {
                                topLeftStack = stack;
                                if (lastLeftStack.equalsStack(currentStack)) {
                                    lastLeftStack = stack;
                                }
                            } else {
                                this.mAmsPolicy.removeRelatedActivity(container, stack, false);
                            }
                        } else if (stackPos == 2 && stack.inHwMagicWindowingMode()) {
                            rightStackList.add(stack);
                            removeStackFromSplitScreenList(stack, pkg);
                            display.moveStackBehindStack(stack, display.getHomeStackEx());
                        } else if (stackPos != 1 || !stack.inHwMagicWindowingMode()) {
                            SlogEx.d(TAG, "adjustBackgroundStackPosition stack not need move");
                        } else {
                            stack.moveToFront("move left stack under home to front for start split mode");
                        }
                    }
                    i2--;
                    i = 1;
                }
                stackPostProcess(rightStackList, handleMainStack(display, lastLeftStack, mainStack, topLeftStack), pkg, currentStack);
                this.mAms.getWindowManagerServiceEx().continueSurfaceLayout();
            }
        }
    }

    private ActivityStackEx handleMainStack(ActivityDisplayEx display, ActivityStackEx lastLeftStack, ActivityStackEx mainStack, ActivityStackEx topLeftStack) {
        if (mainStack != null) {
            finishActivitiesExceptMainAndRelated(mainStack);
            display.moveStackBehindStack(mainStack, lastLeftStack);
            if (topLeftStack == null) {
                return mainStack;
            }
        }
        return topLeftStack;
    }

    private void stackPostProcess(ArrayList<ActivityStackEx> rightStackList, ActivityStackEx topLeftStack, String pkg, ActivityStackEx currentStack) {
        moveStackToPosition(currentStack, 2, pkg);
        addStackToSplitScreenList(currentStack, 2, pkg);
        Iterator<ActivityStackEx> it = rightStackList.iterator();
        while (it.hasNext()) {
            ActivityStackEx rightStack = it.next();
            if (isNeedDestroyWhenReplaceOnRightStack(rightStack)) {
                this.mAms.getActivityTaskManagerEx().removeStackFromStackSupervisor(rightStack);
            } else {
                takeTaskSnapshot(rightStack.getTopActivity());
                moveStackToPosition(rightStack, 3, pkg);
                rightStack.ensureActivitiesVisibleLocked((ActivityRecordEx) null, 0, false);
            }
        }
        if (topLeftStack != null) {
            topLeftStack.ensureActivitiesVisibleLocked((ActivityRecordEx) null, 0, false);
        }
        updateSystemUiVisibility(currentStack);
    }

    private void finishActivitiesExceptMainAndRelated(ActivityStackEx stack) {
        ArrayList<ActivityRecordEx> records = this.mAmsPolicy.getAllActivities(stack);
        if (records != null && records.size() >= 1) {
            int mainIndex = records.size();
            for (int index = 0; index < records.size(); index++) {
                ActivityRecordEx record = records.get(index);
                HwMagicContainer container = this.mMwManager.getContainer(record);
                if (this.mAmsPolicy.isMainActivity(container, record)) {
                    mainIndex = index;
                } else if (mainIndex != records.size() && !this.mAmsPolicy.isRelatedActivity(container, record)) {
                    stack.finishActivityLocked(record, 0, (Intent) null, "activity finish for magicwindow", true, false);
                }
            }
        }
    }

    private boolean isNeedDestroyWhenReplaceOnRightStack(ActivityStackEx stack) {
        if (!isNeedNewTaskStack(stack)) {
            return false;
        }
        Iterator<ActivityRecordEx> it = this.mAmsPolicy.getAllActivities(stack).iterator();
        while (it.hasNext()) {
            if (isNeedDestroyWhenReplaceOnRight(it.next())) {
                return true;
            }
        }
        return false;
    }

    private boolean isNeedDestroyWhenReplaceOnRight(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        return container != null && container.getConfig().isNeedDestroyWhenReplaceOnRight(Utils.getPackageName(activity), Utils.getClassName(activity));
    }

    private boolean isNeedNewTaskActivity(ActivityRecordEx activity) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        return container != null && container.getConfig().isNeedStartByNewTaskActivity(Utils.getPackageName(activity), Utils.getClassName(activity));
    }

    public boolean isNeedNewTaskStack(ActivityStackEx stack) {
        if (stack.isHwActivityStack()) {
            return stack.isMwNewTaskSplitStack();
        }
        return false;
    }

    public boolean isVoipActivity(ActivityRecordEx activity) {
        return isNeedNewTaskActivity(activity) && !isNeedDestroyWhenReplaceOnRight(activity);
    }

    public void moveTaskToFullscreenIfNeed(ActivityRecordEx currentActivity, boolean isMoveStack) {
        HwMagicContainer container = this.mMwManager.getContainer(currentActivity);
        if (checkStatusForMoveTask(currentActivity, container)) {
            ActivityStackEx currentStack = currentActivity.getActivityStackEx();
            String pkg = Utils.getRealPkgName(currentActivity);
            if (isMoveStack) {
                moveStackToPosition(currentStack, 3, pkg);
            }
            ArrayList<ActivityRecordEx> tempActivityList = this.mAmsPolicy.getAllActivities(currentStack);
            if ((tempActivityList.size() == 1 && tempActivityList.get(0).equalsActivityRecord(currentActivity)) || isMoveStack) {
                removeStackFromSplitScreenList(currentActivity.getActivityStackEx(), pkg);
            }
            ActivityStackEx topLeftStack = null;
            Iterator<ActivityStackEx> it = getWindowModeOrAllStack(pkg, false, true, currentActivity.getUserId(), container).iterator();
            while (it.hasNext()) {
                ActivityStackEx stack = it.next();
                if (currentStack.getStackId() != stack.getStackId() || currentStack.numActivities() > 1) {
                    int stackPos = container.getBoundsPosition(stack.getRequestedOverrideBounds());
                    if (stackPos == 1 && topLeftStack == null) {
                        topLeftStack = stack;
                    }
                    if (stackPos == 2) {
                        return;
                    }
                }
            }
            if (topLeftStack != null) {
                if (isMainStack(pkg, topLeftStack)) {
                    clearSplitScreenList(pkg, currentActivity.getUserId());
                    if (!isMoveStack && currentActivity.isTopRunningActivity()) {
                        topLeftStack.moveToFront("move main stack to top");
                    }
                    this.mAmsPolicy.getMode(currentActivity).setActivityBoundByMode(this.mAmsPolicy.getAllActivities(topLeftStack), pkg, null);
                    this.mAms.resizeStack(topLeftStack.getStackId(), container.getBounds(3, pkg), false, false, false, 0);
                } else {
                    quitMagicSplitScreenMode(pkg, isMoveStack ? -1 : currentStack.getStackId(), false, currentActivity.getUserId(), this.mMwManager.getContainer(currentActivity));
                }
                this.mMwManager.getUIController().updateBgColor(container.getDisplayId());
            }
        }
    }

    private boolean checkStatusForMoveTask(ActivityRecordEx currentActivity, HwMagicContainer container) {
        if (container == null || currentActivity == null || container.isInFoldedStatus()) {
            return false;
        }
        String pkg = Utils.getRealPkgName(currentActivity);
        if (container.isInFoldedStatus() || !container.getConfig().isSupportAppTaskSplitScreen(pkg) || currentActivity.getActivityStackEx() == null || this.mAmsPolicy.isRelatedActivity(container, currentActivity)) {
            return false;
        }
        return true;
    }

    private void moveStackToPosition(ActivityStackEx stack, int position, String pkg) {
        HwMagicContainer container = this.mMwManager.getContainer(stack.getTopActivity());
        if (container != null) {
            if (stack.getWindowingMode() != 103) {
                stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
            }
            Rect bound = new Rect(container.getBounds(position, pkg));
            this.mAms.getWindowManagerServiceEx().deferSurfaceLayout();
            if (!isMainStack(pkg, stack) || position == 1 || position == 2) {
                container.getConfig().adjustSplitBound(position, bound);
                Iterator<TaskRecordEx> it = stack.getAllTaskRecordExs().iterator();
                while (it.hasNext()) {
                    Iterator<ActivityRecordEx> it2 = it.next().getActivityRecordExs().iterator();
                    while (it2.hasNext()) {
                        it2.next().setBounds(bound);
                    }
                }
            }
            this.mAms.resizeStack(stack.getStackId(), bound, false, false, false, 0);
            this.mAms.getWindowManagerServiceEx().continueSurfaceLayout();
        }
    }

    public void resizeStackIfNeedOnresume(ActivityRecordEx resumeActivity) {
        ActivityStackEx mainStack;
        HwMagicContainer container = this.mMwManager.getContainer(resumeActivity);
        if (container != null && resumeActivity != null) {
            String pkgName = Utils.getRealPkgName(resumeActivity);
            ActivityStackEx stack = resumeActivity.getActivityStackEx();
            if (pkgName != null && container.getConfig().isSupportAppTaskSplitScreen(pkgName) && (mainStack = getMainActivityStack(resumeActivity)) != null && !mainStack.equalsStack(stack) && isPkgSpliteScreenMode(resumeActivity, true)) {
                int targetPosition = container.getBoundsPosition(resumeActivity.getRequestedOverrideBounds());
                if ((targetPosition == 1 || targetPosition == 2) && targetPosition != container.getBoundsPosition(stack.getBounds())) {
                    moveStackToPosition(stack, targetPosition, pkgName);
                    addStackToSplitScreenList(stack, targetPosition, pkgName);
                }
            }
        }
    }

    public boolean isMainStack(String pkg, ActivityStackEx stack) {
        HwMagicContainer container;
        if (stack == null || (container = this.mMwManager.getContainer(stack.getTopActivity())) == null || !container.getConfig().isSupportAppTaskSplitScreen(pkg)) {
            return false;
        }
        ActivityStackEx mainStack = getMainActivityStack(stack.getTopActivity());
        SlogEx.i(TAG, "isMainStack mainStack " + mainStack + " stack " + stack);
        if (mainStack == null || mainStack.getStackId() != stack.getStackId()) {
            return false;
        }
        return true;
    }

    private ActivityStackEx getMainActivityStack(ActivityRecordEx ar) {
        String pkg = Utils.getRealPkgName(ar);
        if (pkg == null) {
            return null;
        }
        return getMainActivityStack(pkg, ar.getUserId());
    }

    public ActivityStackEx getMainActivityStack(String pkgName, int userId) {
        Integer stackId = this.mMainActivityStackList.get(this.mAmsPolicy.getJoinStr(pkgName, userId));
        if (stackId == null) {
            return null;
        }
        return this.mAmsPolicy.getActivityStackEx(stackId.intValue());
    }

    public boolean isMainStackInMwMode(ActivityRecordEx ar) {
        ActivityStackEx stack = getMainActivityStack(ar);
        return stack != null && stack.inHwMagicWindowingMode();
    }

    public boolean isPkgSpliteScreenMode(ActivityRecordEx ar, boolean checkUnderHomeStacks) {
        ActivityStackEx mainStack;
        String pkg = Utils.getRealPkgName(ar);
        HwMagicContainer container = this.mMwManager.getContainer(ar);
        if (pkg == null || container == null || !container.getConfig().isSupportAppTaskSplitScreen(pkg) || (mainStack = getMainActivityStack(ar)) == null || container.getBoundsPosition(mainStack.getRequestedOverrideBounds()) != 1) {
            return false;
        }
        Iterator<ActivityStackEx> it = getWindowModeOrAllStack(pkg, false, checkUnderHomeStacks, ar.getUserId(), container).iterator();
        while (it.hasNext()) {
            if (container.getBoundsPosition(it.next().getRequestedOverrideBounds()) == 2) {
                return true;
            }
        }
        return false;
    }

    public void updateSpliteStackSequence(ActivityDisplayEx display) {
        if (display != null) {
            HwMagicContainer container = this.mMwManager.getContainerByDisplayId(display.getDisplayId());
            String packageName = this.mAmsPolicy.getFocusedStackPackageName(container);
            if (container != null && container.getConfig().isSupportAppTaskSplitScreen(packageName)) {
                ActivityStackEx voipStack = null;
                ArrayList<ActivityStackEx> stacks = getWindowModeOrAllStack(packageName, false, false, getFocusedUserId(container), container);
                int i = stacks.size();
                while (true) {
                    i--;
                    if (i < 0) {
                        break;
                    }
                    ActivityStackEx stack = stacks.get(i);
                    if (isVoipActivity(stack.getTopActivity())) {
                        voipStack = stack;
                    } else if (container.getBoundsPosition(stack.getRequestedOverrideBounds()) == 2) {
                        stack.moveToFront("set magic window stack to focus");
                    }
                }
                if (voipStack != null) {
                    voipStack.moveToFront("set magic window stack to focus");
                }
            }
        }
    }

    public void setAboveStackToDefault(ActivityStackEx targetStack, int targetPosition) {
        HwMagicContainer container;
        String pkgName = Utils.getRealPkgName(targetStack.getTopActivity());
        if (isPkgSpliteScreenMode(targetStack.getTopActivity(), true) && isMainStack(pkgName, targetStack) && (container = this.mMwManager.getContainer(targetStack.getTopActivity())) != null) {
            ArrayList<ActivityStackEx> stacks = getWindowModeOrAllStack(pkgName, false, true, this.mAmsPolicy.getStackUserId(targetStack), container);
            synchronized (this.mAms.getActivityTaskManagerEx().getGlobalLock()) {
                ActivityDisplayEx display = container.getActivityDisplay();
                if (display != null) {
                    for (int i = stacks.size() - 1; i >= 0; i--) {
                        ActivityStackEx stack = stacks.get(i);
                        int stackPos = container.getBoundsPosition(stack.getRequestedOverrideBounds());
                        if (!stack.equalsStack(targetStack)) {
                            if (stackPos == targetPosition) {
                                if (isNeedDestroyWhenReplaceOnRightStack(stack)) {
                                    this.mAms.getActivityTaskManagerEx().removeStackFromStackSupervisor(stack);
                                } else {
                                    moveStackToPosition(stack, 3, pkgName);
                                    display.moveStackBehindStack(stack, targetStack);
                                }
                                removeStackFromSplitScreenList(stack, pkgName);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<ActivityStackEx> getAllStackUnderHomeOfPkg(String pkgName, int userId, ActivityStackEx mainStack, HwMagicContainer container) {
        boolean isUnderHome = false;
        List<ActivityStackEx> underHomeStacks = new ArrayList<>();
        ActivityDisplayEx display = container.getActivityDisplay();
        if (display == null) {
            return underHomeStacks;
        }
        ArrayList<ActivityStackEx> mStackExs = display.getStackExs();
        ActivityStackEx homeStack = display.getHomeStackEx();
        boolean isUnderMain = false;
        for (int i = mStackExs.size() - 1; i >= 0; i--) {
            ActivityStackEx stack = mStackExs.get(i);
            if (homeStack == null || stack.getStackId() != homeStack.getStackId()) {
                if (isUnderMain) {
                    int pos = container.getBoundsPosition(stack.getRequestedOverrideBounds());
                    SlogEx.i(TAG, "isMainStack getAllStackUnderHomeOfPkg pos " + pos);
                    if (pos == 5) {
                        isUnderHome = true;
                    }
                }
                if (homeStack == null && stack.getStackId() == mainStack.getStackId()) {
                    isUnderMain = true;
                }
                if (isUnderHome && this.mAmsPolicy.getStackUserId(stack) == userId && pkgName.equals(Utils.getRealPkgName(stack.getTopActivity())) && isSpliteModeStack(stack)) {
                    underHomeStacks.add(stack);
                }
            } else {
                isUnderHome = true;
            }
        }
        return underHomeStacks;
    }

    private void moveSplitStacksToFront(List<ActivityStackEx> stacks, ActivityStackEx resumeStack, ActivityStackEx mainStack, String pkgName) {
        ActivityDisplayEx display;
        HwMagicContainer container = this.mMwManager.getContainer(resumeStack.getTopActivity());
        if (!(container == null || (display = container.getActivityDisplay()) == null)) {
            synchronized (this.mAms.getActivityTaskManagerEx().getGlobalLock()) {
                if (!resumeStack.equalsStack(mainStack) || !isSpliteModeStack(resumeStack)) {
                    ActivityStackEx lastLeftStack = null;
                    for (int i = stacks.size() - 1; i >= 0; i--) {
                        ActivityStackEx stack = stacks.get(i);
                        int stackPosition = container.getBoundsPosition(stack.getBounds());
                        if (isSpliteModeStack(stack) && !stack.equalsStack(mainStack)) {
                            display.moveStackBehindStack(stack, resumeStack);
                            if (stackPosition == 1 && lastLeftStack == null) {
                                lastLeftStack = stack;
                            }
                        }
                    }
                    setLeftStackFocued(resumeStack, pkgName, mainStack, lastLeftStack);
                    return;
                }
                ActivityStackEx otherSideStack = getTopMwStackByPosition(2, pkgName, true, this.mAmsPolicy.getStackUserId(resumeStack), container);
                if (otherSideStack != null && ActivityStackEx.containsStack(stacks, otherSideStack)) {
                    this.mAms.getActivityTaskManagerEx().startActivityFromRecents(otherSideStack.topTask().getTaskId(), (Bundle) null);
                }
            }
        }
    }

    private ActivityDisplayEx getActivityDisplayEx(ActivityStackEx stackEx) {
        HwMagicContainer container;
        if (stackEx == null || (container = this.mMwManager.getContainerByDisplayId(stackEx.getDisplayId())) == null) {
            return null;
        }
        return container.getActivityDisplay();
    }

    private ActivityDisplayEx getActivityDisplayEx(ActivityRecordEx activityRecordEx) {
        HwMagicContainer container;
        if (activityRecordEx == null || (container = this.mMwManager.getContainer(activityRecordEx)) == null) {
            return null;
        }
        return container.getActivityDisplay();
    }

    private void setLeftStackFocued(ActivityStackEx resumeStack, String pkgName, ActivityStackEx mainStack, ActivityStackEx lastLeftStack) {
        ActivityStackEx otherSideStack;
        ActivityDisplayEx display = getActivityDisplayEx(resumeStack);
        if (display != null && isSpliteModeStack(mainStack)) {
            if (lastLeftStack == null) {
                display.moveStackBehindStack(mainStack, resumeStack);
            } else {
                display.moveStackBehindStack(mainStack, lastLeftStack);
            }
            if (this.mMwManager.isSlave(resumeStack.getTopActivity()) && (otherSideStack = getTopMwStackByPosition(1, pkgName, false, this.mAmsPolicy.getStackUserId(resumeStack), this.mMwManager.getContainerByDisplayId(display.getDisplayId()))) != null) {
                otherSideStack.getActivityTaskManagerServiceEx().setFocusedStack(otherSideStack.getStackId());
            }
        }
    }

    public void resizeStackWhileResumeSplitAppIfNeed(String pkgName, ActivityRecordEx activity) {
        ActivityStackEx mainStack = getMainActivityStack(activity);
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (mainStack != null && container != null && container.getConfig().isSupportAppTaskSplitScreen(pkgName)) {
            updateSplitScreenForegroundList(pkgName, activity.getUserId(), activity.getDisplayEx().getDisplayId());
            if (!isSpliteModeStack(activity.getActivityStackEx()) || isPkgSpliteScreenMode(activity, true)) {
                List<ActivityStackEx> underHomeStacks = getAllStackUnderHomeOfPkg(pkgName, activity.getUserId(), mainStack, container);
                if (!underHomeStacks.isEmpty() && !ActivityStackEx.containsStack(underHomeStacks, activity.getActivityStackEx()) && isSpliteModeStack(activity.getActivityStackEx())) {
                    moveSplitStacksToFront(underHomeStacks, activity.getActivityStackEx(), mainStack, pkgName);
                    return;
                }
                return;
            }
            SlogEx.d(TAG, " resizeMainStackWhileResumeIfNeed quit split mode: " + activity);
            quitMagicSplitScreenMode(pkgName, -1, false, activity.getUserId(), this.mMwManager.getContainer(activity));
        }
    }

    public void resizeSplitStackBeforeResume(ActivityRecordEx activity, String pkgName) {
        HwMagicContainer container = this.mMwManager.getContainer(activity);
        if (container != null) {
            ActivityStackEx resumeStack = activity.getActivityStackEx();
            ActivityStackEx mainStack = getMainActivityStack(activity);
            if (mainStack != null && mainStack.getStackId() != resumeStack.getStackId() && isPkgSpliteScreenMode(activity, true)) {
                boolean isMainStackAboveHome = false;
                ActivityStackEx topSplitStack = null;
                Iterator<ActivityStackEx> it = getWindowModeOrAllStack(pkgName, false, false, activity.getUserId(), container).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ActivityStackEx stack = it.next();
                    int stackPosition = container.getBoundsPosition(stack.getRequestedOverrideBounds());
                    if (topSplitStack == null && (stackPosition == 2 || stackPosition == 1)) {
                        topSplitStack = stack;
                    }
                    if (stack.getStackId() == mainStack.getStackId()) {
                        isMainStackAboveHome = true;
                        break;
                    }
                }
                if (isMainStackAboveHome && topSplitStack != null) {
                    ActivityStackEx topRightStack = getTopMwStackByPosition(2, pkgName, false, activity.getUserId(), this.mMwManager.getContainer(activity));
                    int targetPosition = container.getBoundsPosition(topSplitStack.getRequestedOverrideBounds());
                    if (topRightStack == null || topRightStack.equalsStack(resumeStack)) {
                        targetPosition = 2;
                    }
                    if (targetPosition != container.getBoundsPosition(resumeStack.getRequestedOverrideBounds())) {
                        moveStackToPosition(resumeStack, targetPosition, pkgName);
                        addStackToSplitScreenList(resumeStack, targetPosition, pkgName);
                    }
                } else if (resumeStack.getWindowingMode() == 103) {
                    moveTaskToFullscreenIfNeed(activity, true);
                }
            }
        }
    }

    public void resizeWhenMoveBackIfNeed(ActivityStackEx stack) {
        ActivityDisplayEx display;
        if (stack != null) {
            HwMagicContainer container = this.mMwManager.getContainer(stack.getTopActivity());
            String pkgName = Utils.getRealPkgName(stack.getTopActivity());
            if (container != null && container.getConfig().isSupportAppTaskSplitScreen(pkgName) && (display = container.getActivityDisplay()) != null) {
                int position = container.getBoundsPosition(stack.getBounds());
                if (position == 1 && isMainStack(pkgName, stack)) {
                    synchronized (this.mAms.getActivityTaskManagerEx().getGlobalLock()) {
                        Iterator<ActivityStackEx> it = getWindowModeOrAllStack(pkgName, false, false, this.mAmsPolicy.getStackUserId(stack), container).iterator();
                        while (it.hasNext()) {
                            ActivityStackEx currentStack = it.next();
                            if (container.getBoundsPosition(currentStack.getRequestedOverrideBounds()) == 2) {
                                display.moveStackBehindStack(currentStack, stack);
                            }
                        }
                    }
                    removeStackFromSplitScreenList(stack, pkgName);
                } else if (position == 1 || position == 2) {
                    this.mAms.getWindowManagerServiceEx().deferSurfaceLayout();
                    if (position == 2) {
                        showMoveAnimation(stack.getTopActivity(), 0);
                    }
                    ActivityRecordEx topRecord = stack.getTopActivity();
                    takeTaskSnapshot(topRecord);
                    moveTaskToFullscreenIfNeed(topRecord, true);
                    this.mAms.getWindowManagerServiceEx().continueSurfaceLayout();
                }
            }
        }
    }

    public boolean isInAppSplite(ActivityStackEx stack, boolean isUnderHomeStacks) {
        if (stack == null) {
            return false;
        }
        HwMagicContainer container = this.mMwManager.getContainer(stack.getTopActivity());
        String pkgName = Utils.getRealPkgName(stack.getTopActivity());
        if (container == null || !container.getConfig().isSupportAppTaskSplitScreen(pkgName)) {
            return false;
        }
        boolean ret = isPkgSpliteScreenMode(stack.getTopActivity(), isUnderHomeStacks);
        SlogEx.i(TAG, "isInAppSplite:ret " + ret + " pkgName " + pkgName);
        return ret;
    }

    public ActivityRecordEx getLatestActivityBySplitMode(String packageName, ActivityStackEx stack, ActivityRecordEx topActivity, ActivityRecordEx latestActivity) {
        ActivityDisplayEx display;
        ActivityStackEx aboveStack;
        if (!stack.inHwMagicWindowingMode()) {
            return latestActivity;
        }
        if (isSpliteModeStack(stack) || (display = getActivityDisplayEx(topActivity)) == null || !isMainStack(packageName, stack)) {
            return topActivity;
        }
        int mainIndex = display.getIndexOf(stack);
        if (mainIndex >= display.getStackExs().size() - 1 || (aboveStack = (ActivityStackEx) display.getStackExs().get(mainIndex + 1)) == null || !packageName.equals(Utils.getRealPkgName(aboveStack.getTopActivity())) || stack.getLRUActivities().size() <= 0 || this.mAmsPolicy.getStackUserId(stack) != topActivity.getUserId()) {
            return latestActivity;
        }
        return (ActivityRecordEx) stack.getLRUActivities().get(stack.getLRUActivities().size() - 1);
    }

    public boolean isSpliteModeStack(ActivityStackEx stack) {
        HwMagicContainer container = this.mMwManager.getContainer(stack.getTopActivity());
        if (container == null) {
            return false;
        }
        int stackPos = container.getBoundsPosition(stack.getRequestedOverrideBounds());
        if ((stackPos == 1 || stackPos == 2) && stack.inHwMagicWindowingMode()) {
            return true;
        }
        return false;
    }

    public ActivityStackEx getNewTopStack(ActivityStackEx oldStack, int otherSideModeToChange) {
        ActivityStackEx newTopTask;
        String pkgName = Utils.getRealPkgName(oldStack.getTopActivity());
        HwMagicContainer container = this.mMwManager.getContainer(oldStack.getTopActivity());
        if (container == null || !isPkgSpliteScreenMode(oldStack.getTopActivity(), false)) {
            return null;
        }
        int oldStackMwPos = container.getBoundsPosition(oldStack.getRequestedOverrideBounds());
        int position = otherSideModeToChange == 100 ? 1 : 2;
        if (oldStackMwPos == position || (newTopTask = getTopMwStackByPosition(position, pkgName, false, this.mAmsPolicy.getStackUserId(oldStack), container)) == null || oldStack.equalsStack(newTopTask)) {
            return null;
        }
        newTopTask.moveToFront("move new top to front");
        return newTopTask;
    }

    public void addOtherSnapShot(ActivityStackEx stack, HwActivityTaskManagerServiceEx hwAtmsEx, List<HwTaskSnapshotWrapper> snapShots) {
        String pkgName = Utils.getRealPkgName(stack.getTopActivity());
        HwMagicContainer container = this.mMwManager.getContainer(stack.getTopActivity());
        if (container != null && isPkgSpliteScreenMode(stack.getTopActivity(), false)) {
            int otherTaskPos = 2;
            if (container.getBoundsPosition(stack.getRequestedOverrideBounds()) == 2) {
                otherTaskPos = 1;
            }
            ActivityStackEx otherStack = getTopMwStackByPosition(otherTaskPos, pkgName, false, this.mAmsPolicy.getStackUserId(stack), container);
            TaskRecordEx otherMwTask = otherStack != null ? otherStack.topTask() : null;
            if (otherMwTask != null) {
                HwTaskSnapshotWrapper shot = ActivityTaskManagerServiceEx.getTaskSnapshot(hwAtmsEx, otherMwTask.getTaskId(), false);
                if (shot != null && otherTaskPos == 1) {
                    snapShots.add(0, shot);
                } else if (shot != null) {
                    snapShots.add(shot);
                } else {
                    SlogEx.d(TAG, "not snapShot found");
                }
            }
        }
    }
}
