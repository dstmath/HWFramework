package com.android.systemui.shared.recents.hwutil;

import android.app.ActivityManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hsm.MediaTransactWrapper;
import android.net.Uri;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.android.systemui.shared.recents.model.Task;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HwRecentsTaskUtils {
    public static final String ACTION_REPLY_REMOVE_TASK_FINISH = "com.huawei.systemmanager.action.REPLY_TRIM_ALL";
    public static final String ACTION_REQUEST_REMOVE_ALL_TASK = "com.huawei.systemmanager.action.REQUEST_TRIM_ALL";
    public static final String ACTION_REQUEST_REMOVE_SINGAL_TASK = "huawei.intent.action.hsm_remove_pkg";
    private static final String AUTHORITY = "com.android.systemui.recent.HwRecentsLockProdiver";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.android.systemui.recent.HwRecentsLockProdiver");
    public static final String DATABASE_RECENT_LOCK_STATE = "recent_lock_state";
    public static final String DATABASE_RECENT_PKG_NAME = "recent_lock_pkgname";
    public static final boolean ENABLED_FREEFORM = SystemProperties.getBoolean("ro.config.hw_freeform_enable", false);
    public static final String EXTRA_REQUEST_ID = "request_id";
    private static final String EXTRA_START_TRIM_TIME = "start_time";
    public static final long MAX_REMOVE_TASK_TIME = 20000;
    public static final String PERMISSION_REPLY_REMOVE_TASK = "com.android.systemui.permission.removeTask";
    public static final String PERMISSION_REQUEST_REMOVE_TASK = "com.huawei.android.launcher.permission.ONEKEYCLEAN";
    public static final String PKG_SYS_MANAGER = "com.huawei.systemmanager";
    private static final String TAG = "HwRecentsTaskUtils";
    private static boolean isRemovingTask = false;
    private static Map<String, Boolean> lockStateMap = null;
    private static long mRequestRemoveTaskClockTime = 0;
    private static long mRequestRemoveTaskSystemTime = 0;
    private static Set<Integer> musiclist = null;

    public static synchronized void setRemoveTaskSystemTime(long removeTaskSystemTime) {
        synchronized (HwRecentsTaskUtils.class) {
            mRequestRemoveTaskSystemTime = removeTaskSystemTime;
        }
    }

    public static synchronized void setRemoveTaskClockTime(long requestRemoveTaskClockTime) {
        synchronized (HwRecentsTaskUtils.class) {
            mRequestRemoveTaskClockTime = requestRemoveTaskClockTime;
        }
    }

    public static synchronized void setInRemoveTask(boolean isRemovingTask2) {
        synchronized (HwRecentsTaskUtils.class) {
            isRemovingTask = isRemovingTask2;
        }
    }

    public static synchronized long getRequestRemoveTaskSystemTime() {
        long j;
        synchronized (HwRecentsTaskUtils.class) {
            j = mRequestRemoveTaskSystemTime;
        }
        return j;
    }

    public static synchronized long getRequestRemoveTaskClockTime() {
        long j;
        synchronized (HwRecentsTaskUtils.class) {
            j = mRequestRemoveTaskClockTime;
        }
        return j;
    }

    public static synchronized boolean isInRemoveTask() {
        boolean z;
        synchronized (HwRecentsTaskUtils.class) {
            z = isRemovingTask && SystemClock.elapsedRealtime() - getRequestRemoveTaskClockTime() < MAX_REMOVE_TASK_TIME;
        }
        return z;
    }

    public static synchronized boolean willRemovedTask(ActivityManager.RecentTaskInfo task) {
        synchronized (HwRecentsTaskUtils.class) {
            boolean isInRemove = isRemovingTask;
            long removeTime = mRequestRemoveTaskSystemTime;
            if (isInRemoveTask()) {
                Log.d(TAG, "in willRemovedTask:" + isInRemove + ", task:" + task.id + ",activeTime:" + task.lastActiveTime + ",requestTime:" + removeTime + ", less:" + (removeTime - task.lastActiveTime) + ", absTime:" + Math.abs(task.lastActiveTime - removeTime));
                if (task.lastActiveTime <= removeTime) {
                    return true;
                }
            }
            return false;
        }
    }

    public static synchronized Map<String, Boolean> searchFromCache() {
        synchronized (HwRecentsTaskUtils.class) {
            if (lockStateMap == null) {
                Log.e(TAG, "when call searchFromCache, lockStateMap is null!!");
                HashMap hashMap = new HashMap();
                return hashMap;
            }
            Map<String, Boolean> map = lockStateMap;
            return map;
        }
    }

    private static synchronized void setLockStateMap(Map<String, Boolean> map) {
        synchronized (HwRecentsTaskUtils.class) {
            lockStateMap = map;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0059, code lost:
        if (r2 == null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x005b, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006c, code lost:
        if (r2 == null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006f, code lost:
        return r0;
     */
    private static Map<String, Boolean> searchFromDate(Context context) {
        Log.d(TAG, "searchFromDate");
        Map<String, Boolean> lockmap = new HashMap<>();
        Context context2 = context;
        CursorLoader cursorLoader = new CursorLoader(context2, AUTHORITY_URI, new String[]{DATABASE_RECENT_PKG_NAME, DATABASE_RECENT_LOCK_STATE}, null, null, null);
        Cursor tmpCursor = null;
        try {
            tmpCursor = cursorLoader.loadInBackground();
            if (tmpCursor != null && tmpCursor.moveToFirst()) {
                do {
                    String key = tmpCursor.getString(tmpCursor.getColumnIndex(DATABASE_RECENT_PKG_NAME));
                    boolean z = true;
                    if (tmpCursor.getInt(tmpCursor.getColumnIndex(DATABASE_RECENT_LOCK_STATE)) != 1) {
                        z = false;
                    }
                    lockmap.put(key, Boolean.valueOf(z));
                } while (tmpCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } catch (Throwable th) {
            if (tmpCursor != null) {
                tmpCursor.close();
            }
            throw th;
        }
    }

    public static synchronized Map<String, Boolean> refreshToCache(Context context) {
        Map<String, Boolean> results;
        synchronized (HwRecentsTaskUtils.class) {
            Log.d(TAG, "refreshToCache");
            new HashMap();
            synchronized (HwRecentsTaskUtils.class) {
                setLockStateMap(searchFromDate(context));
                results = lockStateMap;
            }
        }
        return results;
    }

    public static boolean isHwTaskLocked(String pkgName, boolean def) {
        Map<String, Boolean> map = searchFromCache();
        Boolean locked = Boolean.valueOf(def);
        if (map.get(pkgName) != null) {
            locked = map.get(pkgName);
        }
        return locked.booleanValue();
    }

    public static void refreshPlayingMusicUidSet() {
        musiclist = MediaTransactWrapper.playingMusicUidSet();
    }

    public static boolean getPlayingMusicUid(Context context, Task task) {
        if (context == null || task == null || musiclist == null || musiclist.isEmpty()) {
            return false;
        }
        try {
            if (!musiclist.contains(Integer.valueOf(context.getPackageManager().getPackageUid(task.packageName, task.key.userId)))) {
                return false;
            }
            Log.d(TAG, "PlayingMusic is " + task.packageName);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Can not get packageUid return.");
            return false;
        }
    }

    public static void sendRemoveTaskToSystemManager(Context context, Task task) {
        if (context == null || task == null || task.key == null) {
            Log.i(TAG, "(sendRemoveTaskToSystemManager context == null || task == null || task.key == null), return");
            return;
        }
        Log.i(TAG, "remove task send broadcast packageName=" + task.packageName + ", userId=" + task.key.userId + ",taskId=" + task.key.id);
        Intent intent = new Intent(ACTION_REQUEST_REMOVE_SINGAL_TASK);
        intent.putExtra("pkg_name", task.packageName);
        intent.putExtra("userid", task.key.userId);
        intent.putExtra("taskid", task.key.id);
        intent.setPackage(PKG_SYS_MANAGER);
        context.sendBroadcast(intent);
    }

    private static Intent getRemoveTaskRequestIntent() {
        long currentRequestId = System.currentTimeMillis();
        Log.d(TAG, "gener requestId:" + currentRequestId + " for remove all task");
        return new Intent(ACTION_REQUEST_REMOVE_ALL_TASK).putExtra(EXTRA_REQUEST_ID, currentRequestId).putExtra("start_time", getRequestRemoveTaskSystemTime());
    }

    public static void sendRemoveAllTask(Context context) {
        if (context == null) {
            Log.i(TAG, "(sendRemoveAllTask context == null return");
        } else {
            context.sendBroadcast(getRemoveTaskRequestIntent(), PERMISSION_REQUEST_REMOVE_TASK);
        }
    }
}
