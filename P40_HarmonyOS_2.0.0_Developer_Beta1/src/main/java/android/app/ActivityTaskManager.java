package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.IActivityTaskManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Singleton;
import com.android.internal.R;
import java.util.List;

public class ActivityTaskManager {
    public static final String EXTRA_IGNORE_TARGET_SECURITY = "android.app.extra.EXTRA_IGNORE_TARGET_SECURITY";
    public static final String EXTRA_OPTIONS = "android.app.extra.OPTIONS";
    public static final String EXTRA_PERMISSION_TOKEN = "android.app.extra.PERMISSION_TOKEN";
    @UnsupportedAppUsage(trackingBug = 129726065)
    private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton = new Singleton<IActivityTaskManager>() {
        /* class android.app.ActivityTaskManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IActivityTaskManager create() {
            return IActivityTaskManager.Stub.asInterface(ServiceManager.getService(Context.ACTIVITY_TASK_SERVICE));
        }
    };
    public static final int INVALID_STACK_ID = -1;
    public static final int INVALID_TASK_ID = -1;
    public static final int RESIZE_MODE_FORCED = 2;
    public static final int RESIZE_MODE_PRESERVE_WINDOW = 1;
    public static final int RESIZE_MODE_SYSTEM = 0;
    public static final int RESIZE_MODE_SYSTEM_SCREEN_ROTATION = 1;
    public static final int RESIZE_MODE_USER = 1;
    public static final int RESIZE_MODE_USER_FORCED = 3;
    public static final int SPLIT_SCREEN_CREATE_MODE_BOTTOM_OR_RIGHT = 1;
    public static final int SPLIT_SCREEN_CREATE_MODE_TOP_OR_LEFT = 0;
    private static int sMaxRecentTasks = -1;

    ActivityTaskManager(Context context, Handler handler) {
    }

    public static IActivityTaskManager getService() {
        return IActivityTaskManagerSingleton.get();
    }

    public void setTaskWindowingMode(int taskId, int windowingMode, boolean toTop) throws SecurityException {
        try {
            getService().setTaskWindowingMode(taskId, windowingMode, toTop);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTaskWindowingModeSplitScreenPrimary(int taskId, int createMode, boolean toTop, boolean animate, Rect initialBounds, boolean showRecents) throws SecurityException {
        try {
            getService().setTaskWindowingModeSplitScreenPrimary(taskId, createMode, toTop, animate, initialBounds, showRecents);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void resizeStack(int stackId, Rect bounds) throws SecurityException {
        try {
            getService().resizeStack(stackId, bounds, false, false, false, -1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeStacksInWindowingModes(int[] windowingModes) throws SecurityException {
        try {
            getService().removeStacksInWindowingModes(windowingModes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeStacksWithActivityTypes(int[] activityTypes) throws SecurityException {
        try {
            getService().removeStacksWithActivityTypes(activityTypes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeAllVisibleRecentTasks() {
        try {
            getService().removeAllVisibleRecentTasks();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getMaxRecentTasksStatic() {
        int i = sMaxRecentTasks;
        if (i >= 0) {
            return i;
        }
        int i2 = ActivityManager.isLowRamDeviceStatic() ? 36 : 48;
        sMaxRecentTasks = i2;
        return i2;
    }

    public static int getDefaultAppRecentsLimitStatic() {
        return getMaxRecentTasksStatic() / 6;
    }

    public static int getMaxAppRecentsLimitStatic() {
        return getMaxRecentTasksStatic() / 2;
    }

    public static boolean supportsMultiWindow(Context context) {
        return (!ActivityManager.isLowRamDeviceStatic() || context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) && Resources.getSystem().getBoolean(R.bool.config_supportsMultiWindow);
    }

    public static boolean supportsSplitScreenMultiWindow(Context context) {
        return supportsMultiWindow(context) && Resources.getSystem().getBoolean(R.bool.config_supportsSplitScreenMultiWindow);
    }

    public boolean moveTopActivityToPinnedStack(int stackId, Rect bounds) {
        try {
            return getService().moveTopActivityToPinnedStack(stackId, bounds);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startSystemLockTaskMode(int taskId) {
        try {
            getService().startSystemLockTaskMode(taskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void stopSystemLockTaskMode() {
        try {
            getService().stopSystemLockTaskMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void moveTaskToStack(int taskId, int stackId, boolean toTop) {
        try {
            getService().moveTaskToStack(taskId, stackId, toTop);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void resizeStack(int stackId, Rect bounds, boolean animate) {
        try {
            getService().resizeStack(stackId, bounds, false, false, animate, -1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void resizeTask(int taskId, Rect bounds) {
        try {
            getService().resizeTask(taskId, bounds, 0);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void resizeDockedStack(Rect stackBounds, Rect taskBounds) {
        try {
            getService().resizeDockedStack(stackBounds, taskBounds, null, null, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String listAllStacks() {
        try {
            List<ActivityManager.StackInfo> stacks = getService().getAllStackInfos();
            StringBuilder sb = new StringBuilder();
            if (stacks != null) {
                for (ActivityManager.StackInfo info : stacks) {
                    sb.append(info);
                    sb.append("\n");
                }
            }
            return sb.toString();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearLaunchParamsForPackages(List<String> packageNames) {
        try {
            getService().clearLaunchParamsForPackages(packageNames);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void setDisplayToSingleTaskInstance(int displayId) {
        try {
            getService().setDisplayToSingleTaskInstance(displayId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
