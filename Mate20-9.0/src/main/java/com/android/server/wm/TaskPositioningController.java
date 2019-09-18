package com.android.server.wm;

import android.app.IActivityManager;
import android.freeform.HwFreeFormUtils;
import android.os.Handler;
import android.os.Looper;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.IWindow;
import com.android.internal.annotations.GuardedBy;
import com.android.server.input.InputManagerService;
import com.android.server.input.InputWindowHandle;

class TaskPositioningController {
    private final IActivityManager mActivityManager;
    private final Handler mHandler;
    private final InputManagerService mInputManager;
    private final InputMonitor mInputMonitor;
    private final WindowManagerService mService;
    @GuardedBy("WindowManagerSerivce.mWindowMap")
    private TaskPositioner mTaskPositioner;

    /* access modifiers changed from: package-private */
    public boolean isPositioningLocked() {
        return this.mTaskPositioner != null;
    }

    /* access modifiers changed from: package-private */
    public InputWindowHandle getDragWindowHandleLocked() {
        if (this.mTaskPositioner != null) {
            return this.mTaskPositioner.mDragWindowHandle;
        }
        return null;
    }

    TaskPositioningController(WindowManagerService service, InputManagerService inputManager, InputMonitor inputMonitor, IActivityManager activityManager, Looper looper) {
        this.mService = service;
        this.mInputMonitor = inputMonitor;
        this.mInputManager = inputManager;
        this.mActivityManager = activityManager;
        this.mHandler = new Handler(looper);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0024, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r11.mActivityManager.setFocusedTask(r0.getTask().mTaskId);
     */
    public boolean startMovingTask(IWindow window, float startX, float startY) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                WindowState win = this.mService.windowForClientLocked((Session) null, window, false);
                if (!startPositioningLocked(win, false, false, startX, startY)) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void handleTapOutsideTask(DisplayContent displayContent, int x, int y) {
        this.mHandler.post(new Runnable(x, y, displayContent) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ DisplayContent f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                TaskPositioningController.lambda$handleTapOutsideTask$0(TaskPositioningController.this, this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0066, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006d, code lost:
        if (android.util.HwPCUtils.isPcCastModeInServer() == false) goto L_0x0087;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006f, code lost:
        r12.mService.setFocusedDisplay(r15.getDisplayId(), false, "handleTapOutsideTaskXY");
        r4 = r12.mService;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007c, code lost:
        if (r2 >= 0) goto L_0x0083;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0080, code lost:
        if (r15.isDefaultDisplay != false) goto L_0x0083;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0083, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0084, code lost:
        r4.setPCLauncherFocused(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0087, code lost:
        if (r2 < 0) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        r12.mActivityManager.setFocusedTask(r2);
     */
    public static /* synthetic */ void lambda$handleTapOutsideTask$0(TaskPositioningController taskPositioningController, int x, int y, DisplayContent displayContent) {
        boolean oldPCLauncherFocused;
        int taskId;
        boolean z = true;
        if (HwPCUtils.isPcCastModeInServer() && x == -1 && y == -1) {
            taskPositioningController.mService.setFocusedDisplay(displayContent.getDisplayId(), true, "handleTapOutsideTask-1-1");
            return;
        }
        oldPCLauncherFocused = taskPositioningController.mService.getPCLauncherFocused();
        taskPositioningController.mService.setPCLauncherFocused(false);
        synchronized (taskPositioningController.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                Task task = displayContent.findTaskForResizePoint(x, y);
                if (task != null) {
                    if (!taskPositioningController.startPositioningLocked(task.getTopVisibleAppMainWindow(), true, task.preserveOrientationOnResize(), (float) x, (float) y)) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    taskId = task.mTaskId;
                } else {
                    taskId = displayContent.taskIdFromPoint(x, y);
                    TaskStack freeformStack = displayContent.getStack(5, 1);
                    if (freeformStack != null && freeformStack.taskIdFromPoint(x, y) > 0) {
                        taskId = freeformStack.taskIdFromPoint(x, y);
                    }
                }
            } finally {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
        if (oldPCLauncherFocused != taskPositioningController.mService.getPCLauncherFocused()) {
            synchronized (taskPositioningController.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    displayContent.layoutAndAssignWindowLayersIfNeeded();
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        }
    }

    private boolean startPositioningLocked(WindowState win, boolean resize, boolean preserveOrientation, float startX, float startY) {
        WindowState windowState = win;
        if (windowState == null || windowState.getAppToken() == null) {
            Slog.w("WindowManager", "startPositioningLocked: Bad window " + windowState);
            return false;
        } else if (windowState.mInputChannel == null) {
            Slog.wtf("WindowManager", "startPositioningLocked: " + windowState + " has no input channel,  probably being removed");
            return false;
        } else if (!HwFreeFormUtils.isFreeFormEnable() || windowState.getTask() == null || windowState.getTask().getTopVisibleAppToken() == null || !"com.android.packageinstaller".equals(windowState.getTask().getTopVisibleAppToken().appPackageName)) {
            DisplayContent displayContent = windowState.getDisplayContent();
            if (displayContent == null) {
                Slog.w("WindowManager", "startPositioningLocked: Invalid display content " + windowState);
                return false;
            }
            Display display = displayContent.getDisplay();
            this.mTaskPositioner = TaskPositioner.create(this.mService);
            this.mTaskPositioner.register(displayContent);
            this.mInputMonitor.updateInputWindowsLw(true);
            WindowState transferFocusFromWin = windowState;
            if (!(this.mService.mCurrentFocus == null || this.mService.mCurrentFocus == windowState || this.mService.mCurrentFocus.mAppToken != windowState.mAppToken)) {
                transferFocusFromWin = this.mService.mCurrentFocus;
            }
            if (!this.mInputManager.transferTouchFocus(transferFocusFromWin.mInputChannel, this.mTaskPositioner.mServerChannel)) {
                Slog.e("WindowManager", "startPositioningLocked: Unable to transfer touch focus");
                this.mTaskPositioner.unregister();
                this.mTaskPositioner = null;
                this.mInputMonitor.updateInputWindowsLw(true);
                return false;
            }
            this.mTaskPositioner.startDrag(windowState, resize, preserveOrientation, startX, startY);
            return true;
        } else {
            HwFreeFormUtils.log("wms", "Check permission do not resize freeform.");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void finishTaskPositioning() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                TaskPositioningController.lambda$finishTaskPositioning$1(TaskPositioningController.this);
            }
        });
    }

    public static /* synthetic */ void lambda$finishTaskPositioning$1(TaskPositioningController taskPositioningController) {
        synchronized (taskPositioningController.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (taskPositioningController.mTaskPositioner != null) {
                    taskPositioningController.mTaskPositioner.unregister();
                    taskPositioningController.mTaskPositioner = null;
                    taskPositioningController.mInputMonitor.updateInputWindowsLw(true);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }
}
