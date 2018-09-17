package com.android.server.am;

import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.huawei.android.app.IGameObserver;
import java.util.ArrayList;
import java.util.List;

public final class HwGameAssistantController {
    public static final boolean DEBUG = false;
    public static final int EVENT_MARK_AS_GAME = 4;
    public static final int EVENT_MOVE_BACKGROUND = 2;
    public static final int EVENT_MOVE_FRONT = 1;
    public static final int EVENT_REPLACE_FRONT = 3;
    public static final String GAME_GESTURE_MODE = "game_gesture_disabled_mode";
    public static final int GAME_GESTURE_MODE_CLOSE = 2;
    public static final int GAME_GESTURE_MODE_DEFAULT = 2;
    public static final int GAME_GESTURE_MODE_OPEN = 1;
    public static final String GAME_KEY_CONTROL_MODE = "game_key_control_mode";
    public static final int GAME_KEY_CONTROL_MODE_CLOSE = 2;
    public static final int GAME_KEY_CONTROL_MODE_DEFAULT = 2;
    public static final int GAME_KEY_CONTROL_MODE_OPEN = 1;
    private static final int MSG_FOREGROUND_ACTIVITY_CHANGED = 100;
    private static final int MSG_GAME_LIST_CHANGED = 102;
    private static final int MSG_GAME_STATUS_CHANGED = 101;
    private static final int MSG_MARK_AS_GAME = 103;
    private static final int MSG_PACKAGE_REMOVED_OR_DATA_CLEARED = 104;
    public static final String PERMISSION_READ_GAME_LIST = "com.huawei.gameassistant.permission.READ_GAME_LIST";
    public static final String PERMISSION_WRITE_GAME_LIST = "com.huawei.gameassistant.permission.WIRTE_GAME_LIST";
    static final String TAG = "HwGameAssistantController";
    private final Context mContext;
    private int mCurFgPid;
    private String mCurFgPkg;
    private boolean mCurIsGame;
    final RemoteCallbackList<IGameObserver> mGameObservers = new RemoteCallbackList();
    private ArrayList<String> mGameSpacePackageList = new ArrayList();
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private String mLastForegroundPkg = "";
    private boolean mLastIsGame = false;
    private IProcessObserver mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            Slog.d(HwGameAssistantController.TAG, "pid=" + pid + ", uid=" + uid + ", foreground=" + foregroundActivities);
            HwGameAssistantController.this.mHandler.sendEmptyMessage(100);
        }

        public void onProcessDied(int pid, int uid) {
        }
    };
    private final HwActivityManagerService mService;

    public HwGameAssistantController(HwActivityManagerService service) {
        this.mService = service;
        this.mContext = service.mContext;
        initHandlerThread();
        this.mService.registerProcessObserver(this.mProcessObserver);
    }

    private void initHandlerThread() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                Bundle bundle;
                switch (msg.what) {
                    case 100:
                        HwGameAssistantController.this.handleForegroundActivityChanged();
                        return;
                    case 101:
                        bundle = msg.obj;
                        HwGameAssistantController.this.handleGameStatusChanged(bundle.getString(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY), bundle.getInt("event"));
                        return;
                    case 102:
                        HwGameAssistantController.this.handleGameListChanged();
                        return;
                    case 104:
                        bundle = (Bundle) msg.obj;
                        HwGameAssistantController.this.handlePackageRemovedOrDataCleared(bundle.getInt("userId"), bundle.getString(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY));
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private boolean isPrimaryUser(int userId) {
        UserInfo userInfo = null;
        long identity = Binder.clearCallingIdentity();
        try {
            userInfo = UserManager.get(this.mContext).getUserInfo(userId);
            if (userInfo == null || (userInfo.isPrimary() ^ 1) != 0) {
                return false;
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private UserInfo getCurrentUserInfo() {
        UserInfo userInfo = null;
        long identity = Binder.clearCallingIdentity();
        try {
            userInfo = this.mService.getCurrentUser();
            return userInfo;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private int getCurrentUserId() {
        UserInfo userInfo = getCurrentUserInfo();
        return userInfo != null ? userInfo.id : 0;
    }

    private boolean isGameModeDisabled() {
        UserInfo userInfo = getCurrentUserInfo();
        if (userInfo != null && (userInfo.isPrimary() ^ 1) == 0) {
            return false;
        }
        Slog.e(TAG, "Game Mode is disabled for non-primary user!");
        return true;
    }

    private void updateForegroundActivityState() {
        this.mCurFgPkg = "";
        this.mCurFgPid = 0;
        ActivityRecord topActivity = this.mService.getLastResumedActivity();
        if (topActivity != null && isPrimaryUser(topActivity.userId)) {
            try {
                this.mCurFgPkg = topActivity.packageName;
                if (topActivity.app != null) {
                    this.mCurFgPid = topActivity.app.pid;
                }
            } catch (NullPointerException e) {
                Slog.e(TAG, e.toString());
            }
        }
        this.mCurIsGame = isGame(this.mCurFgPkg);
    }

    private String getForegroundPackage() {
        return this.mLastForegroundPkg;
    }

    private boolean isGameForeground() {
        boolean isForground = isGame(getForegroundPackage());
        Slog.d(TAG, "isGameForeground? " + isForground);
        return isForground;
    }

    private boolean hasPermission(String permission, String func) {
        if (this.mService.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w(TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return false;
    }

    public boolean addGameSpacePackageList(List<String> packageList) {
        if (!hasPermission(PERMISSION_WRITE_GAME_LIST, "addGameSpacePackageList") || isGameModeDisabled()) {
            return false;
        }
        if (packageList == null || packageList.isEmpty()) {
            Slog.e(TAG, "addGameSpacePackageList error: empty list");
            return false;
        }
        Slog.d(TAG, "addGameSpacePackageList, pid=" + Binder.getCallingPid() + ", list=" + packageList.toString());
        boolean changed = false;
        synchronized (this.mGameSpacePackageList) {
            String foregroundPkg = getForegroundPackage();
            int j = packageList.size();
            for (int i = 0; i < j; i++) {
                String pkgName = (String) packageList.get(i);
                if (!(TextUtils.isEmpty(pkgName) || (this.mGameSpacePackageList.contains(pkgName) ^ 1) == 0)) {
                    Slog.d(TAG, "add to Game Space:" + pkgName);
                    this.mGameSpacePackageList.add(pkgName);
                    changed = true;
                    if (foregroundPkg != null && foregroundPkg.equals(pkgName)) {
                        this.mLastIsGame = true;
                        Bundle bundle = new Bundle();
                        bundle.putString(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, pkgName);
                        bundle.putInt("event", 4);
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(101, bundle));
                    }
                }
            }
        }
        if (changed) {
            this.mHandler.sendEmptyMessage(102);
        }
        return changed;
    }

    public boolean delGameSpacePackageList(List<String> packageList) {
        if (!hasPermission(PERMISSION_WRITE_GAME_LIST, "delGameSpacePackageList") || isGameModeDisabled()) {
            return false;
        }
        if (packageList == null || packageList.isEmpty()) {
            Slog.e(TAG, "delGameSpacePackageList error: empty list");
            return false;
        }
        Slog.d(TAG, "delGameSpacePackageList, pid=" + Binder.getCallingPid() + ", list=" + packageList.toString());
        boolean changed = false;
        synchronized (this.mGameSpacePackageList) {
            int j = packageList.size();
            for (int i = 0; i < j; i++) {
                String pkgName = (String) packageList.get(i);
                if (this.mGameSpacePackageList.contains(pkgName)) {
                    Slog.d(TAG, "del from Game Space:" + pkgName);
                    this.mGameSpacePackageList.remove(pkgName);
                    changed = true;
                }
            }
        }
        if (changed) {
            this.mHandler.sendEmptyMessage(102);
        }
        return changed;
    }

    private boolean isGame(String packageName) {
        boolean contains;
        synchronized (this.mGameSpacePackageList) {
            contains = this.mGameSpacePackageList.contains(packageName);
        }
        return contains;
    }

    public boolean isInGameSpace(String packageName) {
        if (isGameModeDisabled()) {
            return false;
        }
        return isGame(packageName);
    }

    public List<String> getGameList() {
        if (!hasPermission(PERMISSION_READ_GAME_LIST, "getGameList") || isGameModeDisabled()) {
            return null;
        }
        List<String> list = new ArrayList();
        synchronized (this.mGameSpacePackageList) {
            list.addAll(this.mGameSpacePackageList);
        }
        Slog.d(TAG, "getGameList: " + list.toString());
        return list;
    }

    public void registerGameObserver(IGameObserver observer) {
        if (observer != null) {
            Slog.d(TAG, "registerGameObserver:" + observer + ", result=" + this.mGameObservers.register(observer));
        }
    }

    public void unregisterGameObserver(IGameObserver observer) {
        if (observer != null) {
            Slog.d(TAG, "unregisterGameObserver:" + observer + ", result=" + this.mGameObservers.unregister(observer));
        }
    }

    public boolean isGameDndOn() {
        return isGameForeground();
    }

    public boolean isGameKeyControlOn() {
        int userId = getCurrentUserId();
        int mode = Secure.getIntForUser(this.mContext.getContentResolver(), GAME_KEY_CONTROL_MODE, 2, userId);
        Slog.d(TAG, "Keycontrol mode is " + mode + " for user " + userId);
        return mode == 1 ? isGameForeground() : false;
    }

    public boolean isGameGestureDisabled() {
        int userId = getCurrentUserId();
        int mode = Secure.getIntForUser(this.mContext.getContentResolver(), GAME_GESTURE_MODE, 2, userId);
        Slog.d(TAG, "Gesture disable mode is " + mode + " for user " + userId);
        return mode == 1 ? isGameForeground() : false;
    }

    private void handleGameStatusChanged(String packageName, int event) {
        dispatchGameStatusChanged(packageName, event);
    }

    private void handleForegroundActivityChanged() {
        updateForegroundActivityState();
        int event = 0;
        if (!(this.mCurFgPkg == null || (this.mCurFgPkg.equals(this.mLastForegroundPkg) ^ 1) == 0)) {
            if (this.mLastIsGame) {
                event = this.mCurIsGame ? 3 : 2;
            } else if (this.mCurIsGame) {
                event = 1;
            }
        }
        this.mLastForegroundPkg = this.mCurFgPkg;
        this.mLastIsGame = this.mCurIsGame;
        if (event != 0) {
            dispatchGameStatusChanged(this.mCurFgPkg, event);
        }
    }

    private void dispatchGameStatusChanged(String packageName, int event) {
        int i = this.mGameObservers.beginBroadcast();
        Slog.d(TAG, "dispatchGameStatusChanged, packageName=" + packageName + ", event=" + event + ", i=" + i);
        while (i > 0) {
            i--;
            IGameObserver observer = (IGameObserver) this.mGameObservers.getBroadcastItem(i);
            if (observer != null) {
                try {
                    observer.onGameStatusChanged(packageName, event);
                } catch (RemoteException e) {
                    Slog.e(TAG, "onGameStatusChanged error." + e.getMessage());
                }
            }
        }
        this.mGameObservers.finishBroadcast();
    }

    private void handleGameListChanged() {
        dispatchGameListChanged();
    }

    private void dispatchGameListChanged() {
        int i = this.mGameObservers.beginBroadcast();
        Slog.d(TAG, "dispatchGameListChanged, i=" + i);
        while (i > 0) {
            i--;
            IGameObserver observer = (IGameObserver) this.mGameObservers.getBroadcastItem(i);
            if (observer != null) {
                try {
                    observer.onGameListChanged();
                } catch (RemoteException e) {
                    Slog.e(TAG, "onGameListChanged error." + e.getMessage());
                }
            }
        }
        this.mGameObservers.finishBroadcast();
    }

    /* JADX WARNING: Missing block: B:15:0x002c, code:
            r3.mHandler.sendEmptyMessage(102);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handlePackageRemovedOrDataCleared(int userId, String packageName) {
        if (isPrimaryUser(userId) && "com.huawei.gameassistant".equals(packageName)) {
            synchronized (this.mGameSpacePackageList) {
                if (this.mGameSpacePackageList.isEmpty()) {
                } else {
                    Slog.d(TAG, "Clear gamespace because GameAssistant is removed or data cleared!");
                    this.mGameSpacePackageList.clear();
                }
            }
        }
    }

    void onPackageRemovedOrDataCleared(Intent intent) {
        if (intent != null && intent.getData() != null) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            String packageName = intent.getData().getSchemeSpecificPart();
            Bundle bundle = new Bundle();
            bundle.putInt("userId", userId);
            bundle.putString(PreciseIgnore.COMP_COMM_RELATED_PACKAGE_ATTR_KEY, packageName);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(104, bundle));
        }
    }
}
