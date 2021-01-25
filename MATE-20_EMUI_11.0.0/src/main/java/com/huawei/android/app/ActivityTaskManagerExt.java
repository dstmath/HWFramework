package com.huawei.android.app;

import android.app.ActivityTaskManager;
import android.content.Context;
import android.os.RemoteException;

public class ActivityTaskManagerExt {
    public static boolean isInLockTaskMode() throws RemoteException {
        return ActivityTaskManager.getService().isInLockTaskMode();
    }

    public static void stopSystemLockTaskMode() throws RemoteException {
        ActivityTaskManager.getService().stopSystemLockTaskMode();
    }

    public static boolean supportsMultiWindow(Context context) {
        return ActivityTaskManager.supportsMultiWindow(context);
    }

    public static void setFocusedTask(int taskId) throws RemoteException {
        ActivityTaskManager.getService().setFocusedTask(taskId);
    }

    public static int getLockTaskModeState() throws RemoteException {
        return ActivityTaskManager.getService().getLockTaskModeState();
    }

    public static void removeStacksInWindowingModes(int[] windowingModes) {
        try {
            ActivityTaskManager.getService().removeStacksInWindowingModes(windowingModes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
