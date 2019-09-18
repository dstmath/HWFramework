package com.android.systemui.shared.system;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Log;
import com.android.systemui.shared.recents.model.ThumbnailData;
import java.util.ArrayList;
import java.util.List;

public class TaskStackChangeListeners extends TaskStackListener {
    private static final String TAG = TaskStackChangeListeners.class.getSimpleName();
    private final Handler mHandler;
    private boolean mRegistered;
    /* access modifiers changed from: private */
    public final List<TaskStackChangeListener> mTaskStackListeners = new ArrayList();
    private final List<TaskStackChangeListener> mTmpListeners = new ArrayList();

    private final class H extends Handler {
        private static final int ON_ACTIVITY_DISMISSING_DOCKED_STACK = 7;
        private static final int ON_ACTIVITY_FORCED_RESIZABLE = 6;
        private static final int ON_ACTIVITY_LAUNCH_ON_SECONDARY_DISPLAY_FAILED = 11;
        private static final int ON_ACTIVITY_PINNED = 3;
        private static final int ON_ACTIVITY_REQUESTED_ORIENTATION_CHANGE = 15;
        private static final int ON_ACTIVITY_UNPINNED = 10;
        private static final int ON_PINNED_ACTIVITY_RESTART_ATTEMPT = 4;
        private static final int ON_PINNED_STACK_ANIMATION_ENDED = 5;
        private static final int ON_PINNED_STACK_ANIMATION_STARTED = 9;
        private static final int ON_TASK_CREATED = 12;
        private static final int ON_TASK_MOVED_TO_FRONT = 14;
        private static final int ON_TASK_PROFILE_LOCKED = 8;
        private static final int ON_TASK_REMOVED = 13;
        private static final int ON_TASK_SNAPSHOT_CHANGED = 2;
        private static final int ON_TASK_STACK_CHANGED = 1;

        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            synchronized (TaskStackChangeListeners.this.mTaskStackListeners) {
                switch (msg.what) {
                    case 1:
                        Trace.beginSection("onTaskStackChanged");
                        for (int i = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i >= 0; i--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i)).onTaskStackChanged();
                        }
                        Trace.endSection();
                        break;
                    case 2:
                        Trace.beginSection("onTaskSnapshotChanged");
                        for (int i2 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i2 >= 0; i2--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i2)).onTaskSnapshotChanged(msg.arg1, new ThumbnailData((ActivityManager.TaskSnapshot) msg.obj));
                        }
                        Trace.endSection();
                        break;
                    case 3:
                        PinnedActivityInfo info = (PinnedActivityInfo) msg.obj;
                        int i3 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1;
                        while (true) {
                            int i4 = i3;
                            if (i4 < 0) {
                                break;
                            } else {
                                ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i4)).onActivityPinned(info.mPackageName, info.mUserId, info.mTaskId, info.mStackId);
                                i3 = i4 - 1;
                            }
                        }
                    case 4:
                        for (int i5 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i5 >= 0; i5--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i5)).onPinnedActivityRestartAttempt(msg.arg1 != 0);
                        }
                        break;
                    case 5:
                        for (int i6 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i6 >= 0; i6--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i6)).onPinnedStackAnimationEnded();
                        }
                        break;
                    case 6:
                        for (int i7 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i7 >= 0; i7--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i7)).onActivityForcedResizable((String) msg.obj, msg.arg1, msg.arg2);
                        }
                        break;
                    case 7:
                        for (int i8 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i8 >= 0; i8--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i8)).onActivityDismissingDockedStack();
                        }
                        break;
                    case 8:
                        for (int i9 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i9 >= 0; i9--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i9)).onTaskProfileLocked(msg.arg1, msg.arg2);
                        }
                        break;
                    case 9:
                        for (int i10 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i10 >= 0; i10--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i10)).onPinnedStackAnimationStarted();
                        }
                        break;
                    case 10:
                        for (int i11 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i11 >= 0; i11--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i11)).onActivityUnpinned();
                        }
                        break;
                    case 11:
                        for (int i12 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i12 >= 0; i12--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i12)).onActivityLaunchOnSecondaryDisplayFailed();
                        }
                        break;
                    case 12:
                        for (int i13 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i13 >= 0; i13--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i13)).onTaskCreated(msg.arg1, (ComponentName) msg.obj);
                        }
                        break;
                    case 13:
                        for (int i14 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i14 >= 0; i14--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i14)).onTaskRemoved(msg.arg1);
                        }
                        break;
                    case 14:
                        for (int i15 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i15 >= 0; i15--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i15)).onTaskMovedToFront(msg.arg1);
                        }
                        break;
                    case 15:
                        for (int i16 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; i16 >= 0; i16--) {
                            ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(i16)).onActivityRequestedOrientationChanged(msg.arg1, msg.arg2);
                        }
                        break;
                }
            }
        }
    }

    private static class PinnedActivityInfo {
        final String mPackageName;
        final int mStackId;
        final int mTaskId;
        final int mUserId;

        PinnedActivityInfo(String packageName, int userId, int taskId, int stackId) {
            this.mPackageName = packageName;
            this.mUserId = userId;
            this.mTaskId = taskId;
            this.mStackId = stackId;
        }
    }

    public TaskStackChangeListeners(Looper looper) {
        this.mHandler = new H(looper);
    }

    public void addListener(IActivityManager am, TaskStackChangeListener listener) {
        this.mTaskStackListeners.add(listener);
        if (!this.mRegistered) {
            try {
                am.registerTaskStackListener(this);
                this.mRegistered = true;
            } catch (Exception e) {
                Log.w(TAG, "Failed to call registerTaskStackListener", e);
            }
        }
    }

    public void removeListener(TaskStackChangeListener listener) {
        this.mTaskStackListeners.remove(listener);
    }

    public void onTaskStackChanged() throws RemoteException {
        synchronized (this.mTaskStackListeners) {
            this.mTmpListeners.clear();
            this.mTmpListeners.addAll(this.mTaskStackListeners);
        }
        for (int i = this.mTmpListeners.size() - 1; i >= 0; i--) {
            this.mTmpListeners.get(i).onTaskStackChangedBackground();
        }
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(1);
    }

    public void onActivityPinned(String packageName, int userId, int taskId, int stackId) throws RemoteException {
        this.mHandler.removeMessages(3);
        this.mHandler.obtainMessage(3, new PinnedActivityInfo(packageName, userId, taskId, stackId)).sendToTarget();
    }

    public void onActivityUnpinned() throws RemoteException {
        this.mHandler.removeMessages(10);
        this.mHandler.sendEmptyMessage(10);
    }

    public void onPinnedActivityRestartAttempt(boolean clearedTask) throws RemoteException {
        this.mHandler.removeMessages(4);
        this.mHandler.obtainMessage(4, clearedTask, 0).sendToTarget();
    }

    public void onPinnedStackAnimationStarted() throws RemoteException {
        this.mHandler.removeMessages(9);
        this.mHandler.sendEmptyMessage(9);
    }

    public void onPinnedStackAnimationEnded() throws RemoteException {
        this.mHandler.removeMessages(5);
        this.mHandler.sendEmptyMessage(5);
    }

    public void onActivityForcedResizable(String packageName, int taskId, int reason) throws RemoteException {
        this.mHandler.obtainMessage(6, taskId, reason, packageName).sendToTarget();
    }

    public void onActivityDismissingDockedStack() throws RemoteException {
        this.mHandler.sendEmptyMessage(7);
    }

    public void onActivityLaunchOnSecondaryDisplayFailed() throws RemoteException {
        this.mHandler.sendEmptyMessage(11);
    }

    public void onTaskProfileLocked(int taskId, int userId) throws RemoteException {
        this.mHandler.obtainMessage(8, taskId, userId).sendToTarget();
    }

    public void onTaskSnapshotChanged(int taskId, ActivityManager.TaskSnapshot snapshot) throws RemoteException {
        this.mHandler.obtainMessage(2, taskId, 0, snapshot).sendToTarget();
    }

    public void onTaskCreated(int taskId, ComponentName componentName) throws RemoteException {
        this.mHandler.obtainMessage(12, taskId, 0, componentName).sendToTarget();
    }

    public void onTaskRemoved(int taskId) throws RemoteException {
        this.mHandler.obtainMessage(13, taskId, 0).sendToTarget();
    }

    public void onTaskMovedToFront(int taskId) throws RemoteException {
        this.mHandler.obtainMessage(14, taskId, 0).sendToTarget();
    }

    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) throws RemoteException {
        this.mHandler.obtainMessage(15, taskId, requestedOrientation).sendToTarget();
    }
}
