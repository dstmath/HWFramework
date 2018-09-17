package android.app;

import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskSnapshot;
import android.app.ITaskStackListener.Stub;
import android.content.ComponentName;
import android.os.RemoteException;

public abstract class TaskStackListener extends Stub {
    public void onTaskStackChanged() throws RemoteException {
    }

    public void onActivityPinned(String packageName, int taskId) throws RemoteException {
    }

    public void onActivityUnpinned() throws RemoteException {
    }

    public void onPinnedActivityRestartAttempt(boolean clearedTask) throws RemoteException {
    }

    public void onPinnedStackAnimationStarted() throws RemoteException {
    }

    public void onPinnedStackAnimationEnded() throws RemoteException {
    }

    public void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException {
    }

    public void onActivityDismissingDockedStack() throws RemoteException {
    }

    public void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException {
    }

    public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
    }

    public void onTaskRemoved(int taskId) throws RemoteException {
    }

    public void onTaskMovedToFront(int taskId) throws RemoteException {
    }

    public void onTaskRemovalStarted(int taskId) {
    }

    public void onTaskDescriptionChanged(int taskId, TaskDescription td) throws RemoteException {
    }

    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException {
    }

    public void onTaskProfileLocked(int taskId, int userId) {
    }

    public void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot) throws RemoteException {
    }
}
