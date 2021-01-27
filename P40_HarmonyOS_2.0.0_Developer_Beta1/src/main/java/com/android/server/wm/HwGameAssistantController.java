package com.android.server.wm;

import android.app.ActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.am.ActivityManagerService;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.app.IGameObserverEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.util.ArrayList;
import java.util.List;

public final class HwGameAssistantController {
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_PID = "pid";
    private static final String ATTR_UID = "uid";
    private static final String ATTR_WINDOWING_MODE = "windowingMode";
    private static final int EVENT_MARK_AS_GAME = 4;
    private static final int EVENT_MOVE_BACKGROUND = 2;
    private static final int EVENT_MOVE_FRONT = 1;
    private static final int EVENT_REPLACE_FRONT = 3;
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
    private static final int MSG_PACKAGE_REMOVED_OR_DATA_CLEARED = 104;
    private static final String PERMISSION_READ_GAME_LIST = "com.huawei.gameassistant.permission.READ_GAME_LIST";
    private static final String PERMISSION_WRITE_GAME_LIST = "com.huawei.gameassistant.permission.WIRTE_GAME_LIST";
    private static final String TAG = "HwGameAssistantController";
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        /* class com.android.server.wm.HwGameAssistantController.AnonymousClass1 */

        public void call(Bundle extras) {
            if (extras == null) {
                Slog.i(HwGameAssistantController.TAG, "AMS callback , extras=null");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putInt(HwGameAssistantController.ATTR_PID, extras.getInt("toPid"));
            bundle.putInt("uid", extras.getInt("toUid"));
            bundle.putString("packageName", extras.getString("toPackage"));
            bundle.putInt(HwGameAssistantController.ATTR_WINDOWING_MODE, extras.getInt(HwGameAssistantController.ATTR_WINDOWING_MODE));
            HwGameAssistantController.this.mHandler.sendMessage(HwGameAssistantController.this.mHandler.obtainMessage(100, bundle));
        }
    };
    private final Context mContext;
    private int mCurFgPid;
    private int mCurFgPidEx;
    private String mCurFgPkg;
    private String mCurFgPkgEx;
    private int mCurrentUserId = 0;
    final RemoteCallbackList<IGameObserver> mGameObservers = new RemoteCallbackList<>();
    final RemoteCallbackList<IGameObserverEx> mGameObserversEx = new RemoteCallbackList<>();
    private final List<String> mGameSpacePackageList = new ArrayList();
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mIsCurGame;
    private boolean mIsCurGameEx;
    private boolean mIsLastGame = false;
    private boolean mIsLastGameEx = false;
    private String mLastFgPkgEx = "";
    private String mLastForegroundPkg = "";
    private SynchronousUserSwitchObserver mUserSwitchObserver = new SynchronousUserSwitchObserver() {
        /* class com.android.server.wm.HwGameAssistantController.AnonymousClass2 */

        public void onUserSwitching(int newUserId) throws RemoteException {
            Slog.w(HwGameAssistantController.TAG, "onUserSwitching newUserId = " + newUserId);
            HwGameAssistantController.this.mCurrentUserId = newUserId;
        }
    };

    public HwGameAssistantController(Context context) {
        this.mContext = context;
        initHandlerThread();
    }

    public void systemReady() {
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, "appSwitch");
        try {
            ActivityManager.getService().registerUserSwitchObserver(this.mUserSwitchObserver, TAG);
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGameAssistantController call registerUserSwitchObserver error");
        }
        registerBroadcast();
    }

    private void initHandlerThread() {
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            /* class com.android.server.wm.HwGameAssistantController.AnonymousClass3 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 100:
                        if (msg.obj instanceof Bundle) {
                            Bundle bundle = (Bundle) msg.obj;
                            int pid = bundle.getInt(HwGameAssistantController.ATTR_PID);
                            int uid = bundle.getInt("uid");
                            String pkgName = bundle.getString("packageName");
                            HwGameAssistantController.this.updateForegroundActivityState(pid, uid, pkgName);
                            HwGameAssistantController.this.updateForegroundActivityStateEx(pid, uid, pkgName, bundle.getInt(HwGameAssistantController.ATTR_WINDOWING_MODE));
                            HwGameAssistantController.this.handleForegroundActivityChangedEx();
                            HwGameAssistantController.this.handleForegroundActivityChanged();
                            return;
                        }
                        return;
                    case 101:
                        if (msg.obj instanceof Bundle) {
                            Bundle bundle2 = (Bundle) msg.obj;
                            HwGameAssistantController.this.handleGameStatusChanged(bundle2.getString("packageName"), bundle2.getInt("event"));
                            return;
                        }
                        return;
                    case 102:
                        HwGameAssistantController.this.handleGameListChanged();
                        return;
                    case 103:
                    default:
                        return;
                    case 104:
                        if (msg.obj instanceof Bundle) {
                            Bundle bundle3 = (Bundle) msg.obj;
                            HwGameAssistantController.this.handlePackageRemovedOrDataCleared(bundle3.getInt("userId"), bundle3.getString("packageName"));
                            return;
                        }
                        return;
                }
            }
        };
    }

    private boolean isPrimaryUser(int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = UserManager.get(this.mContext).getUserInfo(userId);
            if (userInfo == null || !userInfo.isPrimary()) {
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
            userInfo = ActivityManager.getService().getCurrentUser();
        } catch (RemoteException e) {
            Slog.w(TAG, "getCurrentUser failed");
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return userInfo;
    }

    private int getCurrentUserId() {
        UserInfo userInfo = getCurrentUserInfo();
        if (userInfo != null) {
            return userInfo.id;
        }
        return 0;
    }

    private boolean isGameModeDisabled() {
        UserInfo userInfo = getCurrentUserInfo();
        if (userInfo != null && userInfo.isPrimary()) {
            return false;
        }
        Slog.e(TAG, "Game Mode is disabled for non-primary user!");
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateForegroundActivityState(int pid, int uid, String packageName) {
        this.mCurFgPkg = "";
        this.mCurFgPid = 0;
        if (isPrimaryUser(UserHandle.getUserId(uid))) {
            this.mCurFgPkg = packageName;
            this.mCurFgPid = pid;
        }
        this.mIsCurGame = isGame(this.mCurFgPkg);
        Slog.d(TAG, "UPDATE: mCurFgPkg=" + this.mCurFgPkg + ", mCurFgPid=" + this.mCurFgPid + ", mIsCurGame=" + this.mIsCurGame);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateForegroundActivityStateEx(int pid, int uid, String packageName, int windowingMode) {
        if (!isFloating(windowingMode)) {
            updateForegroundActivityStateEx(pid, uid, packageName);
        } else if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG, "UPDATE_Ex: no update because windowingMode=" + windowingMode);
        }
    }

    private void updateForegroundActivityStateEx(int pid, int uid, String packageName) {
        this.mCurFgPkgEx = "";
        this.mCurFgPidEx = 0;
        if (isPrimaryUser(UserHandle.getUserId(uid))) {
            this.mCurFgPkgEx = packageName;
            this.mCurFgPidEx = pid;
        }
        this.mIsCurGameEx = isGame(this.mCurFgPkgEx);
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG, "UPDATE_Ex: mCurFgPkgEx=" + this.mCurFgPkgEx + ", mCurFgPidEx=" + this.mCurFgPidEx + ", mIsCurGameEx=" + this.mIsCurGameEx);
        }
    }

    private boolean isFloating(int windowingMode) {
        return windowingMode == 5 || windowingMode == 2 || windowingMode == 10 || windowingMode == 102;
    }

    public void updateByToggleFreeFormMaximize(int pid, int uid, String packageName) {
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG, "updateByFreeFormMax: pkgName=" + packageName);
        }
        updateForegroundActivityStateEx(pid, uid, packageName);
        handleForegroundActivityChangedEx();
    }

    private String getForegroundPackage() {
        return this.mLastForegroundPkg;
    }

    private boolean isGameForeground() {
        boolean isGameFg = this.mCurrentUserId == 0 && this.mIsLastGame;
        Slog.d(TAG, "isGameForeground? " + isGameFg);
        return isGameFg;
    }

    private boolean isGameForegroundEx() {
        boolean isGameFg = this.mCurrentUserId == 0 && this.mIsLastGameEx;
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG, "isGameForegroundEx? " + isGameFg);
        }
        return isGameFg;
    }

    /* access modifiers changed from: package-private */
    public int checkCallingPermission(String permission) {
        if (Binder.getCallingPid() == ActivityManagerService.MY_PID) {
            return 0;
        }
        return this.mContext.checkCallingPermission(permission);
    }

    private boolean hasPermission(String permission, String func) {
        if (checkCallingPermission(permission) == 0) {
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
        boolean isChanged = false;
        synchronized (this.mGameSpacePackageList) {
            String foregroundPkg = getForegroundPackage();
            int j = packageList.size();
            for (int i = 0; i < j; i++) {
                String pkgName = packageList.get(i);
                if (!TextUtils.isEmpty(pkgName) && !this.mGameSpacePackageList.contains(pkgName)) {
                    Slog.d(TAG, "add to Game Space:" + pkgName);
                    this.mGameSpacePackageList.add(pkgName);
                    isChanged = true;
                    if (foregroundPkg != null && foregroundPkg.equals(pkgName)) {
                        this.mIsLastGame = true;
                        this.mIsCurGameEx = true;
                        this.mIsLastGameEx = true;
                        Bundle bundle = new Bundle();
                        bundle.putString("packageName", pkgName);
                        bundle.putInt("event", 4);
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(101, bundle));
                    }
                }
            }
        }
        if (isChanged) {
            this.mHandler.sendEmptyMessage(102);
        }
        return isChanged;
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
        boolean isChanged = false;
        synchronized (this.mGameSpacePackageList) {
            int j = packageList.size();
            for (int i = 0; i < j; i++) {
                String pkgName = packageList.get(i);
                if (this.mGameSpacePackageList.contains(pkgName)) {
                    Slog.d(TAG, "del from Game Space:" + pkgName);
                    this.mGameSpacePackageList.remove(pkgName);
                    isChanged = true;
                }
            }
        }
        if (isChanged) {
            this.mHandler.sendEmptyMessage(102);
        }
        return isChanged;
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
        List<String> gameList = new ArrayList<>();
        synchronized (this.mGameSpacePackageList) {
            gameList.addAll(this.mGameSpacePackageList);
        }
        Slog.d(TAG, "getGameList: " + gameList.toString());
        return gameList;
    }

    public void registerGameObserver(IGameObserver observer) {
        if (observer != null) {
            boolean isRegisterSuccess = this.mGameObservers.register(observer);
            Slog.d(TAG, "registerGameObserver:" + observer + ", isRegisterSuccess=" + isRegisterSuccess);
        }
    }

    public void unregisterGameObserver(IGameObserver observer) {
        if (observer != null) {
            boolean isUnregisterSuccess = this.mGameObservers.unregister(observer);
            Slog.d(TAG, "unregisterGameObserver:" + observer + ", isUnregisterSuccess=" + isUnregisterSuccess);
        }
    }

    public void registerGameObserverEx(IGameObserverEx observer) {
        if (observer != null) {
            boolean isSuccess = this.mGameObserversEx.register(observer);
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG, "registerGameObserverEx:" + observer + ", result=" + isSuccess);
            }
        }
    }

    public void unregisterGameObserverEx(IGameObserverEx observer) {
        if (observer != null) {
            boolean isSuccess = this.mGameObserversEx.unregister(observer);
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG, "unregisterGameObserverEx:" + observer + ", result=" + isSuccess);
            }
        }
    }

    public boolean isGameDndOn() {
        return isGameForeground();
    }

    public boolean isGameDndOnEx() {
        return isGameForegroundEx();
    }

    public boolean isGameKeyControlOn() {
        int userId = getCurrentUserId();
        int mode = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), GAME_KEY_CONTROL_MODE, 2, userId);
        Slog.d(TAG, "Keycontrol mode is " + mode + " for user " + userId);
        return mode == 1 && isGameForeground();
    }

    public boolean isGameGestureDisabled() {
        int userId = getCurrentUserId();
        int mode = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), GAME_GESTURE_MODE, 2, userId);
        Slog.d(TAG, "Gesture disable mode is " + mode + " for user " + userId);
        return mode == 1 && isGameForeground();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGameStatusChanged(String packageName, int event) {
        dispatchGameStatusChanged(packageName, event);
        dispatchGameStatusChangedEx(packageName, event);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleForegroundActivityChanged() {
        int event = 0;
        String str = this.mCurFgPkg;
        if (str != null && !str.equals(this.mLastForegroundPkg)) {
            if (this.mIsLastGame) {
                event = this.mIsCurGame ? 3 : 2;
            } else if (this.mIsCurGame) {
                event = 1;
            }
        }
        String str2 = this.mCurFgPkg;
        this.mLastForegroundPkg = str2;
        this.mIsLastGame = this.mIsCurGame;
        if (event != 0) {
            dispatchGameStatusChanged(str2, event);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleForegroundActivityChangedEx() {
        int event = 0;
        String str = this.mCurFgPkgEx;
        if (str != null && !str.equals(this.mLastFgPkgEx)) {
            if (this.mIsLastGameEx) {
                event = this.mIsCurGameEx ? 3 : 2;
            } else if (this.mIsCurGameEx) {
                event = 1;
            }
        }
        this.mLastFgPkgEx = this.mCurFgPkgEx;
        this.mIsLastGameEx = this.mIsCurGameEx;
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG, "handleForegroundActivityChangedEx, mIsLastGameEx=" + this.mIsLastGameEx + ", mLastFgPkgEx=" + this.mLastFgPkgEx);
        }
        if (event != 0) {
            dispatchGameStatusChangedEx(this.mCurFgPkgEx, event);
        }
    }

    private void dispatchGameStatusChanged(String packageName, int event) {
        int num = this.mGameObservers.beginBroadcast();
        Slog.d(TAG, "dispatchGameStatusChanged, packageName=" + packageName + ", event=" + event + ", num=" + num);
        while (num > 0) {
            num--;
            IGameObserver observer = this.mGameObservers.getBroadcastItem(num);
            if (observer != null) {
                try {
                    observer.onGameStatusChanged(packageName, event);
                } catch (RemoteException e) {
                    Slog.e(TAG, "dispatchGameStatusChanged error because RemoteException!");
                }
            }
        }
        this.mGameObservers.finishBroadcast();
    }

    private void dispatchGameStatusChangedEx(String packageName, int event) {
        Bundle bundle = new Bundle();
        bundle.putString("packageName", packageName);
        int num = this.mGameObserversEx.beginBroadcast();
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG, "IGameObserverEx.onGameStatusChanged, pkgName=" + packageName + ", event=" + event + ", num=" + num);
        }
        while (num > 0) {
            num--;
            IGameObserverEx observer = this.mGameObserversEx.getBroadcastItem(num);
            if (observer != null) {
                try {
                    observer.onGameStatusChanged(event, bundle);
                } catch (RemoteException e) {
                    Slog.e(TAG, "dispatchGameStatusChangedEx error because RemoteException!");
                }
            }
        }
        this.mGameObserversEx.finishBroadcast();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGameListChanged() {
        dispatchGameListChanged();
        dispatchGameListChangedEx();
    }

    private void dispatchGameListChanged() {
        int num = this.mGameObservers.beginBroadcast();
        Slog.d(TAG, "dispatchGameListChanged, num=" + num);
        while (num > 0) {
            num--;
            IGameObserver observer = this.mGameObservers.getBroadcastItem(num);
            if (observer != null) {
                try {
                    observer.onGameListChanged();
                } catch (RemoteException e) {
                    Slog.e(TAG, "dispatchGameListChanged error because RemoteException!");
                }
            }
        }
        this.mGameObservers.finishBroadcast();
    }

    private void dispatchGameListChangedEx() {
        int num = this.mGameObserversEx.beginBroadcast();
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.d(TAG, "dispatchGameListChangedEx, num=" + num);
        }
        while (num > 0) {
            num--;
            IGameObserverEx observer = this.mGameObserversEx.getBroadcastItem(num);
            if (observer != null) {
                try {
                    observer.onGameListChanged();
                } catch (RemoteException e) {
                    Slog.e(TAG, "dispatchGameListChangedEx error because RemoteException!");
                }
            }
        }
        this.mGameObserversEx.finishBroadcast();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackageRemovedOrDataCleared(int userId, String packageName) {
        if ("com.huawei.gameassistant".equals(packageName) && isPrimaryUser(userId)) {
            synchronized (this.mGameSpacePackageList) {
                if (!this.mGameSpacePackageList.isEmpty()) {
                    Slog.d(TAG, "Clear gamespace because GameAssistant is removed or data cleared!");
                    this.mGameSpacePackageList.clear();
                    this.mHandler.sendEmptyMessage(102);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPackageRemovedOrDataCleared(Intent intent) {
        if (intent != null && intent.getData() != null) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            String packageName = intent.getData().getSchemeSpecificPart();
            Bundle bundle = new Bundle();
            bundle.putInt("userId", userId);
            bundle.putString("packageName", packageName);
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(104, bundle));
        }
    }

    private void registerBroadcast() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            /* class com.android.server.wm.HwGameAssistantController.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    char c = 65535;
                    if (action.hashCode() == 267468725 && action.equals("android.intent.action.PACKAGE_DATA_CLEARED")) {
                        c = 0;
                    }
                    if (c == 0) {
                        HwGameAssistantController.this.onPackageRemovedOrDataCleared(intent);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        this.mContext.registerReceiverAsUser(receiver, UserHandle.ALL, filter, null, null);
    }
}
