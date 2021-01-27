package com.huawei.android.app;

import android.app.ActivityManager;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.os.RemoteException;

public class TaskStackListenerBridge extends TaskStackListener {
    private TaskStackListenerEx mTaskStackListenerEx;

    public void setTaskStackListenerEx(TaskStackListenerEx taskStackListenerEx) {
        this.mTaskStackListenerEx = taskStackListenerEx;
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onTaskSnapshotChanged(int taskId, ActivityManager.TaskSnapshot snapshot) throws RemoteException {
        super.onTaskSnapshotChanged(taskId, snapshot);
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onTaskStackChanged() throws RemoteException {
        super.onTaskStackChanged();
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onTaskStackChanged();
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onActivityPinned(String packageName, int userId, int taskId, int stackId) throws RemoteException {
        super.onActivityPinned(packageName, userId, taskId, stackId);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onActivityPinned(packageName, userId, taskId, stackId);
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onActivityUnpinned() throws RemoteException {
        super.onActivityUnpinned();
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onActivityUnpinned();
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onPinnedActivityRestartAttempt(boolean clearedTask) throws RemoteException {
        super.onPinnedActivityRestartAttempt(clearedTask);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onPinnedActivityRestartAttempt(clearedTask);
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onPinnedStackAnimationStarted() throws RemoteException {
        super.onPinnedStackAnimationStarted();
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onPinnedStackAnimationStarted();
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onPinnedStackAnimationEnded() throws RemoteException {
        super.onPinnedStackAnimationEnded();
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onPinnedStackAnimationEnded();
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException {
        super.onActivityForcedResizable(packageName, taskId, reason);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onActivityForcedResizable(packageName, taskId, reason);
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onActivityDismissingDockedStack() throws RemoteException {
        super.onActivityDismissingDockedStack();
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onActivityDismissingDockedStack();
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
        super.onTaskCreated(taskId, componentName);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onTaskCreated(taskId, componentName);
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onTaskRemoved(int taskId) throws RemoteException {
        super.onTaskRemoved(taskId);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onTaskRemoved(taskId);
        }
    }

    @Override // android.app.TaskStackListener
    public void onTaskMovedToFront(int taskId) throws RemoteException {
        super.onTaskMovedToFront(taskId);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onTaskMovedToFront(taskId);
        }
    }

    @Override // android.app.TaskStackListener
    public void onTaskRemovalStarted(int taskId) throws RemoteException {
        super.onTaskRemovalStarted(taskId);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onTaskRemovalStarted(taskId);
        }
    }

    @Override // android.app.TaskStackListener
    public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td) throws RemoteException {
        super.onTaskDescriptionChanged(taskId, td);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onTaskDescriptionChanged(taskId, td);
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException {
        super.onActivityRequestedOrientationChanged(taskId, requestedOrientation);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onActivityRequestedOrientationChanged(taskId, requestedOrientation);
        }
    }

    @Override // android.app.TaskStackListener, android.app.ITaskStackListener
    public void onTaskProfileLocked(int taskId, int userId) throws RemoteException {
        super.onTaskProfileLocked(taskId, userId);
        TaskStackListenerEx taskStackListenerEx = this.mTaskStackListenerEx;
        if (taskStackListenerEx != null) {
            taskStackListenerEx.onTaskProfileLocked(taskId, userId);
        }
    }
}
