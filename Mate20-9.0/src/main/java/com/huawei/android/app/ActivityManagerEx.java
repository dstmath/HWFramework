package com.huawei.android.app;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageDataObserver;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.android.os.HwTransCodeEx;
import java.util.ArrayList;
import java.util.List;

public class ActivityManagerEx {
    public static final int MOVE_TASK_TO_BACK = 0;
    public static final int MOVE_TASK_TO_FRONT = 1;
    public static int RECENT_IGNORE_HOME_AND_RECENTS_STACK_TASKS = 0;
    public static int RECENT_INCLUDE_PROFILES = 0;
    public static int RECENT_INGORE_DOCKED_STACK_TOP_TASK = 0;
    public static int RECENT_INGORE_PINNED_STACK_TASKS = 0;
    public static final int REMOVE_TASK = 2;
    private static final String TAG = "ActivityManagerEx";

    public static class RunningAppProcessInfoEx {
        public static int FLAG_CANT_SAVE_STATE = 1;
        public static int FLAG_PERSISTENT = 2;
        public static int IMPORTANCE_CANT_SAVE_STATE = 350;
    }

    @Deprecated
    public static boolean isClonedProcess(int pid) {
        return false;
    }

    @Deprecated
    public static String getPackageNameForPid(int pid) {
        String res = null;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            data.writeInt(pid);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.GET_PACKAGE_NAME_FOR_PID_TRANSACTION, data, reply, 0);
            reply.readException();
            res = reply.readString();
            data.recycle();
            reply.recycle();
            return res;
        } catch (Exception e) {
            Log.e(TAG, "getPackageNameForPid", e);
            return res;
        }
    }

    public static int getFocusedStackId() throws RemoteException {
        ActivityManager.StackInfo info = ActivityManager.getService().getFocusedStackInfo();
        if (info != null) {
            return info.stackId;
        }
        return -1;
    }

    public static int getCurrentUser() {
        return ActivityManager.getCurrentUser();
    }

    public static int checkComponentPermission(String permission, int uid, int owningUid, boolean exported) {
        return ActivityManager.checkComponentPermission(permission, uid, owningUid, exported);
    }

    public static void registerUserSwitchObserver(UserSwitchObserverEx observer, String name) throws RemoteException {
        ActivityManager.getService().registerUserSwitchObserver(observer.getUserSwitchObserver(), name);
    }

    public static void setProcessForeground(IBinder token, int pid, boolean isForeground) throws RemoteException {
        ActivityManager.getService().setProcessImportant(token, pid, isForeground, "");
    }

    public static Configuration getConfiguration() throws RemoteException {
        return ActivityManagerNative.getDefault().getConfiguration();
    }

    public static void updatePersistentConfiguration(Configuration values) throws RemoteException {
        ActivityManagerNative.getDefault().updatePersistentConfiguration(values);
    }

    public static void resumeAppSwitches() throws RemoteException {
        ActivityManagerNative.getDefault().resumeAppSwitches();
    }

    public static void stopAppSwitches() throws RemoteException {
        ActivityManagerNative.getDefault().stopAppSwitches();
    }

    public static List<ActivityManager.RecentTaskInfo> getRecentTasksForUser(ActivityManager am, int maxNum, int flags, int userId) throws SecurityException {
        try {
            return ActivityManager.getService().getRecentTasks(maxNum, flags, userId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean startUserInBackground(int userId) throws RemoteException {
        return ActivityManagerNative.getDefault().startUserInBackground(userId);
    }

    public static boolean isUserRunning(int userId) throws RemoteException {
        return ActivityManagerNative.getDefault().isUserRunning(userId, 0);
    }

    public static long getLastActiveTime(ActivityManager.RecentTaskInfo info) {
        if (info != null) {
            return info.lastActiveTime;
        }
        return 0;
    }

    public static long getUserId(ActivityManager.RecentTaskInfo info) {
        if (info != null) {
            return (long) info.userId;
        }
        return 0;
    }

    public static void switchUser(int userId) throws RemoteException {
        ActivityManagerNative.getDefault().switchUser(userId);
    }

    public static boolean requestContentNode(ComponentName componentName, Bundle bundle, int token) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = false;
        boolean result = false;
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            if (componentName != null) {
                data.writeInt(1);
                componentName.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            if (bundle != null) {
                data.writeInt(1);
                bundle.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            data.writeInt(token);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.REQUEST_CONTENT, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            result = z;
        } catch (Exception e) {
            Log.e(TAG, "registerActivityObserver ", e);
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static boolean requestContentOther(ComponentName componentName, Bundle bundle, int token) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = false;
        boolean result = false;
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            if (componentName != null) {
                data.writeInt(1);
                componentName.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            if (bundle != null) {
                data.writeInt(1);
                bundle.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            data.writeInt(token);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.REQUEST_CONTENT_OTHER, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            result = z;
        } catch (Exception e) {
            Log.e(TAG, "registerActivityObserver ", e);
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static boolean addGameSpacePackageList(List<String> packageList) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = false;
        boolean result = false;
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            data.writeStringList(packageList);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.ADD_GAMESPACE_PACKAGE_LIST_TRANSACTION, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            result = z;
        } catch (RemoteException e) {
            Log.e(TAG, "addGameSpacePackageList error:" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static boolean delGameSpacePackageList(List<String> packageList) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = false;
        boolean result = false;
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            data.writeStringList(packageList);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.DEL_GAMESPACE_PACKAGE_LIST_TRANSACTION, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            result = z;
        } catch (RemoteException e) {
            Log.e(TAG, "delGameSpacePackageList error:" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static void registerGameObserver(IGameObserver observer) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            data.writeStrongBinder(observer != null ? observer.asBinder() : null);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.REGISTER_GAME_OBSERVER_TRANSACTION, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "registerGameObserver error:" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
    }

    public static void unregisterGameObserver(IGameObserver observer) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            data.writeStrongBinder(observer != null ? observer.asBinder() : null);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.UNREGISTER_GAME_OBSERVER_TRANSACTION, data, reply, 0);
            reply.readException();
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterGameObserver error:" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
    }

    public static boolean isInGameSpace(String packageName) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = false;
        boolean result = false;
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            data.writeString(packageName);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.IS_IN_GAMESPACE_TRANSACTION, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            result = z;
        } catch (RemoteException e) {
            Log.e(TAG, "isInGameSpace error:" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static List<String> getGameList() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<String> result = new ArrayList<>();
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.GET_GAMESPACE_LIST_TRANSACTION, data, reply, 0);
            reply.readException();
            reply.readStringList(result);
        } catch (RemoteException e) {
            Log.e(TAG, "getGameList error:" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static boolean isGameDndOn() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = false;
        boolean result = false;
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.IS_GAME_DND_ON_TRANSACTION, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            result = z;
        } catch (RemoteException e) {
            Log.e(TAG, "isGameDndOn error:" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static boolean isGameKeyControlOn() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = false;
        boolean result = false;
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.IS_GAME_KEYCONTROL_ON_TRANSACTION, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            result = z;
        } catch (RemoteException e) {
            Log.e(TAG, "isGameKeyControlOn error:" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static boolean isGameGestureDisabled() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = false;
        boolean result = false;
        try {
            data.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.IS_GAME_GESTURE_DISABLED_TRANSACTION, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                z = true;
            }
            result = z;
        } catch (RemoteException e) {
            Log.e(TAG, "isGameGestureDisabled error:" + e.getMessage());
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static void registerProcessObserver(IProcessObserverEx observerEx) throws RemoteException {
        if (observerEx != null) {
            ActivityManager.getService().registerProcessObserver(observerEx.getIProcessObserver());
        }
    }

    public static void unregisterProcessObserver(IProcessObserverEx observerEx) throws RemoteException {
        if (observerEx != null) {
            ActivityManager.getService().unregisterProcessObserver(observerEx.getIProcessObserver());
        }
    }

    public static int startActivityFromRecents(int taskId, Bundle bOptions) {
        try {
            return ActivityManagerNative.getDefault().startActivityFromRecents(taskId, bOptions);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static long[] getProcessPss(int[] pids) throws RemoteException {
        return ActivityManager.getService().getProcessPss(pids);
    }

    public static boolean clearApplicationUserData(String packageName, IPackageDataObserver observer) throws RemoteException {
        return ActivityManager.getService().clearApplicationUserData(packageName, false, observer, UserHandle.myUserId());
    }

    public static void forceStopPackage(String pkgname, int userId) throws RemoteException {
        ActivityManager.getService().forceStopPackage(pkgname, userId);
    }

    public static boolean isFreeFormVisible() {
        return HwActivityManager.isFreeFormVisible();
    }

    public static void registerHwActivityNotifier(IHwActivityNotifierEx notifier, String reason) {
        if (notifier != null) {
            HwActivityManager.registerHwActivityNotifier(notifier.getHwActivityNotifier(), reason);
        }
    }

    public static void unregisterHwActivityNotifier(IHwActivityNotifierEx notifier) {
        if (notifier != null) {
            HwActivityManager.unregisterHwActivityNotifier(notifier.getHwActivityNotifier());
        }
    }

    public static ActivityInfo getLastResumedActivity() {
        return HwActivityManager.getLastResumedActivity();
    }

    public static void moveTaskByType(int taskId, int flags, Bundle bOptions, int type) {
        switch (type) {
            case 0:
                ActivityManagerNative.getDefault().moveTaskBackwards(taskId);
                return;
            case 1:
                ActivityManagerNative.getDefault().moveTaskToFront(taskId, flags, bOptions);
                return;
            case 2:
                try {
                    ActivityManagerNative.getDefault().removeTask(taskId);
                    return;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            default:
                return;
        }
    }

    public static List<ActivityManager.RunningTaskInfo> getTasks(int num) {
        try {
            return ActivityManagerNative.getDefault().getTasks(num);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static int getStackId(ActivityManager.RunningTaskInfo taskInfo) {
        return taskInfo.stackId;
    }

    public static boolean isHighEndGfx() {
        return ActivityManager.isHighEndGfx();
    }

    public static void dismissSplitScreenToFocusedStack() {
        HwActivityManager.dismissSplitScreenToFocusedStack();
    }

    public static boolean enterCoordinationMode(Intent intent) {
        return HwActivityManager.enterCoordinationMode(intent);
    }

    public static boolean exitCoordinationMode(boolean toTop) {
        return HwActivityManager.exitCoordinationMode(toTop);
    }
}
