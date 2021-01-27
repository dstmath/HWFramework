package com.android.systemui.shared.system;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.AppGlobals;
import android.app.IAssistDataReceiver;
import android.app.WindowConfiguration;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.IRecentsAnimationController;
import android.view.IRecentsAnimationRunner;
import android.view.RemoteAnimationTarget;
import com.android.internal.app.IVoiceInteractionManagerService;
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.recents.model.ThumbnailData;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ActivityManagerWrapper {
    public static final String CLOSE_SYSTEM_WINDOWS_REASON_RECENTS = "recentapps";
    private static final String TAG = "ActivityManagerWrapper";
    private static final ActivityManagerWrapper sInstance = new ActivityManagerWrapper();
    private final BackgroundExecutor mBackgroundExecutor;
    private final PackageManager mPackageManager;
    private final TaskStackChangeListeners mTaskStackChangeListeners;

    private ActivityManagerWrapper() {
        Context context = AppGlobals.getInitialApplication();
        this.mPackageManager = context != null ? context.getPackageManager() : null;
        this.mBackgroundExecutor = BackgroundExecutor.get();
        this.mTaskStackChangeListeners = new TaskStackChangeListeners(Looper.getMainLooper());
    }

    public static ActivityManagerWrapper getInstance() {
        return sInstance;
    }

    public int getCurrentUserId() {
        try {
            UserInfo ui = ActivityManager.getService().getCurrentUser();
            if (ui != null) {
                return ui.id;
            }
            return 0;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ActivityManager.RunningTaskInfo getRunningTask() {
        return getRunningTask(3);
    }

    public ActivityManager.RunningTaskInfo getRunningTask(@WindowConfiguration.ActivityType int ignoreActivityType) {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ActivityTaskManager.getService().getFilteredTasks(1, ignoreActivityType, 2);
            if (tasks.isEmpty()) {
                return null;
            }
            return tasks.get(0);
        } catch (RemoteException e) {
            return null;
        }
    }

    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int numTasks, int userId) {
        try {
            return ActivityTaskManager.getService().getRecentTasks(numTasks, 2, userId).getList();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get recent tasks", e);
            return new ArrayList();
        }
    }

    public ThumbnailData getTaskThumbnail(int taskId, boolean reducedResolution) {
        ActivityManager.TaskSnapshot snapshot = null;
        try {
            snapshot = ActivityTaskManager.getService().getTaskSnapshot(taskId, reducedResolution);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to retrieve task snapshot", e);
        }
        if (snapshot != null) {
            return new ThumbnailData(snapshot);
        }
        return new ThumbnailData();
    }

    public String getBadgedActivityLabel(ActivityInfo info, int userId) {
        return getBadgedLabel(info.loadLabel(this.mPackageManager).toString(), userId);
    }

    public String getBadgedApplicationLabel(ApplicationInfo appInfo, int userId) {
        return getBadgedLabel(appInfo.loadLabel(this.mPackageManager).toString(), userId);
    }

    public String getBadgedContentDescription(ActivityInfo info, int userId, ActivityManager.TaskDescription td) {
        String activityLabel;
        if (td == null || td.getLabel() == null) {
            activityLabel = info.loadLabel(this.mPackageManager).toString();
        } else {
            activityLabel = td.getLabel();
        }
        String applicationLabel = info.applicationInfo.loadLabel(this.mPackageManager).toString();
        String badgedApplicationLabel = getBadgedLabel(applicationLabel, userId);
        if (applicationLabel.equals(activityLabel)) {
            return badgedApplicationLabel;
        }
        return badgedApplicationLabel + " " + activityLabel;
    }

    private String getBadgedLabel(String label, int userId) {
        PackageManager packageManager;
        if (userId == UserHandle.myUserId() || (packageManager = this.mPackageManager) == null) {
            return label;
        }
        return packageManager.getUserBadgedLabel(label, new UserHandle(userId)).toString();
    }

    public void startRecentsActivity(Intent intent, final AssistDataReceiver assistDataReceiver, final RecentsAnimationListener animationHandler, final Consumer<Boolean> resultCallback, Handler resultCallbackHandler) {
        IAssistDataReceiver receiver = null;
        if (assistDataReceiver != null) {
            try {
                receiver = new IAssistDataReceiver.Stub() {
                    /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass1 */

                    public void onHandleAssistData(Bundle resultData) {
                        assistDataReceiver.onHandleAssistData(resultData);
                    }

                    public void onHandleAssistScreenshot(Bitmap screenshot) {
                        assistDataReceiver.onHandleAssistScreenshot(screenshot);
                    }
                };
            } catch (Exception e) {
                if (resultCallback != null) {
                    resultCallbackHandler.post(new Runnable() {
                        /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass4 */

                        @Override // java.lang.Runnable
                        public void run() {
                            resultCallback.accept(false);
                        }
                    });
                    return;
                }
                return;
            }
        }
        IRecentsAnimationRunner runner = null;
        if (animationHandler != null) {
            runner = new IRecentsAnimationRunner.Stub() {
                /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass2 */

                public void onAnimationStart(IRecentsAnimationController controller, RemoteAnimationTarget[] apps, Rect homeContentInsets, Rect minimizedHomeBounds) {
                    animationHandler.onAnimationStart(new RecentsAnimationControllerCompat(controller), RemoteAnimationTargetCompat.wrap(apps), homeContentInsets, minimizedHomeBounds);
                }

                public void onAnimationCanceled(boolean deferredWithScreenshot) {
                    animationHandler.onAnimationCanceled(deferredWithScreenshot);
                }
            };
        }
        ActivityTaskManager.getService().startRecentsActivity(intent, receiver, runner);
        if (resultCallback != null) {
            resultCallbackHandler.post(new Runnable() {
                /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    resultCallback.accept(true);
                }
            });
        }
    }

    public void cancelRecentsAnimation(boolean restoreHomeStackPosition) {
        try {
            ActivityTaskManager.getService().cancelRecentsAnimation(restoreHomeStackPosition);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to cancel recents animation", e);
        }
    }

    public void startActivityFromRecentsAsync(Task.TaskKey taskKey, ActivityOptions options, Consumer<Boolean> resultCallback, Handler resultCallbackHandler) {
        startActivityFromRecentsAsync(taskKey, options, 0, 0, resultCallback, resultCallbackHandler);
    }

    public void startActivityFromRecentsAsync(final Task.TaskKey taskKey, final ActivityOptions options, int windowingMode, int activityType, final Consumer<Boolean> resultCallback, final Handler resultCallbackHandler) {
        if (taskKey.windowingMode == 3) {
            if (options == null) {
                options = ActivityOptions.makeBasic();
            }
            options.setLaunchWindowingMode(4);
        } else if (!(windowingMode == 0 && activityType == 0)) {
            if (options == null) {
                options = ActivityOptions.makeBasic();
            }
            options.setLaunchWindowingMode(windowingMode);
            options.setLaunchActivityType(activityType);
        }
        this.mBackgroundExecutor.submit(new Runnable() {
            /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                final boolean result = false;
                try {
                    result = ActivityManagerWrapper.this.startActivityFromRecents(taskKey.id, options);
                } catch (Exception e) {
                }
                if (resultCallback != null) {
                    resultCallbackHandler.post(new Runnable() {
                        /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass5.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            resultCallback.accept(Boolean.valueOf(result));
                        }
                    });
                }
            }
        });
    }

    public boolean startActivityFromRecents(int taskId, ActivityOptions options) {
        Bundle optsBundle;
        if (options == null) {
            optsBundle = null;
        } else {
            try {
                optsBundle = options.toBundle();
            } catch (Exception e) {
                return false;
            }
        }
        ActivityTaskManager.getService().startActivityFromRecents(taskId, optsBundle);
        return true;
    }

    public boolean setTaskWindowingModeSplitScreenPrimary(int taskId, int createMode, Rect initialBounds) {
        try {
            return ActivityTaskManager.getService().setTaskWindowingModeSplitScreenPrimary(taskId, createMode, true, false, initialBounds, true);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void registerTaskStackListener(TaskStackChangeListener listener) {
        synchronized (this.mTaskStackChangeListeners) {
            this.mTaskStackChangeListeners.addListener(ActivityManager.getService(), listener);
        }
    }

    public void unregisterTaskStackListener(TaskStackChangeListener listener) {
        synchronized (this.mTaskStackChangeListeners) {
            this.mTaskStackChangeListeners.removeListener(listener);
        }
    }

    public void closeSystemWindows(final String reason) {
        this.mBackgroundExecutor.submit(new Runnable() {
            /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    ActivityManager.getService().closeSystemDialogs(reason);
                } catch (RemoteException e) {
                    Log.w(ActivityManagerWrapper.TAG, "Failed to close system windows", e);
                }
            }
        });
    }

    public void removeTask(final int taskId) {
        this.mBackgroundExecutor.submit(new Runnable() {
            /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    ActivityTaskManager.getService().removeTask(taskId);
                } catch (RemoteException e) {
                    Log.w(ActivityManagerWrapper.TAG, "Failed to remove task=" + taskId, e);
                }
            }
        });
    }

    public void removeAllRecentTasks() {
        this.mBackgroundExecutor.submit(new Runnable() {
            /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    ActivityTaskManager.getService().removeAllVisibleRecentTasks();
                } catch (RemoteException e) {
                    Log.w(ActivityManagerWrapper.TAG, "Failed to remove all tasks", e);
                }
            }
        });
    }

    public void cancelWindowTransition(int taskId) {
        try {
            ActivityTaskManager.getService().cancelTaskWindowTransition(taskId);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to cancel window transition for task=" + taskId, e);
        }
    }

    public boolean isScreenPinningActive() {
        try {
            return ActivityTaskManager.getService().getLockTaskModeState() == 2;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isScreenPinningEnabled() {
        if (AppGlobals.getInitialApplication() == null || Settings.System.getInt(AppGlobals.getInitialApplication().getContentResolver(), "lock_to_app_enabled", 0) == 0) {
            return false;
        }
        return true;
    }

    public boolean isLockToAppActive() {
        try {
            return ActivityTaskManager.getService().getLockTaskModeState() != 0;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isLockTaskKioskModeActive() {
        try {
            return ActivityTaskManager.getService().getLockTaskModeState() == 1;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean showVoiceSession(IBinder token, Bundle args, int flags) {
        IVoiceInteractionManagerService service = IVoiceInteractionManagerService.Stub.asInterface(ServiceManager.getService("voiceinteraction"));
        if (service == null) {
            return false;
        }
        try {
            return service.showSessionFromSession(token, args, flags);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean supportsFreeformMultiWindow(Context context) {
        if (context == null) {
            Log.w(TAG, "supportsFreeformMultiWindow, context is null, return false!!!");
            return false;
        }
        boolean freeformDevOption = Settings.Global.getInt(context.getContentResolver(), "enable_freeform_support", 0) != 0;
        if (!ActivityTaskManager.supportsMultiWindow(context) || context.getPackageManager() == null) {
            return false;
        }
        if (context.getPackageManager().hasSystemFeature("android.software.freeform_window_management") || freeformDevOption) {
            return true;
        }
        return false;
    }
}
