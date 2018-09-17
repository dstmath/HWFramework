package com.android.server.wm;

import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskSnapshot;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.EventLog;
import android.util.Slog;
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
                        listener.onSnapshotChanged((TaskSnapshot) msg.obj);
                        break;
                    case 1:
                        listener.requestResize((Rect) msg.obj, msg.arg1);
                        break;
                }
            }
        }
    }

    public TaskWindowContainerController(int taskId, TaskWindowContainerListener listener, StackWindowController stackController, int userId, Rect bounds, Configuration overrideConfig, int resizeMode, boolean supportsPictureInPicture, boolean homeTask, boolean toTop, boolean showForAllUsers, TaskDescription taskDescription) {
        this(taskId, listener, stackController, userId, bounds, overrideConfig, resizeMode, supportsPictureInPicture, homeTask, toTop, showForAllUsers, taskDescription, WindowManagerService.getInstance());
    }

    public TaskWindowContainerController(int taskId, TaskWindowContainerListener listener, StackWindowController stackController, int userId, Rect bounds, Configuration overrideConfig, int resizeMode, boolean supportsPictureInPicture, boolean homeTask, boolean toTop, boolean showForAllUsers, TaskDescription taskDescription, WindowManagerService service) {
        super(listener, service);
        this.mTaskId = taskId;
        this.mHandler = new H(new WeakReference(this), service.mH.getLooper());
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                TaskStack stack = stackController.mContainer;
                if (stack == null) {
                    throw new IllegalArgumentException("TaskWindowContainerController: invalid stack=" + stackController);
                }
                EventLog.writeEvent(EventLogTags.WM_TASK_CREATED, new Object[]{Integer.valueOf(taskId), Integer.valueOf(stack.mStackId)});
                stack.addTask(createTask(taskId, stack, userId, bounds, overrideConfig, resizeMode, supportsPictureInPicture, homeTask, taskDescription), toTop ? HwBootFail.STAGE_BOOT_SUCCESS : Integer.MIN_VALUE, showForAllUsers, true);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    Task createTask(int taskId, TaskStack stack, int userId, Rect bounds, Configuration overrideConfig, int resizeMode, boolean supportsPictureInPicture, boolean homeTask, TaskDescription taskDescription) {
        return new Task(taskId, stack, userId, this.mService, bounds, overrideConfig, resizeMode, supportsPictureInPicture, homeTask, taskDescription, this);
    }

    public void removeContainer() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                } else {
                    ((Task) this.mContainer).removeIfPossible();
                    super.removeContainer();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void positionChildAt(AppWindowContainerController childController, int position) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                AppWindowToken aToken = childController.mContainer;
                if (aToken == null) {
                    Slog.w("WindowManager", "Attempted to position of non-existing app : " + childController);
                } else {
                    Task task = this.mContainer;
                    if (task == null) {
                        throw new IllegalArgumentException("positionChildAt: invalid task=" + this);
                    }
                    task.positionChildAt(position, aToken, false);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void reparent(StackWindowController stackController, int position, boolean moveParents) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                } else {
                    TaskStack stack = stackController.mContainer;
                    if (stack == null) {
                        throw new IllegalArgumentException("reparent: could not find stack=" + stackController);
                    }
                    ((Task) this.mContainer).reparent(stack, position, moveParents);
                    ((Task) this.mContainer).getDisplayContent().layoutAndAssignWindowLayersIfNeeded();
                    WindowManagerService.resetPriorityAfterLockedSection();
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
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void resize(Rect bounds, Configuration overrideConfig, boolean relayout, boolean forced) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    throw new IllegalArgumentException("resizeTask: taskId " + this.mTaskId + " not found.");
                }
                if (((Task) this.mContainer).resizeLocked(bounds, overrideConfig, forced) && relayout) {
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
                } else {
                    bounds.setEmpty();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setTaskDockedResizing(boolean resizing) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "setTaskDockedResizing: taskId " + this.mTaskId + " not found.");
                } else {
                    ((Task) this.mContainer).setDragResizing(resizing, 1);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void cancelWindowTransition() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "cancelWindowTransition: taskId " + this.mTaskId + " not found.");
                } else {
                    ((Task) this.mContainer).cancelTaskWindowTransition();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void cancelThumbnailTransition() {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "cancelThumbnailTransition: taskId " + this.mTaskId + " not found.");
                } else {
                    ((Task) this.mContainer).cancelTaskThumbnailTransition();
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setTaskDescription(TaskDescription taskDescription) {
        synchronized (this.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mContainer == null) {
                    Slog.w("WindowManager", "setTaskDescription: taskId " + this.mTaskId + " not found.");
                } else {
                    ((Task) this.mContainer).setTaskDescription(taskDescription);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    void reportSnapshotChanged(TaskSnapshot snapshot) {
        this.mHandler.obtainMessage(0, snapshot).sendToTarget();
    }

    void requestResize(Rect bounds, int resizeMode) {
        this.mHandler.obtainMessage(1, resizeMode, 0, bounds).sendToTarget();
    }

    public String toString() {
        return "{TaskWindowContainerController taskId=" + this.mTaskId + "}";
    }
}
