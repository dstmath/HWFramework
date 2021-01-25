package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

public abstract class TaskStackListener extends ITaskStackListener.Stub {
    static final String TAG = "TaskStackListener";

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onTaskStackChanged() throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onActivityPinned(String packageName, int userId, int taskId, int stackId) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onActivityUnpinned() throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onPinnedActivityRestartAttempt(boolean clearedTask) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onPinnedStackAnimationStarted() throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onPinnedStackAnimationEnded() throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onActivityDismissingDockedStack() throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    public void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) throws RemoteException {
        onActivityLaunchOnSecondaryDisplayFailed();
    }

    @UnsupportedAppUsage
    @Deprecated
    public void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onTaskRemoved(int taskId) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    public void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException {
        onTaskMovedToFront(taskInfo.taskId);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void onTaskMovedToFront(int taskId) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    public void onTaskRemovalStarted(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException {
        onTaskRemovalStarted(taskInfo.taskId);
    }

    @Deprecated
    public void onTaskRemovalStarted(int taskId) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    public void onTaskDescriptionChanged(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException {
        onTaskDescriptionChanged(taskInfo.taskId, taskInfo.taskDescription);
    }

    @Deprecated
    public void onTaskDescriptionChanged(int taskId, ActivityManager.TaskDescription td) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onTaskProfileLocked(int taskId, int userId) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onTaskSnapshotChanged(int taskId, ActivityManager.TaskSnapshot snapshot) throws RemoteException {
        if (Binder.getCallingPid() != Process.myPid() && snapshot != null && snapshot.getSnapshot() != null) {
            Log.d(TAG, "destory snapshot binder pid " + Binder.getCallingPid() + " myPid " + Process.myPid() + " snapshot " + snapshot.toString());
            snapshot.getSnapshot().destroy();
        }
    }

    @Override // android.app.ITaskStackListener
    @UnsupportedAppUsage
    public void onSizeCompatModeActivityChanged(int displayId, IBinder activityToken) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo taskInfo) throws RemoteException {
    }

    @Override // android.app.ITaskStackListener
    public void onTaskDisplayChanged(int taskId, int newDisplayId) throws RemoteException {
    }
}
