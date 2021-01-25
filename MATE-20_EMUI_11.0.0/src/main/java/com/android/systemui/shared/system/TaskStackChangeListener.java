package com.android.systemui.shared.system;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.shared.recents.model.ThumbnailData;

public abstract class TaskStackChangeListener {
    public void onTaskStackChangedBackground() {
    }

    public void onTaskStackChanged() {
    }

    public void onTaskSnapshotChanged(int taskId, ThumbnailData snapshot) {
    }

    public void onActivityPinned(String packageName, int userId, int taskId, int stackId) {
    }

    public void onActivityUnpinned() {
    }

    public void onPinnedActivityRestartAttempt(boolean clearedTask) {
    }

    public void onPinnedStackAnimationStarted() {
    }

    public void onPinnedStackAnimationEnded() {
    }

    public void onActivityForcedResizable(String packageName, int taskId, int reason) {
    }

    public void onActivityDismissingDockedStack() {
    }

    public void onActivityLaunchOnSecondaryDisplayFailed() {
    }

    public void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo taskInfo) {
        onActivityLaunchOnSecondaryDisplayFailed();
    }

    public void onActivityLaunchOnSecondaryDisplayRerouted() {
    }

    public void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo taskInfo) {
        onActivityLaunchOnSecondaryDisplayRerouted();
    }

    public void onTaskProfileLocked(int taskId, int userId) {
    }

    public void onTaskCreated(int taskId, ComponentName componentName) {
    }

    public void onTaskRemoved(int taskId) {
    }

    public void onTaskMovedToFront(int taskId) {
    }

    public void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo) {
        onTaskMovedToFront(taskInfo.taskId);
    }

    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) {
    }

    public void onSizeCompatModeActivityChanged(int displayId, IBinder activityToken) {
    }

    public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo taskInfo) {
    }

    public void onTaskDisplayChanged(int taskId, int newDisplayId) {
    }

    /* access modifiers changed from: protected */
    public final boolean checkCurrentUserId(int currentUserId, boolean debug) {
        int processUserId = UserHandle.myUserId();
        if (processUserId == currentUserId) {
            return true;
        }
        if (!debug) {
            return false;
        }
        Log.d("TaskStackChangeListener", "UID mismatch. Process is uid=" + processUserId + " and the current user is uid=" + currentUserId);
        return false;
    }
}
