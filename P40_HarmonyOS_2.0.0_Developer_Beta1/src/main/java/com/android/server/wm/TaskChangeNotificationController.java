package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.app.TaskInfo;
import android.content.ComponentName;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Slog;
import java.util.ArrayList;

public class TaskChangeNotificationController {
    private static final int LOG_STACK_STATE_MSG = 1;
    private static final int NOTIFY_ACTIVITY_DISMISSING_DOCKED_STACK_MSG = 7;
    private static final int NOTIFY_ACTIVITY_LAUNCH_ON_SECONDARY_DISPLAY_FAILED_MSG = 18;
    private static final int NOTIFY_ACTIVITY_LAUNCH_ON_SECONDARY_DISPLAY_REROUTED_MSG = 19;
    private static final int NOTIFY_ACTIVITY_PINNED_LISTENERS_MSG = 3;
    private static final int NOTIFY_ACTIVITY_REQUESTED_ORIENTATION_CHANGED_LISTENERS = 12;
    private static final int NOTIFY_ACTIVITY_UNPINNED_LISTENERS_MSG = 17;
    private static final int NOTIFY_BACK_PRESSED_ON_TASK_ROOT = 21;
    private static final int NOTIFY_FORCED_RESIZABLE_MSG = 6;
    private static final int NOTIFY_PINNED_ACTIVITY_RESTART_ATTEMPT_LISTENERS_MSG = 4;
    private static final int NOTIFY_PINNED_STACK_ANIMATION_ENDED_LISTENERS_MSG = 5;
    private static final int NOTIFY_PINNED_STACK_ANIMATION_STARTED_LISTENERS_MSG = 16;
    private static final int NOTIFY_SIZE_COMPAT_MODE_ACTIVITY_CHANGED_MSG = 20;
    private static final int NOTIFY_TASK_ADDED_LISTENERS_MSG = 8;
    private static final int NOTIFY_TASK_DESCRIPTION_CHANGED_LISTENERS_MSG = 11;
    private static final int NOTIFY_TASK_DISPLAY_CHANGED_LISTENERS_MSG = 22;
    private static final int NOTIFY_TASK_MOVED_TO_FRONT_LISTENERS_MSG = 10;
    private static final int NOTIFY_TASK_PROFILE_LOCKED_LISTENERS_MSG = 14;
    private static final int NOTIFY_TASK_REMOVAL_STARTED_LISTENERS = 13;
    private static final int NOTIFY_TASK_REMOVED_LISTENERS_MSG = 9;
    private static final int NOTIFY_TASK_SNAPSHOT_CHANGED_LISTENERS_MSG = 15;
    private static final int NOTIFY_TASK_STACK_CHANGE_LISTENERS_DELAY = 100;
    private static final int NOTIFY_TASK_STACK_CHANGE_LISTENERS_MSG = 2;
    private static final String TAG = "TaskChangeNotificationController";
    private final Handler mHandler;
    private final ArrayList<ITaskStackListener> mLocalTaskStackListeners = new ArrayList<>();
    private final TaskStackConsumer mNotifyActivityDismissingDockedStack = $$Lambda$TaskChangeNotificationController$0m_qN9QkcgkoWun2Biw8le4l1Y.INSTANCE;
    private final TaskStackConsumer mNotifyActivityForcedResizable = $$Lambda$TaskChangeNotificationController$byMDuIFUN4cQ1lT9jVjMwLhaLDw.INSTANCE;
    private final TaskStackConsumer mNotifyActivityLaunchOnSecondaryDisplayFailed = $$Lambda$TaskChangeNotificationController$yaW9HlZsz3L55CTQ4b7y33IGo94.INSTANCE;
    private final TaskStackConsumer mNotifyActivityLaunchOnSecondaryDisplayRerouted = $$Lambda$TaskChangeNotificationController$wuBjs4dj7gB_MI4dIdt2gV2Osus.INSTANCE;
    private final TaskStackConsumer mNotifyActivityPinned = $$Lambda$TaskChangeNotificationController$ncM_yje7m7HuiJvorBIH_C8Ou4.INSTANCE;
    private final TaskStackConsumer mNotifyActivityRequestedOrientationChanged = $$Lambda$TaskChangeNotificationController$MS67FdGix7tWO0Od9imcaKVXL7I.INSTANCE;
    private final TaskStackConsumer mNotifyActivityUnpinned = $$Lambda$TaskChangeNotificationController$qONfw3ssOxjb_iMuO2oMzCbXfrg.INSTANCE;
    private final TaskStackConsumer mNotifyBackPressedOnTaskRoot = $$Lambda$TaskChangeNotificationController$SByuGj5tpcCpjTH9lf5zHHv2gNM.INSTANCE;
    private final TaskStackConsumer mNotifyPinnedActivityRestartAttempt = $$Lambda$TaskChangeNotificationController$9ngbiJ2r3x2ASHwN59tUFO22BQ.INSTANCE;
    private final TaskStackConsumer mNotifyPinnedStackAnimationEnded = $$Lambda$TaskChangeNotificationController$k0FXXCHcWJhmtm6Kruo6nGeXI.INSTANCE;
    private final TaskStackConsumer mNotifyPinnedStackAnimationStarted = $$Lambda$TaskChangeNotificationController$M2NSB3SSVJR2Tu4vihNfsIL31s4.INSTANCE;
    private final TaskStackConsumer mNotifyTaskCreated = $$Lambda$TaskChangeNotificationController$1ziXgnyLi0gQjqMGJAbSzs0dmE.INSTANCE;
    private final TaskStackConsumer mNotifyTaskDescriptionChanged = $$Lambda$TaskChangeNotificationController$Ge3jFevRwpndz6qRSLDXODq2VjE.INSTANCE;
    private final TaskStackConsumer mNotifyTaskDisplayChanged = $$Lambda$TaskChangeNotificationController$cFUeUwnRjuOQKcg2c4PnDS0ImTw.INSTANCE;
    private final TaskStackConsumer mNotifyTaskMovedToFront = $$Lambda$TaskChangeNotificationController$ZLPZtiEvD_F4WUgH7BD4KPpdAWM.INSTANCE;
    private final TaskStackConsumer mNotifyTaskProfileLocked = $$Lambda$TaskChangeNotificationController$Dvvt1gNNfFRVEKlSCdL_9VnilUE.INSTANCE;
    private final TaskStackConsumer mNotifyTaskRemovalStarted = $$Lambda$TaskChangeNotificationController$NLoKy9SbVr1EJpEjznsKi7yAlpg.INSTANCE;
    private final TaskStackConsumer mNotifyTaskRemoved = $$Lambda$TaskChangeNotificationController$kss8MGli3T9b_YQDzR2cB843y8.INSTANCE;
    private final TaskStackConsumer mNotifyTaskSnapshotChanged = $$Lambda$TaskChangeNotificationController$UexNbaqPy0mc3VxTw2coCctHho8.INSTANCE;
    private final TaskStackConsumer mNotifyTaskStackChanged = $$Lambda$TaskChangeNotificationController$SAbrujQOZNUflKs1FAg2mBnjx3A.INSTANCE;
    private final TaskStackConsumer mOnSizeCompatModeActivityChanged = $$Lambda$TaskChangeNotificationController$sS6OHbZtuWHjzmkm8bleSWZWFqA.INSTANCE;
    private final RemoteCallbackList<ITaskStackListener> mRemoteTaskStackListeners = new RemoteCallbackList<>();
    private final Object mServiceLock;
    private final ActivityStackSupervisor mStackSupervisor;

    @FunctionalInterface
    public interface TaskStackConsumer {
        void accept(ITaskStackListener iTaskStackListener, Message message) throws RemoteException;
    }

    static /* synthetic */ void lambda$new$10(ITaskStackListener l, Message m) throws RemoteException {
        l.onPinnedActivityRestartAttempt(m.arg1 != 0);
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    synchronized (TaskChangeNotificationController.this.mServiceLock) {
                        TaskChangeNotificationController.this.mStackSupervisor.logStackState();
                    }
                    return;
                case 2:
                    TaskChangeNotificationController taskChangeNotificationController = TaskChangeNotificationController.this;
                    taskChangeNotificationController.forAllRemoteListeners(taskChangeNotificationController.mNotifyTaskStackChanged, msg);
                    return;
                case 3:
                    TaskChangeNotificationController taskChangeNotificationController2 = TaskChangeNotificationController.this;
                    taskChangeNotificationController2.forAllRemoteListeners(taskChangeNotificationController2.mNotifyActivityPinned, msg);
                    return;
                case 4:
                    TaskChangeNotificationController taskChangeNotificationController3 = TaskChangeNotificationController.this;
                    taskChangeNotificationController3.forAllRemoteListeners(taskChangeNotificationController3.mNotifyPinnedActivityRestartAttempt, msg);
                    return;
                case 5:
                    TaskChangeNotificationController taskChangeNotificationController4 = TaskChangeNotificationController.this;
                    taskChangeNotificationController4.forAllRemoteListeners(taskChangeNotificationController4.mNotifyPinnedStackAnimationEnded, msg);
                    return;
                case TaskChangeNotificationController.NOTIFY_FORCED_RESIZABLE_MSG /* 6 */:
                    TaskChangeNotificationController taskChangeNotificationController5 = TaskChangeNotificationController.this;
                    taskChangeNotificationController5.forAllRemoteListeners(taskChangeNotificationController5.mNotifyActivityForcedResizable, msg);
                    return;
                case TaskChangeNotificationController.NOTIFY_ACTIVITY_DISMISSING_DOCKED_STACK_MSG /* 7 */:
                    TaskChangeNotificationController taskChangeNotificationController6 = TaskChangeNotificationController.this;
                    taskChangeNotificationController6.forAllRemoteListeners(taskChangeNotificationController6.mNotifyActivityDismissingDockedStack, msg);
                    return;
                case TaskChangeNotificationController.NOTIFY_TASK_ADDED_LISTENERS_MSG /* 8 */:
                    TaskChangeNotificationController taskChangeNotificationController7 = TaskChangeNotificationController.this;
                    taskChangeNotificationController7.forAllRemoteListeners(taskChangeNotificationController7.mNotifyTaskCreated, msg);
                    return;
                case TaskChangeNotificationController.NOTIFY_TASK_REMOVED_LISTENERS_MSG /* 9 */:
                    TaskChangeNotificationController taskChangeNotificationController8 = TaskChangeNotificationController.this;
                    taskChangeNotificationController8.forAllRemoteListeners(taskChangeNotificationController8.mNotifyTaskRemoved, msg);
                    return;
                case TaskChangeNotificationController.NOTIFY_TASK_MOVED_TO_FRONT_LISTENERS_MSG /* 10 */:
                    TaskChangeNotificationController taskChangeNotificationController9 = TaskChangeNotificationController.this;
                    taskChangeNotificationController9.forAllRemoteListeners(taskChangeNotificationController9.mNotifyTaskMovedToFront, msg);
                    return;
                case 11:
                    TaskChangeNotificationController taskChangeNotificationController10 = TaskChangeNotificationController.this;
                    taskChangeNotificationController10.forAllRemoteListeners(taskChangeNotificationController10.mNotifyTaskDescriptionChanged, msg);
                    return;
                case TaskChangeNotificationController.NOTIFY_ACTIVITY_REQUESTED_ORIENTATION_CHANGED_LISTENERS /* 12 */:
                    TaskChangeNotificationController taskChangeNotificationController11 = TaskChangeNotificationController.this;
                    taskChangeNotificationController11.forAllRemoteListeners(taskChangeNotificationController11.mNotifyActivityRequestedOrientationChanged, msg);
                    return;
                case TaskChangeNotificationController.NOTIFY_TASK_REMOVAL_STARTED_LISTENERS /* 13 */:
                    TaskChangeNotificationController taskChangeNotificationController12 = TaskChangeNotificationController.this;
                    taskChangeNotificationController12.forAllRemoteListeners(taskChangeNotificationController12.mNotifyTaskRemovalStarted, msg);
                    return;
                case 14:
                    TaskChangeNotificationController taskChangeNotificationController13 = TaskChangeNotificationController.this;
                    taskChangeNotificationController13.forAllRemoteListeners(taskChangeNotificationController13.mNotifyTaskProfileLocked, msg);
                    return;
                case 15:
                    TaskChangeNotificationController taskChangeNotificationController14 = TaskChangeNotificationController.this;
                    taskChangeNotificationController14.forAllRemoteListeners(taskChangeNotificationController14.mNotifyTaskSnapshotChanged, msg);
                    return;
                case 16:
                    TaskChangeNotificationController taskChangeNotificationController15 = TaskChangeNotificationController.this;
                    taskChangeNotificationController15.forAllRemoteListeners(taskChangeNotificationController15.mNotifyPinnedStackAnimationStarted, msg);
                    return;
                case 17:
                    TaskChangeNotificationController taskChangeNotificationController16 = TaskChangeNotificationController.this;
                    taskChangeNotificationController16.forAllRemoteListeners(taskChangeNotificationController16.mNotifyActivityUnpinned, msg);
                    return;
                case 18:
                    TaskChangeNotificationController taskChangeNotificationController17 = TaskChangeNotificationController.this;
                    taskChangeNotificationController17.forAllRemoteListeners(taskChangeNotificationController17.mNotifyActivityLaunchOnSecondaryDisplayFailed, msg);
                    return;
                case 19:
                    TaskChangeNotificationController taskChangeNotificationController18 = TaskChangeNotificationController.this;
                    taskChangeNotificationController18.forAllRemoteListeners(taskChangeNotificationController18.mNotifyActivityLaunchOnSecondaryDisplayRerouted, msg);
                    return;
                case TaskChangeNotificationController.NOTIFY_SIZE_COMPAT_MODE_ACTIVITY_CHANGED_MSG /* 20 */:
                    TaskChangeNotificationController taskChangeNotificationController19 = TaskChangeNotificationController.this;
                    taskChangeNotificationController19.forAllRemoteListeners(taskChangeNotificationController19.mOnSizeCompatModeActivityChanged, msg);
                    return;
                case 21:
                    TaskChangeNotificationController taskChangeNotificationController20 = TaskChangeNotificationController.this;
                    taskChangeNotificationController20.forAllRemoteListeners(taskChangeNotificationController20.mNotifyBackPressedOnTaskRoot, msg);
                    return;
                case 22:
                    TaskChangeNotificationController taskChangeNotificationController21 = TaskChangeNotificationController.this;
                    taskChangeNotificationController21.forAllRemoteListeners(taskChangeNotificationController21.mNotifyTaskDisplayChanged, msg);
                    return;
                default:
                    return;
            }
        }
    }

    public TaskChangeNotificationController(Object serviceLock, ActivityStackSupervisor stackSupervisor, Handler handler) {
        this.mServiceLock = serviceLock;
        this.mStackSupervisor = stackSupervisor;
        this.mHandler = new MainHandler(handler.getLooper());
    }

    public void registerTaskStackListener(ITaskStackListener listener) {
        synchronized (this.mServiceLock) {
            if (listener != null) {
                if (Binder.getCallingPid() != Process.myPid()) {
                    this.mRemoteTaskStackListeners.register(listener);
                } else if (!this.mLocalTaskStackListeners.contains(listener)) {
                    this.mLocalTaskStackListeners.add(listener);
                }
            }
        }
    }

    public void unregisterTaskStackListener(ITaskStackListener listener) {
        synchronized (this.mServiceLock) {
            if (listener != null) {
                if (Binder.getCallingPid() == Process.myPid()) {
                    this.mLocalTaskStackListeners.remove(listener);
                } else {
                    this.mRemoteTaskStackListeners.unregister(listener);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void forAllRemoteListeners(TaskStackConsumer callback, Message message) {
        synchronized (this.mServiceLock) {
            for (int i = this.mRemoteTaskStackListeners.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    callback.accept(this.mRemoteTaskStackListeners.getBroadcastItem(i), message);
                } catch (RemoteException e) {
                }
            }
            this.mRemoteTaskStackListeners.finishBroadcast();
        }
    }

    private void forAllLocalListeners(TaskStackConsumer callback, Message message) {
        synchronized (this.mServiceLock) {
            for (int i = this.mLocalTaskStackListeners.size() - 1; i >= 0; i--) {
                try {
                    if (Binder.getCallingPid() != Process.myPid()) {
                        Slog.e(TAG, "forAllLocalListeners pid must be equal, but the callingPid " + Binder.getCallingPid() + " pid " + Process.myPid(), new Exception());
                    }
                    callback.accept(this.mLocalTaskStackListeners.get(i), message);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskStackChanged() {
        this.mHandler.sendEmptyMessage(1);
        this.mHandler.removeMessages(2);
        Message msg = this.mHandler.obtainMessage(2);
        forAllLocalListeners(this.mNotifyTaskStackChanged, msg);
        this.mHandler.sendMessageDelayed(msg, 100);
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityPinned(ActivityRecord r) {
        this.mHandler.removeMessages(3);
        Message msg = this.mHandler.obtainMessage(3, r.getTaskRecord().taskId, r.getStackId(), r.packageName);
        msg.sendingUid = r.mUserId;
        forAllLocalListeners(this.mNotifyActivityPinned, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityUnpinned() {
        this.mHandler.removeMessages(17);
        Message msg = this.mHandler.obtainMessage(17);
        forAllLocalListeners(this.mNotifyActivityUnpinned, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyPinnedActivityRestartAttempt(boolean clearedTask) {
        this.mHandler.removeMessages(4);
        Message msg = this.mHandler.obtainMessage(4, clearedTask ? 1 : 0, 0);
        forAllLocalListeners(this.mNotifyPinnedActivityRestartAttempt, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyPinnedStackAnimationStarted() {
        this.mHandler.removeMessages(16);
        Message msg = this.mHandler.obtainMessage(16);
        forAllLocalListeners(this.mNotifyPinnedStackAnimationStarted, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyPinnedStackAnimationEnded() {
        this.mHandler.removeMessages(5);
        Message msg = this.mHandler.obtainMessage(5);
        forAllLocalListeners(this.mNotifyPinnedStackAnimationEnded, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityDismissingDockedStack() {
        this.mHandler.removeMessages(NOTIFY_ACTIVITY_DISMISSING_DOCKED_STACK_MSG);
        Message msg = this.mHandler.obtainMessage(NOTIFY_ACTIVITY_DISMISSING_DOCKED_STACK_MSG);
        forAllLocalListeners(this.mNotifyActivityDismissingDockedStack, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityForcedResizable(int taskId, int reason, String packageName) {
        this.mHandler.removeMessages(NOTIFY_FORCED_RESIZABLE_MSG);
        Message msg = this.mHandler.obtainMessage(NOTIFY_FORCED_RESIZABLE_MSG, taskId, reason, packageName);
        forAllLocalListeners(this.mNotifyActivityForcedResizable, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityLaunchOnSecondaryDisplayFailed(TaskInfo ti, int requestedDisplayId) {
        this.mHandler.removeMessages(18);
        Message msg = this.mHandler.obtainMessage(18, requestedDisplayId, 0, ti);
        forAllLocalListeners(this.mNotifyActivityLaunchOnSecondaryDisplayFailed, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityLaunchOnSecondaryDisplayRerouted(TaskInfo ti, int requestedDisplayId) {
        this.mHandler.removeMessages(19);
        Message msg = this.mHandler.obtainMessage(19, requestedDisplayId, 0, ti);
        forAllLocalListeners(this.mNotifyActivityLaunchOnSecondaryDisplayRerouted, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskCreated(int taskId, ComponentName componentName) {
        Message msg = this.mHandler.obtainMessage(NOTIFY_TASK_ADDED_LISTENERS_MSG, taskId, 0, componentName);
        forAllLocalListeners(this.mNotifyTaskCreated, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskRemoved(int taskId) {
        Message msg = this.mHandler.obtainMessage(NOTIFY_TASK_REMOVED_LISTENERS_MSG, taskId, 0);
        forAllLocalListeners(this.mNotifyTaskRemoved, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskMovedToFront(TaskInfo ti) {
        Message msg = this.mHandler.obtainMessage(NOTIFY_TASK_MOVED_TO_FRONT_LISTENERS_MSG, ti);
        forAllLocalListeners(this.mNotifyTaskMovedToFront, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskDescriptionChanged(TaskInfo taskInfo) {
        Message msg = this.mHandler.obtainMessage(11, taskInfo);
        forAllLocalListeners(this.mNotifyTaskDescriptionChanged, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyActivityRequestedOrientationChanged(int taskId, int orientation) {
        Message msg = this.mHandler.obtainMessage(NOTIFY_ACTIVITY_REQUESTED_ORIENTATION_CHANGED_LISTENERS, taskId, orientation);
        forAllLocalListeners(this.mNotifyActivityRequestedOrientationChanged, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskRemovalStarted(ActivityManager.RunningTaskInfo taskInfo) {
        Message msg = this.mHandler.obtainMessage(NOTIFY_TASK_REMOVAL_STARTED_LISTENERS, taskInfo);
        forAllLocalListeners(this.mNotifyTaskRemovalStarted, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskProfileLocked(int taskId, int userId) {
        Message msg = this.mHandler.obtainMessage(14, taskId, userId);
        forAllLocalListeners(this.mNotifyTaskProfileLocked, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskSnapshotChanged(int taskId, ActivityManager.TaskSnapshot snapshot) {
        Message msg = this.mHandler.obtainMessage(15, taskId, 0, snapshot);
        forAllLocalListeners(this.mNotifyTaskSnapshotChanged, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifySizeCompatModeActivityChanged(int displayId, IBinder activityToken) {
        Message msg = this.mHandler.obtainMessage(NOTIFY_SIZE_COMPAT_MODE_ACTIVITY_CHANGED_MSG, displayId, 0, activityToken);
        forAllLocalListeners(this.mOnSizeCompatModeActivityChanged, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyBackPressedOnTaskRoot(TaskInfo taskInfo) {
        Message msg = this.mHandler.obtainMessage(21, taskInfo);
        forAllLocalListeners(this.mNotifyBackPressedOnTaskRoot, msg);
        msg.sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void notifyTaskDisplayChanged(int taskId, int newDisplayId) {
        Message msg = this.mHandler.obtainMessage(22, taskId, newDisplayId);
        forAllLocalListeners(this.mNotifyTaskStackChanged, msg);
        msg.sendToTarget();
    }
}
