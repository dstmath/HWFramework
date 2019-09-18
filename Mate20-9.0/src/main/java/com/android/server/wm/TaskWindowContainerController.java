package com.android.server.wm;

import android.app.ActivityManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.EventLog;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.EventLogTags;
import com.android.server.os.HwBootFail;
import java.lang.ref.WeakReference;

public class TaskWindowContainerController extends WindowContainerController<Task, TaskWindowContainerListener> {
    private final H mHandler;
    private final int mTaskId;

    private static final class H extends Handler {
        static final int REPORT_SNAPSHOT_CHANGED = 0;
        static final int REQUEST_RESIZE = 1;
        private final WeakReference<TaskWindowContainerController> mController;

        H(WeakReference<TaskWindowContainerController> controller, Looper looper) {
            super(looper);
            this.mController = controller;
        }

        public void handleMessage(Message msg) {
            TaskWindowContainerController controller = (TaskWindowContainerController) this.mController.get();
            TaskWindowContainerListener listener = controller != null ? (TaskWindowContainerListener) controller.mListener : null;
            if (listener != null) {
                switch (msg.what) {
                    case 0:
                        listener.onSnapshotChanged((ActivityManager.TaskSnapshot) msg.obj);
                        break;
                    case 1:
                        listener.requestResize((Rect) msg.obj, msg.arg1);
                        break;
                }
            }
        }
    }

    public /* bridge */ /* synthetic */ void onOverrideConfigurationChanged(Configuration configuration) {
        super.onOverrideConfigurationChanged(configuration);
    }

    public TaskWindowContainerController(int taskId, TaskWindowContainerListener listener, StackWindowController stackController, int userId, Rect bounds, int resizeMode, boolean supportsPictureInPicture, boolean toTop, boolean showForAllUsers, ActivityManager.TaskDescription taskDescription) {
        this(taskId, listener, stackController, userId, bounds, resizeMode, supportsPictureInPicture, toTop, showForAllUsers, taskDescription, WindowManagerService.getInstance());
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public TaskWindowContainerController(int taskId, TaskWindowContainerListener listener, StackWindowController stackController, int userId, Rect bounds, int resizeMode, boolean supportsPictureInPicture, boolean toTop, boolean showForAllUsers, ActivityManager.TaskDescription taskDescription, WindowManagerService service) {
        super(listener, r11);
        StackWindowController stackWindowController = stackController;
        boolean z = toTop;
        WindowManagerService windowManagerService = service;
        int i = taskId;
        this.mTaskId = i;
        this.mHandler = new H(new WeakReference(this), windowManagerService.mH.getLooper());
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                TaskStack stack = (TaskStack) stackWindowController.mContainer;
                if (stack != null) {
                    EventLog.writeEvent(EventLogTags.WM_TASK_CREATED, new Object[]{Integer.valueOf(taskId), Integer.valueOf(stack.mStackId)});
                    stack.addTask(createTask(i, stack, userId, resizeMode, supportsPictureInPicture, taskDescription), z ? HwBootFail.STAGE_BOOT_SUCCESS : Integer.MIN_VALUE, showForAllUsers, z);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                boolean z2 = showForAllUsers;
                throw new IllegalArgumentException("TaskWindowContainerController: invalid stack=" + stackWindowController);
            } catch (Throwable th) {
                th = th;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Task createTask(int taskId, TaskStack stack, int userId, int resizeMode, boolean supportsPictureInPicture, ActivityManager.TaskDescription taskDescription) {
        Task task = new Task(taskId, stack, userId, this.mService, resizeMode, supportsPictureInPicture, taskDescription, this);
        return task;
    }

    public void removeContainer() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((Task) this.mContainer).removeIfPossible();
                super.removeContainer();
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void positionChildAtTop(AppWindowContainerController childController) {
        positionChildAt(childController, HwBootFail.STAGE_BOOT_SUCCESS);
    }

    public void positionChildAt(AppWindowContainerController childController, int position) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                AppWindowToken aToken = (AppWindowToken) childController.mContainer;
                if (aToken == null) {
                    Slog.w("WindowManager", "Attempted to position of non-existing app : " + childController);
                    return;
                }
                Task task = (Task) this.mContainer;
                if (task != null) {
                    task.positionChildAt(position, aToken, false);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                throw new IllegalArgumentException("positionChildAt: invalid task=" + this);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void reparent(StackWindowController stackController, int position, boolean moveParents) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    TaskStack stack = (TaskStack) stackController.mContainer;
                    if (stack != null) {
                        ((Task) this.mContainer).reparent(stack, position, moveParents);
                        ((Task) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    throw new IllegalArgumentException("reparent: could not find stack=" + stackController);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setResizeable(int resizeMode) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((Task) this.mContainer).setResizeable(resizeMode);
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

    public void resize(boolean relayout, boolean forced) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    throw new IllegalArgumentException("resizeTask: taskId " + this.mTaskId + " not found.");
                } else if (((Task) this.mContainer).setBounds(((Task) this.mContainer).getOverrideBounds(), forced) != 0 && relayout) {
                    ((Task) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void getBounds(Rect bounds) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer != null) {
                    ((Task) this.mContainer).getBounds(bounds);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                bounds.setEmpty();
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void setTaskDockedResizing(boolean resizing) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "setTaskDockedResizing: taskId " + this.mTaskId + " not found.");
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((Task) this.mContainer).setDragResizing(resizing, 1);
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void cancelWindowTransition() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "cancelWindowTransition: taskId " + this.mTaskId + " not found.");
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((Task) this.mContainer).cancelTaskWindowTransition();
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "setTaskDescription: taskId " + this.mTaskId + " not found.");
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                ((Task) this.mContainer).setTaskDescription(taskDescription);
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportSnapshotChanged(ActivityManager.TaskSnapshot snapshot) {
        this.mHandler.obtainMessage(0, snapshot).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void requestResize(Rect bounds, int resizeMode) {
        this.mHandler.obtainMessage(1, resizeMode, 0, bounds).sendToTarget();
    }

    public String toString() {
        return "{TaskWindowContainerController taskId=" + this.mTaskId + "}";
    }
}
